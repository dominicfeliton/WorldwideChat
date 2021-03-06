package com.expl0itz.worldwidechat.misc;

public class ActiveTranslator {

    private String playerUUID = "";
    private String inLangCode = "";
    private String outLangCode = "";
    private boolean hasBeenShownColorCodeWarning = false;

    public ActiveTranslator(String uuid, String langIn, String langOut, boolean h) {
        playerUUID = uuid;
        inLangCode = langIn;
        outLangCode = langOut;
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

    public boolean getCCWarning() {
        return hasBeenShownColorCodeWarning;
    }
}