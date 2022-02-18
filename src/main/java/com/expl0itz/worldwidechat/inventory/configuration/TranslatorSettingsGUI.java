package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsCharacterLimitConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsErrorLimitConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGlobalRateConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsTranslationCacheConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class TranslatorSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	public static final SmartInventory translatorSettings = SmartInventory.builder().id("translatorSettingsMenu")
			.provider(new TranslatorSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUITranslatorSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);

			/* Option One: Watson */
			WWCInventoryManager.genericOpenSubmenuButton(1, 1, player, contents, main.getTranslatorName().equals("Watson"), "wwcConfigGUIWatsonButton", EachTranslatorSettingsGUI.getCurrentTranslatorSettings("Watson"));

			/* Option Two: Google Translate */
			WWCInventoryManager.genericOpenSubmenuButton(1, 2, player, contents, main.getTranslatorName().equals("Google Translate"), "wwcConfigGUIGoogleTranslateButton", EachTranslatorSettingsGUI.getCurrentTranslatorSettings("Google Translate"));

			/* Option Three: Amazon Translate */
			WWCInventoryManager.genericOpenSubmenuButton(1, 3, player, contents, main.getTranslatorName().equals("Amazon Translate"), "wwcConfigGUIAmazonTranslateButton", EachTranslatorSettingsGUI.getCurrentTranslatorSettings("Amazon Translate"));

			/* Option Four: Translator Cache Size */
			WWCInventoryManager.genericConversationButton(1, 4, player, contents, new TranslatorSettingsTranslationCacheConversation(), XMaterial.NAME_TAG, "wwcConfigGUITranslatorCacheButton");

			/* Option Five : Global Rate Limit */
			WWCInventoryManager.genericConversationButton(1, 5, player, contents, new TranslatorSettingsGlobalRateConversation(), XMaterial.NAME_TAG, "wwcConfigGUIGlobalRateLimitButton");
			
			/* Option Six: Translator Error Limit */
			WWCInventoryManager.genericConversationButton(1, 6, player, contents, new TranslatorSettingsErrorLimitConversation(), XMaterial.NAME_TAG, "wwcConfigGUIErrorLimitButton");
			
			/* Option Seven: Message Character Limit */
			WWCInventoryManager.genericConversationButton(1, 7, player, contents, new TranslatorSettingsCharacterLimitConversation(), XMaterial.NAME_TAG, "wwcConfigGUICharacterLimitButton");
			
			/* Bottom Left Option: Previous Page */
			WWCInventoryManager.setCommonButton(2, 2, player, contents, "Previous", new Object[] {ChatSettingsGUI.chatSettings});

			/* Bottom Middle Option: Quit */
			WWCInventoryManager.setCommonButton(2, 4, player, contents, "Quit");
			
			/* Last Option: Page Number */
			WWCInventoryManager.setCommonButton(2, 8, player, contents, "Page Number", new String[] {"4"});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
