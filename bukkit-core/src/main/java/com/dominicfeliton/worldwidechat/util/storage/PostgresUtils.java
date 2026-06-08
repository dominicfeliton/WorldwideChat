package com.dominicfeliton.worldwidechat.util.storage;

import java.util.List;

public class PostgresUtils extends JdbcStorageUtils {
    public PostgresUtils(String host, String port, String database, String username, String password, List<String> argList, boolean ssl) {
        super(JdbcStorageDialect.POSTGRESQL, host, port, database, username, password, argList, ssl);
    }
}
