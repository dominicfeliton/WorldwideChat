package com.expl0itz.worldwidechat.runnables;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;

import co.aikar.taskchain.TaskChainTasks.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ItemTranslation implements Task<ItemStack, ItemStack> {

    private PlayerInteractEvent event;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public ItemTranslation(PlayerInteractEvent e) {
        event = e;
    }

    @Override
    public ItemStack run(ItemStack currentItem) {
    	/* Init vars */
    	ItemMeta meta = currentItem.getItemMeta();
    	String translatedName = "";
    	List<String> itemLore = meta.getLore();
    	ArrayList<String> outLore = new ArrayList<String>();
    	boolean sameResult = false;
    	
    	/* Send message */
        final TextComponent itemStart = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
            .build();
        main.adventure().sender(event.getPlayer()).sendMessage(itemStart);
    	
    	/* Translate item title */
    	if (meta.hasDisplayName()) {
    		translatedName = CommonDefinitions.translateText(meta.getDisplayName(), event.getPlayer());
    		if (!translatedName.equalsIgnoreCase(meta.getDisplayName())) {
    			/* Set completed message */
                final TextComponent itemTitleDone = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateTitleDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(itemTitleDone);
    		} else {
    			translatedName = "";
    			final TextComponent itemTitleFail = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateTitleFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                        .build();
                main.adventure().sender(event.getPlayer()).sendMessage(itemTitleFail);
    		}
    	} else {
    		final TextComponent itemStockTitleFail = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateTitleStock")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(itemStockTitleFail);
    		// We cannot translate stock items to a custom name, as all item enums are in English.
    		// If the user is using another language in their respective Minecraft client, there is no way that
    		// The server can know this. Therefore, vanilla items are not supported for translation.
    	}
		
		/* Translate item lore */
		if (meta.hasLore()) {
			final TextComponent itemLoreStart = Component.text()
		            .append(main.getPluginPrefix().asComponent())
		            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateLoreStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
		            .build();
		        main.adventure().sender(event.getPlayer()).sendMessage(itemLoreStart);
			outLore = new ArrayList<String>();
			for (String eaLine : itemLore) {
				String translatedLine = CommonDefinitions.translateText(eaLine, event.getPlayer());
				if (eaLine.equals(translatedLine)) {
					sameResult = true;
				}
				outLore.add(translatedLine);
			}
			if (!sameResult) {
				/* Set completed message */
	            final TextComponent itemLoreDone = Component.text()
	                .append(main.getPluginPrefix().asComponent())
	                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateLoreDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
	                .build();
	            main.adventure().sender(event.getPlayer()).sendMessage(itemLoreDone);
			} else {
				final TextComponent itemLoreFail = Component.text()
	                    .append(main.getPluginPrefix().asComponent())
	                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateLoreFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
	                    .build();
	                main.adventure().sender(event.getPlayer()).sendMessage(itemLoreFail);
			}
		}
    	
		/* Create "fake" item to be displayed to user */
		ItemStack translatedItem = currentItem.clone();
		ItemMeta translatedMeta = translatedItem.getItemMeta();
		translatedMeta.setDisplayName(translatedName);
		translatedMeta.setLore(outLore);
		translatedItem.setItemMeta(translatedMeta);
		
		/* Return fake item */
		if (!translatedName.equals("") || !outLore.isEmpty()) {
			getCurrentChain().setTaskData("translatedItem", translatedItem);
	    	return translatedItem;
		}
		getCurrentChain().setTaskData("translatedItem", null);
		return null;
    }
}