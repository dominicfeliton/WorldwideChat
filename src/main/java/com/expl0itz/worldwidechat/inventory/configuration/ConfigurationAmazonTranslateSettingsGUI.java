package com.expl0itz.worldwidechat.inventory.configuration;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateAccessKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateRegionConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateSecretKeyConversation;
import com.expl0itz.worldwidechat.translators.AmazonTranslation;

import co.aikar.taskchain.TaskChain;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationAmazonTranslateSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory amazonTranslateSettings = SmartInventory.builder()
			.id("amazonTranslateSettingsMenu")
			.provider(new ConfigurationAmazonTranslateSettingsGUI())
			.parent(ConfigurationTranslatorSettingsGUI.translatorSettings)
			.size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIAmazonTranslateSettings"))
			.build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		/* White stained glass borders */
		ItemStack customBorders = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
		ItemMeta borderMeta = customBorders.getItemMeta();
		borderMeta.setDisplayName(" ");
		customBorders.setItemMeta(borderMeta);
		contents.fillBorders(ClickableItem.empty(customBorders));
		
		/* First Option: Amazon Translate Status Button */
		amazonTranslateStatusButton(player, contents);
		
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
		amazonTranslateStatusButton(player, contents);
	}

	public void amazonTranslateStatusButton(Player player, InventoryContents contents) {
		ItemStack translatorStatusButton;
		if (main.getTranslatorName().equals("Amazon Translate")) {
			translatorStatusButton = new ItemStack(Material.EMERALD_BLOCK);
		} else {
			translatorStatusButton = new ItemStack(Material.REDSTONE_BLOCK);
		}
		ItemMeta translatorStatusButtonMeta = translatorStatusButton.getItemMeta();
		translatorStatusButtonMeta.setDisplayName(ChatColor.GOLD + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIAmazonTranslateStatusButton"));
		translatorStatusButton.setItemMeta(translatorStatusButtonMeta);
		contents.set(1, 1, ClickableItem.of(translatorStatusButton, 
				e -> {
					    if (!main.getTranslatorName().equals("Amazon Translate")) {
					    	TaskChain<?> chain = WorldwideChat.newSharedChain("enableAmazonTranslate");
					    	chain
					    	    .sync(() -> {
					    	    	main.removePlayerUsingConfigurationGUI(player);
					    	        player.closeInventory();
					    	    })
					    	    .asyncFirst(() -> {
					    	    	boolean translatorStatus = false;
					    	    	try {
									    AmazonTranslation testConnection = new AmazonTranslation(main.getConfigManager().getMainConfig().getString("Translator.amazonAccessKey"), 
									    		main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey"), 
									    		main.getConfigManager().getMainConfig().getString("Translator.amazonRegion"));
									    testConnection.initializeConnection();
									    translatorStatus = true;
									} catch (Exception bad) {
										final TextComponent badResult = Component.text()
									            .append(main.getPluginPrefix().asComponent())
									            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationTranslatorFail").replace("%i", "Amazon Translate")).color(NamedTextColor.RED))
									            .build();
									        Audience adventureSender = main.adventure().sender(player);
									    adventureSender.sendMessage(badResult);
									    main.getLogger().severe(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationConsoleTranslatorFail").replace("%i", player.getName()).replace("%o", "Amazon Translate"));
									    bad.printStackTrace();
									}
					    	    	return translatorStatus;
					    	    })
					    	    .storeAsData("connectionStatus")
					    	    .<Boolean>returnData("connectionStatus")
					    	    .syncLast((connectionStatus) -> {
					    	    	if (!connectionStatus) {
					    	    		amazonTranslateSettings.open(player);
					    	    		main.addPlayerUsingConfigurationGUI(player);
					    	    		chain.abortChain();
					    	    	}
					    	    })
					    	    .sync(() -> {
					    	    	main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", false);
								    main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate", true);
								    main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate", false);
								    try {
										main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
									} catch (IOException e1) {
										e1.printStackTrace();
									}
					    	    })
					    	    .async(() -> {
					    	    	final TextComponent successfulChange = Component.text()
							                .append(main.getPluginPrefix().asComponent())
							                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationTranslatorSuccess").replace("%i", "Amazon Translate")).color(NamedTextColor.GREEN))
							                .build();
							            Audience adventureSender = main.adventure().sender(player);
							        adventureSender.sendMessage(successfulChange);
							        main.getLogger().info(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationConsoleTranslatorSuccess").replace("%i", player.getName()).replace("%o", "Amazon Translate"));
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
