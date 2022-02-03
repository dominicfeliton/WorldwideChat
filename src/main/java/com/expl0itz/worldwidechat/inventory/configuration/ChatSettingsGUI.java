package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
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
			ItemStack customBorders = XMaterial.WHITE_STAINED_GLASS_PANE.parseItem();
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));

			/* First Button: Send a chat if the user is actively translating */
			WWCInventoryManager.genericToggleButton(1, 1, player, contents, "wwcConfigGUISendTranslationChatButton", "wwcConfigConversationSendTranslationChatSuccess", "Chat.sendTranslationChat");

			/* Second Button: Send a notification in chat if the plugin requires an update */
			WWCInventoryManager.genericToggleButton(1, 2, player, contents, "wwcConfigGUIPluginUpdateChatButton", "wwcConfigConversationPluginUpdateChatSuccess", "Chat.sendPluginUpdateChat");

			/* Third Button: Send a message to the user if their translation has failed */
			WWCInventoryManager.genericToggleButton(1, 3, player, contents, "wwcConfigGUISendFailedTranslationChatButton", "wwcConfigConversationSendFailedTranslationChatSuccess", "Chat.sendFailedTranslationChat");
			
			/* Fourth Button: Hover over incoming translated chat to see the original message */
			WWCInventoryManager.genericToggleButton(1, 4, player, contents, "wwcConfigGUISendIncomingHoverTextChatButton", "wwcConfigConversationSendIncomingHoverTextChatSuccess", "Chat.sendIncomingHoverTextChat");
			
			/* Fifth Button: Let user override default plugin messages */
			ItemStack messagesOverrideChatButton = XMaterial.WRITABLE_BOOK.parseItem();
			ItemMeta messagesOverrideChatMeta = messagesOverrideChatButton.getItemMeta();
			messagesOverrideChatMeta.setDisplayName(ChatColor.GOLD + CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideChatButton"));
			messagesOverrideChatButton.setItemMeta(messagesOverrideChatMeta);
			contents.set(1, 5, ClickableItem.of(messagesOverrideChatButton, e -> {
				MessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
			}));
			
			/* Bottom Right Option: Previous Page */
			contents.set(2, 2, ClickableItem.of(WWCInventoryManager.getCommonButton("Previous"),
					e -> GeneralSettingsGUI.generalSettings.open(player)));

			/* Bottom Middle Option: Quit */
			contents.set(2, 4, ClickableItem.of(WWCInventoryManager.getSaveMainConfigButton(), e -> {
				WWCInventoryManager.saveMainConfigAndReload(player, contents);
			}));

			/* Bottom Right Option: Next Page */
			contents.set(2, 6, ClickableItem.of(WWCInventoryManager.getCommonButton("Next"),
					e -> TranslatorSettingsGUI.translatorSettings.open(player)));
			
			/* Last Option: Page Number */
			contents.set(2, 8, ClickableItem.of(WWCInventoryManager.getCommonButton("Page Number", new String[] {"2"}), e -> {}));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
}
