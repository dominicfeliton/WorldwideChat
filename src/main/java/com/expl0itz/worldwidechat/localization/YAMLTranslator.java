package com.expl0itz.worldwidechat.localization;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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
    			.withRegion(System.getProperty(amazonRegion))
    			.build();
       
		//Successfully piped; begin translation
    	List<String> translatedLines = Collections.emptyList();
		for (int i = 0; i < untranslated.size(); i++) {
			String translatedLine = "";
			if (untranslated.get(i).indexOf(":") != -1) {
				translatedLine = untranslated.get(i).substring(0, untranslated.get(i).indexOf(":"));
				
				/* Actual translation */
		    	TranslateTextRequest request = new TranslateTextRequest()
		    			.withText(untranslated.get(i).substring(untranslated.get(i).indexOf(":"), untranslated.size() - 1))
		    			.withSourceLanguageCode(inputLang)
		    			.withTargetLanguageCode(outputLang);
		    	TranslateTextResult result = translate.translateText(request);
		    	translatedLine += result.toString();
			} else {
				/* Actual translation */
		    	TranslateTextRequest request = new TranslateTextRequest()
		    			.withText(untranslated.get(i).substring(0, untranslated.size() - 1))
		    			.withSourceLanguageCode(inputLang)
		    			.withTargetLanguageCode(outputLang);
		    	TranslateTextResult result = translate.translateText(request);
		    	translatedLine += result.toString();
			}
	    	translatedLines.add(translatedLine);
	    	System.out.println("Translated line " + (i+1));
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
		System.out.println("Wrote translation to " + outputYAML + " successfully! Exiting...");
		scanner.close();
	}
	
}
