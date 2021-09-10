package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGlobalRateConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsTranslationCacheConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ConfigurationTranslatorSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();

	public static final SmartInventory translatorSettings = SmartInventory.builder().id("translatorSettingsMenu")
			.provider(new ConfigurationTranslatorSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUITranslatorSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			ItemStack customBorders = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
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
			ItemStack translatorCacheButton = new ItemStack(Material.NAME_TAG);
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
			ItemStack rateLimitButton = new ItemStack(Material.NAME_TAG);
			ItemMeta rateLimitMeta = rateLimitButton.getItemMeta();
			rateLimitMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUIGlobalRateLimit"));
			rateLimitButton.setItemMeta(rateLimitMeta);
			contents.set(1, 5, ClickableItem.of(rateLimitButton, e -> {
				rateConvo.buildConversation(player).begin();
			}));

			/* Bottom Right Option: Previous Page */
			ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta previousPageMeta = previousPageButton.getItemMeta();
			previousPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUIPreviousPageButton"));
			previousPageButton.setItemMeta(previousPageMeta);
			contents.set(2, 1,
					ClickableItem.of(previousPageButton, e -> ConfigurationChatSettingsGUI.chatSettings.open(player)));

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
			translatorButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorButton = new ItemStack(Material.REDSTONE_BLOCK);
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
			translatorButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorButton = new ItemStack(Material.REDSTONE_BLOCK);
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
			translatorButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorButton = new ItemStack(Material.REDSTONE_BLOCK);
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
