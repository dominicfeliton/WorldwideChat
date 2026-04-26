package com.dominicfeliton.worldwidechat.util.storage.migration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ChangeUnit(id = "normalize-storage-schema", order = "001", author = "WorldwideChat", transactional = false)
public class V1NormalizeMongoStorage {

    private static final String ACTIVE_TRANSLATORS = "ActiveTranslators";
    private static final String PLAYER_RECORDS = "PlayerRecords";
    private static final String PERSISTENT_CACHE = "PersistentCache";

    @Execution
    public void execution(MongoDatabase database) {
        ensureCollection(database, ACTIVE_TRANSLATORS);
        ensureCollection(database, PLAYER_RECORDS);
        ensureCollection(database, PERSISTENT_CACHE);

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

    private void normalizePersistentCache(MongoCollection<Document> cacheCollection) {
        List<WriteModel<Document>> writes = new ArrayList<>();
        Set<String> seenCacheKeys = new HashSet<>();

        for (Document document : cacheCollection.find()) {
            Object id = document.get("_id");
            if (document.getString("randomUUID") == null || document.getString("randomUUID").isEmpty()) {
                writes.add(new UpdateOneModel<>(Filters.eq("_id", id),
                        Updates.set("randomUUID", UUID.randomUUID().toString())));
            }

            String cacheKey = document.getString("inputLang") + "\u0000"
                    + document.getString("outputLang") + "\u0000"
                    + document.getString("inputPhrase");
            if (!seenCacheKeys.add(cacheKey)) {
                writes.add(new DeleteOneModel<>(Filters.eq("_id", id)));
            }
        }

        if (!writes.isEmpty()) {
            cacheCollection.bulkWrite(writes);
        }

        cacheCollection.createIndex(Indexes.ascending("randomUUID"),
                new IndexOptions().unique(true).name("idx_persistent_cache_random"));
        cacheCollection.createIndex(Indexes.ascending("inputLang", "outputLang", "inputPhrase"),
                new IndexOptions().unique(true).name("idx_persistent_cache_lookup"));
    }
}
