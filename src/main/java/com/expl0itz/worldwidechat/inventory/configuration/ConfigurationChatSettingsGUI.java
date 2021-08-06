package com.expl0itz.worldwidechat.inventory.configuration;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationChatSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory chatSettings = SmartInventory.builder()
			.id("chatSettingsMenu")
			.provider(new ConfigurationChatSettingsGUI())
			.size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIChatSettings"))
			.build();	
	
	@Override
	public void init(Player player, InventoryContents contents) {
		/* White stained glass borders */
		ItemStack customBorders = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta borderMeta = customBorders.getItemMeta();
		borderMeta.setDisplayName(" ");
		customBorders.setItemMeta(borderMeta);
		contents.fillBorders(ClickableItem.empty(customBorders));
		
		/* First Button: Send a chat if the user is actively translating */
		sendTranslationChatButton(player, contents);
		
		/* Second Button: Send a notification in chat if the plugin requires an update */
		sendPluginUpdateChatButton(player, contents);
		
		/* Bottom Right Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPreviousPageButton"));
	    previousPageButton.setItemMeta(previousPageMeta);
	    contents.set(2, 1, ClickableItem.of(previousPageButton,
                e -> ConfigurationGeneralSettingsGUI.generalSettings.open(player)));
		
		/* Bottom Middle Option: Quit */
		ItemStack quitButton = new ItemStack(Material.BARRIER);
		ItemMeta quitMeta = quitButton.getItemMeta();
		quitMeta.setDisplayName(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIQuitButton"));
		quitButton.setItemMeta(quitMeta);
		WWCReload rel = new WWCReload(player, null, null, null);
		contents.set(2, 4, ClickableItem.of(quitButton,
                e -> {
                	main.removePlayerUsingConfigurationGUI(player);
                	player.closeInventory(); 
                	Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                		@Override
                		public void run() {
                			rel.processCommand();
                		}
                });
        }));
		
		/* Bottom Right Option: Next Page */
		ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta nextPageMeta = nextPageButton.getItemMeta();
		nextPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUINextPageButton"));
	    nextPageButton.setItemMeta(nextPageMeta);
	    contents.set(2, 7, ClickableItem.of(nextPageButton,
                e -> ConfigurationTranslatorSettingsGUI.translatorSettings.open(player)));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		sendTranslationChatButton(player, contents);
		sendPluginUpdateChatButton(player, contents);
	}
	
	public void sendTranslationChatButton(Player player, InventoryContents contents) {
		ItemStack translationChatButton;
		if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat")) {
			translationChatButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translationChatButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translationChatMeta = translationChatButton.getItemMeta();
		translationChatMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUISendTranslationChatButton"));
		translationChatButton.setItemMeta(translationChatMeta);
		contents.set(1, 1, ClickableItem.of(translationChatButton, 
				e -> {
					main.addPlayerUsingConfigurationGUI(player);
					main.getConfigManager().getMainConfig().set("Chat.sendTranslationChat", !(main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat")));
					try {
						main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
						final TextComponent successfulChange = Component.text()
				                .append(main.getPluginPrefix().asComponent())
				                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationSendTranslationChatSuccess")).color(NamedTextColor.GREEN))
				                .build();
				            Audience adventureSender = main.adventure().sender(player);
				        adventureSender.sendMessage(successfulChange);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		    }));
	}
	
	public void sendPluginUpdateChatButton(Player player, InventoryContents contents) {
		ItemStack pluginUpdateChatButton;
		if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")) {
			pluginUpdateChatButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			pluginUpdateChatButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta pluginUpdateChatMeta = pluginUpdateChatButton.getItemMeta();
		pluginUpdateChatMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPluginUpdateChatButton"));
		pluginUpdateChatButton.setItemMeta(pluginUpdateChatMeta);
		contents.set(1, 2, ClickableItem.of(pluginUpdateChatButton, 
				e -> {
					main.addPlayerUsingConfigurationGUI(player);
					main.getConfigManager().getMainConfig().set("Chat.sendPluginUpdateChat", !(main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")));
					try {
						main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
						final TextComponent successfulChange = Component.text()
				                .append(main.getPluginPrefix().asComponent())
				                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationPluginUpdateChatSuccess")).color(NamedTextColor.GREEN))
				                .build();
				            Audience adventureSender = main.adventure().sender(player);
				        adventureSender.sendMessage(successfulChange);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		    }));
	}
}
