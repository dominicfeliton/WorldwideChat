package com.expl0itz.worldwidechat.misc;

public class WWCActiveTranslator {

    private String playerUUID = "";
    private String inLangCode = "";
    private String outLangCode = "";
    private String translatorName = "";
    private boolean hasBeenShownColorCodeWarning = false;

    public WWCActiveTranslator(String uuid, String langIn, String langOut, String translator, boolean h) {
        playerUUID = uuid;
        inLangCode = langIn;
        outLangCode = langOut;
        translatorName = translator;
        hasBeenShownColorCodeWarning = h;
    }

    /* Setters */
    public void setUUID(String i) {
        playerUUID = i;
    }

    public void setInLangCode(String i) {
        inLangCode = i;
    }

    public void setOutLangCode(String i) {
        outLangCode = i;
    }

    public void setTranslatorName(String i) {
        translatorName = i;
    }

    public void setCCWarning(boolean i) {
        hasBeenShownColorCodeWarning = i;
    }

    /* Getters */
    public String getUUID() {
        return playerUUID;
    }

    public String getInLangCode() {
        return inLangCode;
    }

    public String getOutLangCode() {
        return outLangCode;
    }

    public String getTranslator() {
        return translatorName;
    }

    public boolean getCCWarning() {
        return hasBeenShownColorCodeWarning;
    }
}