package com.expl0itz.worldwidechat.misc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.amazonaws.services.translate.model.InvalidRequestException;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.amazontranslate.AmazonTranslateSupportedLanguageObject;
import com.expl0itz.worldwidechat.amazontranslate.AmazonTranslation;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslateSupportedLanguageObject;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslation;
import com.expl0itz.worldwidechat.watson.WatsonSupportedLanguageObject;
import com.expl0itz.worldwidechat.watson.WatsonTranslation;
import com.google.cloud.translate.TranslateException;
import com.google.common.base.CharMatcher;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class CommonDefinitions {

    private WorldwideChat main = WorldwideChat.getInstance();
    private ArrayList < WatsonSupportedLanguageObject > supportedWatsonLanguages = main.getSupportedWatsonLanguages();
    private ArrayList < GoogleTranslateSupportedLanguageObject > supportedGoogleTranslateLanguages = main.getSupportedGoogleTranslateLanguages();
    private ArrayList < AmazonTranslateSupportedLanguageObject > supportedAmazonTranslateLanguages = main.getSupportedAmazonTranslateLanguages();
    
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
        } else if (translator.equalsIgnoreCase("Amazon Translate")) {
        	for (AmazonTranslateSupportedLanguageObject eaLang : supportedAmazonTranslateLanguages) {
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
        } else if (translator.equalsIgnoreCase("Amazon Translate")) {
        	for (AmazonTranslateSupportedLanguageObject eaLang : supportedAmazonTranslateLanguages) {
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
        } else if (translator.equalsIgnoreCase("Amazon Translate")) {
        	for (AmazonTranslateSupportedLanguageObject eaLang : supportedAmazonTranslateLanguages) {
        		if ((eaLang.getLangName().equals(getSupportedAmazonTranslateLang(first).getLangName())
        			&& eaLang.getLangName().equals(getSupportedAmazonTranslateLang(second).getLangName()))) {
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
    
    public AmazonTranslateSupportedLanguageObject getSupportedAmazonTranslateLang(String in) {
    	for (AmazonTranslateSupportedLanguageObject eaLang : supportedAmazonTranslateLanguages) {
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
        } else if (main.getTranslatorName().equals("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), ";  
            }
        } else if (main.getTranslatorName().equals("Amazon Translate")) {
        	for (AmazonTranslateSupportedLanguageObject eaLang : supportedAmazonTranslateLanguages) {
        		out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), ";
        	}
        }
        if (out.indexOf(",") != -1) {
            out = out.substring(0, out.lastIndexOf(","));
        }
        return out;
    }
    
    public static String translateText(String inMessage, Player currPlayer) {
    	 /* Sanitize Inputs */
        //Warn user about color codes
        //EssentialsX chat and maybe others replace "&4Test" with " 4Test"
        //Therefore, we find the " #" regex or the "&" char, and warn the user about it
        boolean essentialsColorCodeWarning = false;
        Audience adventureSender = WorldwideChat.getInstance().adventure().sender(currPlayer);
        for (int i = 0; i < inMessage.toCharArray().length; i++) {
        	try {
                if (inMessage.toCharArray()[i] >= '0' && inMessage.toCharArray()[i] <= '9') {
                    if (!(inMessage.toCharArray()[i - 1] >= '0' && inMessage.toCharArray()[i - 1] <= '~')) {
                        essentialsColorCodeWarning = true;
                        break; //don't waste time on this
                    }
                }
        	} catch (Exception e) {
        		//Usually an ArrayIndexOutOfBoundsException; literally just ignore this, this check is purely optional anyways
        		//DEBUG: main.getLogger().info("caught exception :(");
        	}
        }
        if (!(WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) //don't do any of this if /wwcg is enabled; players may not be in ArrayList and this will throw an exception
            &&
            (essentialsColorCodeWarning || inMessage.indexOf("&") != -1) //check sent chat to make sure it includes a CC
            &&
            !(WorldwideChat.getInstance().getActiveTranslator(Bukkit.getServer().getPlayer(currPlayer.getName()).getUniqueId().toString()).getCCWarning())) //check if user has already been sent CC warning
        {
            final TextComponent watsonCCWarning = Component.text()
                .append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
                .append(Component.text().content(WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.watsonColorCodeWarning")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                .build();
            adventureSender.sendMessage(watsonCCWarning);
            //Set got CC warning of current translator to true, so that they don't get spammed by it if they keep using CCs
            WorldwideChat.getInstance().getActiveTranslator(Bukkit.getServer().getPlayer(currPlayer.getName()).getUniqueId().toString()).setCCWarning(true);
            //we're still gonna translate it but it won't look pretty
        }
    	
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
        
        /* Check cache */
        if (WorldwideChat.getInstance().getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
            //Check cache for inputs, since config says we should
            for (int c = 0; c < WorldwideChat.getInstance().getCache().size(); c++) {
                CachedTranslation currentTerm = WorldwideChat.getInstance().getCache().get(c);
                if (currentTerm.getInputLang().equalsIgnoreCase(currActiveTranslator.getInLangCode())
                        && (currentTerm.getOutputLang().equalsIgnoreCase(currActiveTranslator.getOutLangCode()))
                        && (currentTerm.getInputPhrase().equalsIgnoreCase(inMessage))
                        ) {
                    currentTerm.setNumberOfTimes(currentTerm.getNumberOfTimes()+1);
                    //DEBUG: main.getLogger().info("Term was already cached: How many times = " + currentTerm.getNumberOfTimes() + " List size: " + main.getCache().size());
                    // Update stats, return output
                    if (currPlayerRecord != null) {
                        currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations()+1);    
                        currPlayerRecord.setLastTranslationTime();
                        currPlayerRecord.writeToConfig();
                    }
                    return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', currentTerm.getOutputPhrase())); //done :)
                }
            }
        }
        
        /* Rate limit check */
        boolean isExempt = false;
        int personalRateLimit = 0;
        Set<PermissionAttachmentInfo> perms = currPlayer.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : perms) {
        	if (perm.getPermission().startsWith("worldwidechat.ratelimit.")) {
        		if (perm.getPermission().indexOf("exempt") != -1) {
        			isExempt = true;
        			break;
        		} else {
        			String delayStr = CharMatcher.inRange('0', '9').retainFrom(perm.getPermission());
        			if (!delayStr.isEmpty()) {
        				personalRateLimit = Integer.parseInt(delayStr);
        			}
        		}
        	}
        } if (!isExempt && personalRateLimit > 0) { // Personal Limits (Override Global)
        	if (!(currActiveTranslator.getRateLimitPreviousTime().equals("None"))) {
        		Instant previous = Instant.parse(currActiveTranslator.getRateLimitPreviousTime());
        		Instant currTime = Instant.now();
        		if (currTime.compareTo(previous.plus(personalRateLimit, ChronoUnit.SECONDS)) < 0) {
        			        final TextComponent rateLimit = Component.text()
                                .append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
                                .append(Component.text().content(WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcRateLimit")
                                		.replace("%i", "" + ChronoUnit.SECONDS.between(currTime, previous.plus(personalRateLimit, ChronoUnit.SECONDS)))).color(NamedTextColor.YELLOW))
                                .build();
                            WorldwideChat.getInstance().adventure().sender(currPlayer).sendMessage(rateLimit);
                            return inMessage;  	
        				} else {
        					currActiveTranslator.setRateLimitPreviousTime(Instant.now());
        				}
        	} else {
        		currActiveTranslator.setRateLimitPreviousTime(Instant.now());
        	}	
        } else if (!isExempt && WorldwideChat.getInstance().getRateLimit() > 0) { // Global Limits
           if (!(currActiveTranslator.getRateLimitPreviousTime().equals("None"))) {
        		Instant previous = Instant.parse(currActiveTranslator.getRateLimitPreviousTime());
        		Instant currTime = Instant.now();
        		int globalLimit = WorldwideChat.getInstance().getRateLimit();
        		if (currTime.compareTo(previous.plus(globalLimit, ChronoUnit.SECONDS)) < 0) {
        			        final TextComponent rateLimit = Component.text()
                                .append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
                                .append(Component.text().content(WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcRateLimit")
                                		.replace("%i", "" + ChronoUnit.SECONDS.between(currTime, previous.plus(globalLimit, ChronoUnit.SECONDS)))).color(NamedTextColor.YELLOW))
                                .build();
                            WorldwideChat.getInstance().adventure().sender(currPlayer).sendMessage(rateLimit);
                            return inMessage;  	
        				} else {
        					currActiveTranslator.setRateLimitPreviousTime(Instant.now());
        				}
        	} else {
        		currActiveTranslator.setRateLimitPreviousTime(Instant.now());
        	}	
        }
        
    	/* Begin actual translation, set message to output */
        String out = "";
        if (WorldwideChat.getInstance().getTranslatorName().equals("Watson")) {
            try {
                WatsonTranslation watsonInstance = new WatsonTranslation(inMessage,
                    currActiveTranslator.getInLangCode(),
                    currActiveTranslator.getOutLangCode(),
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
        } else if (WorldwideChat.getInstance().getTranslatorName().equals("Amazon Translate")) {
        	try {
        		AmazonTranslation amazonTranslateInstance = new AmazonTranslation(inMessage,
        			currActiveTranslator.getInLangCode(),
        			currActiveTranslator.getOutLangCode(),
        			currPlayer);
        		out = amazonTranslateInstance.translate();
        	} catch (InvalidRequestException e){
        		/* Low confidence exception, Amazon Translate Edition */
        		final TextComponent lowConfidence = Component.text()
                        .append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
                        .append(Component.text().content(WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                        .build();
                WorldwideChat.getInstance().adventure().sender(currPlayer).sendMessage(lowConfidence);
                return inMessage;
        	}
        }
        
        /* Update stats */
        if (currPlayerRecord != null) {
            currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations()+1);    
            currPlayerRecord.setLastTranslationTime();
            currPlayerRecord.writeToConfig();
        }
        
        /* Add to cache */
        if (WorldwideChat.getInstance().getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0 && !(currActiveTranslator.getInLangCode().equals("None"))) {
            CachedTranslation newTerm = new CachedTranslation(currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), inMessage, out);   
            WorldwideChat.getInstance().addCacheTerm(newTerm);
        }
        return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', out));
    }
    
    public String[] getSupportedPluginLangCodes() {
        return supportedPluginLangCodes;
    }

    public String[] getSupportedMCVersions() {
        return supportedMCVersions;
    }
}