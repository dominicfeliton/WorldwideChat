package com.expl0itz.worldwidechat.localization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
		//String supportedLangs[] = new CommonDefinitions().getSupportedPluginLangCodes();
		String inputLang = "en";
		String originalYAMLDir = "";
		String outputYAMLDir = "";
		String originalYAML = "";
		String outputYAML = "";
		
		Scanner scanner = new Scanner(System.in);
		
		/* Get supported languages from AWS docs */
    	ArrayList < String > temp = new ArrayList < String >();
		Document doc;
		try {
			doc = Jsoup.connect("https://docs.aws.amazon.com/translate/latest/dg/what-is.html#what-is-languages").get();
			Elements tr = doc.select("tr");
			for (int i = 1; i < tr.size(); i++) {
				Elements td = tr.get(i).select("td");
				if (td.size() > 0) {
					//langCode, langName == AmazonLangObj constructor
					//HTML page starts with langName, then langCode
					temp.add(td.get(1).html());
				}
			}
		} catch (IOException e2) {
			e2.printStackTrace();
			System.exit(0);
		}
		String[] supportedLangs = new String[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			supportedLangs[i] = temp.get(i);
		}
		
		System.out.println("Enter parent directory of original YAML: (include ending /)");
		originalYAMLDir = scanner.nextLine().toString();
		
		System.out.println("Enter parent directory of output YAML (include ending /): ");
		outputYAMLDir = scanner.nextLine().toString();
		
		for (String eaSupportedLang : supportedLangs) {
			if (!eaSupportedLang.equals(inputLang)) {
				originalYAML = originalYAMLDir + "messages-" + inputLang + ".yml";
				outputYAML = outputYAMLDir + "messages-" + eaSupportedLang + ".yml";
				
				System.out.println("Input language is " + inputLang + ".");
				
				System.out.println("Output language is currently " + eaSupportedLang + ".");
				
				//Parse YAML into local HashMap
				HashMap<String, String> untranslated = new HashMap<String, String>();
				YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(new File(originalYAML));
				YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(new File(outputYAML));
				
				try { 
					//Don't translate existing values
					if (new File(outputYAML).exists()) {
						System.out.println("Found existing file at output YAML path. \nParsing...");
						//Find new keys from original config
						for (String eaKey : messagesConfig.getConfigurationSection("Messages").getKeys(true)) {
							if (!newConfig.contains("Messages." + eaKey)) {
								untranslated.put(eaKey, messagesConfig.getString("Messages." + eaKey));
							}
						}
						//Find old unneeded keys from new config and delete them
						for (String eaKey : newConfig.getConfigurationSection("Messages").getKeys(true)) {
							if (!messagesConfig.contains("Messages." + eaKey)) {
								newConfig.set("Messages." + eaKey, null);
								newConfig.save(outputYAML);
								System.out.println("Deleted old key: " + eaKey);
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
				} catch (Exception e) {
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
				    			.withTargetLanguageCode(eaSupportedLang);
				    	TranslateTextResult result = translate.translateText(request);
				    	translatedLine += result.getTranslatedText();
					}
					
					//Replace BStats with bStats
					translatedLine = translatedLine.replaceAll("(?i)BStats", "bStats");
					
					//Fix WorldwideChat Typos
					translatedLine = translatedLine.replaceAll("(?i)WorldWideChat", "WorldwideChat");
					translatedLine = translatedLine.replaceAll("(?i)WorldVideChat", "WorldwideChat");
					translatedLine = translatedLine.replaceAll("(?i)WorldwideCat", "WorldwideChat");
					
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
						if ((sortChars.get(j) == '!' || sortChars.get(j) == '.' || sortChars.get(j) == '?' || sortChars.get(j) == ':') && j-1 > -1 && (Character.isSpaceChar(sortChars.get(j-1)) || Character.isWhitespace(sortChars.get(j-1)))) {
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
			}
			System.out.println("Done with " + eaSupportedLang + "...");
		}
		//Done!
		System.out.println("Wrote translation(s) to " + outputYAMLDir + " successfully! \nExiting...");
		scanner.close();
	}
	
}
