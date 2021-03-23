package com.expl0itz.worldwidechat.misc;

import com.expl0itz.worldwidechat.WorldwideChat;

public class ActiveTranslator {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    private String playerUUID = "";
    private String inLangCode = "";
    private String outLangCode = "";
    
    private boolean hasBeenShownColorCodeWarning = false;
    private boolean translatingBook = false;
    private boolean translatingSign = false;

    public ActiveTranslator(String uuid, String langIn, String langOut, boolean hasBeenShownColorCodeWarning) {
        playerUUID = uuid;
        inLangCode = langIn;
        outLangCode = langOut;
        this.hasBeenShownColorCodeWarning = hasBeenShownColorCodeWarning;
        
        /* Add player to records if they do not exist */
        main.getPlayerRecord(uuid, true);
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

    public void setTranslatingBook(boolean i) {
        translatingBook = i;
    }
    
    public void setTranslatingSign(boolean i) {
        translatingSign = i;
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
    
    public boolean getTranslatingBook() {
        return translatingBook;
    }
    
    public boolean getTranslatingSign() {
        return translatingSign;
    }
}