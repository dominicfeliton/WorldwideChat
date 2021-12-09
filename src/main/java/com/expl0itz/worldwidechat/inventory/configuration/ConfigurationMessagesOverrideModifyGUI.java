package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.ChatSettingsModifyOverrideTextConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfigurationMessagesOverrideModifyGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	
	private String currentOverrideName = "";
	
	public ConfigurationMessagesOverrideModifyGUI(String currentOverrideName) {
		this.currentOverrideName = currentOverrideName;
	}
	
	public static SmartInventory getModifyCurrentOverride(String currentOverrideName) {
		return SmartInventory.builder().id("overrideModifyMenu")
				.provider(new ConfigurationMessagesOverrideModifyGUI(currentOverrideName)).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatMessagesModifyOverride"))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Set borders to orange */
			ItemStack customDefaultBorders = XMaterial.ORANGE_STAINED_GLASS_PANE.parseItem();
			ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
			defaultBorderMeta.setDisplayName(" ");
			customDefaultBorders.setItemMeta(defaultBorderMeta);
			contents.fillBorders(ClickableItem.empty(customDefaultBorders));

			/* Middle Option: Change existing text */
			ConversationFactory textConvo = new ConversationFactory(main).withModality(true)
					.withFirstPrompt(new ChatSettingsModifyOverrideTextConversation(getModifyCurrentOverride(currentOverrideName), currentOverrideName));
			ItemStack changeExistingButton = XMaterial.WRITABLE_BOOK.parseItem();
			ItemMeta changeExistingMeta = changeExistingButton.getItemMeta();
			changeExistingMeta.setDisplayName(ChatColor.YELLOW
					+ CommonDefinitions.getMessage("wwcConfigGUIChatMessagesOverrideChangeButton"));
			changeExistingButton.setItemMeta(changeExistingMeta);
			contents.set(1, 4, ClickableItem.of(changeExistingButton, e -> {
				textConvo.buildConversation(player).begin();
			}));
			
			/* Right Option: Delete override */
			ItemStack deleteOverrideButton = XMaterial.BARRIER.parseItem();
			ItemMeta deleteOverrideMeta = deleteOverrideButton.getItemMeta();
			deleteOverrideMeta.setDisplayName(ChatColor.RED
					+ CommonDefinitions.getMessage("wwcConfigGUIChatMessagesOverrideDeleteButton"));
			deleteOverrideButton.setItemMeta(deleteOverrideMeta);
			contents.set(1, 6, ClickableItem.of(deleteOverrideButton, e -> {
				new BukkitRunnable() {
					@Override
					public void run() {
						main.getConfigManager().getMessagesConfig().set("Overrides." + currentOverrideName, null);
						main.getConfigManager().saveMessagesConfig(false);
						final TextComponent successfulChange = Component.text()
								.append(Component.text()
										.content(CommonDefinitions.getMessage("wwcConfigConversationOverrideDeletionSuccess"))
										.color(NamedTextColor.GREEN))
								.build();
						CommonDefinitions.sendMessage(player, successfulChange);
						new BukkitRunnable() {
							@Override
							public void run() {
								ConfigurationMessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
							}
						}.runTask(main);
					}
				}.runTaskAsynchronously(WorldwideChat.instance);
			}));
			
			
			/* Left Option: Previous Page */
			contents.set(1, 2, ClickableItem.of(WWCInventoryManager.getCommonButton("Previous"), e -> {
				ConfigurationMessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
			}));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
