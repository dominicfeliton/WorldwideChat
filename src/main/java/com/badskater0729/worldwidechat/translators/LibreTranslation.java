package com.badskater0729.worldwidechat.translators;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.getSupportedTranslatorLang;

public class LibreTranslation extends BasicTranslation {

	public LibreTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}

	public LibreTranslation(String apikey, String serviceUrl, boolean isInitializing) {
		super(isInitializing);
		if (apikey == null || apikey.equalsIgnoreCase("none")) {
			System.setProperty("LIBRE_API_KEY", "");
		} else {
			System.setProperty("LIBRE_API_KEY", apikey);
		}
		System.setProperty("LIBRE_SERVICE_URL", serviceUrl);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut;
		
		/* Get translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		process.cancel(true);
		executor.shutdownNow();
		
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		@Override
		public String call() throws Exception {
			// Init vars
			Gson gson = new Gson();

			if (isInitializing) {
				/* Get languages */
				URL url = new URL(System.getProperty("LIBRE_SERVICE_URL") + "/languages");
				
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.connect();
				
				int listResponseCode = conn.getResponseCode();
				
				List<SupportedLang> outLangList = new ArrayList<>();
				List<SupportedLang> inLangList = new ArrayList<>();
				if (listResponseCode == 200) {
					// Scan response
					StringBuilder inLine = new StringBuilder();
				    Scanner scanner = new Scanner(url.openStream());
				  
				    while (scanner.hasNext()) {
				       inLine.append(scanner.nextLine());
				    }
				    
				    scanner.close();
				    
				    // Get lang code/name, remove spaces from name
				    JsonElement jsonTree = JsonParser.parseString(inLine.toString());
					for (JsonElement element : jsonTree.getAsJsonArray()) {
						JsonObject eaProperty = (JsonObject) element;
						SupportedLang currLang = new SupportedLang(
								eaProperty.get("code").getAsString(),
								StringUtils.deleteWhitespace(eaProperty.get("name").getAsString()));
						outLangList.add(currLang);
						inLangList.add(currLang);
					}
				} else {
					checkError(listResponseCode);
				}
				
				/* Parse languages */
				main.setOutputLangs(outLangList);
				main.setInputLangs(inLangList);

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
			
			/* Detect inputLang */
			if (inputLang.equals("None")) { // if we do not know the input
				/* Craft detection request */
				URL url = new URL(System.getProperty("LIBRE_SERVICE_URL") + "/detect");
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				httpConn.setRequestMethod("POST");

				httpConn.setRequestProperty("accept", "application/json");
				httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				httpConn.setDoOutput(true);

				OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());

				writer.write("q=" + URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8));
				writer.flush();
				writer.close();
				httpConn.getOutputStream().close();

	            /* Process response */
				int statusCode = httpConn.getResponseCode();
				if (statusCode == 200) {
					InputStream responseStream = httpConn.getInputStream();
					Scanner s = new Scanner(responseStream).useDelimiter("\\A");
					String response = s.hasNext() ? s.next() : "";

					DetectResponse[] outArray = gson.fromJson(response, DetectResponse[].class);
					inputLang = outArray[0].getLanguage();
				} else {
					debugMsg("Failed..." + statusCode);
					checkError(statusCode);
				}
			}

			/* Actual translation */
			URL url = new URL(System.getProperty("LIBRE_SERVICE_URL") + "/translate");
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("POST");

			httpConn.setRequestProperty("accept", "application/json");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			httpConn.setDoOutput(true);

			OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());

			writer.write("q=" + URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8) + "&source=" + inputLang + "&target=" + outputLang + "&format=text");
			writer.flush();
			writer.close();
			httpConn.getOutputStream().close();

			/* Checking response */
			int statusCode = httpConn.getResponseCode();
			if (statusCode == 200) {
				InputStream responseStream = httpConn.getInputStream();
				Scanner s = new Scanner(responseStream).useDelimiter("\\A");
				String response = s.hasNext() ? s.next() : "";

				return gson.fromJson(response, TranslateResponse.class).getTranslatedText();
			} else {
				debugMsg("Failed..." + statusCode);
				checkError(statusCode);
			}
			return textToTranslate;
		}
	}
	
	private void checkError(int in) throws Exception {
		switch (in) {
		case 400:
		case 403:
		case 429:
		case 500:
			throw new Exception(getMsg("libreHttp" + in));
		default:
			throw new Exception(getMsg("libreHttpUnknown", in + ""));
		}
	}
}

class TranslateResponse {
	String translatedText;

	public String getTranslatedText() {
		return translatedText;
	}
}

class DetectResponse {
	private String language;

	private double confidenceLevel;

	public String getLanguage() { return language; }

	public Double getConfidenceLevel() { return confidenceLevel; }
}