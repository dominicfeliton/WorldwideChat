package com.expl0itz.worldwidechat.amazontranslate;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.misc.SupportedLanguageObject;

public class AmazonTranslation {
    private String textToTranslate = "";
    private String inputLang = "";
    private String outputLang = "";
    private CommandSender sender;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public AmazonTranslation(String textToTranslate, String inputLang, String outputLang, CommandSender sender) {
        this.textToTranslate = textToTranslate;
        this.inputLang = inputLang;
        this.outputLang = outputLang;
        this.sender = sender;
    }
    
    public AmazonTranslation(String accessKeyId, String secretKeyId, String region) {
        System.setProperty("AMAZON_KEY_ID", accessKeyId);
        System.setProperty("AMAZON_SECRET_KEY", secretKeyId);
        System.setProperty("AMAZON_REGION", region);
    }
    
    public void initializeConnection() throws IOException {
    	/* Initialize AWS Creds + Translation Object */
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(System.getProperty("AMAZON_KEY_ID"), System.getProperty("AMAZON_SECRET_KEY"));
    	AmazonTranslate translate = AmazonTranslateClient.builder()
    			.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    			.withRegion(System.getProperty("AMAZON_REGION"))
    			.build();
    	
    	/* Test translation, will throw exception if it fails */
    	TranslateTextRequest request = new TranslateTextRequest()
    			.withText("Hi, how are you?")
    			.withSourceLanguageCode("en")
    			.withTargetLanguageCode("es");
    	translate.translateText(request);
    	
    	/* Get supported languages from AWS docs */
    	ArrayList < SupportedLanguageObject > supportedLangs = new ArrayList < SupportedLanguageObject >();
		Document doc = Jsoup.connect("https://docs.aws.amazon.com/translate/latest/dg/what-is.html#what-is-languages").get();
		Elements tr = doc.select("tr");
		for (int i = 1; i < tr.size(); i++) {
			Elements td = tr.get(i).select("td");
			if (td.size() > 0) {
				//langCode, langName == AmazonLangObj constructor
				//HTML page starts with langName, then langCode
				SupportedLanguageObject newObj = new SupportedLanguageObject(td.get(1).html(),
						td.get(0).html(), "", true, true);
				supportedLangs.add(newObj);
			}
		}
		main.setSupportedTranslatorLanguages(supportedLangs);
    }
    
    public String translate() {
        /* Convert input + output lang to lang code because this API is funky, man */
        CommonDefinitions defs = new CommonDefinitions();
        if (!(inputLang.equals("None")) && !defs.getSupportedTranslatorLang(inputLang).getLangCode().equals(inputLang)) {
            inputLang = defs.getSupportedTranslatorLang(inputLang).getLangCode();
        }
        if (!defs.getSupportedTranslatorLang(outputLang).getLangCode().equals(outputLang)) {
            outputLang = defs.getSupportedTranslatorLang(outputLang).getLangCode();
        }
        
        /* Initialize AWS Creds + Translation Object */
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(System.getProperty("AMAZON_KEY_ID"), System.getProperty("AMAZON_SECRET_KEY"));
    	AmazonTranslate translate = AmazonTranslateClient.builder()
    			.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    			.withRegion(System.getProperty("AMAZON_REGION"))
    			.build();
    	
    	/* Actual translation */
    	TranslateTextRequest request = new TranslateTextRequest()
    			.withText(textToTranslate)
    			.withSourceLanguageCode(inputLang.equals("None") ? "auto" : inputLang)
    			.withTargetLanguageCode(outputLang);
    	TranslateTextResult result = translate.translateText(request);
        
        /* Process + Return final result */
        return result.getTranslatedText();
    }
}
