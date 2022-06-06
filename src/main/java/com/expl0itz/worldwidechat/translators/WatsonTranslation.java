package com.expl0itz.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
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

public class WatsonTranslation extends BasicTranslation {

	// For normal translation operation
	public WatsonTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}

	// For initializeConnection
	public WatsonTranslation(String apikey, String serviceUrl, boolean isInitializing) {
		super(isInitializing);
		System.setProperty("WATSON_API_KEY", apikey);
		System.setProperty("WATSON_SERVICE_URL", serviceUrl);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut = "";
		
		/* Get test translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		process.cancel(true);
		executor.shutdownNow();
		
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
				List<SupportedLanguageObject> outList = new ArrayList<SupportedLanguageObject>();
				for (JsonElement element : dataJson) {
					if (((JsonObject) element).get("supported_as_source").getAsBoolean()
							&& ((JsonObject) element).get("supported_as_target").getAsBoolean()) {
						outList.add(new SupportedLanguageObject(((JsonObject) element).get("language").getAsString(),
								((JsonObject) element).get("language_name").getAsString(),
								((JsonObject) element).get("native_language_name").getAsString(),
								((JsonObject) element).get("supported_as_source").getAsBoolean(),
								((JsonObject) element).get("supported_as_target").getAsBoolean()));
					}
				}

				/* Set supported translator languages */
				main.setSupportedTranslatorLanguages(outList);

				if (outList.size() == 0) {
					main.getLogger().warning(CommonDefinitions.getMessage("wwcBackupLangCodesWarning"));
					CommonDefinitions.sendDebugMessage("---> Using backup codes!!! Fix this!!! <---");
					setBackupCodes();
				}
				
				/* Setup test translation */
				textToTranslate = "Hello, how are you?";
				inputLang = "en";
				outputLang = "es";
			}
			/* Actual translation */
			TranslateOptions options = new TranslateOptions.Builder().addText(textToTranslate)
					.source(inputLang.equals("None") ? "" : inputLang).target(outputLang).build();

			/* Process final output */
			TranslationResult translationResult = translatorService.translate(options).execute().getResult();
			JsonElement jsonTree = JsonParser.parseString(translationResult.toString());
			JsonObject jsonObject = jsonTree.getAsJsonObject();
			JsonElement translationSection = jsonObject.getAsJsonArray("translations").get(0).getAsJsonObject()
					.get("translation");
			String finalOut = translationSection.toString().substring(1, translationSection.toString().length() - 1);

			/* Return result */
			return finalOut;
		}
	}

}