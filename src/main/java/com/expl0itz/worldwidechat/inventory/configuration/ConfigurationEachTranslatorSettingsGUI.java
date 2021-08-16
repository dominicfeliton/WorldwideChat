package com.expl0itz.worldwidechat.inventory.configuration;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateAccessKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateRegionConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateSecretKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGoogleTranslateApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonServiceUrlConversation;
import com.expl0itz.worldwidechat.translators.AmazonTranslation;
import com.expl0itz.worldwidechat.translators.GoogleTranslation;
import com.expl0itz.worldwidechat.translators.WatsonTranslation;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationEachTranslatorSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private String translatorName = "Invalid";
	
	public ConfigurationEachTranslatorSettingsGUI(String translatorName) {
		this.translatorName = translatorName;
	}
	
	public static SmartInventory getCurrentTranslatorSettings(String translatorName) {
		return SmartInventory.builder()
				.id("currentTranslatorSettings")
				.provider(new ConfigurationEachTranslatorSettingsGUI(translatorName))
				.parent(ConfigurationTranslatorSettingsGUI.translatorSettings)
				.size(3, 9)
				.manager(WorldwideChat.getInstance().getInventoryManager())
				.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIEachTranslatorSettings").replace("%i",translatorName))
			.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		if (translatorName.equals("Watson")) {
			/* White stained glass borders */
			ItemStack customBorders = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));
			
			/* First Option: Watson Status Button */
			translateStatusButton(player, contents);
			
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
		} else if (translatorName.equals("Google Translate")) {
			/* White stained glass borders */
			ItemStack customBorders = new ItemStack(Material.RED_STAINED_GLASS_PANE);
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));
			
			/* First Option: Google Translate is Enabled */
			translateStatusButton(player, contents);
			
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
		} else if (translatorName.equals("Amazon Translate")) {
			ItemStack customBorders = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));
			
			/* First Option: Amazon Translate Status Button */
			translateStatusButton(player, contents);
			
			/* Second Option: Amazon Access Key */
			ConversationFactory accessKeyConvo = new ConversationFactory(main)
					.withModality(true)
					.withFirstPrompt(new TranslatorSettingsAmazonTranslateAccessKeyConversation());
			ItemStack accessKeyButton = new ItemStack(Material.NAME_TAG);
			ItemMeta accessKeyMeta = accessKeyButton.getItemMeta();
			accessKeyMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIAmazonTranslateAccessKeyButton"));
			accessKeyButton.setItemMeta(accessKeyMeta);
			contents.set(1, 2, ClickableItem.of(accessKeyButton, 
					e -> {accessKeyConvo.buildConversation(player).begin();}));
			
			/* Third Option: Amazon Secret Key */
			ConversationFactory secretKeyConvo = new ConversationFactory(main)
					.withModality(true)
			        .withFirstPrompt(new TranslatorSettingsAmazonTranslateSecretKeyConversation());
			ItemStack secretKeyButton = new ItemStack(Material.NAME_TAG);
			ItemMeta secretKeyMeta = secretKeyButton.getItemMeta();
		    secretKeyMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIAmazonTranslateSecretKeyButton"));
			secretKeyButton.setItemMeta(secretKeyMeta);
			contents.set(1, 3, ClickableItem.of(secretKeyButton, 
					e -> {secretKeyConvo.buildConversation(player).begin();}));
			
			/* Fourth Option: Amazon Region */
			ConversationFactory regionConvo = new ConversationFactory(main)
					.withModality(true)
			        .withFirstPrompt(new TranslatorSettingsAmazonTranslateRegionConversation());
			ItemStack regionButton = new ItemStack(Material.NAME_TAG);
			ItemMeta regionMeta = regionButton.getItemMeta();
			regionMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIAmazonTranslateRegionButton"));
			regionButton.setItemMeta(regionMeta);
			contents.set(1, 4, ClickableItem.of(regionButton, 
					e -> {regionConvo.buildConversation(player).begin();}));
		} else {
			ItemStack customBorders = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));
		}
		
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
		translateStatusButton(player, contents);
		
	}
	
	public void translateStatusButton(Player player, InventoryContents contents) {
		ItemStack translatorStatusButton;
		if (main.getTranslatorName().equals(translatorName)) {
			translatorStatusButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorStatusButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorStatusButtonMeta = translatorStatusButton.getItemMeta();
		translatorStatusButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIToggleButton").replace("%i", translatorName));
		translatorStatusButton.setItemMeta(translatorStatusButtonMeta);
		
		contents.set(1, 1, ClickableItem.of(translatorStatusButton, 
				e -> {
					    if (!main.getTranslatorName().equals(translatorName)) {
					    	new BukkitRunnable() {
								@Override
								public void run() {
									//Close player inventory
									main.removePlayerUsingConfigurationGUI(player);
					    	        player.closeInventory();
					    	        
					    	        //Test current translator settings
					    	        new BukkitRunnable() {
					    	        	@Override
					    	        	public void run() {
					    	        		boolean translatorStatus = false;
							    	    	try {
							    	    		if (translatorName.equals("Watson")) {
							    	    			WatsonTranslation testConnection = new WatsonTranslation(main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"), main.getConfigManager().getMainConfig().getString("Translator.watsonURL"));
												    testConnection.initializeConnection();
												    translatorStatus = true;
							    	    		} else if (translatorName.equals("Google Translate")) {
							    	    			GoogleTranslation testConnection = new GoogleTranslation(main.getConfigManager().getMainConfig().getString("Translator.googleTranslateAPIKey"));
												    testConnection.initializeConnection();
												    translatorStatus = true;
							    	    		} else if (translatorName.equals("Amazon Translate")) {
							    	    			AmazonTranslation testConnection = new AmazonTranslation(main.getConfigManager().getMainConfig().getString("Translator.amazonAccessKey"), 
												    		main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey"), 
												    		main.getConfigManager().getMainConfig().getString("Translator.amazonRegion"));
												    testConnection.initializeConnection();
												    translatorStatus = true;
							    	    		}
											} catch (Exception bad) {
												final TextComponent badResult = Component.text()
											            .append(main.getPluginPrefix().asComponent())
											            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationTranslatorFail").replace("%i", translatorName)).color(NamedTextColor.RED))
											            .build();
											        Audience adventureSender = main.adventure().sender(player);
											    adventureSender.sendMessage(badResult);
											    main.getLogger().severe(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationConsoleTranslatorFail").replace("%i", player.getName()).replace("%o", translatorName));
											    bad.printStackTrace();
											}
							    	    	final boolean check = translatorStatus;
							    	    	
							    	    	//Check if we failed; continue if we did not
							    	    	new BukkitRunnable() {
							    	    		@Override
							    	    		public void run() {
							    	    			if (!check) {
							    	    				ConfigurationEachTranslatorSettingsGUI.getCurrentTranslatorSettings(translatorName).open(player);
									    	    		main.addPlayerUsingConfigurationGUI(player);
									    	    		this.cancel();
									    	    		return;
							    	    			}
							    	    			if (translatorName.equals("Watson")) {
							    	    				main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", true);
													    main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate", false);
													    main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate", false);
							    	    			} else if (translatorName.equals("Google Translate")) {
							    	    				main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", false);
													    main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate", false);
													    main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate", true);
							    	    			} else if (translatorName.equals("Amazon Translate")) {
							    	    				main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", false);
													    main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate", true);
													    main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate", false);
							    	    			}
												    try {
														main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
													} catch (IOException e1) {
														e1.printStackTrace();
														this.cancel();
														return;
													}
												    
												    //Send successful change message
												    new BukkitRunnable() {
												    	@Override
												    	public void run() {
												    		final TextComponent successfulChange = Component.text()
													                .append(main.getPluginPrefix().asComponent())
													                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationTranslatorSuccess").replace("%i", translatorName)).color(NamedTextColor.GREEN))
													                .build();
													            Audience adventureSender = main.adventure().sender(player);
													        adventureSender.sendMessage(successfulChange);
													        main.getLogger().info(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationConsoleTranslatorSuccess").replace("%i", player.getName()).replace("%o", translatorName));
												    	    
													        //Reload the plugin
													        new BukkitRunnable() {
												    	    	@Override
												    	    	public void run() {
												    	    		WWCReload rel = new WWCReload(player, null, null, null);
															        rel.processCommand();
												    	    	}
												    	    }.runTask(main);
												    	}
												    }.runTaskAsynchronously(main);
							    	    		}
							    	    	}.runTask(main);
					    	        	}
					    	        }.runTaskAsynchronously(main);
								}
							}.runTask(main);
					    }
		    }));
	}
	
}
