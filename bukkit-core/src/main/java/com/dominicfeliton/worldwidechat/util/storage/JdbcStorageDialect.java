package com.dominicfeliton.worldwidechat.util.storage;

public enum JdbcStorageDialect {
    MYSQL("MySQL", "com.mysql.cj.jdbc.MysqlDataSource", "port"),
    POSTGRESQL("PostgreSQL", "org.postgresql.ds.PGSimpleDataSource", "portNumber");

    private final String displayName;
    private final String dataSourceClassName;
    private final String portPropertyName;

    JdbcStorageDialect(String displayName, String dataSourceClassName, String portPropertyName) {
        this.displayName = displayName;
        this.dataSourceClassName = dataSourceClassName;
        this.portPropertyName = portPropertyName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public String getPortPropertyName() {
        return portPropertyName;
    }
}
