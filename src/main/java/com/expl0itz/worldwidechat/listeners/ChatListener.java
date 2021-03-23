package com.expl0itz.worldwidechat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.ibm.cloud.sdk.core.service.exception.NotFoundException;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslation;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
import com.expl0itz.worldwidechat.watson.WatsonTranslation;
import com.google.cloud.translate.TranslateException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChatListener implements Listener {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) instanceof ActiveTranslator || main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) {
            event.setMessage(processPlayerChat(event.getPlayer(), event.getMessage())); 
        }
    }
    
    public String processPlayerChat(Player inPlayer, String inMessage) {
        String out = "";
        /* Initialize current ActiveTranslator, sanity checks */
        ActiveTranslator currPlayer;
        if (!(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) && (main.getActiveTranslator(inPlayer.getUniqueId().toString()) != null)) {
            //This UDID is never valid, but we can use it as a less elegant way to check if global translate (/wwcg) is enabled.
            currPlayer = main.getActiveTranslator(inPlayer.getUniqueId().toString());
        } else if ((main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) && (main.getActiveTranslator(inPlayer.getUniqueId().toString()) != null)){
            //global translation won't override per person
            currPlayer = main.getActiveTranslator(inPlayer.getUniqueId().toString()); 
        } else {
            currPlayer = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
        }
        
        /* Modify or create new player record*/
        PlayerRecord currPlayerRecord = main.getPlayerRecord(inPlayer.getUniqueId().toString(), true);
        currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations()+1);
        currPlayerRecord.writeToConfig();
        
        /* Begin actual translation, set message to output */
        if (main.getTranslatorName().equals("Watson")) {
            try {
            WatsonTranslation watsonInstance = new WatsonTranslation(inMessage,
                currPlayer.getInLangCode(),
                currPlayer.getOutLangCode(),
                main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"),
                main.getConfigManager().getMainConfig().getString("Translator.watsonURL"),
                inPlayer);
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
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(inPlayer).sendMessage(lowConfidence);
                return inMessage;
            }
        } else if (main.getTranslatorName().equals("Google Translate")) {
            try {
                GoogleTranslation googleTranslateInstance = new GoogleTranslation(inMessage,
                    currPlayer.getInLangCode(),
                    currPlayer.getOutLangCode(),
                    inPlayer);
                out =  googleTranslateInstance.translate();
            } catch (TranslateException e) {
                /* This exception happens for the same reason that Watson does: low confidence.
                 * Usually when a player tries to get around our same language translation block.
                 * Examples of when this triggers:
                 * .wwct en and typing in English.
                 */
                final TextComponent lowConfidence = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(inPlayer).sendMessage(lowConfidence);
                return inMessage;
            }
        }
        
        /* Set rest of record */
        currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations()+1);    
        currPlayerRecord.setLastTranslationTime();
        currPlayerRecord.writeToConfig();
        return out;
    }

}