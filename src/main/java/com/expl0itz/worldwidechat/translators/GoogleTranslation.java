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

import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

import com.google.cloud.translate.TranslateOptions;

public class GoogleTranslation {

	private String textToTranslate = "";
	private String inputLang = "";
	private String outputLang = "";

	private CommandSender sender;

	private boolean isInitializing = false;

	private WorldwideChat main = WorldwideChat.instance;

	public GoogleTranslation(String textToTranslate, String inputLang, String outputLang, CommandSender sender) {
		this.textToTranslate = textToTranslate;
		this.inputLang = inputLang;
		this.outputLang = outputLang;
		this.sender = sender;
	}

	public GoogleTranslation(String apikey) {
		System.setProperty("GOOGLE_API_KEY", apikey); // we do this because .setApi() spams console :(
		isInitializing = true;
	}

	public String useTranslator() throws TranslatorTimeoutException, TranslatorFailException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut = "";
		try {
			/* Get translation */
			finalOut = process.get(main.getMaxResponseTime(), TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			CommonDefinitions.sendDebugMessage("Google Translate Timeout!!");
			process.cancel(true);
			if (e instanceof ExecutionException) {
			    throw new TranslatorFailException(e);	
			}
			throw new TranslatorTimeoutException("Timed out while waiting for Google Translate response.", e);
		} finally {
			executor.shutdownNow();
		}
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		@Override
		public String call() throws Exception {
			/* Initialize translation object again */
			Translate translate = TranslateOptions.getDefaultInstance().getService();

			if (isInitializing) {
				/* Get languages */
				List<Language> allLanguages = translate.listSupportedLanguages();

				/* Parse languages */
				List<SupportedLanguageObject> outList = new ArrayList<SupportedLanguageObject>();
				for (Language eaLang : allLanguages) {
					outList.add(new SupportedLanguageObject(eaLang.getCode(), eaLang.getName(), "", true, true));
				}

				/* Set languages list */
				main.setSupportedTranslatorLanguages(outList);

				/* Setup test translation */
				inputLang = "en";
				outputLang = "es";
				textToTranslate = "How are you?";
			}
			/* Convert input + output lang to lang code because this API is funky, man */
			if (!(inputLang.equals("None"))
					&& !CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode().equals(inputLang)) {
				inputLang = CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode();
			}
			if (!CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode().equals(outputLang)) {
				outputLang = CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode();
			}

			/* Detect inputLang */
			if (inputLang.equals("None")) { // if we do not know the input
				Detection detection = translate.detect(textToTranslate);
				inputLang = detection.getLanguage();
			}

			/* Actual translation */
			Translation translation = translate.translate(textToTranslate, TranslateOption.sourceLanguage(inputLang),
					TranslateOption.targetLanguage(outputLang), TranslateOption.format("text"));
			return translation.getTranslatedText();
		}
	}

}