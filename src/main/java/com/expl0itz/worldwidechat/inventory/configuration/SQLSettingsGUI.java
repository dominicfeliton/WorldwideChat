package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsDatabaseConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsHostnameConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsOptionalArgsConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsPasswordConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsPortConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsUsernameConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class SQLSettingsGUI implements InventoryProvider {

	public static final SmartInventory sqlSettings = SmartInventory.builder().id("sqlSettingsMenu")
			.provider(new SQLSettingsGUI()).size(4, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUISQLSettings"))
			.build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			
			/* First Button: Enable/Disable SQL with current settings */
			WWCInventoryManager.genericToggleButton(1, 1, player, contents, "wwcConfigGUIToggleSQLButton", "wwcConfigConversationToggleSQLSuccess", "Storage.useSQL");
			
			/* Second Button: Change SQL Hostname */
			WWCInventoryManager.genericConversationButton(1, 2, player, contents, new SQLSettingsHostnameConversation(), XMaterial.NAME_TAG, "wwcConfigGUISQLHostnameButton");
			
			/* Third Button: Change SQL Port */
			WWCInventoryManager.genericConversationButton(1, 3, player, contents, new SQLSettingsPortConversation(), XMaterial.NAME_TAG, "wwcConfigGUISQLPortButton");
			
			/* Fourth Button: Change SQL Database */
			WWCInventoryManager.genericConversationButton(1, 4, player, contents, new SQLSettingsDatabaseConversation(), XMaterial.NAME_TAG, "wwcConfigGUISQLDatabaseNameButton");
			
			/* Fifth Button: Change SQL Username */
			WWCInventoryManager.genericConversationButton(1, 5, player, contents, new SQLSettingsUsernameConversation(), XMaterial.NAME_TAG, "wwcConfigGUISQLUsernameButton");
			
			/* Sixth Button: Change SQL Password: */
			WWCInventoryManager.genericConversationButton(1, 6, player, contents, new SQLSettingsPasswordConversation(), XMaterial.NAME_TAG, "wwcConfigGUISQLPasswordButton");
			
			/* Seventh Button: Change SQL SSL Status */
			WWCInventoryManager.genericToggleButton(1, 7, player, contents, "wwcConfigGUIToggleSQLSSLButton", "wwcConfigConversationToggleSQLSSLSuccess", "Storage.sqlUseSSL");
			
			/* Eighth Button: Change SQL Optional Arguments */
			WWCInventoryManager.genericConversationButton(2, 1, player, contents, new SQLSettingsOptionalArgsConversation(), XMaterial.NAME_TAG, "wwcConfigGUISQLOptionalArgsButton");
			
			/* Bottom Left Option: Previous Page */
			WWCInventoryManager.setCommonButton(3, 2, player, contents, "Previous", new Object[] {StorageSettingsGUI.storageSettings});

			/* Bottom Middle Option: Quit */
			WWCInventoryManager.setCommonButton(3, 4, player, contents, "Quit");
			
			/* Last Option: Page Number */
			WWCInventoryManager.setCommonButton(3, 8, player, contents, "Page Number", new String[] {"1"});
	    } catch (Exception e) {
		    WWCInventoryManager.inventoryError(player, e);
	    }
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
