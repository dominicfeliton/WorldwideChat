package com.expl0itz.worldwidechat.localization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;

public class YAMLTranslator {

	//Local YAMLTranslator used for translating our localization files.
	//This uses amazon translate exclusively.
	
	public static void main(String[] args) {
		//Creds
		String amazonAccessKey = "";
		String amazonSecretKey = "";
		String amazonRegion = "us-east-2";
	
		//Other vars
		String inputLang = "";
		String outputLang = "";
		String originalYAML = "";
		String outputYAML = "";
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Enter directory of original YAML: ");
		originalYAML = scanner.nextLine().toString();
		
		System.out.println("Enter directory of output YAML: ");
		outputYAML = scanner.nextLine().toString();
		
		System.out.println("Original language: ");
		inputLang = scanner.nextLine().toString();
		
		System.out.println("New language: ");
		outputLang = scanner.nextLine().toString();
		
		//Parse YAML into local HashMap
		HashMap<String, String> untranslated = new HashMap<String, String>();
		YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(new File(originalYAML));
		YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(new File(outputYAML));
		
		try { 
			//Don't translate existing values
			if (new File(outputYAML).exists()) {
				System.out.println("Found existing file at output YAML path. \nParsing...");
				for (String eaKey : messagesConfig.getConfigurationSection("Messages").getKeys(true)) {
					if (!newConfig.contains("Messages." + eaKey)) {
						untranslated.put(eaKey, messagesConfig.getString("Messages." + eaKey));
					}
				}
			} else {
				/* Create new config */
				System.out.println("Creating new YAML...");
				newConfig.createSection("Messages");
				try {
					newConfig.save(new File(outputYAML));
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(0);
				}
				for (String eaKey : messagesConfig.getConfigurationSection("Messages").getKeys(true)) {
					untranslated.put(eaKey, messagesConfig.getString("Messages." + eaKey));
				}
			}
		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
	        System.exit(0);
		}

		/* Initialize AWS Creds + Translation Object */
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(amazonAccessKey, amazonSecretKey);
    	AmazonTranslate translate = AmazonTranslateClient.builder()
    			.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    			.withRegion(amazonRegion)
    			.build();
       
		//Successfully piped; begin translation
    	//ArrayList<String> translatedLines = new ArrayList<String>();
		for (Map.Entry<String, String> entry : untranslated.entrySet()) {
			String translatedLineName = entry.getKey();
			String translatedLine = "";
			System.out.println("(Original) " + entry.getValue());
			
			/* Actual translation */
			if (entry.getValue().length() > 0) {
				TranslateTextRequest request = new TranslateTextRequest()
		    			.withText(entry.getValue())
		    			.withSourceLanguageCode(inputLang)
		    			.withTargetLanguageCode(outputLang);
		    	TranslateTextResult result = translate.translateText(request);
		    	translatedLine += result.getTranslatedText();
			}
			
			//Replace BStats with bStats
			translatedLine = translatedLine.replaceAll("(?i)BStats", "bStats");
			
			//Replace WorldWideChat with WorldwideChat
			translatedLine = translatedLine.replaceAll("WorldWideChat", "WorldwideChat");
			
			//Replace any weird vars
			translatedLine = translatedLine.replaceAll("%I", "%i");
			translatedLine = translatedLine.replaceAll("%O", "%o");
			translatedLine = translatedLine.replaceAll("%E", "%e");
			translatedLine = translatedLine.replaceAll("(?i)% o", "%o");
			translatedLine = translatedLine.replaceAll("(?i)% e", "%e");
			translatedLine = translatedLine.replaceAll("(?i)% i", "%i");
			
			//Add a space before every %, escape all apostrophes
			ArrayList<Character> sortChars = new ArrayList<Character>(translatedLine.chars().mapToObj(c -> (char) c).collect(Collectors.toList()));
			for (int j = sortChars.size() - 1; j >= 0; j--) {
				//% check; no space before %
				if ((sortChars.get(j) == '%') && j-1 > -1 && !(Character.isSpaceChar(sortChars.get(j-1)) || Character.isWhitespace(sortChars.get(j-1)))) {
					sortChars.add(j, ' ');
					j--;
				}
				
				//Apostrophe check
				if (sortChars.get(j) == '\'') {
					sortChars.add(j, '\\');
					j--;
				}
				//Punctuation check
				if ((sortChars.get(j) == '!' || sortChars.get(j) == '.' || sortChars.get(j) == '?' || sortChars.get(j) == ':') && (sortChars.get(j-1) == ' ' || sortChars.get(j-1) == ' ')) {
					sortChars.remove(j-1);
					j--;
				}
			}
			
			//Save final translatedLine
			StringBuilder builder = new StringBuilder(sortChars.size());
			for (Character ch : sortChars) {
				builder.append(ch);
			}
			translatedLine = builder.toString();
			System.out.println("(Translated) " + translatedLine);
			
			//Translation done, write to new config file
			newConfig.set("Messages." + translatedLineName, translatedLine);
            try {
				newConfig.save(new File(outputYAML));
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		//Done!
		System.out.println("Wrote translation(s) to " + outputYAML + " successfully! \nExiting...");
		scanner.close();
	}
	
}
