package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ChatSettingsGUI implements InventoryProvider {

	public static final SmartInventory chatSettings = SmartInventory.builder().id("chatSettingsMenu")
			.provider(new ChatSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);

			/* First Button: Send a chat if the user is actively translating */
			WWCInventoryManager.genericToggleButton(1, 1, player, contents, "wwcConfigGUISendTranslationChatButton", "wwcConfigConversationSendTranslationChatSuccess", "Chat.sendTranslationChat");

			/* Second Button: Send a notification in chat if the plugin requires an update */
			WWCInventoryManager.genericToggleButton(1, 2, player, contents, "wwcConfigGUIPluginUpdateChatButton", "wwcConfigConversationPluginUpdateChatSuccess", "Chat.sendPluginUpdateChat");

			/* Third Button: Send a message to the user if their translation has failed */
			WWCInventoryManager.genericToggleButton(1, 3, player, contents, "wwcConfigGUISendFailedTranslationChatButton", "wwcConfigConversationSendFailedTranslationChatSuccess", "Chat.sendFailedTranslationChat");
			
			/* Fourth Button: Hover over incoming translated chat to see the original message */
			WWCInventoryManager.genericToggleButton(1, 4, player, contents, "wwcConfigGUISendIncomingHoverTextChatButton", "wwcConfigConversationSendIncomingHoverTextChatSuccess", "Chat.sendIncomingHoverTextChat");
			
			/* Fifth Button: Let user override default plugin messages */
			WWCInventoryManager.genericOpenSubmenuButton(1, 5, player, contents, "wwcConfigGUIMessagesOverrideChatButton", MessagesOverrideCurrentListGUI.overrideMessagesSettings);
			
			/* Bottom Left Option: Previous Page */
			WWCInventoryManager.setCommonButton(2, 2, player, contents, "Previous", new Object[] {StorageSettingsGUI.storageSettings});

			/* Bottom Middle Option: Quit */
			WWCInventoryManager.setCommonButton(2, 4, player, contents, "Quit");

			/* Bottom Right Option: Next Page */
			WWCInventoryManager.setCommonButton(2, 6, player, contents, "Next", new Object[] {TranslatorSettingsGUI.translatorSettings});
			
			/* Last Option: Page Number */
			WWCInventoryManager.setCommonButton(2, 8, player, contents, "Page Number", new String[] {"3"});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
}
