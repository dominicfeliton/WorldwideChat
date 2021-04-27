package com.expl0itz.worldwidechat.localization;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
		String amazonRegion = "";
	
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
		
		//Parse YAML into local ArrayList
		List<String> untranslated = Collections.emptyList(); 
		try { 
			untranslated = Files.readAllLines(Paths.get(originalYAML), StandardCharsets.UTF_8); 
		} catch (IOException e) { // TODO Auto-generated catch block
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
    	ArrayList<String> translatedLines = new ArrayList<String>();
		for (int i = 0; i < untranslated.size(); i++) {
			String translatedLine = "";
			System.out.println("(Original) " + untranslated.get(i));
			if (untranslated.get(i).indexOf("'") != -1) {
				translatedLine = untranslated.get(i).substring(0, untranslated.get(i).indexOf("'")+1);
				if ((untranslated.get(i).substring(untranslated.get(i).indexOf("'"), untranslated.get(i).length() - 1).length() > 0)) {
					/* Actual translation */
			    	TranslateTextRequest request = new TranslateTextRequest()
			    			.withText(untranslated.get(i).substring(untranslated.get(i).indexOf("'") + 1, untranslated.get(i).length()))
			    			.withSourceLanguageCode(inputLang)
			    			.withTargetLanguageCode(outputLang);
			    	TranslateTextResult result = translate.translateText(request);
			    	translatedLine += result.getTranslatedText();
				}
			} else {
				/* Actual translation */
				if (untranslated.get(i).length() > 0) {
					TranslateTextRequest request = new TranslateTextRequest()
			    			.withText(untranslated.get(i).substring(0, untranslated.get(i).length()))
			    			.withSourceLanguageCode(inputLang)
			    			.withTargetLanguageCode(outputLang);
			    	TranslateTextResult result = translate.translateText(request);
			    	translatedLine += result.getTranslatedText();
				}
			}
			
			//Replace WorldWideChat with WorldwideChat
			translatedLine = translatedLine.replaceAll("WorldWideChat", "WorldwideChat");
			
			//Remove any extra spaces added to the end of the line by translator
			if (translatedLine.indexOf(" ") != -1 && translatedLine.substring(translatedLine.lastIndexOf(" "), translatedLine.length()).length() < 3 && translatedLine.substring(translatedLine.lastIndexOf(" "), translatedLine.length()).length() < untranslated.get(i).substring(untranslated.get(i).lastIndexOf(" "), untranslated.get(i).length()).length()) {
				translatedLine = translatedLine.substring(0, translatedLine.lastIndexOf(" ")) + translatedLine.substring(translatedLine.lastIndexOf(" ") +1, translatedLine.length());
			}
			
			//Replace weird "»" with a '
			translatedLine = translatedLine.replaceAll("»", "'");
			
			//Replace any capital vars with lowercase ones
			translatedLine = translatedLine.replaceAll("%I", "%i");
			translatedLine = translatedLine.replaceAll("%O", "%o");
			translatedLine = translatedLine.replaceAll("%E", "%e");
			
			//Ensure there is an ending '
			if ((!(translatedLine.lastIndexOf("'") > translatedLine.length() - 3)) && translatedLine.indexOf("#") == -1) {
				translatedLine = translatedLine += "'";
			}
			
			//Add a space before every %
			ArrayList<Character> sortChars = new ArrayList<Character>(translatedLine.chars().mapToObj(c -> (char) c).collect(Collectors.toList()));
			for (int j = 0; j < sortChars.size(); j++) {
				if ((sortChars.get(j) == '%') && j-1 > -1 && sortChars.get(j-1) != ' ') {
					sortChars.add(j, ' ');
					j++;
				}
			}
			
			//Save final translatedLine
			StringBuilder builder = new StringBuilder(sortChars.size());
			for (Character ch : sortChars) {
				builder.append(ch);
			}
			translatedLine = builder.toString();
			System.out.println("(Translated) " + translatedLine);
			
	    	translatedLines.add(translatedLine);
		}
		
		//Translation done, put into new file
		try {
			FileWriter writer = new FileWriter(outputYAML); 
			for(String str: translatedLines) {
				writer.write(str + System.lineSeparator());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		//Done!
		System.out.println("Wrote translation to " + outputYAML + " successfully! \nExiting...");
		scanner.close();
	}
	
}
