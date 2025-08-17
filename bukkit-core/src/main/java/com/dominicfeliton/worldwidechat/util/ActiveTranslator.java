package com.dominicfeliton.worldwidechat.util;

import org.threeten.bp.Instant;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ActiveTranslator {

    private final AtomicInteger rateLimit = new AtomicInteger(0);

    private final AtomicReference<String> playerUUID = new AtomicReference<>("");
    private final AtomicReference<String> inLangCode = new AtomicReference<>("");
    private final AtomicReference<String> outLangCode = new AtomicReference<>("");
    private final AtomicReference<String> rateLimitPreviousTime = new AtomicReference<>("None");

    private final AtomicBoolean hasBeenShownColorCodeWarning = new AtomicBoolean(false);
    private final AtomicBoolean hasBeenShownSignEditWarning = new AtomicBoolean(false);
    private final AtomicBoolean translatingChatOutgoing = new AtomicBoolean(true);
    private final AtomicBoolean translatingChatIncoming = new AtomicBoolean(false);
    private final AtomicBoolean translatingBook = new AtomicBoolean(false);
    private final AtomicBoolean translatingSign = new AtomicBoolean(false);
    private final AtomicBoolean translatingItem = new AtomicBoolean(false);
    private final AtomicBoolean translatingEntity = new AtomicBoolean(false);
    private final AtomicBoolean hasBeenSaved = new AtomicBoolean(false);

    public ActiveTranslator(String uuid, String langIn, String langOut) {
        playerUUID.set(uuid);
        inLangCode.set(langIn);
        outLangCode.set(langOut);
    }

    public void setRateLimit(int i) {
        hasBeenSaved.set(false);
        rateLimit.set(i);
    }

    public void setUUID(String i) {
        hasBeenSaved.set(false);
        playerUUID.set(i);
    }

    public void setInLangCode(String i) {
        hasBeenSaved.set(false);
        inLangCode.set(i);
    }

    public void setOutLangCode(String i) {
        hasBeenSaved.set(false);
        outLangCode.set(i);
    }

    public void setSignWarning(boolean i) {
        hasBeenShownSignEditWarning.set(i);
    }

    public void setCCWarning(boolean i) {
        hasBeenShownColorCodeWarning.set(i);
    }

    public void setTranslatingChatOutgoing(boolean i) {
        hasBeenSaved.set(false);
        translatingChatOutgoing.set(i);
    }

    public void setTranslatingChatIncoming(boolean i) {
        hasBeenSaved.set(false);
        translatingChatIncoming.set(i);
    }

    public void setTranslatingBook(boolean i) {
        hasBeenSaved.set(false);
        translatingBook.set(i);
    }

    public void setTranslatingSign(boolean i) {
        hasBeenSaved.set(false);
        translatingSign.set(i);
    }

    public void setTranslatingItem(boolean i) {
        hasBeenSaved.set(false);
        translatingItem.set(i);
    }

    public void setTranslatingEntity(boolean i) {
        hasBeenSaved.set(false);
        translatingEntity.set(i);
    }

    public void setHasBeenSaved(boolean i) {
        hasBeenSaved.set(i);
    }

    public void setRateLimitPreviousTime(Instant i) {
        hasBeenSaved.set(false);
        rateLimitPreviousTime.set(i.toString());
    }

    public int getRateLimit() {
        return rateLimit.get();
    }

    public String getUUID() {
        return playerUUID.get();
    }

    public String getInLangCode() {
        return inLangCode.get();
    }

    public String getOutLangCode() {
        return outLangCode.get();
    }

    public boolean getCCWarning() {
        return hasBeenShownColorCodeWarning.get();
    }

    public boolean getSignWarning() {
        return hasBeenShownSignEditWarning.get();
    }

    public boolean getTranslatingChatOutgoing() {
        return translatingChatOutgoing.get();
    }

    public boolean getTranslatingChatIncoming() {
        return translatingChatIncoming.get();
    }

    public boolean getTranslatingBook() {
        return translatingBook.get();
    }

    public boolean getTranslatingSign() {
        return translatingSign.get();
    }

    public boolean getTranslatingItem() {
        return translatingItem.get();
    }

    public boolean getTranslatingEntity() {
        return translatingEntity.get();
    }

    public boolean getHasBeenSaved() {
        return hasBeenSaved.get();
    }

    public String getRateLimitPreviousTime() {
        return rateLimitPreviousTime.get();
    }
}