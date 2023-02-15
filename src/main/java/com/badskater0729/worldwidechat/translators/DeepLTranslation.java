package com.badskater0729.worldwidechat.translators;

import static com.badskater0729.worldwidechat.util.CommonRefs.getSupportedTranslatorLang;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;

public class DeepLTranslation extends BasicTranslation {

	public DeepLTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}
	
	public DeepLTranslation(String apikey, boolean isInitializing) {
		super(isInitializing);
		System.setProperty("DEEPL_API_KEY", apikey);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut = "";
		
		/* Get translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		process.cancel(true);
		executor.shutdownNow();
		
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		@Override
		public String call() throws Exception {
			//TODO: DeepL is currently a bit broken. Langs aren't being recognized properly for some reason...
			// Additionally, we likely will need to implement lists for supported source langs and target langs...
			// That sucks.
			// Because of above, english does not currently show as an option for DeepL.
			// Fix is imminent.
			
			/* Initialize translation object again */
			Translator translate = new Translator(System.getProperty("DEEPL_API_KEY"));

			if (isInitializing) {
				/* Get supported languages */
				List<Language> supportedLangs = translate.getTargetLanguages();
				
				/* Parse Supported Languages */
				List<SupportedLang> sourceLangs = new ArrayList<SupportedLang>();
				for (Language eaLang : translate.getSourceLanguages()) {
					sourceLangs.add(new SupportedLang(eaLang.getCode(), StringUtils.deleteWhitespace(eaLang.getName())));
				}
				List<SupportedLang> targetLangs = new ArrayList<SupportedLang>();
				for (Language eaLang : translate.getTargetLanguages()) {
					targetLangs.add(new SupportedLang(eaLang.getCode(), StringUtils.deleteWhitespace(eaLang.getName())));
				}

				/* Set languages list */
				main.setOutputLangs(targetLangs);
				main.setInputLangs(sourceLangs);

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
					inputLang = getSupportedTranslatorLang(inputLang, "in").getLangCode();
				}
				outputLang = getSupportedTranslatorLang(outputLang, "out").getLangCode();
			}

			/* If inputLang set to None, set as null for translateText() */
			if (inputLang.equals("None")) { // if we do not know the input
				inputLang = null;
			}

			/* Actual translation */
			TextResult result = translate.translateText(textToTranslate, inputLang,
					outputLang);
			return result.getText();
		}
	}
	
}
