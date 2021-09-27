package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationChatSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();

	public static final SmartInventory chatSettings = SmartInventory.builder().id("chatSettingsMenu")
			.provider(new ConfigurationChatSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			ItemStack customBorders = XMaterial.WHITE_STAINED_GLASS_PANE.parseItem();
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));

			/* First Button: Send a chat if the user is actively translating */
			sendTranslationChatButton(player, contents);

			/*
			 * Second Button: Send a notification in chat if the plugin requires an update
			 */
			sendPluginUpdateChatButton(player, contents);

			/* Third Button: Let user override default plugin messages */
			ItemStack messagesOverrideChatButton = XMaterial.WRITABLE_BOOK.parseItem();
			ItemMeta messagesOverrideChatMeta = messagesOverrideChatButton.getItemMeta();
			messagesOverrideChatMeta.setDisplayName(ChatColor.GOLD + CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideChatButton"));
			messagesOverrideChatButton.setItemMeta(messagesOverrideChatMeta);
			contents.set(1, 3, ClickableItem.of(messagesOverrideChatButton, e -> {
				ConfigurationMessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
			}));
			
			/* Bottom Right Option: Previous Page */
			contents.set(2, 1, ClickableItem.of(WWCInventoryManager.getCommonButton("Previous"),
					e -> ConfigurationGeneralSettingsGUI.generalSettings.open(player)));

			/* Bottom Middle Option: Quit */
			ItemStack quitButton = XMaterial.BARRIER.parseItem();
			ItemMeta quitMeta = quitButton.getItemMeta();
			quitMeta.setDisplayName(ChatColor.RED
					+ CommonDefinitions.getMessage("wwcConfigGUIQuitButton"));
			quitButton.setItemMeta(quitMeta);
			WWCReload rel = new WWCReload(player, null, null, new String[0]);
			contents.set(2, 4, ClickableItem.of(quitButton, e -> {
				main.removePlayerUsingConfigurationGUI(player);
				player.closeInventory();
				rel.processCommand();
			}));

			/* Bottom Right Option: Next Page */
			contents.set(2, 7, ClickableItem.of(WWCInventoryManager.getCommonButton("Next"),
					e -> ConfigurationTranslatorSettingsGUI.translatorSettings.open(player)));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	private void sendTranslationChatButton(Player player, InventoryContents contents) {
		ItemStack translationChatButton = XMaterial.BEDROCK.parseItem();
		if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat")) {
			translationChatButton = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			translationChatButton = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta translationChatMeta = translationChatButton.getItemMeta();
		translationChatMeta.setDisplayName(ChatColor.GOLD + CommonDefinitions.getMessage("wwcConfigGUISendTranslationChatButton"));
		translationChatButton.setItemMeta(translationChatMeta);
		contents.set(1, 1, ClickableItem.of(translationChatButton, e -> {
			main.addPlayerUsingConfigurationGUI(player);
			main.getConfigManager().getMainConfig().set("Chat.sendTranslationChat",
					!(main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat")));
			main.getConfigManager().saveMainConfig(true);
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcConfigConversationSendTranslationChatSuccess"))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage(player, successfulChange);
			sendTranslationChatButton(player, contents);
		}));
	}

	private void sendPluginUpdateChatButton(Player player, InventoryContents contents) {
		ItemStack pluginUpdateChatButton = XMaterial.BEDROCK.parseItem();
		if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")) {
			pluginUpdateChatButton = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			pluginUpdateChatButton = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta pluginUpdateChatMeta = pluginUpdateChatButton.getItemMeta();
		pluginUpdateChatMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage("wwcConfigGUIPluginUpdateChatButton"));
		pluginUpdateChatButton.setItemMeta(pluginUpdateChatMeta);
		contents.set(1, 2, ClickableItem.of(pluginUpdateChatButton, e -> {
			main.addPlayerUsingConfigurationGUI(player);
			main.getConfigManager().getMainConfig().set("Chat.sendPluginUpdateChat",
					!(main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")));
			main.getConfigManager().saveMainConfig(true);
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcConfigConversationPluginUpdateChatSuccess"))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage(player, successfulChange);
			sendPluginUpdateChatButton(player, contents);
		}));
	}
}
