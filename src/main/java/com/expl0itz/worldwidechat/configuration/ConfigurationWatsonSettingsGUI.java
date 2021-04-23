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
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonServiceUrlConversation;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslation;
import com.expl0itz.worldwidechat.watson.WatsonTranslation;

import co.aikar.taskchain.TaskChain;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationWatsonSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory watsonSettings = SmartInventory.builder()
			.id("watsonSettingsMenu")
			.provider(new ConfigurationWatsonSettingsGUI())
			.parent(ConfigurationTranslatorSettingsGUI.translatorSettings)
			.size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIWatsonSettings"))
			.build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		/* White stained glass borders */
		ItemStack customBorders = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		ItemMeta borderMeta = customBorders.getItemMeta();
		borderMeta.setDisplayName(" ");
		customBorders.setItemMeta(borderMeta);
		contents.fillBorders(ClickableItem.empty(customBorders));
		
		/* First Option: Watson Status Button */
		watsonStatusButton(player, contents);
		
		/* Second Option: Watson API Key */
		ConversationFactory apiConvo = new ConversationFactory(main)
				.withModality(true)
				.withFirstPrompt(new TranslatorSettingsWatsonApiKeyConversation());
		ItemStack apiKeyButton = new ItemStack(Material.NAME_TAG);
		ItemMeta apiKeyMeta = apiKeyButton.getItemMeta();
		apiKeyMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIWatsonAPIKeyButton"));
		apiKeyButton.setItemMeta(apiKeyMeta);
		contents.set(1, 2, ClickableItem.of(apiKeyButton, 
				e -> {apiConvo.buildConversation(player).begin();}));
		
		/* Third Option: Watson Service URL */
		ConversationFactory urlConvo = new ConversationFactory(main)
				.withModality(true)
				.withFirstPrompt(new TranslatorSettingsWatsonServiceUrlConversation());
		ItemStack urlButton = new ItemStack(Material.NAME_TAG);
		ItemMeta urlMeta = urlButton.getItemMeta();
		urlMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIWatsonURLButton"));
		urlButton.setItemMeta(urlMeta);
		contents.set(1, 3, ClickableItem.of(urlButton, 
				e -> {urlConvo.buildConversation(player).begin();}));
		
		/* Bottom Right Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPreviousPageButton"));
	    previousPageButton.setItemMeta(previousPageMeta);
	    contents.set(2, 1, ClickableItem.of(previousPageButton,
                e -> ConfigurationTranslatorSettingsGUI.translatorSettings.open(player)));
		
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
		watsonStatusButton(player, contents);
	}
	
	public void watsonStatusButton(Player player, InventoryContents contents) {
		ItemStack translatorStatusButton;
		if (main.getConfigManager().getMainConfig().getBoolean("Translator.useWatsonTranslate")) {
			translatorStatusButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorStatusButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorStatusButtonMeta = translatorStatusButton.getItemMeta();
		translatorStatusButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIWatsonStatusButton"));
		translatorStatusButton.setItemMeta(translatorStatusButtonMeta);
		contents.set(1, 1, ClickableItem.of(translatorStatusButton, 
				e -> {
					    if (!main.getConfigManager().getMainConfig().getBoolean("Translator.useWatsonTranslate")) {
					    	TaskChain<?> chain = WorldwideChat.newSharedChain("enableWatson");
					    	chain
					    	    .sync(() -> {
					    	        player.closeInventory();
					    	    })
					    	    .async(() -> {
					    	    	try {
									    WatsonTranslation testConnection = new WatsonTranslation(main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"), main.getConfigManager().getMainConfig().getString("Translator.watsonURL"));
									    testConnection.initializeConnection();
									    main.addPlayerUsingConfigurationGUI(player);
									    main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", true);
									    main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate", false);
									    main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate", false);
									    main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
									    final TextComponent successfulChange = Component.text()
								                .append(main.getPluginPrefix().asComponent())
								                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationWatsonSuccess")).color(NamedTextColor.GREEN))
								                .build();
								            Audience adventureSender = main.adventure().sender(player);
								        adventureSender.sendMessage(successfulChange);
									} catch (Exception bad) {
										bad.printStackTrace();
										final TextComponent badResult = Component.text()
									            .append(main.getPluginPrefix().asComponent())
									            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationWatsonFail")).color(NamedTextColor.RED))
									            .build();
									        Audience adventureSender = main.adventure().sender(player);
									    adventureSender.sendMessage(badResult);
									}
					    	    })
					    	    .sync(() -> {
					    	    	watsonSettings.open(player);
					    	    })
					    	    .sync(TaskChain::abort)
					    	    .execute();
					}
		    }));
	}

}
