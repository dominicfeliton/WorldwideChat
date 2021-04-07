package com.expl0itz.worldwidechat.misc;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslateSupportedLanguageObject;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslation;
import com.expl0itz.worldwidechat.watson.WatsonSupportedLanguageObject;
import com.expl0itz.worldwidechat.watson.WatsonTranslation;
import com.google.cloud.translate.TranslateException;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class CommonDefinitions {

    private WorldwideChat main = WorldwideChat.getInstance();
    private ArrayList < WatsonSupportedLanguageObject > supportedWatsonLanguages = main.getSupportedWatsonLanguages();
    private ArrayList < GoogleTranslateSupportedLanguageObject > supportedGoogleTranslateLanguages = main.getSupportedGoogleTranslateLanguages();
    
    /* Important vars */
    private String[] supportedMCVersions = {
        "1.16",
        "1.15",
        "1.14"
    };
    
    private String[] supportedPluginLangCodes = {
        "en",
        "es"
    };

    /* Getters */
    public boolean isSupportedLangForSource(String in, String translator) {
        if (translator.equalsIgnoreCase("Watson")) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in)) 
                    && eaLang.getSupportedAsSource()) {
                    return true;
                }
            }
        } else if (translator.equalsIgnoreCase("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSupportedLangForTarget(String in, String translator) {
        if (translator.equalsIgnoreCase("Watson")) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in)) 
                    && eaLang.getSupportedAsTarget()) {
                    return true;
                }
            }
        } else if (translator.equalsIgnoreCase("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isSameLang(String first, String second, String translator) {
        if (translator.equalsIgnoreCase("Watson") ) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                if ((eaLang.getLangName().equals(getSupportedWatsonLang(first).getLangName()) 
                        && eaLang.getLangName().equals(getSupportedWatsonLang(second).getLangName()))) {
                        return true;
                    }
            }
        } else if (translator.equalsIgnoreCase("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                if ((eaLang.getLangName().equals(getSupportedGoogleTranslateLang(first).getLangName()) 
                    && eaLang.getLangName().equals(getSupportedGoogleTranslateLang(second).getLangName()))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public WatsonSupportedLanguageObject getSupportedWatsonLang(String in) {
        for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in))) {
                return eaLang;
            }
        }
        return null;
    }
    
    public GoogleTranslateSupportedLanguageObject getSupportedGoogleTranslateLang(String in) {
        for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in))) {
                return eaLang;
            }
        }
        return null;
    }
    
    public String getValidLangCodes() {
        String out = "\n";
        if (main.getTranslatorName().equals("Watson")) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), "; 
            }
        }
        else if (main.getTranslatorName().equals("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), ";  
            }
        }
        if (out.indexOf(",") != -1) {
            out = out.substring(0, out.lastIndexOf(","));
        }
        return out;
    }
    
    public static String translateText(String inMessage, Player currPlayer) {
    	/* Any checks beforehand */
    	if (!(inMessage.length() > 0)) {
    		return "";
    	}
    	
    	/* Modify or create new player record */
        PlayerRecord currPlayerRecord = WorldwideChat.getInstance().getPlayerRecord(currPlayer.getUniqueId().toString(), true);
        if (currPlayerRecord != null) { //check if null
            currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations()+1);
            currPlayerRecord.writeToConfig();
        }
    	
    	/* Initialize current ActiveTranslator, sanity checks */
        ActiveTranslator currActiveTranslator;
        if (!(WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) && (WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString()) != null)) {
            //This UDID is never valid, but we can use it as a less elegant way to check if global translate (/wwcg) is enabled.
            currActiveTranslator = WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString());
        } else if ((WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) && (WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString()) != null)){
            //global translation won't override per person
            currActiveTranslator = WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString()); 
        } else {
        	currActiveTranslator = WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
        }
        
    	/* Begin actual translation, set message to output */
        String out = "";
        if (WorldwideChat.getInstance().getTranslatorName().equals("Watson")) {
            try {
                WatsonTranslation watsonInstance = new WatsonTranslation(inMessage,
                    currActiveTranslator.getInLangCode(),
                    currActiveTranslator.getOutLangCode(),
                    WorldwideChat.getInstance().getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"),
                    WorldwideChat.getInstance().getConfigManager().getMainConfig().getString("Translator.watsonURL"),
                    currPlayer);
                //Get username + pass from config
                out = watsonInstance.translate();
            } catch (NotFoundException lowConfidenceInAnswer) {
                /* This exception happens if the Watson translator is auto-detecting the input language.
                 * By definition, the translator is unsure if the source language detected is accurate due to 
                 * confidence levels being below a certain threshold.
                 * Usually, either already translated input is given or occasionally a phrase is not fully translatable.
                 * This is where we catch that and send the player a message telling them that their message was unable to be
                 * parsed by the translator.
                 * You should be able to turn this off in the config.
                 */
                final TextComponent lowConfidence = Component.text()
                    .append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
                    .append(Component.text().content(WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .build();
                WorldwideChat.getInstance().adventure().sender(currPlayer).sendMessage(lowConfidence);
                return inMessage;
            }
        } else if (WorldwideChat.getInstance().getTranslatorName().equals("Google Translate")) {
            try {
                GoogleTranslation googleTranslateInstance = new GoogleTranslation(inMessage,
                    currActiveTranslator.getInLangCode(),
                    currActiveTranslator.getOutLangCode(),
                    currPlayer);
                out = googleTranslateInstance.translate();
            } catch (TranslateException e) {
                /* This exception happens for the same reason that Watson does: low confidence.
                 * Usually when a player tries to get around our same language translation block.
                 * Examples of when this triggers:
                 * .wwct en and typing in English.
                 */
                final TextComponent lowConfidence = Component.text()
                    .append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
                    .append(Component.text().content(WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .build();
                WorldwideChat.getInstance().adventure().sender(currPlayer).sendMessage(lowConfidence);
                return inMessage;
            }
        }
        
        /* Update stats, return output */
        if (currPlayerRecord != null) {
            currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations()+1);    
            currPlayerRecord.setLastTranslationTime();
            currPlayerRecord.writeToConfig();
        }
        return out;
    }
    
    public String[] getSupportedPluginLangCodes() {
        return supportedPluginLangCodes;
    }

    public String[] getSupportedMCVersions() {
        return supportedMCVersions;
    }
}