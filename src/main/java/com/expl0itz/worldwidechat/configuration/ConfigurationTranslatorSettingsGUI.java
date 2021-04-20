package com.expl0itz.worldwidechat.configuration;

import org.bukkit.Bukkit;
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

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ConfigurationTranslatorSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory translatorSettings = SmartInventory.builder()
			.id("translatorSettingsMenu")
			.provider(new ConfigurationTranslatorSettingsGUI())
			.size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUITranslatorSettings"))
			.build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
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
		ConversationFactory cacheConvo = new ConversationFactory(main)
				.withModality(true)
				.withFirstPrompt(new TranslatorSettingsTranslationCacheConversation());
		ItemStack translatorCacheButton = new ItemStack(Material.NAME_TAG);
		ItemMeta translatorCacheMeta = translatorCacheButton.getItemMeta();
		translatorCacheMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUITranslatorCacheButton"));
		translatorCacheButton.setItemMeta(translatorCacheMeta);
		contents.set(1, 4, ClickableItem.of(translatorCacheButton,
				e -> {cacheConvo.buildConversation(player).begin();}));
		
		/* Option Five : Global Rate Limit */
		ConversationFactory rateConvo = new ConversationFactory(main)
				.withModality(true)
				.withFirstPrompt(new TranslatorSettingsGlobalRateConversation());
		ItemStack rateLimitButton = new ItemStack(Material.NAME_TAG);
		ItemMeta rateLimitMeta = rateLimitButton.getItemMeta();
		rateLimitMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIGlobalRateLimit"));
		rateLimitButton.setItemMeta(rateLimitMeta);
		contents.set(1, 5, ClickableItem.of(rateLimitButton, 
				e -> {rateConvo.buildConversation(player).begin();}));
		
		
		/* Bottom Right Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPreviousPageButton"));
	    previousPageButton.setItemMeta(previousPageMeta);
	    contents.set(2, 1, ClickableItem.of(previousPageButton,
                e -> ConfigurationChatSettingsGUI.chatSettings.open(player)));
		
		/* Bottom Middle Option: Quit */
		ItemStack quitButton = new ItemStack(Material.BARRIER);
		ItemMeta quitMeta = quitButton.getItemMeta();
		quitMeta.setDisplayName(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIQuitButton"));
		quitButton.setItemMeta(quitMeta);
		WWCReload rel = new WWCReload(player, null, null, null);
		contents.set(2, 4, ClickableItem.of(quitButton,
                e -> {
                	main.removePlayerUsingGUI(player);
                	player.closeInventory(); 
                	Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                		@Override
                		public void run() {
                			rel.processCommand();
                		}
                });
        }));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		watsonInventory(player, contents);
		googleTranslateInventory(player, contents);
		amazonTranslateInventory(player, contents);
	}

	public void watsonInventory(Player player, InventoryContents contents) {
		/* Option One: Watson */
		ItemStack translatorButton;
		if (main.getConfigManager().getMainConfig().getBoolean("Translator.useWatsonTranslate")) {
			translatorButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorButtonMeta = translatorButton.getItemMeta();
		translatorButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIWatsonButton"));
		translatorButton.setItemMeta(translatorButtonMeta);
		contents.set(1, 1, ClickableItem.of(translatorButton, 
				e -> {
					ConfigurationWatsonSettingsGUI.watsonSettings.open(player);
		    }));
	}
	
	public void googleTranslateInventory(Player player, InventoryContents contents) {
		/* Option Two: Google Translate */
		ItemStack translatorButton;
		if (main.getConfigManager().getMainConfig().getBoolean("Translator.useGoogleTranslate")) {
			translatorButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorButtonMeta = translatorButton.getItemMeta();
		translatorButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIGoogleTranslateButton"));
		translatorButton.setItemMeta(translatorButtonMeta);
		contents.set(1, 2, ClickableItem.of(translatorButton, 
				e -> {
				    ConfigurationGoogleTranslateSettingsGUI.googleTranslateSettings.open(player);
		    }));
	}
	
	public void amazonTranslateInventory(Player player, InventoryContents contents) {
		/* Option Three: Amazon Translate */
		ItemStack translatorButton;
		if (main.getConfigManager().getMainConfig().getBoolean("Translator.useAmazonTranslate")) {
			translatorButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorButtonMeta = translatorButton.getItemMeta();
		translatorButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIAmazonTranslateButton"));
		translatorButton.setItemMeta(translatorButtonMeta);
		contents.set(1, 3, ClickableItem.of(translatorButton, 
				e -> {
					ConfigurationAmazonTranslateSettingsGUI.amazonTranslateSettings.open(player);
		    }));
	}
	
}
