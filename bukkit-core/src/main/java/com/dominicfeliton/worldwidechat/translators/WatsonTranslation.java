package com.dominicfeliton.worldwidechat.translators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.Languages;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

public class WatsonTranslation extends BasicTranslation {

	// For normal translation operation
	public WatsonTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	// For initializeConnection
	public WatsonTranslation(String apikey, String serviceUrl, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("WATSON_API_KEY", apikey);
		System.setProperty("WATSON_SERVICE_URL", serviceUrl);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		Future<String> process = callbackExecutor.submit(new translationTask());
		String finalOut = "";
		
		/* Get test translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		
		/* Return final result */
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		@Override
		public String call() throws Exception {
			/* Init credentials */
			IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(System.getProperty("WATSON_API_KEY"))
					.build();
			LanguageTranslator translatorService = new LanguageTranslator("2018-05-01", authenticator);
			translatorService.setServiceUrl(System.getProperty("WATSON_SERVICE_URL"));

			if (isInitializing) {
				/* Get languages */
				Languages allLanguages = translatorService.listLanguages().execute().getResult();
				JsonElement jsonTree = JsonParser.parseString(allLanguages.toString());
				JsonObject jsonObject = jsonTree.getAsJsonObject();

				/* Parse json */
				final JsonArray dataJson = jsonObject.getAsJsonArray("languages");
				Map<String, SupportedLang> outLangList = new HashMap<>();
				Map<String, SupportedLang> inLangList = new HashMap<>();
				for (JsonElement element : dataJson) {
					// Ignore Chinese, IBM Watson lies and says they support it, but they do not
					if (((JsonObject) element).get("language_name").getAsString().contains("Chinese")) {
						continue;
					}

					// Generate SupportedLang obj
					SupportedLang currLang = new SupportedLang(((JsonObject) element).get("language").getAsString(),
							StringUtils.deleteWhitespace(((JsonObject) element).get("language_name").getAsString()),
							StringUtils.deleteWhitespace(((JsonObject) element).get("native_language_name").getAsString()));
					
					// Add to source list, if supported
					if (((JsonObject) element).get("supported_as_source").getAsBoolean()) {
						inLangList.put(currLang.getLangCode(), currLang);
						inLangList.put(currLang.getLangName(), currLang);
						inLangList.put(currLang.getNativeLangName(), currLang);
					}
					// Add to target list, if supported
					if (((JsonObject) element).get("supported_as_target").getAsBoolean()) {
						outLangList.put(currLang.getLangCode(), currLang);
						outLangList.put(currLang.getLangName(), currLang);
						outLangList.put(currLang.getNativeLangName(), currLang);
					}
				}

				/* Set supported translator languages */
				main.setOutputLangs(outLangList);
				main.setInputLangs(inLangList);

				/* Setup test translation */
				textToTranslate = "Hello, how are you?";
				inputLang = "en";
				outputLang = "es";
			}
			/* Actual translation */
			TranslateOptions options = new TranslateOptions.Builder().addText(textToTranslate)
					.source(inputLang.equalsIgnoreCase("None") ? "" : inputLang).target(outputLang).build();

			/* Process final output */
			TranslationResult translationResult = translatorService.translate(options).execute().getResult();
			JsonElement jsonTree = JsonParser.parseString(translationResult.toString());
			JsonObject jsonObject = jsonTree.getAsJsonObject();
			JsonElement translationSection = jsonObject.getAsJsonArray("translations").get(0).getAsJsonObject()
					.get("translation");
			String finalOut = translationSection.getAsString();

			/* Return result */
			return finalOut;
		}
	}

}