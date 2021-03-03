package com.expl0itz.worldwidechat.misc;

import com.expl0itz.worldwidechat.WorldwideChat;

public class WWCActiveTranslator {

    private WorldwideChat main;
    private String playerUUID = "";
    private String inLangCode = "";
    private String outLangCode = "";
    private String translatorName = "";
    
	public WWCActiveTranslator(WorldwideChat mainInstance, String uuid, String langIn,String langOut, String translator)
	{
		main = mainInstance;
		playerUUID = uuid;
		inLangCode = langIn;
		outLangCode = langOut;
		translatorName = translator;
	}
	
	/* Setters */
	public void setUUID(String i)
	{
		playerUUID = i;
	}
	
	public void setInLangCode(String i)
	{
		inLangCode = i;
	}
	
	public void setOutLangCode(String i)
	{
		outLangCode = i;
	}
	
	public void setTranslatorName(String i)
	{
		translatorName = i;
	}
	
	/* Getters */
	public String getUUID() 
	{
		return playerUUID;
	}
	
	public String getInLangCode()
	{
		return inLangCode;
	}
	
	public String getOutLangCode()
	{
		return outLangCode;
	}
	
	public String getTranslator()
	{
		return translatorName;
	}
}
