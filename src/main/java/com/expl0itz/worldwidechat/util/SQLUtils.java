package com.expl0itz.worldwidechat.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
		hikari = new HikariDataSource();
		hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
		hikari.addDataSourceProperty("serverName", host);
		hikari.addDataSourceProperty("port", port);
		hikari.addDataSourceProperty("databaseName", database);
		hikari.addDataSourceProperty("user", username);
		hikari.addDataSourceProperty("password", password);
		hikari.addDataSourceProperty("useSSL", useSSL);
		if (list != null) {
			for (String eaArg : list) {
				if (eaArg.indexOf("=") != -1) {
					hikari.addDataSourceProperty(eaArg.substring(0, eaArg.indexOf("=")), eaArg.substring(eaArg.indexOf("=")));
				}
			}
		}
		hikari.getConnection();
		
	}
	
	public static void disconnect() {
		if (isConnected()) {
			hikari.close();
			hikari = null;
		}
	}
}
