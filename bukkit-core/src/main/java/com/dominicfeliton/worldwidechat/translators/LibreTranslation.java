package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class LibreTranslation extends BasicTranslation {

	CommonRefs refs = main.getServerFactory().getCommonRefs();
	public LibreTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	public LibreTranslation(String apikey, String serviceUrl, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		if (apikey == null || apikey.equalsIgnoreCase("none")) {
			System.setProperty("LIBRE_API_KEY", "");
		} else {
			System.setProperty("LIBRE_API_KEY", apikey);
		}
		System.setProperty("LIBRE_SERVICE_URL", serviceUrl);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		Future<String> process = callbackExecutor.submit(new translationTask());
		String finalOut;
		
		/* Get translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		
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
				
				Map<String, SupportedLang> outLangMap = new HashMap<>();
				Map<String, SupportedLang> inLangMap = new HashMap<>();

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
						outLangMap.put(currLang.getLangCode(), currLang);
						outLangMap.put(currLang.getLangName(), currLang);

						inLangMap.put(currLang.getLangCode(), currLang);
						inLangMap.put(currLang.getLangName(), currLang);
					}
				} else {
					checkError(listResponseCode);
				}
				
				/* Parse languages */
				main.setOutputLangs(refs.fixLangNames(outLangMap, true, false));
				main.setInputLangs(refs.fixLangNames(inLangMap, true, false));

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
					inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
				}
				outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
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
					refs.debugMsg("Failed..." + statusCode);
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
				refs.debugMsg("Failed..." + statusCode);
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
			throw new Exception(refs.getMsg("libreHttp" + in, null));
		default:
			throw new Exception(refs.getMsg("libreHttpUnknown", in + "", null));
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