package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsCharacterLimitConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsErrorLimitConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGlobalRateConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsTranslationCacheConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ConfigurationTranslatorSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	public static final SmartInventory translatorSettings = SmartInventory.builder().id("translatorSettingsMenu")
			.provider(new ConfigurationTranslatorSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUITranslatorSettings"))
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

			/* Option One: Watson */
			watsonInventory(player, contents);

			/* Option Two: Google Translate */
			googleTranslateInventory(player, contents);

			/* Option Three: Amazon Translate */
			amazonTranslateInventory(player, contents);

			/* Option Four: Translator Cache Size */
			ConversationFactory cacheConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new TranslatorSettingsTranslationCacheConversation());
			ItemStack translatorCacheButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta translatorCacheMeta = translatorCacheButton.getItemMeta();
			translatorCacheMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUITranslatorCacheButton"));
			translatorCacheButton.setItemMeta(translatorCacheMeta);
			contents.set(1, 4, ClickableItem.of(translatorCacheButton, e -> {
				cacheConvo.buildConversation(player).begin();
			}));

			/* Option Five : Global Rate Limit */
			ConversationFactory rateConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new TranslatorSettingsGlobalRateConversation());
			ItemStack rateLimitButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta rateLimitMeta = rateLimitButton.getItemMeta();
			rateLimitMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUIGlobalRateLimitButton"));
			rateLimitButton.setItemMeta(rateLimitMeta);
			contents.set(1, 5, ClickableItem.of(rateLimitButton, e -> {
				rateConvo.buildConversation(player).begin();
			}));
			
			/* Option Six: Translator Error Limit */
			ConversationFactory errorConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new TranslatorSettingsErrorLimitConversation());
			ItemStack errorLimitButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta errorLimitMeta = errorLimitButton.getItemMeta();
			errorLimitMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUIErrorLimitButton"));
			errorLimitButton.setItemMeta(errorLimitMeta);
			contents.set(1, 6, ClickableItem.of(errorLimitButton, e -> {
				errorConvo.buildConversation(player).begin();
			}));
			
			/* Option Seven: Message Character Limit */
			ConversationFactory charConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new TranslatorSettingsCharacterLimitConversation());
			ItemStack charLimitButton = XMaterial.NAME_TAG.parseItem();
			ItemMeta charLimitMeta = charLimitButton.getItemMeta();
			charLimitMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUICharacterLimitButton"));
			charLimitButton.setItemMeta(charLimitMeta);
			contents.set(1, 7, ClickableItem.of(charLimitButton, e -> {
				charConvo.buildConversation(player).begin();
			}));
			
			/* Bottom Right Option: Previous Page */
			contents.set(2, 1,
					ClickableItem.of(WWCInventoryManager.getCommonButton("Previous"), 
							e -> ConfigurationChatSettingsGUI.chatSettings.open(player)));

			/* Bottom Middle Option: Quit */
			ItemStack quitButton = XMaterial.BARRIER.parseItem();
			ItemMeta quitMeta = quitButton.getItemMeta();
			quitMeta.setDisplayName(ChatColor.RED
					+ CommonDefinitions.getMessage("wwcConfigGUIQuitButton"));
			quitButton.setItemMeta(quitMeta);
			contents.set(2, 4, ClickableItem.of(quitButton, e -> {
				main.removePlayerUsingConfigurationGUI(player);
				player.closeInventory();
				main.reload(player);
			}));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	private void watsonInventory(Player player, InventoryContents contents) {
		/* Option One: Watson */
		ItemStack translatorButton;
		if (main.getTranslatorName().equals("Watson")) {
			translatorButton = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			translatorButton = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta translatorButtonMeta = translatorButton.getItemMeta();
		translatorButtonMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage("wwcConfigGUIWatsonButton"));
		translatorButton.setItemMeta(translatorButtonMeta);
		contents.set(1, 1, ClickableItem.of(translatorButton, e -> {
			ConfigurationEachTranslatorSettingsGUI.getCurrentTranslatorSettings("Watson").open(player);
		}));
	}

	private void googleTranslateInventory(Player player, InventoryContents contents) {
		/* Option Two: Google Translate */
		ItemStack translatorButton;
		if (main.getTranslatorName().equals("Google Translate")) {
			translatorButton = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			translatorButton = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta translatorButtonMeta = translatorButton.getItemMeta();
		translatorButtonMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage("wwcConfigGUIGoogleTranslateButton"));
		translatorButton.setItemMeta(translatorButtonMeta);
		contents.set(1, 2, ClickableItem.of(translatorButton, e -> {
			ConfigurationEachTranslatorSettingsGUI.getCurrentTranslatorSettings("Google Translate").open(player);
		}));
	}

	private void amazonTranslateInventory(Player player, InventoryContents contents) {
		/* Option Three: Amazon Translate */
		ItemStack translatorButton;
		if (main.getTranslatorName().equals("Amazon Translate")) {
			translatorButton = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			translatorButton = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta translatorButtonMeta = translatorButton.getItemMeta();
		translatorButtonMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage("wwcConfigGUIAmazonTranslateButton"));
		translatorButton.setItemMeta(translatorButtonMeta);
		contents.set(1, 3, ClickableItem.of(translatorButton, e -> {
			ConfigurationEachTranslatorSettingsGUI.getCurrentTranslatorSettings("Amazon Translate").open(player);
		}));
	}

}
