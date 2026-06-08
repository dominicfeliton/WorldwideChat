package com.dominicfeliton.worldwidechat.util.storage;

import java.util.List;

public class SQLUtils extends JdbcStorageUtils {
    public SQLUtils(String host, String port, String database, String username, String password, List<String> argList, boolean useSSL) {
        super(JdbcStorageDialect.MYSQL, host, port, database, username, password, argList, useSSL);
    }
}
