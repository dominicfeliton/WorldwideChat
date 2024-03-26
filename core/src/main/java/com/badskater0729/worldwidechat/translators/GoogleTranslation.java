package com.badskater0729.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.badskater0729.worldwidechat.util.CommonRefs;
import org.apache.commons.lang3.StringUtils;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

import com.google.cloud.translate.TranslateOptions;

public class GoogleTranslation extends BasicTranslation {

	public GoogleTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	public GoogleTranslation(String apikey, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("GOOGLE_API_KEY", apikey); // we do this because .setApi() spams console :(
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		Future<String> process = callbackExecutor.submit(new translationTask());
		String finalOut = "";
		
		/* Get translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String call() throws Exception {
			/* Initialize translation object again */
			Translate translate = TranslateOptions.getDefaultInstance().getService();

			if (isInitializing) {
				/* Get languages */
				List<Language> allLanguages = translate.listSupportedLanguages();

				/* Parse languages */
				List<SupportedLang> outLangList = new ArrayList<SupportedLang>();
				List<SupportedLang> inLangList = new ArrayList<SupportedLang>();
				
				for (Language eaLang : allLanguages) {
					// Remove spaces from language name
					SupportedLang currLang = new SupportedLang(eaLang.getCode(), StringUtils.deleteWhitespace(eaLang.getName()), "");
					outLangList.add(currLang);
					inLangList.add(currLang);
				}

				/* Set languages list */
				main.setOutputLangs(refs.fixLangNames(outLangList, true));
				main.setInputLangs(refs.fixLangNames(inLangList, true));

				/* Setup test translation */
				inputLang = "en";
				outputLang = "es";
				textToTranslate = "How are you?";
			}
			
			/* Get language code of current input/output language. 
			 * APIs generally recognize language codes (en, es, etc.)
			 * instead of full names (English, Spanish) */
			if (!isInitializing) {
				if (!inputLang.equals("None")) {
					inputLang = refs.getSupportedTranslatorLang(inputLang, "in").getLangCode();
				}
				outputLang = refs.getSupportedTranslatorLang(outputLang, "out").getLangCode();
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