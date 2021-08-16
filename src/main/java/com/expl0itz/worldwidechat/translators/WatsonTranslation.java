package com.expl0itz.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.Languages;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

public class WatsonTranslation {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    private String textToTranslate = "";
    private String inputLang = "";
    private String outputLang = "";
    private CommandSender sender;

    //For normal translation operation
    public WatsonTranslation(String textToTranslate, String inputLang, String outputLang, CommandSender sender) {
        this.textToTranslate = textToTranslate;
        this.inputLang = inputLang;
        this.outputLang = outputLang;
        this.sender = sender;
    }

    //For initializeConnection
    public WatsonTranslation(String apikey, String serviceUrl) {
        System.setProperty("WATSON_API_KEY", apikey);
        System.setProperty("WATSON_SERVICE_URL", serviceUrl);
    }

    public void initializeConnection() {
        /* Init credentials */
        IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(System.getProperty("WATSON_API_KEY")).build();
        LanguageTranslator translatorService = new LanguageTranslator("2018-05-01", authenticator);
        translatorService.setServiceUrl(System.getProperty("WATSON_SERVICE_URL"));

        /* Get languages */
        Languages allLanguages = translatorService.listLanguages().execute().getResult();
        //we have to use deprecated methods here; trying to reference JsonParser statically results in a class not found error?? :(
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonTree = jsonParser.parse(allLanguages.toString());
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        
        /* Parse json */
        final JsonArray dataJson = jsonObject.getAsJsonArray("languages");
        List < SupportedLanguageObject > outList = new ArrayList <SupportedLanguageObject >();
        for (JsonElement element : dataJson) {
        	if (((JsonObject) element).get("supported_as_source").getAsBoolean() && ((JsonObject) element).get("supported_as_target").getAsBoolean()) {
        		outList.add(new SupportedLanguageObject(
                        ((JsonObject) element).get("language").getAsString(),
                        ((JsonObject) element).get("language_name").getAsString(),
                        ((JsonObject) element).get("native_language_name").getAsString(),
                        ((JsonObject) element).get("supported_as_source").getAsBoolean(),
                        ((JsonObject) element).get("supported_as_target").getAsBoolean()));
        	}
        }
        
        /* Test translation */
        TranslateOptions options = new TranslateOptions.Builder()
                .addText("Hello, how are you?")
                .source("en")
                .target("es")
                .build();
        
        /* Set supported watson languages */
        main.setSupportedTranslatorLanguages(outList);
    }
    
    public String translate() {
        /* Init credentials */
        IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(System.getProperty("WATSON_API_KEY")).build();
        LanguageTranslator translatorService = new LanguageTranslator("2018-05-01", authenticator);
        translatorService.setServiceUrl(System.getProperty("WATSON_SERVICE_URL"));
        
        /* Actual translation */
        TranslateOptions options = new TranslateOptions.Builder()
            .addText(textToTranslate)
            .source(inputLang.equals("None") ? "" : inputLang)
            .target(outputLang)
            .build();
        
        /* Process final output */
        TranslationResult translationResult = translatorService.translate(options).execute().getResult();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonTree = jsonParser.parse(translationResult.toString());
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        JsonElement translationSection = jsonObject.getAsJsonArray("translations").get(0).getAsJsonObject().get("translation");
        String finalOut = translationSection.toString().substring(1, translationSection.toString().length() - 1);
        
        /* Return final result */
        return finalOut;
    }

}