package com.badskater0729.worldwidechat.translators;

import java.io.InputStream;
import java.net.URL;
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

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonDefinitions;
import com.badskater0729.worldwidechat.util.SupportedLanguageObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
			if (isInitializing) {
				/* Get languages */
				URL url = new URL(System.getProperty("LIBRE_SERVICE_URL") + "/languages");
				
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setReadTimeout(WorldwideChat.translatorConnectionTimeoutSeconds*1000);
				conn.setConnectTimeout(WorldwideChat.translatorFatalAbortSeconds*1000);
				conn.connect();
				
				int listResponseCode = conn.getResponseCode();
				
				List<SupportedLanguageObject> outList = new ArrayList<SupportedLanguageObject>();
				if (listResponseCode == 200) {
					// Scan response
					String inLine = "";
				    Scanner scanner = new Scanner(url.openStream());
				  
				    while (scanner.hasNext()) {
				       inLine += scanner.nextLine();
				    }
				    
				    scanner.close();
				    
				    JsonElement jsonTree = JsonParser.parseString(inLine);
					for (JsonElement element : jsonTree.getAsJsonArray()) {
						JsonObject eaProperty = (JsonObject) element;
						outList.add(new SupportedLanguageObject(
								eaProperty.get("code").getAsString(),
								eaProperty.get("name").getAsString()));
					}
				} else {
					checkError(listResponseCode);
				}
				
				/* Parse languages */
				main.setSupportedTranslatorLanguages(outList);
				
				if (outList.size() == 0) {
					main.getLogger().warning(CommonDefinitions.getMessage("wwcBackupLangCodesWarning"));
					CommonDefinitions.sendDebugMessage("---> Using backup codes!!! Fix this!!! <---");
					setBackupCodes();
				}

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
					inputLang = CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode();
				}
				outputLang = CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode();
			}
			
			/* Detect inputLang */
			if (inputLang.equals("None")) { // if we do not know the input
				/* Craft detection request */
				CloseableHttpClient client = HttpClients.createDefault();
				CloseableHttpResponse response;
				
		        HttpPost post = new HttpPost(System.getProperty("LIBRE_SERVICE_URL") + "/detect");
				String baseJson = "{\"q\":\"" + textToTranslate + "\"";
				baseJson = appendJsonEnding(baseJson);
				StringEntity se = new StringEntity(baseJson);
	            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	            post.setEntity(se);
	            
	            response = client.execute(post);
	            int statusCode = response.getStatusLine().getStatusCode();
	            
	            /* Process response */
	            if (response != null && statusCode == 200) {
	                InputStream in = response.getEntity().getContent(); //Get the data in the entity
	                String unparsedResult = IOUtils.toString(in, StandardCharsets.UTF_8);
	                JsonElement jsonTree = JsonParser.parseString(unparsedResult);
	                String result = jsonTree.getAsJsonArray().get(0).getAsJsonObject().get("language").getAsString();
				    inputLang = result;
	            } else {
	            	CommonDefinitions.sendDebugMessage("Failed..." + statusCode);
	            	checkError(statusCode);
	            }
			}

			/* Actual translation */
			CloseableHttpClient client = HttpClients.createDefault();
			
			CloseableHttpResponse response;
	        HttpPost post = new HttpPost(System.getProperty("LIBRE_SERVICE_URL") + "/translate");
	        String baseJson = "{\"q\":\"" + textToTranslate + 
					"\",\"source\":\"" + inputLang + 
					"\",\"target\":\"" + outputLang + 
					"\",\"format\":\"text\"";
	        baseJson = appendJsonEnding(baseJson);
	        StringEntity se = new StringEntity(baseJson);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();

            /* Checking response */
            if (response != null && statusCode == 200) {
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                String unparsedResult = IOUtils.toString(in, StandardCharsets.UTF_8);
                JsonElement jsonTree = JsonParser.parseString(unparsedResult);
                String result = jsonTree.getAsJsonObject().get("translatedText").getAsString();
			    return result;
            } else {
            	CommonDefinitions.sendDebugMessage("Failed..." + statusCode);
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
			throw new Exception(CommonDefinitions.getMessage("libreHttp" + in));
		default:
			throw new Exception(CommonDefinitions.getMessage("libreHttpUnknown", new String[] {in + ""}));
		}
	}
	
	private String appendJsonEnding(String baseJson) {
		if (System.getProperty("LIBRE_API_KEY") != null && !System.getProperty("LIBRE_API_KEY").equals("")) {
			CommonDefinitions.sendDebugMessage("Using api key...");
        	baseJson += ",\"api_key\":\"" + System.getProperty("LIBRE_API_KEY") + "\"}";
        } else {
        	baseJson += "}";
        }
		return baseJson;
	}

}
