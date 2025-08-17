package com.dominicfeliton.worldwidechat.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerRecord {

    private final AtomicInteger attemptedTranslations = new AtomicInteger(0);
    private final AtomicInteger successfulTranslations = new AtomicInteger(0);

    private final AtomicReference<String> lastTranslationTime = new AtomicReference<>("");
    private final AtomicReference<String> playerUUID = new AtomicReference<>("");
    private final AtomicReference<String> localizationCode = new AtomicReference<>("");

    private final AtomicBoolean hasBeenSaved = new AtomicBoolean(false);

    public PlayerRecord(String lastTranslationTime, String playerUUID, int attemptedTranslations,
                        int successfulTranslations) {
        this.attemptedTranslations.set(attemptedTranslations);
        this.successfulTranslations.set(successfulTranslations);
        this.lastTranslationTime.set(lastTranslationTime);
        this.playerUUID.set(playerUUID);
    }

    public int getAttemptedTranslations() {
        return attemptedTranslations.get();
    }

    public int getSuccessfulTranslations() {
        return successfulTranslations.get();
    }

    public String getLastTranslationTime() {
        return lastTranslationTime.get();
    }

    public String getUUID() {
        return playerUUID.get();
    }

    public String getLocalizationCode() {
        return localizationCode.get();
    }

    public boolean getHasBeenSaved() {
        return hasBeenSaved.get();
    }

    public void setAttemptedTranslations(int i) {
        hasBeenSaved.set(false);
        attemptedTranslations.set(i);
    }

    public void setSuccessfulTranslations(int i) {
        hasBeenSaved.set(false);
        successfulTranslations.set(i);
    }

    public void setLastTranslationTime() {
        hasBeenSaved.set(false);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        lastTranslationTime.set(formatter.format(date));
    }

    public void setUUID(String i) {
        hasBeenSaved.set(false);
        playerUUID.set(i);
    }

    public void setLocalizationCode(String s) {
        hasBeenSaved.set(false);
        localizationCode.set(s);
    }

    public void setHasBeenSaved(boolean i) {
        hasBeenSaved.set(i);
    }
}