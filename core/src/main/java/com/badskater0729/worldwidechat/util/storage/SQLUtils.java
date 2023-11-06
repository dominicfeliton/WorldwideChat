package com.badskater0729.worldwidechat.util.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonRefs;
import com.zaxxer.hikari.HikariDataSource;

public class SQLUtils {

	private CommonRefs refs = new CommonRefs();
	private HikariDataSource hikari;
	private String host;
	private String port;
	private String database;
	private String username;
	private String password;
	private List<String> argList;
	private boolean useSSL;


	public SQLUtils(String host, String port, String database, String username, String password, List<String> argList, boolean useSSL) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.argList = argList;
		this.useSSL = useSSL;
	}

	public void connect() throws SQLException {
		HikariDataSource testSource = new HikariDataSource();
		testSource.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
		testSource.addDataSourceProperty("serverName", host);
		testSource.addDataSourceProperty("port", port);
		testSource.addDataSourceProperty("databaseName", database);
		testSource.addDataSourceProperty("user", username);
		testSource.addDataSourceProperty("password", password);
		testSource.addDataSourceProperty("useSSL", useSSL);
		testSource.setConnectionTimeout(WorldwideChat.translatorFatalAbortSeconds * 1000);
		if (argList != null) {
			for (String eaArg : argList) {
				if (eaArg.indexOf("=") != -1) {
					testSource.addDataSourceProperty(eaArg.substring(0, eaArg.indexOf("=")), eaArg.substring(eaArg.indexOf("=")+1));
				    refs.debugMsg(eaArg.substring(0, eaArg.indexOf("=")) + ":" + eaArg.substring(eaArg.indexOf("=")+1));
				}
			}
		}
		testSource.getConnection();
		hikari = testSource;
	}
	
	public void disconnect() {
		if (isConnected()) {
			hikari.close();
			hikari = null;
		}
	}

	public boolean isConnected() {
		return (hikari != null);
	}

	public Connection getConnection() throws SQLException {
		return hikari.getConnection();
	}
}
