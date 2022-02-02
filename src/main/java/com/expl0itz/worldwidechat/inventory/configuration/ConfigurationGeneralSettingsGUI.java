package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsLangConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsPrefixConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsSyncUserDataConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsUpdateCheckerConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ConfigurationGeneralSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	public static final SmartInventory generalSettings = SmartInventory.builder().id("generalSettingsMenu")
			.provider(new ConfigurationGeneralSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIGeneralSettings"))
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

			/* Option One: Plugin Prefix */
			ConversationFactory prefixConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new GeneralSettingsPrefixConversation());
			ItemStack prefixButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta prefixMeta = prefixButton.getItemMeta();
			prefixMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUIPrefixButton"));
			prefixButton.setItemMeta(prefixMeta);
			contents.set(1, 1, ClickableItem.of(prefixButton, e -> {
				prefixConvo.buildConversation(player).begin();
			}));

			/* Option Two: bStats */
			WWCInventoryManager.genericToggleButton(1, 2, player, contents, "wwcConfigGUIbStatsButton", "wwcConfigConversationbStatsSuccess", "General.enablebStats");

			/* Option Three: Change Plugin Lang */
			ConversationFactory langConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new GeneralSettingsLangConversation());
			ItemStack langButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta langMeta = langButton.getItemMeta();
			langMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUILangButton"));
			langButton.setItemMeta(langMeta);
			contents.set(1, 3, ClickableItem.of(langButton, e -> {
				langConvo.buildConversation(player).begin();
			}));

			/* Option Four: Update Checker Delay */
			ConversationFactory updateCheckerConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new GeneralSettingsUpdateCheckerConversation());
			ItemStack updateCheckerButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta updateCheckerMeta = updateCheckerButton.getItemMeta();
			updateCheckerMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUIUpdateCheckerButton"));
			updateCheckerButton.setItemMeta(updateCheckerMeta);
			contents.set(1, 4, ClickableItem.of(updateCheckerButton, e -> {
				updateCheckerConvo.buildConversation(player).begin();
			}));

			/* Option Five: Sync User Data Delay */
			ConversationFactory syncUserDataConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new GeneralSettingsSyncUserDataConversation());
			ItemStack syncUserDataButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta syncUserDataMeta = syncUserDataButton.getItemMeta();
			syncUserDataMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUISyncUserDataButton"));
			syncUserDataButton.setItemMeta(syncUserDataMeta);
			contents.set(1, 5, ClickableItem.of(syncUserDataButton, e -> {
				syncUserDataConvo.buildConversation(player).begin();
			}));
			
			/* Option Six: Debug Mode */
			WWCInventoryManager.genericToggleButton(1, 6, player, contents, "wwcConfigGUIDebugModeButton", "wwcConfigConversationDebugModeSuccess", "General.enableDebugMode");

			/* Bottom Middle Option: Quit */
			contents.set(2, 4, ClickableItem.of(WWCInventoryManager.getSaveMainConfigButton(), e -> {
				WWCInventoryManager.saveMainConfigAndReload(player, contents);
			}));

			/* Bottom Right Option: Next Page */
			contents.set(2, 6,
					ClickableItem.of(WWCInventoryManager.getCommonButton("Next"), 
							e -> ConfigurationChatSettingsGUI.chatSettings.open(player)));
			
			/* Last Option: Page Number */
			contents.set(2, 8, ClickableItem.of(WWCInventoryManager.getCommonButton("Page Number", new String[] {"1"}), e -> {}));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
}
