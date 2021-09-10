package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCReload;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationGeneralSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();

	public static final SmartInventory generalSettings = SmartInventory.builder().id("generalSettingsMenu")
			.provider(new ConfigurationGeneralSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIGeneralSettings"))
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

			/* Option One: Plugin Prefix */
			ConversationFactory prefixConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new GeneralSettingsPrefixConversation());
			ItemStack prefixButton = new ItemStack(Material.NAME_TAG);
			ItemMeta prefixMeta = prefixButton.getItemMeta();
			prefixMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUIPrefixButton"));
			prefixButton.setItemMeta(prefixMeta);
			contents.set(1, 1, ClickableItem.of(prefixButton, e -> {
				prefixConvo.buildConversation(player).begin();
			}));

			/* Option Two: bStats */
			bStatsButton(player, contents);

			/* Option Three: Change Plugin Lang */
			ConversationFactory langConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new GeneralSettingsLangConversation());
			ItemStack langButton = new ItemStack(Material.NAME_TAG);
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
			ItemStack updateCheckerButton = new ItemStack(Material.NAME_TAG);
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
			ItemStack syncUserDataButton = new ItemStack(Material.NAME_TAG);
			ItemMeta syncUserDataMeta = syncUserDataButton.getItemMeta();
			syncUserDataMeta.setDisplayName(ChatColor.GOLD
					+ CommonDefinitions.getMessage("wwcConfigGUISyncUserDataButton"));
			syncUserDataButton.setItemMeta(syncUserDataMeta);
			contents.set(1, 5, ClickableItem.of(syncUserDataButton, e -> {
				syncUserDataConvo.buildConversation(player).begin();
			}));
			
			/* Option Six: Debug Mode */
			debugModeButton(player, contents);

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

			/* Bottom Right Option: Next Page */
			ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta nextPageMeta = nextPageButton.getItemMeta();
			nextPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
			nextPageButton.setItemMeta(nextPageMeta);
			contents.set(2, 7,
					ClickableItem.of(nextPageButton, e -> ConfigurationChatSettingsGUI.chatSettings.open(player)));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
	
	private void bStatsButton(Player player, InventoryContents contents) {
		ItemStack bStatsButton = new ItemStack(Material.BEDROCK);
		if (main.getbStats()) {
			bStatsButton.setType(Material.EMERALD_BLOCK);
		} else {
			bStatsButton.setType(Material.REDSTONE_BLOCK);
		}
		ItemMeta bStatsMeta = bStatsButton.getItemMeta();
		bStatsMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage("wwcConfigGUIbStatsButton"));
		bStatsButton.setItemMeta(bStatsMeta);
		contents.set(1, 2, ClickableItem.of(bStatsButton, e -> {
			main.addPlayerUsingConfigurationGUI(player);
			main.getConfigManager().getMainConfig().set("General.enablebStats",
					!(main.getConfigManager().getMainConfig().getBoolean("General.enablebStats")));
			main.setbStats(!main.getbStats());
			main.getConfigManager().saveMainConfig(true);
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcConfigConversationbStatsSuccess"))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage(player, successfulChange);
			bStatsButton(player, contents);
		}));
	}
	
	private void debugModeButton(Player player, InventoryContents contents) {
		ItemStack debugModeButton = new ItemStack(Material.BEDROCK);
		if (main.getDebugMode()) {
			debugModeButton.setType(Material.EMERALD_BLOCK);
		} else {
			debugModeButton.setType(Material.REDSTONE_BLOCK);
		}
		ItemMeta debugModeMeta = debugModeButton.getItemMeta();
		debugModeMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage("wwcConfigGUIDebugModeButton"));
		debugModeButton.setItemMeta(debugModeMeta);
		contents.set(1, 6, ClickableItem.of(debugModeButton, e -> {
			main.addPlayerUsingConfigurationGUI(player);
			main.getConfigManager().getMainConfig().set("General.enableDebugMode",
					!(main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")));
			main.setDebugMode(!main.getDebugMode());
			main.getConfigManager().saveMainConfig(true);
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcConfigConversationDebugModeSuccess"))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage(player, successfulChange);
			debugModeButton(player, contents);
		}));
	}
}
