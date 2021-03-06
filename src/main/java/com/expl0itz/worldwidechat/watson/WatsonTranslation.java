package com.expl0itz.worldwidechat.watson;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CachedTranslation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WatsonTranslation {

    //TODO: Color codes get completely removed...this is a must, but maybe we can add them back (unlikely)

    private WorldwideChat main = WorldwideChat.getInstance();
    
    private String textToTranslate = "";
    private String inputLang = "";
    private String outputLang = "";
    private String apikey = "";
    private String serviceUrl = "";
    private CommandSender sender;

    public WatsonTranslation(String textToTranslate, String inputLang, String outputLang, String apikey, String serviceUrl, CommandSender sender) {
        this.textToTranslate = textToTranslate;
        this.inputLang = inputLang;
        this.outputLang = outputLang;
        this.apikey = apikey;
        this.serviceUrl = serviceUrl;
        this.sender = sender;
    }

    public WatsonTranslation(String apikey, String serviceUrl, WorldwideChat main) {
        this.apikey = apikey;
        this.serviceUrl = serviceUrl;
        this.main = main;
    }

    public void testConnection() {
        /* Init credentials */
        IamAuthenticator authenticator = new IamAuthenticator(apikey);
        LanguageTranslator translatorService = new LanguageTranslator("2018-05-01", authenticator);
        translatorService.setServiceUrl(serviceUrl);

        /* Actual translation */
        TranslateOptions options = new TranslateOptions.Builder()
            .addText("la manzana")
            .source("es")
            .target("en")
            .build();

        /* Process final output */
        translatorService.translate(options).execute().getResult();
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
            final TextComponent watsonCCWarning = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonColorCodeWarning")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                .build();
            adventureSender.sendMessage(watsonCCWarning);
            //Set got CC warning of current translator to true, so that they don't get spammed by it if they keep using CCs
            main.getActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString()).setCCWarning(true);
            //we're still gonna translate it but it won't look pretty
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
                    return currentTerm.getOutputPhrase(); //done :)
                }
            }
        }
        
        /* Init credentials */
        IamAuthenticator authenticator = new IamAuthenticator(apikey);
        LanguageTranslator translatorService = new LanguageTranslator("2018-05-01", authenticator);
        translatorService.setServiceUrl(serviceUrl);

        /* Actual translation */
        TranslateOptions options = new TranslateOptions.Builder()
            .addText(textToTranslate)
            .source(inputLang.equals("None") ? "" : inputLang)
            .target(outputLang)
            .build();
        
        /* Process final output */
        TranslationResult translationResult = translatorService.translate(options).execute().getResult();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonTree = jsonParser.parse(translationResult.toString());
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        JsonElement translationSection = jsonObject.getAsJsonArray("translations").get(0).getAsJsonObject().get("translation");
        String finalOut = translationSection.toString().substring(1, translationSection.toString().length() - 1);
        
        /* Add to cache */
        if (main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0 && !(inputLang.equals("None"))) {
            CachedTranslation newTerm = new CachedTranslation(inputLang, outputLang, textToTranslate, finalOut);   
            main.addCacheTerm(newTerm);
        }
        
        /* Return final result */
        return finalOut;
    }

}