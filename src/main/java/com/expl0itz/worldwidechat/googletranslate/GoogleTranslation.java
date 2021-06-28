package com.expl0itz.worldwidechat.googletranslate;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.misc.SupportedLanguageObject;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.TranslateOptions;

public class GoogleTranslation {

    private String textToTranslate = "";
    private String inputLang = "";
    private String outputLang = "";
    private CommandSender sender;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public GoogleTranslation(String textToTranslate, String inputLang, String outputLang, CommandSender sender) {
        this.textToTranslate = textToTranslate;
        this.inputLang = inputLang;
        this.outputLang = outputLang;
        this.sender = sender;
    }
    
    public GoogleTranslation(String apikey) {
        System.setProperty("GOOGLE_API_KEY", apikey); //we do this because .setApi() spams console :(
    }
    
    public void initializeConnection() {
        Translate translate = TranslateOptions.getDefaultInstance().getService(); //we can do this because API key was already set by initializeConnection()
        
        /* Get languages */
        List <Language> allLanguages = translate.listSupportedLanguages();
        
        /* Parse languages */
        List < SupportedLanguageObject > outList = new ArrayList < SupportedLanguageObject >();
        for (Language eaLang : allLanguages) {
            outList.add(new SupportedLanguageObject(
                eaLang.getCode(),
                eaLang.getName(),
                "",
                true,
                true));
        }
        
        /* Test translation */
        Translation translation = translate.translate(
        		"Hello, how are you?", 
        		TranslateOption.sourceLanguage("en"),
        		TranslateOption.targetLanguage("es"),
        		TranslateOption.format("text"));
        
        /* Set langList in Main */
        main.setSupportedTranslatorLanguages(outList);
    }
    
    public String translate() {
        /* Convert input + output lang to lang code because this API is funky, man */
        if (!(inputLang.equals("None")) && !CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode().equals(inputLang)) {
            inputLang = CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode();
        }
        if (!CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode().equals(outputLang)) {
            outputLang = CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode();
        }
        
        /* Initialize translation object */
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        
        /* Actual translation */
        if (inputLang.equals("None")) { //if we do not know the input
            Detection detection = translate.detect(textToTranslate);
            inputLang = detection.getLanguage();
        }
        
        Translation translation = translate.translate(
            textToTranslate,
            TranslateOption.sourceLanguage(inputLang),
            TranslateOption.targetLanguage(outputLang),
            TranslateOption.format("text"));
        
        /* Process final output */
        String finalOut = translation.getTranslatedText();
        
        /* Return final result */
        return finalOut;
    }
    
}