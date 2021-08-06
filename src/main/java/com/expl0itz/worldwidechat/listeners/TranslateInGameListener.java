package com.expl0itz.worldwidechat.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.TempItemInventory;
import com.expl0itz.worldwidechat.runnables.BookTranslation;
import com.expl0itz.worldwidechat.runnables.ItemTranslation;
import com.expl0itz.worldwidechat.runnables.SignTranslation;

import co.aikar.taskchain.TaskChain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TranslateInGameListener implements Listener {
	
    private WorldwideChat main = WorldwideChat.getInstance();
    
    /* Book Translation */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookTranslate(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingBook() 
                && event.getHand() != null 
                && event.getItem() != null
                && Material.WRITTEN_BOOK == event.getItem().getType()
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            /* Get vars, start book translation */
            try {
                TaskChain<?> chain = WorldwideChat.newSharedChain("bookTranslate");
                ItemStack currentBook = event.getItem();
                chain
                    .async(() -> {
                        new BookTranslation(event).run(currentBook);
                    })
                    .sync(() -> {
                        ItemStack newBook = chain.getTaskData("translatedBook");
                        event.getPlayer().openBook(newBook);
                    })
                    .sync(TaskChain::abort)
                    .execute();
            } catch (Exception e) {
                final TextComponent translationFailMsg = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslationFailCatastrophic")).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
            }
        }
    }
    
    /* Sign Translation */
    @EventHandler(priority = EventPriority.HIGHEST) 
    public void onSignTranslate(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingSign() 
                && event.getClickedBlock() != null) {
            if ((event.getClickedBlock().getType().name().contains("SIGN") && event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                try {
                    /* Start sign translation */
                    Sign currentSign = (Sign) event.getClickedBlock().getState();
                    TaskChain<?> chain = WorldwideChat.newSharedChain("signTranslate");
                    chain
                        .async(() -> {
                            new SignTranslation(event).run(currentSign);
                        })
                        .sync(() -> {
                            String[] translatedSign = chain.getTaskData("translatedSign");
                            if (translatedSign != null) {
                                event.getPlayer().sendSignChange(currentSign.getLocation(), translatedSign);
                            }
                        })
                        .sync(TaskChain::abort)
                        .execute();
                } catch (Exception e) {
                    final TextComponent translationFailMsg = Component.text()
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslationFailCatastrophic")).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                            .build();
                        main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
                }
            }
        }
    }
    
    /* Item Name + Lore Translation */
    @EventHandler(priority = EventPriority.HIGHEST)
	public void onItemTranslateRequest(PlayerInteractEvent event) {
		if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
				&& main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingItem() 
				&& event.getPlayer().getInventory() != null && event.getPlayer().getInventory().getItemInMainHand() != null 
				&& event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR
				&& event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				) {
			try {
				/* Start item translation */
				ItemStack currentItem = event.getPlayer().getInventory().getItemInMainHand();
				TaskChain<?> chain = WorldwideChat.newSharedChain("itemTranslate");
				chain
				    .async(() -> {
				    	new ItemTranslation(event).run(currentItem);
				    })
				    .sync(() -> {
				    	ItemStack translatedItem = chain.getTaskData("translatedItem");
				    	if (translatedItem != null) {
				    		TempItemInventory.getTempItemInventory(translatedItem).open(event.getPlayer());
				    	}
				    })
				    .sync(TaskChain::abort)
				    .execute();
			} catch (Exception e) {
				final TextComponent translationFailMsg = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslationFailCatastrophic").replace("%i", main.getTranslatorName())).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
			}
		}
	}
}
