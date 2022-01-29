package com.expl0itz.worldwidechat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class SQLManager {
	
	private static Connection connection;
	
	public static boolean isConnected() {
		return (connection == null ? false : true);
	}
	
	public static Connection getConnection() {
		return connection;
	}
	
	public static void connect(String host, String port, String database, String username, String password, List<String> list, boolean useSSL) throws SQLException {
		String finalURL = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL;
		if (list != null) {
			for (String eaArg : list) {
				finalURL += "&" + eaArg;
			}
		}
		connection = DriverManager.getConnection(finalURL, username, password);
	}
	
	public static void disconnect() {
		if (isConnected()) {
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
