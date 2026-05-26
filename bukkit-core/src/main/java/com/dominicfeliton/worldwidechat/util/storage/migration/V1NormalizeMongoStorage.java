package com.dominicfeliton.worldwidechat.util.storage.migration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ChangeUnit(id = "normalize-storage-schema", order = "001", author = "WorldwideChat", transactional = false)
public class V1NormalizeMongoStorage {

    private static final String ACTIVE_TRANSLATORS = "ActiveTranslators";
    private static final String PLAYER_RECORDS = "PlayerRecords";
    private static final String PERSISTENT_CACHE = "PersistentCache";
    private static final int WRITE_BATCH_SIZE = 1000;
    private static final DateTimeFormatter PLAYER_RECORD_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Execution
    public void execution(MongoDatabase database) {
        ensureCollection(database, ACTIVE_TRANSLATORS);
        ensureCollection(database, PLAYER_RECORDS);
        ensureCollection(database, PERSISTENT_CACHE);

        normalizePlayerCollection(database.getCollection(ACTIVE_TRANSLATORS), CollectionKind.ACTIVE_TRANSLATOR);
        normalizePlayerCollection(database.getCollection(PLAYER_RECORDS), CollectionKind.PLAYER_RECORD);

        database.getCollection(ACTIVE_TRANSLATORS).createIndex(
                Indexes.ascending("playerUUID"), new IndexOptions().unique(true).name("idx_active_translators_player"));
        database.getCollection(PLAYER_RECORDS).createIndex(
                Indexes.ascending("playerUUID"), new IndexOptions().unique(true).name("idx_player_records_player"));

        normalizePersistentCache(database.getCollection(PERSISTENT_CACHE));
    }

    @RollbackExecution
    public void rollback() {
    }

    private void ensureCollection(MongoDatabase database, String collectionName) {
        for (String existingName : database.listCollectionNames()) {
            if (existingName.equals(collectionName)) {
                return;
            }
        }
        database.createCollection(collectionName);
    }

    private void normalizePlayerCollection(MongoCollection<Document> collection, CollectionKind kind) {
        requireNoMissingPlayerUuid(collection, kind.collectionName);

        List<WriteModel<Document>> writes = new ArrayList<>();
        List<Document> currentGroup = new ArrayList<>();
        String currentUuid = null;

        try (MongoCursor<Document> cursor = collection.find()
                .sort(Sorts.ascending("playerUUID", "_id"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String uuid = stringValue(document, "playerUUID");
                if (currentUuid != null && !currentUuid.equals(uuid)) {
                    normalizePlayerGroup(collection, writes, currentUuid, currentGroup, kind);
                    currentGroup.clear();
                }
                currentUuid = uuid;
                currentGroup.add(document);
            }
        }

        if (!currentGroup.isEmpty()) {
            normalizePlayerGroup(collection, writes, currentUuid, currentGroup, kind);
        }
        flushWrites(collection, writes, 1);
    }

    private void requireNoMissingPlayerUuid(MongoCollection<Document> collection, String collectionName) {
        int missingDocuments = 0;
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Object uuid = cursor.next().get("playerUUID");
                if (!(uuid instanceof String value) || !hasText(value)) {
                    missingDocuments++;
                }
            }
        }

