package com.expl0itz.worldwidechat.runnables;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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

public class BookTranslation implements Task{

    private PlayerInteractEvent event;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public BookTranslation(PlayerInteractEvent e) {
        event = e;
    }

    @Override
    public Object run(Object input) {
        /* Init vars */
        ItemStack currentBook = event.getItem();
        ActiveTranslator currPlayer = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
        PlayerRecord currPlayerRecord = main.getPlayerRecord(event.getPlayer().getUniqueId().toString());
        BookMeta meta = (BookMeta) currentBook.getItemMeta();
        List<String> pages = meta.getPages();
        List<String> translatedPages = new ArrayList<String>();
        boolean sameResult = false;
        
        /* Translate pages */
        for (String eaPage : pages) {
            String out = "";
            if (main.getTranslatorName().equals("Watson") && eaPage.length() > 0) {
                try {
                    WatsonTranslation watsonInstance = new WatsonTranslation(eaPage,
                        currPlayer.getInLangCode(),
                        currPlayer.getOutLangCode(),
                        main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"),
                        main.getConfigManager().getMainConfig().getString("Translator.watsonURL"),
                        event.getPlayer());
                    out = watsonInstance.translate(); //lets \n be properly recognized
                } catch (NotFoundException lowConfidenceInAnswer) {
                    final TextComponent lowConfidence = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
                }
            } else if (main.getTranslatorName().equals("Google Translate") && eaPage.length() > 0) {
                try {
                    GoogleTranslation googleTranslateInstance = new GoogleTranslation(eaPage,
                        currPlayer.getInLangCode(),
                        currPlayer.getOutLangCode(),
                        event.getPlayer());
                    out = googleTranslateInstance.translate();
                } catch (TranslateException e) {
                    final TextComponent lowConfidence = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
                }
            }
            if (out.equals("") || out.equalsIgnoreCase(eaPage)) {
                sameResult = true;
                out = main.getConfigManager().getMessagesConfig().getString("Messages.wwctbTranslatePageFail").replace("%i", eaPage);
            }
            translatedPages.add(out);
        }
        
        /* Translate title - User never sees the title yet so for now we comment this out */
        String title = meta.getTitle();
        /*
        String newTitle = "";
        if (main.getTranslatorName().equals("Watson") && title.length() > 0) {
            try {
                WatsonTranslation watsonInstance = new WatsonTranslation(title,
                    currPlayer.getInLangCode(),
                    currPlayer.getOutLangCode(),
                    main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"),
                    main.getConfigManager().getMainConfig().getString("Translator.watsonURL"),
                    event.getPlayer());
                newTitle = watsonInstance.translate();
            } catch (NotFoundException lowConfidenceInAnswer) {
                final TextComponent lowConfidence = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
            }
        } else if (main.getTranslatorName().equals("Google Translate") && title.length() > 0) {
            try {
                GoogleTranslation googleTranslateInstance = new GoogleTranslation(title,
                    currPlayer.getInLangCode(),
                    currPlayer.getOutLangCode(),
                   event.getPlayer());
                newTitle = googleTranslateInstance.translate();
            } catch (TranslateException e) {
                final TextComponent lowConfidence = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.watsonNotFoundExceptionNotification")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(lowConfidence);
            }
        }
        if (newTitle.equals("") || newTitle.equalsIgnoreCase(title)) {
            sameResult = true;
            title = "?";
        }
        */
        if (!sameResult && currentBook != null) {
            /* Set completed message */
            currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations()+1);
            currPlayerRecord.setLastTranslationTime();
            currPlayerRecord.writeToConfig();
            final TextComponent bookDone = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                .build();
            main.adventure().sender(event.getPlayer()).sendMessage(bookDone);
        }
        else if (sameResult) {
            /* If we are here, one or more translations was unsuccessful */
            final TextComponent translationNoticeMsg = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslationFail").replace("%i", main.getTranslatorName())).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(translationNoticeMsg);
        }
        
        /* Create the modified book */
        ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta newMeta = (BookMeta) newBook.getItemMeta();
            newMeta.setAuthor(meta.getAuthor());
            newMeta.setGeneration(meta.getGeneration());
            newMeta.setTitle(title);
            newMeta.setPages(translatedPages);
            newBook.setItemMeta(newMeta);
        getCurrentChain().setTaskData("translatedBook", newBook);
        return null;
    }

}
