package com.badskater0729.worldwidechat.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerRecord {

	private int attemptedTranslations = 0;
	private int successfulTranslations = 0;

	private String lastTranslationTime = "";
	private String playerUUID = "";
	
	private boolean hasBeenSaved = false;

	public PlayerRecord(String lastTranslationTime, String playerUUID, int attemptedTranslations,
			int successfulTranslations) {
		this.attemptedTranslations = attemptedTranslations;
		this.successfulTranslations = successfulTranslations;
		this.lastTranslationTime = lastTranslationTime;
		this.playerUUID = playerUUID;
	}

	/* Getters */
	public int getAttemptedTranslations() {
		return attemptedTranslations;
	}

	public int getSuccessfulTranslations() {
		return successfulTranslations;
	}

	public String getLastTranslationTime() {
		return lastTranslationTime;
	}

	public String getUUID() {
		return playerUUID;
	}
	
	public boolean getHasBeenSaved() {
		return hasBeenSaved;
	}

	/* Setters */
	public void setAttemptedTranslations(int i) {
		hasBeenSaved = false;
		attemptedTranslations = i;
	}

	public void setSuccessfulTranslations(int i) {
		hasBeenSaved = false;
		successfulTranslations = i;
	}

	public void setLastTranslationTime() {
		hasBeenSaved = false;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		lastTranslationTime = formatter.format(date);
	}

	public void setUUID(String i) {
		hasBeenSaved = false;
		playerUUID = i;
	}
	
	public void setHasBeenSaved(boolean i) {
		hasBeenSaved = i;
	}
}