        if (missingDocuments > 0) {
            throw new IllegalStateException("Cannot migrate " + collectionName + ": found " + missingDocuments
                    + " documents with a missing playerUUID. Remove or repair those legacy documents before starting WorldwideChat.");
        }
    }

    private void normalizePlayerGroup(MongoCollection<Document> collection, List<WriteModel<Document>> writes,
                                      String uuid, List<Document> group, CollectionKind kind) {
        if (group.size() < 2) {
            return;
        }

        Document preferred = kind == CollectionKind.ACTIVE_TRANSLATOR
                ? preferredActiveTranslator(group)
                : preferredPlayerRecord(group);
        Object preferredId = preferred.get("_id");
        Document mergedFields = kind == CollectionKind.ACTIVE_TRANSLATOR
                ? mergedActiveTranslatorFields(uuid, preferred, group)
                : mergedPlayerRecordFields(uuid, preferred, group);

        for (Document document : group) {
            Object id = document.get("_id");
            if (!Objects.equals(preferredId, id)) {
                writes.add(new DeleteOneModel<>(Filters.eq("_id", id)));
            }
        }
        writes.add(new UpdateOneModel<>(Filters.eq("_id", preferredId), new Document("$set", mergedFields)));
        flushWrites(collection, writes, WRITE_BATCH_SIZE);
    }

    private Document mergedPlayerRecordFields(String uuid, Document preferred, List<Document> group) {
        return new Document("creationDate", newestInstantValue(group, "creationDate"))
                .append("playerUUID", uuid)
                .append("attemptedTranslations", maxInt(group, "attemptedTranslations"))
                .append("successfulTranslations", maxInt(group, "successfulTranslations"))
                .append("lastTranslationTime", firstNonBlank(preferred, group, "lastTranslationTime", ""))
                .append("localizationCode", firstNonBlank(preferred, group, "localizationCode", ""));
    }

    private Document mergedActiveTranslatorFields(String uuid, Document preferred, List<Document> group) {
        return new Document("creationDate", newestInstantValue(group, "creationDate"))
                .append("playerUUID", uuid)
                .append("inLangCode", text(stringValue(preferred, "inLangCode")))
                .append("outLangCode", text(stringValue(preferred, "outLangCode")))
                .append("rateLimit", intValue(preferred, "rateLimit"))
                .append("rateLimitPreviousTime", firstNonBlank(preferred, group, "rateLimitPreviousTime", "None"))
                .append("translatingChatOutgoing", booleanValue(preferred, "translatingChatOutgoing", true))
                .append("translatingChatIncoming", booleanValue(preferred, "translatingChatIncoming", false))
                .append("translatingBook", booleanValue(preferred, "translatingBook", false))
                .append("translatingSign", booleanValue(preferred, "translatingSign", false))
                .append("translatingItem", booleanValue(preferred, "translatingItem", false))
                .append("translatingEntity", booleanValue(preferred, "translatingEntity", false));
    }

    private Document preferredPlayerRecord(List<Document> group) {
        return group.stream()
                .max(Comparator
                        .comparing((Document document) -> parsePlayerRecordTime(stringValue(document, "lastTranslationTime"))
                                .orElse(Instant.MIN))
                        .thenComparingInt(document -> intValue(document, "attemptedTranslations"))
                        .thenComparingInt(document -> intValue(document, "successfulTranslations"))
                        .thenComparing(document -> text(stringValue(document, "creationDate")))
                        .thenComparing(document -> text(stringValue(document, "lastTranslationTime")))
                        .thenComparing(document -> text(stringValue(document, "localizationCode")))
                        .thenComparing(document -> text(String.valueOf(document.get("_id")))))
                .orElseThrow();
    }

    private Document preferredActiveTranslator(List<Document> group) {
        return group.stream()
                .max(Comparator
                        .comparing(this::activeTranslatorActivityTime)
                        .thenComparing(document -> text(stringValue(document, "creationDate")))
                        .thenComparing(document -> text(stringValue(document, "inLangCode")))
                        .thenComparing(document -> text(stringValue(document, "outLangCode")))
                        .thenComparingInt(document -> intValue(document, "rateLimit"))
                        .thenComparing(document -> text(stringValue(document, "rateLimitPreviousTime")))
                        .thenComparing(document -> text(String.valueOf(document.get("_id")))))
                .orElseThrow();
    }

    private Instant activeTranslatorActivityTime(Document document) {
        Optional<Instant> rateLimitTime = parseInstantValue(stringValue(document, "rateLimitPreviousTime"));
        return rateLimitTime.orElseGet(() -> parseInstantValue(stringValue(document, "creationDate")).orElse(Instant.MIN));
    }

    private Optional<Instant> parseInstantValue(String value) {
        if (!hasText(value) || value.equalsIgnoreCase("None")) {
            return Optional.empty();
        }

        try {
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private Optional<Instant> parsePlayerRecordTime(String value) {
        if (!hasText(value)) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDateTime.parse(value, PLAYER_RECORD_TIME).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private String newestInstantValue(List<Document> group, String field) {
        String newestValue = null;
        Instant newestInstant = Instant.MIN;
        for (Document document : group) {
            String value = stringValue(document, field);
            Optional<Instant> parsed = parseInstantValue(value);
            if (parsed.isPresent() && parsed.get().compareTo(newestInstant) > 0) {
                newestInstant = parsed.get();
                newestValue = value;
            }
        }
        if (newestValue != null) {
            return newestValue;
        }
        return firstNonBlank(group, field, Instant.now().toString());
    }

    private String firstNonBlank(Document preferred, List<Document> group, String field, String fallback) {
        String preferredValue = stringValue(preferred, field);
        if (hasText(preferredValue)) {
            return preferredValue;
        }
        return firstNonBlank(group, field, fallback);
    }

    private String firstNonBlank(List<Document> group, String field, String fallback) {
        for (Document document : group) {
            String value = stringValue(document, field);
            if (hasText(value)) {
                return value;
            }
        }
        return fallback;
    }

    private int maxInt(List<Document> group, String field) {
        return group.stream()
                .mapToInt(document -> intValue(document, field))
                .max()
                .orElse(0);
    }

    private int intValue(Document document, String field) {
        Object value = document.get(field);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue && hasText(stringValue)) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private boolean booleanValue(Document document, String field, boolean fallback) {
        Object value = document.get(field);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue && hasText(stringValue)) {
            return Boolean.parseBoolean(stringValue);
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String stringValue(Document document, String field) {
        Object value = document.get(field);
        return value == null ? null : value.toString();
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private void normalizePersistentCache(MongoCollection<Document> cacheCollection) {
        List<WriteModel<Document>> writes = new ArrayList<>();
        String lastCacheKey = null;

        try (MongoCursor<Document> cursor = cacheCollection.find()
                .sort(Sorts.ascending("inputLang", "outputLang", "inputPhrase", "_id"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Object id = document.get("_id");
                String cacheKey = document.getString("inputLang") + "\u0000"
                        + document.getString("outputLang") + "\u0000"
                        + document.getString("inputPhrase");

                if (cacheKey.equals(lastCacheKey)) {
                    writes.add(new DeleteOneModel<>(Filters.eq("_id", id)));
                } else {
                    if (document.getString("randomUUID") == null || document.getString("randomUUID").isEmpty()) {
                        writes.add(new UpdateOneModel<>(Filters.eq("_id", id),
                                Updates.set("randomUUID", UUID.randomUUID().toString())));
                    }
                    lastCacheKey = cacheKey;
                }

                flushWrites(cacheCollection, writes, WRITE_BATCH_SIZE);
            }
        }
        flushWrites(cacheCollection, writes, 1);

        cacheCollection.createIndex(Indexes.ascending("randomUUID"),
                new IndexOptions().unique(true).name("idx_persistent_cache_random"));
        cacheCollection.createIndex(Indexes.ascending("inputLang", "outputLang", "inputPhrase"),
                new IndexOptions().unique(true).name("idx_persistent_cache_lookup"));
    }

    private void flushWrites(MongoCollection<Document> cacheCollection, List<WriteModel<Document>> writes,
                             int minimumBatchSize) {
        if (writes.size() >= minimumBatchSize) {
            cacheCollection.bulkWrite(writes);
            writes.clear();
        }
    }

    private enum CollectionKind {
        ACTIVE_TRANSLATOR(ACTIVE_TRANSLATORS),
        PLAYER_RECORD(PLAYER_RECORDS);

        private final String collectionName;

        CollectionKind(String collectionName) {
            this.collectionName = collectionName;
        }
    }
}
