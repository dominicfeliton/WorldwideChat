package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
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
		/* White stained glass borders */
		ItemStack customBorders = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
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
		ItemStack messagesOverrideChatButton;
		messagesOverrideChatButton = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta messagesOverrideChatMeta = messagesOverrideChatButton.getItemMeta();
		messagesOverrideChatMeta.setDisplayName(ChatColor.GOLD + CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideChatButton"));
		messagesOverrideChatButton.setItemMeta(messagesOverrideChatMeta);
		contents.set(1, 3, ClickableItem.of(messagesOverrideChatButton, e -> {
			ConfigurationMessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
		}));
		
		/* Bottom Right Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN
				+ CommonDefinitions.getMessage("wwcConfigGUIPreviousPageButton"));
		previousPageButton.setItemMeta(previousPageMeta);
		contents.set(2, 1, ClickableItem.of(previousPageButton,
				e -> ConfigurationGeneralSettingsGUI.generalSettings.open(player)));

		/* Bottom Middle Option: Quit */
		ItemStack quitButton = new ItemStack(Material.BARRIER);
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
		ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta nextPageMeta = nextPageButton.getItemMeta();
		nextPageMeta.setDisplayName(ChatColor.GREEN
				+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
		nextPageButton.setItemMeta(nextPageMeta);
		contents.set(2, 7, ClickableItem.of(nextPageButton,
				e -> ConfigurationTranslatorSettingsGUI.translatorSettings.open(player)));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	private void sendTranslationChatButton(Player player, InventoryContents contents) {
		ItemStack translationChatButton = new ItemStack(Material.BEDROCK);
		if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat")) {
			translationChatButton.setType(Material.EMERALD_BLOCK);
		} else {
			translationChatButton.setType(Material.REDSTONE_BLOCK);
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
		ItemStack pluginUpdateChatButton = new ItemStack(Material.BEDROCK);
		if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")) {
			pluginUpdateChatButton.setType(Material.EMERALD_BLOCK);;
		} else {
			pluginUpdateChatButton.setType(Material.REDSTONE_BLOCK);
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
