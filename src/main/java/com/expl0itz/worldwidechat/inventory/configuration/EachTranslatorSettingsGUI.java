package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateAccessKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateRegionConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateSecretKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGoogleTranslateApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonServiceUrlConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class EachTranslatorSettingsGUI implements InventoryProvider {

	private String translatorName = "Invalid";

	public EachTranslatorSettingsGUI(String translatorName) {
		this.translatorName = translatorName;
	}

	public static SmartInventory getCurrentTranslatorSettings(String translatorName) {
		return SmartInventory.builder().id("currentTranslatorSettings")
				.provider(new EachTranslatorSettingsGUI(translatorName))
				.parent(TranslatorSettingsGUI.translatorSettings).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIEachTranslatorSettings", new String[] {translatorName}))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			if (translatorName.equals("Watson")) {
				/* Blue stained glass borders */
				WWCInventoryManager.setBorders(contents, XMaterial.BLUE_STAINED_GLASS_PANE);

				/* First Option: Watson Status Button */
				WWCInventoryManager.genericToggleButton(1, 1, player, contents, "wwcConfigGUIToggleWatsonTranslateButton", "wwcConfigConversationWatsonTranslateToggleSuccess", "Translator.useWatsonTranslate", new String[] {"Translator.useGoogleTranslate", "Translator.useAmazonTranslate"});

				/* Second Option: Watson API Key */
				WWCInventoryManager.genericConversationButton(1, 2, player, contents, new TranslatorSettingsWatsonApiKeyConversation(), XMaterial.NAME_TAG, "wwcConfigGUIWatsonAPIKeyButton");

				/* Third Option: Watson Service URL */
				WWCInventoryManager.genericConversationButton(1, 3, player, contents, new TranslatorSettingsWatsonServiceUrlConversation(), XMaterial.NAME_TAG, "wwcConfigGUIWatsonURLButton");
			} else if (translatorName.equals("Google Translate")) {
				/* Red stained glass borders */
				WWCInventoryManager.setBorders(contents, XMaterial.RED_STAINED_GLASS_PANE);

				/* First Option: Google Translate is Enabled */
				WWCInventoryManager.genericToggleButton(1, 1, player, contents, "wwcConfigGUIToggleGoogleTranslateButton", "wwcConfigConversationGoogleTranslateToggleSuccess", "Translator.useGoogleTranslate", new String[] {"Translator.useWatsonTranslate", "Translator.useAmazonTranslate"});

				/* Second Option: Google Translate API Key */
				WWCInventoryManager.genericConversationButton(1, 2, player, contents, new TranslatorSettingsGoogleTranslateApiKeyConversation(), XMaterial.NAME_TAG, "wwcConfigGUIGoogleTranslateAPIKeyButton");
			} else if (translatorName.equals("Amazon Translate")) {
				/* Yellow stained glass borders */
				WWCInventoryManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);

				/* First Option: Amazon Translate Status Button */
				WWCInventoryManager.genericToggleButton(1, 1, player, contents, "wwcConfigGUIToggleAmazonTranslateButton", "wwcConfigConversationAmazonTranslateToggleSuccess", "Translator.useAmazonTranslate", new String[] {"Translator.useGoogleTranslate", "Translator.useWatsonTranslate"});

				/* Second Option: Amazon Access Key */
				WWCInventoryManager.genericConversationButton(1, 2, player, contents, new TranslatorSettingsAmazonTranslateAccessKeyConversation(), XMaterial.NAME_TAG, "wwcConfigGUIAmazonTranslateAccessKeyButton");

				/* Third Option: Amazon Secret Key */
				WWCInventoryManager.genericConversationButton(1, 3, player, contents, new TranslatorSettingsAmazonTranslateSecretKeyConversation(), XMaterial.NAME_TAG, "wwcConfigGUIAmazonTranslateSecretKeyButton");

				/* Fourth Option: Amazon Region */
				WWCInventoryManager.genericConversationButton(1, 4, player, contents, new TranslatorSettingsAmazonTranslateRegionConversation(), XMaterial.NAME_TAG, "wwcConfigGUIAmazonTranslateRegionButton");
			} else {
				/* White stained glass borders, if unknown */
				WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			}

			/* Bottom Right Option: Previous Page */
			WWCInventoryManager.setCommonButton(2, 2, player, contents, "Previous", new Object[] {TranslatorSettingsGUI.translatorSettings});

			/* Bottom Middle Option: Quit */
			WWCInventoryManager.setCommonButton(2, 4, player, contents, "Quit");
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
}
