package com.expl0itz.worldwidechat.util.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.zaxxer.hikari.HikariDataSource;

public class SQLUtils {
	
	private static HikariDataSource hikari;
	
	public static boolean isConnected() {
		return (hikari == null ? false : true);
	}
	
	public static Connection getConnection() throws SQLException {
		return hikari.getConnection();
	}
	
	public static void connect(String host, String port, String database, String username, String password, List<String> list, boolean useSSL) throws SQLException {
		HikariDataSource testSource = new HikariDataSource();
		testSource.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
		testSource.addDataSourceProperty("serverName", host);
		testSource.addDataSourceProperty("port", port);
		testSource.addDataSourceProperty("databaseName", database);
		testSource.addDataSourceProperty("user", username);
		testSource.addDataSourceProperty("password", password);
		testSource.addDataSourceProperty("useSSL", useSSL);
		if (list != null) {
			for (String eaArg : list) {
				if (eaArg.indexOf("=") != -1) {
					testSource.addDataSourceProperty(eaArg.substring(0, eaArg.indexOf("=")), eaArg.substring(eaArg.indexOf("=")+1));
				    CommonDefinitions.sendDebugMessage(eaArg.substring(0, eaArg.indexOf("=")) + ":" + eaArg.substring(eaArg.indexOf("=")+1));
				}
			}
		}
		testSource.getConnection();
		hikari = testSource;
	}
	
	public static void disconnect() {
		if (isConnected()) {
			hikari.close();
			hikari = null;
		}
	}
}