package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
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

public class MessagesOverrideModifyGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	
	private String currentOverrideName = "";
	
	public MessagesOverrideModifyGUI(String currentOverrideName) {
		this.currentOverrideName = currentOverrideName;
	}
	
	public static SmartInventory getModifyCurrentOverride(String currentOverrideName) {
		return SmartInventory.builder().id("overrideModifyMenu")
				.provider(new MessagesOverrideModifyGUI(currentOverrideName)).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatMessagesModifyOverride"))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Set borders to orange */
			WWCInventoryManager.setBorders(contents, XMaterial.ORANGE_STAINED_GLASS_PANE);

			/* Middle Option: Change existing text */
			WWCInventoryManager.genericConversationButton(1, 4, player, contents, new ChatSettingsModifyOverrideTextConversation(getModifyCurrentOverride(currentOverrideName), currentOverrideName), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesOverrideChangeButton");
			
			/* Right Option: Delete override */
			ItemStack deleteOverrideButton = XMaterial.BARRIER.parseItem();
			ItemMeta deleteOverrideMeta = deleteOverrideButton.getItemMeta();
			deleteOverrideMeta.setDisplayName(ChatColor.RED
					+ CommonDefinitions.getMessage("wwcConfigGUIChatMessagesOverrideDeleteButton"));
			deleteOverrideButton.setItemMeta(deleteOverrideMeta);
			contents.set(1, 6, ClickableItem.of(deleteOverrideButton, e -> {
				BukkitRunnable saveMessages = new BukkitRunnable() {
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
						BukkitRunnable out = new BukkitRunnable() {
							@Override
							public void run() {
								MessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
							}
						};
						CommonDefinitions.scheduleTask(out);
					}
				};
				CommonDefinitions.scheduleTaskAsynchronously(saveMessages);
			}));
			
			
			/* Left Option: Previous Page */
			WWCInventoryManager.setCommonButton(1, 2, player, contents, "Previous", new Object[] {MessagesOverrideCurrentListGUI.overrideMessagesSettings});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
