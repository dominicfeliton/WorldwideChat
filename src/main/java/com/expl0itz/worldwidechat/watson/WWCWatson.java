package com.expl0itz.worldwidechat.watson;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

public class WWCWatson {

    private WorldwideChat main;
	
	public WWCWatson(WorldwideChat mainInstance)
	{
		main = mainInstance;
	}
	
	public String translateWithAPIKey(String textToTranslate, String inputLang, String outputLang, String apikey, String serviceUrl)
	{
		//Init credentials
		IamAuthenticator authenticator = new IamAuthenticator(apikey);
		LanguageTranslator translatorService = new LanguageTranslator("2018-05-01", authenticator);
		translatorService.setServiceUrl(serviceUrl);
		
		//Actual translation
		TranslateOptions options = new TranslateOptions.Builder()
				.addText(textToTranslate)
				.source(inputLang.equals("None")?"":inputLang)
				.target(outputLang)
				.build();
		
         //Process final output
		 TranslationResult translationResult = translatorService.translate(options).execute().getResult();
		 JsonParser jsonParser = new JsonParser();
		 JsonElement jsonTree = jsonParser.parse(translationResult.toString());
		 JsonObject jsonObject = jsonTree.getAsJsonObject();
		 JsonElement translationSection = jsonObject.getAsJsonArray("translations").get(0).getAsJsonObject().get("translation");
		 
		 return translationSection.toString().substring(1, translationSection.toString().length() - 1);
	      
	}
	
	
	
}
