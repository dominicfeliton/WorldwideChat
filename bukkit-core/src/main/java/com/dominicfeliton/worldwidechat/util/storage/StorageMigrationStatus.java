package com.dominicfeliton.worldwidechat.util.storage;

public class StorageMigrationStatus {

    private final String backend;
    private final String currentVersion;
    private final int pendingCount;
    private final int executedCount;
    private final boolean success;
    private final String detail;

    public StorageMigrationStatus(String backend, String currentVersion, int pendingCount, int executedCount,
                                  boolean success, String detail) {
        this.backend = backend;
        this.currentVersion = currentVersion;
        this.pendingCount = pendingCount;
        this.executedCount = executedCount;
        this.success = success;
        this.detail = detail;
    }

    public static StorageMigrationStatus skipped(String backend, String detail) {
        return new StorageMigrationStatus(backend, "n/a", 0, 0, true, detail);
    }

    public String describe() {
        String state = success ? "stable" : "failed";
        return String.format("%s schema is %s. Current version: %s. Pending: %d. Executed this startup: %d. %s",
                backend, state, currentVersion, pendingCount, executedCount, detail);
    }

    public boolean isSuccess() {
        return success;
    }
}
