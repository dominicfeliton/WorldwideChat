package com.expl0itz.worldwidechat.util;

import org.threeten.bp.Instant;

public class ActiveTranslator {

	private int rateLimit = 0;

	private String playerUUID = "";
	private String inLangCode = "";
	private String outLangCode = "";
	private String rateLimitPreviousTime = "None";

	private boolean hasBeenShownColorCodeWarning = false;
	private boolean translatingChatOutgoing = true;
	private boolean translatingChatIncoming = true;
	private boolean translatingBook = false;
	private boolean translatingSign = false;
	private boolean translatingItem = false;
	private boolean translatingEntity = false;
	private boolean hasBeenSaved = false;

	public ActiveTranslator(String uuid, String langIn, String langOut) {
		playerUUID = uuid;
		inLangCode = langIn;
		outLangCode = langOut;
	}

	/* Setters */
	public void setRateLimit(int i) {
		hasBeenSaved = false;
		rateLimit = i;
	}

	public void setUUID(String i) {
		hasBeenSaved = false;
		playerUUID = i;
	}
	
	public void setInLangCode(String i) {
		hasBeenSaved = false;
		inLangCode = i;
	}

	public void setOutLangCode(String i) {
		hasBeenSaved = false;
		outLangCode = i;
	}

	public void setCCWarning(boolean i) {
		hasBeenShownColorCodeWarning = i;
	}

	public void setTranslatingChatOutgoing(boolean i) {
		hasBeenSaved = false;
		translatingChatOutgoing = i;
	}
	
	public void setTranslatingChatIncoming(boolean i) {
		hasBeenSaved = false;
		translatingChatIncoming = i;
	}
	
	public void setTranslatingBook(boolean i) {
		hasBeenSaved = false;
		translatingBook = i;
	}

	public void setTranslatingSign(boolean i) {
		hasBeenSaved = false;
		translatingSign = i;
	}

	public void setTranslatingItem(boolean i) {
		hasBeenSaved = false;
		translatingItem = i;
	}
	
	public void setTranslatingEntity(boolean i) {
		hasBeenSaved = false;
		translatingEntity = i;
	}

	public void setHasBeenSaved(boolean i) {
		hasBeenSaved = i;
	}
	
	public void setRateLimitPreviousTime(Instant i) {
		hasBeenSaved = false;
		rateLimitPreviousTime = i.toString();
	}

	/* Getters */
	public int getRateLimit() {
		return rateLimit;
	}

	public String getUUID() {
		return playerUUID;
	}

	public String getInLangCode() {
		return inLangCode;
	}

	public String getOutLangCode() {
		return outLangCode;
	}

	public boolean getCCWarning() {
		return hasBeenShownColorCodeWarning;
	}

	public boolean getTranslatingChatOutgoing() {
		return translatingChatOutgoing;
	}
	
	public boolean getTranslatingChatIncoming() {
		return translatingChatIncoming;
	}
	
	public boolean getTranslatingBook() {
		return translatingBook;
	}

	public boolean getTranslatingSign() {
		return translatingSign;
	}

	public boolean getTranslatingItem() {
		return translatingItem;
	}
	
	public boolean getTranslatingEntity() {
		return translatingEntity;
	}
	
	public boolean getHasBeenSaved() {
		return hasBeenSaved;
	}

	public String getRateLimitPreviousTime() {
		return rateLimitPreviousTime;
	}
}