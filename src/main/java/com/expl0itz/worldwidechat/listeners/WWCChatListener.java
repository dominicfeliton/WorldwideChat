package com.expl0itz.worldwidechat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.ibm.cloud.sdk.core.service.exception.NotFoundException;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.WWCActiveTranslator;
import com.expl0itz.worldwidechat.watson.WWCWatson;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WWCChatListener implements Listener {

    private WorldwideChat main = WorldwideChat.getInstance();

    //TODO: Add a new command to turn off lowConfidenceInAnswer message, as it may begin to get annoying.
    //Also add to config, maybe

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) instanceof WWCActiveTranslator || main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof WWCActiveTranslator) {
            WWCActiveTranslator currPlayer;
            if (!(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof WWCActiveTranslator)) {
                //This UDID is never valid, but we can use it as a less elegant way to check if global translate (/wwcg) is enabled.
                currPlayer = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
            } else {
                currPlayer = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
            }
            //if watson, if google translate, if bing, etc (TODO)
            //if global (TODO)
            try {
                if (main.getTranslatorName().equals("Watson")) {
                    WWCWatson watsonInstance = new WWCWatson(event.getMessage(),
                        currPlayer.getInLangCode(),
                        currPlayer.getOutLangCode(),
                        main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"),
                        main.getConfigManager().getMainConfig().getString("Translator.watsonURL"),
                        event.getPlayer());
                    //Get username + pass from config
                    event.setMessage(watsonInstance.translate());
                }
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
                main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
            }
        }
    }

}