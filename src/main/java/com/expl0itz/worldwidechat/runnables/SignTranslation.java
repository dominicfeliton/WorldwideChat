package com.expl0itz.worldwidechat.runnables;

import java.util.Arrays;

import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslation;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
import com.expl0itz.worldwidechat.watson.WatsonTranslation;
import com.google.cloud.translate.TranslateException;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;

import co.aikar.taskchain.TaskChainTasks.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SignTranslation implements Task{

    private PlayerInteractEvent event;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public SignTranslation(PlayerInteractEvent e) {
        event = e;
    }

    @Override
    public Object run(Object input) {
        /* Get valid sign */
        Sign currentSign = (Sign) input;
        
        /* Init vars */
        ActiveTranslator currPlayer = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
        PlayerRecord currPlayerRecord = main.getPlayerRecord(event.getPlayer().getUniqueId().toString());
        String[] signText = currentSign.getLines();
        String[] changedSignText = new String[signText.length];
        boolean textLimit = false;
        boolean sameResult = false;
        
        /* Send message */
        final TextComponent signStart = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslateStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
            .build();
        main.adventure().sender(event.getPlayer()).sendMessage(signStart);
        
        /* Translate each line of sign */
        for (int i = 0; i < changedSignText.length; i++) {
            String eaLine = signText[i];
            if (main.getTranslatorName().equals("Watson") && eaLine.length() > 0) {
                try {
                    WatsonTranslation watsonInstance = new WatsonTranslation(eaLine,
                        currPlayer.getInLangCode(),
                        currPlayer.getOutLangCode(),
                        main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"),
                        main.getConfigManager().getMainConfig().getString("Translator.watsonURL"),
                        event.getPlayer());
                    eaLine = watsonInstance.translate();
                } catch (NotFoundException lowConfidenceInAnswer) {
                    final TextComponent lowConfidence = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
                }
            } else if (main.getTranslatorName().equals("Google Translate") && eaLine.length() > 0) {
                try {
                    GoogleTranslation googleTranslateInstance = new GoogleTranslation(eaLine,
                        currPlayer.getInLangCode(),
                        currPlayer.getOutLangCode(),
                        event.getPlayer());
                    eaLine = googleTranslateInstance.translate();
                } catch (TranslateException e) {
                    final TextComponent lowConfidence = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
                }
            }
            /* Save translated line */
            if (eaLine.length() > 15) {
                textLimit = true;
            }
            changedSignText[i] = eaLine;
        }
            /* Change sign for this user only, if translationNotTooLong and sign still exists*/
            sameResult = Arrays.equals(signText, changedSignText);
            if (!textLimit && !sameResult && currentSign.getLocation() != null) {
                /* Set completed message */
                currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations()+1);
                currPlayerRecord.setLastTranslationTime();
                currPlayerRecord.writeToConfig();
                getCurrentChain().setTaskData("translatedSign", changedSignText);
                final TextComponent signDone = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(signDone);
            }
            else if (sameResult) {
                /* If we are here, translation was unsuccessful */
                final TextComponent translationNoticeMsg = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslationFail").replace("%i", main.getTranslatorName())).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(translationNoticeMsg);
                    getCurrentChain().setTaskData("translatedSign", null);
            }
            else if (textLimit) {
                /* Format sign for chat, if translation exceeds 15 chars or sign was already deleted */
                String out = "\n";
                for (String eaLine : changedSignText) {
                    if (eaLine.length() > 1)
                        out += eaLine + "\n";
                }
                
                /* If we are here, sign is too long or deleted msg */
                final TextComponent translationNoticeMsg = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignDeletedOrTooLong")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .append(Component.text().content("\n" + "---------------").color(NamedTextColor.GOLD)
                    .append(Component.text().content(out).color(NamedTextColor.WHITE))
                    .append(Component.text().content("---------------").color(NamedTextColor.GOLD)))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(translationNoticeMsg);
                getCurrentChain().setTaskData("translatedSign", null);
            }
            return null;
    }

}
