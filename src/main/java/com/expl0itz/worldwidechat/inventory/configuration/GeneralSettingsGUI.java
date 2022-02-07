package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsLangConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsPrefixConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsSyncUserDataConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsUpdateCheckerConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class GeneralSettingsGUI implements InventoryProvider {

	public static final SmartInventory generalSettings = SmartInventory.builder().id("generalSettingsMenu")
			.provider(new GeneralSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIGeneralSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);

			/* Option One: Plugin Prefix */
			WWCInventoryManager.genericConversationButton(1, 1, player, contents, new GeneralSettingsPrefixConversation(), XMaterial.NAME_TAG, "wwcConfigGUIPrefixButton");

			/* Option Two: bStats */
			WWCInventoryManager.genericToggleButton(1, 2, player, contents, "wwcConfigGUIbStatsButton", "wwcConfigConversationbStatsSuccess", "General.enablebStats");

			/* Option Three: Change Plugin Lang */
			WWCInventoryManager.genericConversationButton(1, 3, player, contents, new GeneralSettingsLangConversation(), XMaterial.NAME_TAG, "wwcConfigGUILangButton");

			/* Option Four: Update Checker Delay */
			WWCInventoryManager.genericConversationButton(1, 4, player, contents, new GeneralSettingsUpdateCheckerConversation(), XMaterial.NAME_TAG, "wwcConfigGUIUpdateCheckerButton");

			/* Option Five: Sync User Data Delay */
			WWCInventoryManager.genericConversationButton(1, 5, player, contents, new GeneralSettingsSyncUserDataConversation(), XMaterial.NAME_TAG, "wwcConfigGUISyncUserDataButton");
			
			/* Option Six: Debug Mode */
			WWCInventoryManager.genericToggleButton(1, 6, player, contents, "wwcConfigGUIDebugModeButton", "wwcConfigConversationDebugModeSuccess", "General.enableDebugMode");

			/* Bottom Middle Option: Quit */
			WWCInventoryManager.setCommonButton(2, 4, player, contents, "Quit");

			/* Bottom Right Option: Next Page */
			WWCInventoryManager.setCommonButton(2, 6, player, contents, "Next", new Object[] {StorageSettingsGUI.storageSettings});
			
			/* Last Option: Page Number */
			WWCInventoryManager.setCommonButton(2, 8, player, contents, "Page Number", new String[] {"1"});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
}
