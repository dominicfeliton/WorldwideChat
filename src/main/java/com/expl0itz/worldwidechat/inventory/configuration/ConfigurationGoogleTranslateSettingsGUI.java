package com.expl0itz.worldwidechat.inventory.configuration;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGoogleTranslateApiKeyConversation;
import com.expl0itz.worldwidechat.translators.GoogleTranslation;

import co.aikar.taskchain.TaskChain;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationGoogleTranslateSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory googleTranslateSettings = SmartInventory.builder()
			.id("googleTranslateSettingsMenu")
			.provider(new ConfigurationGoogleTranslateSettingsGUI())
			.parent(ConfigurationTranslatorSettingsGUI.translatorSettings)
			.size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIGoogleTranslateSettings"))
			.build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		/* White stained glass borders */
		ItemStack customBorders = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta borderMeta = customBorders.getItemMeta();
		borderMeta.setDisplayName(" ");
		customBorders.setItemMeta(borderMeta);
		contents.fillBorders(ClickableItem.empty(customBorders));
		
		/* First Option: Google Translate is Enabled */
		googleTranslateStatusButton(player, contents);
		
		/* Second Option: Google Translate API Key */
		ConversationFactory apiConvo = new ConversationFactory(main)
				.withModality(true)
				.withFirstPrompt(new TranslatorSettingsGoogleTranslateApiKeyConversation());
		ItemStack apiKeyButton = new ItemStack(Material.NAME_TAG);
		ItemMeta apiKeyMeta = apiKeyButton.getItemMeta();
		apiKeyMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIGoogleTranslateAPIKeyButton"));
		apiKeyButton.setItemMeta(apiKeyMeta);
		contents.set(1, 2, ClickableItem.of(apiKeyButton, 
				e -> {apiConvo.buildConversation(player).begin();}));
		
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
                	main.removePlayerUsingConfigurationGUI(player);
                	player.closeInventory(); 
                	rel.processCommand();
        }));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		googleTranslateStatusButton(player, contents);
	}
	
	public void googleTranslateStatusButton(Player player, InventoryContents contents) {
		ItemStack translatorStatusButton;
		if (main.getTranslatorName().equals("Google Translate")) {
			translatorStatusButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorStatusButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorStatusButtonMeta = translatorStatusButton.getItemMeta();
		translatorStatusButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIGoogleTranslateStatusButton"));
		translatorStatusButton.setItemMeta(translatorStatusButtonMeta);
		contents.set(1, 1, ClickableItem.of(translatorStatusButton, 
				e -> {
					    if (!main.getTranslatorName().equals("Google Translate")) {
					    	TaskChain<?> chain = WorldwideChat.newSharedChain("enableGoogleTranslate");
					    	chain
					    	    .sync(() -> {
					    	    	main.removePlayerUsingConfigurationGUI(player);
					    	        player.closeInventory();
					    	    })
					    	    .asyncFirst(() -> {
					    	    	boolean translatorStatus = false;
					    	    	try {
					    	    		GoogleTranslation testConnection = new GoogleTranslation(main.getConfigManager().getMainConfig().getString("Translator.googleTranslateAPIKey"));
									    testConnection.initializeConnection();
									    translatorStatus = true;
									} catch (Exception bad) {
										final TextComponent badResult = Component.text()
									            .append(main.getPluginPrefix().asComponent())
									            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationTranslatorFail").replace("%i", "Google Translate")).color(NamedTextColor.RED))
									            .build();
									        Audience adventureSender = main.adventure().sender(player);
									    adventureSender.sendMessage(badResult);
									    main.getLogger().severe(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationConsoleTranslatorFail").replace("%i", player.getName()).replace("%o", "Google Translate"));
									    bad.printStackTrace();
									}
					    	    	return translatorStatus;
					    	    })
					    	    .storeAsData("connectionStatus")
					    	    .<Boolean>returnData("connectionStatus")
					    	    .syncLast((connectionStatus) -> {
					    	    	if (!connectionStatus) {
					    	    		googleTranslateSettings.open(player);
					    	    		main.addPlayerUsingConfigurationGUI(player);
					    	    		chain.abortChain();
					    	    	}
					    	    })
					    	    .sync(() -> {
					    	    	main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", false);
								    main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate", false);
								    main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate", true);
								    try {
										main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
									} catch (IOException e1) {
										e1.printStackTrace();
									}
					    	    })
					    	    .async(() -> {
					    	    	final TextComponent successfulChange = Component.text()
							                .append(main.getPluginPrefix().asComponent())
							                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationTranslatorSuccess").replace("%i", "Google Translate")).color(NamedTextColor.GREEN))
							                .build();
							            Audience adventureSender = main.adventure().sender(player);
							        adventureSender.sendMessage(successfulChange);
							        main.getLogger().info(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationConsoleTranslatorSuccess").replace("%i", player.getName()).replace("%o", "Google Translate"));
					    	    })
					    	    .sync(() -> {
					    	    	WWCReload rel = new WWCReload(player, null, null, null);
							        rel.processCommand();
					    	    })
					    	    .sync(TaskChain::abort)
					    	    .execute();
					}
		    }));
	}

}
