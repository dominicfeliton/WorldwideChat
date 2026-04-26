package com.dominicfeliton.worldwidechat.util.storage;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.storage.migration.V1__Normalize_storage_schema;
import com.dominicfeliton.worldwidechat.util.storage.migration.V1NormalizeMongoStorage;
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver;
import io.mongock.runner.standalone.MongockStandalone;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.output.MigrateResult;

public final class StorageMigrationUtils {

    private static final WorldwideChat main = WorldwideChat.instance;
    private static final String FLYWAY_TABLE = "wwc_schema_history";
    private static final String MONGO_CHANGELOG_COLLECTION = "wwc_mongock_changelog";
    private static final String MONGO_LOCK_COLLECTION = "wwc_mongock_lock";
    private static StorageMigrationStatus lastStatus = StorageMigrationStatus.skipped("YAML", "No database migrations required.");

    private StorageMigrationUtils() {
    }

    public static void migrateCurrentBackend() {
        if (main.isSQLConnValid(true)) {
            lastStatus = migrateJdbc(main.getSqlSession());
        } else if (main.isPostgresConnValid(true)) {
            lastStatus = migrateJdbc(main.getPostgresSession());
        } else if (main.isMongoConnValid(true)) {
            lastStatus = migrateMongo(main.getMongoSession());
        } else {
            lastStatus = StorageMigrationStatus.skipped("YAML", "No database migrations required.");
        }
        main.getServerFactory().getCommonRefs().debugMsg(lastStatus.describe());
    }

    public static StorageMigrationStatus getLastStatus() {
        return lastStatus;
    }

    private static StorageMigrationStatus migrateJdbc(JdbcStorageUtils jdbc) {
        Flyway flyway = Flyway.configure()
                .dataSource(jdbc.getDataSource())
                .table(FLYWAY_TABLE)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .cleanDisabled(true)
                .javaMigrations(new V1__Normalize_storage_schema())
                .load();

        MigrateResult result = flyway.migrate();
        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        String currentVersion = current == null || current.getVersion() == null ? "0" : current.getVersion().getVersion();
        return new StorageMigrationStatus(jdbc.getDialect().getDisplayName(), currentVersion,
                info.pending().length, result.migrationsExecuted, result.success, "Flyway history table: " + FLYWAY_TABLE + ".");
    }

    private static StorageMigrationStatus migrateMongo(MongoDBUtils mongo) {
        MongoSync4Driver driver = MongoSync4Driver.withDefaultLock(mongo.getClient(), mongo.getDatabaseName());
        driver.setMigrationRepositoryName(MONGO_CHANGELOG_COLLECTION);
        driver.setLockRepositoryName(MONGO_LOCK_COLLECTION);
        driver.disableTransaction();

        MongockStandalone.builder()
                .setDriver(driver)
                .addDependency(mongo.getActiveDatabase())
                .addMigrationClass(V1NormalizeMongoStorage.class)
                .buildRunner()
                .execute();

        long appliedMigrations = mongo.getActiveDatabase().getCollection(MONGO_CHANGELOG_COLLECTION).countDocuments();
        return new StorageMigrationStatus("MongoDB", String.valueOf(appliedMigrations), 0, 0,
                true, "Mongock changelog collection: " + MONGO_CHANGELOG_COLLECTION + ".");
    }
}
