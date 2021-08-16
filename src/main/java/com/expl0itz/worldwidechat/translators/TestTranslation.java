package com.expl0itz.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;

public class TestTranslation {

    private String textToTranslate = "";
    private String inputLang = "";
    private String outputLang = "";
    private CommandSender sender;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public TestTranslation(String textToTranslate, String inputLang, String outputLang, CommandSender sender) {
        this.textToTranslate = textToTranslate;
        this.inputLang = inputLang;
        this.outputLang = outputLang;
        this.sender = sender;
    }
    
    public TestTranslation(String apikey) {
        System.setProperty("FAKE_API_KEY", apikey);
    }
	
    public void initializeConnection() {
    	/* Generate fake supported langs list */
    	List < SupportedLanguageObject > outList = new ArrayList < SupportedLanguageObject >();
    	outList.add(new SupportedLanguageObject("en", "English", "", true, true));
    	outList.add(new SupportedLanguageObject("es", "Spanish", "", true, true));
    	outList.add(new SupportedLanguageObject("fr", "French", "", true, true));
    	
    	/* Set langList in Main */
    	main.setSupportedTranslatorLanguages(outList);
    }
    
    public String translate() {
    	if (inputLang.equals("en") && outputLang.equals("es") && textToTranslate.equals("Hello, how are you?")) {
    		return "Hola, como estas?";
    	}
    	return "bad test result";
    }
	
}
