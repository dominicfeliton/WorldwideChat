package com.expl0itz.worldwidechat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLManager {
	
	private static Connection connection;
	
	public static boolean isConnected() {
		return (connection == null ? false : true);
	}
	
	public static Connection getConnection() {
		return connection;
	}
	
	public static void connect(String host, String port, String database, String username, String password, boolean useSSL) throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL, username, password);
	}
	
	public static void disconnect() {
		if (isConnected()) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
