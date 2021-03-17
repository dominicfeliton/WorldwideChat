package com.expl0itz.worldwidechat.googletranslate;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CachedTranslation;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.TranslateOptions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class GoogleTranslation {

    private String apiKey = "";
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
        this.apiKey = apikey;
        System.setProperty("GOOGLE_API_KEY", apiKey); //we do this because .setApi() spams console :(
    }
    
    public void initializeConnection() {
        Translate translate = TranslateOptions.getDefaultInstance().getService(); //we can do this because API key was already set by initializeConnection()
        
        /* Get languages */
        List<Language> allLanguages = translate.listSupportedLanguages();
        
        /* Parse languages */
        ArrayList < GoogleTranslateSupportedLanguageObject > outList = new ArrayList < GoogleTranslateSupportedLanguageObject >();
        for (Language eaLang : allLanguages) {
            outList.add(new GoogleTranslateSupportedLanguageObject(
                eaLang.getCode(),
                eaLang.getName()));
        }
        /* Set langList in Main */
        main.setSupportedGoogleTranslateLanguages(outList);
    }
    
    public String translate() {
        /* Sanitize Inputs */
        //Warn user about color codes
        //EssentialsX chat and maybe others replace "&4Test" with " 4Test"
        //Therefore, we find the " #" regex or the "&" char, and warn the user about it
        boolean essentialsColorCodeWarning = false;
        Audience adventureSender = main.adventure().sender(sender);
        
        for (int i = 0; i < textToTranslate.toCharArray().length; i++) {
            if (textToTranslate.toCharArray()[i] >= '0' && textToTranslate.toCharArray()[i] <= '9') {
                if (!(textToTranslate.toCharArray()[i - 1] >= '0' && textToTranslate.toCharArray()[i - 1] <= '~')) {
                    essentialsColorCodeWarning = true;
                    break; //don't waste time on this
                }
            }
        }
        if (!(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) //don't do any of this if /wwcg is enabled; players may not be in ArrayList and this will throw an exception
            &&
            (essentialsColorCodeWarning || textToTranslate.indexOf("&") != -1) //check sent chat to make sure it includes a CC
            &&
            !(main.getActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString()).getCCWarning())) //check if user has already been sent CC warning
        {
            final TextComponent googleTranslateCCWarning = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonColorCodeWarning")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                .build();
            adventureSender.sendMessage(googleTranslateCCWarning);
            //Set got CC warning of current translator to true, so that they don't get spammed by it if they keep using CCs
            main.getActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString()).setCCWarning(true);
            //we're still gonna translate it but it won't look pretty
        }

        /* Convert input + output lang to lang code because this API is funky, man */
        CommonDefinitions defs = new CommonDefinitions();
        if (!(inputLang.equals("None")) && !defs.getSupportedGoogleTranslateLang(inputLang).getLangCode().equals(inputLang)) {
            inputLang = defs.getSupportedGoogleTranslateLang(inputLang).getLangCode();
        }
        if (!defs.getSupportedGoogleTranslateLang(outputLang).getLangCode().equals(outputLang)) {
            outputLang = defs.getSupportedGoogleTranslateLang(outputLang).getLangCode();
        }
        
        /* Check cache */
        if (main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
            //Check cache for inputs, since config says we should
            for (int c = 0; c < main.getCache().size(); c++) {
                CachedTranslation currentTerm = main.getCache().get(c);
                if (currentTerm.getInputLang().equalsIgnoreCase(inputLang)
                        && (currentTerm.getOutputLang().equalsIgnoreCase(outputLang))
                        && (currentTerm.getInputPhrase().equalsIgnoreCase(textToTranslate))
                        ) {
                    currentTerm.setNumberOfTimes(currentTerm.getNumberOfTimes()+1);
                    //DEBUG: main.getLogger().info("Term was already cached: How many times = " + currentTerm.getNumberOfTimes() + " List size: " + main.getCache().size());
                    return ChatColor.translateAlternateColorCodes('&', currentTerm.getOutputPhrase()); //done :)
                }
            }
        }
        
        /* Initialize translation object */
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        
        /* Actual translation */
        boolean badInput = false;
        if (inputLang.equals("None")) { //if we do not know the input
            badInput = true;
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
        
        /* Add to cache */
        if (main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0 && !badInput) {
            CachedTranslation newTerm = new CachedTranslation(inputLang, outputLang, textToTranslate, finalOut);   
            main.addCacheTerm(newTerm);
        }
        
        /* Return final result */
        return ChatColor.translateAlternateColorCodes('&', finalOut);
    }
    
}