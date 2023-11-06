package com.badskater0729.worldwidechat.inventory.configuration;

import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.conversations.configuration.ChatSettingsConvos;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class MessagesOverrideModifyGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = new CommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();
	
	private String currentOverrideName = "";
	
	public MessagesOverrideModifyGui(String currentOverrideName) {
		this.currentOverrideName = currentOverrideName;
	}
	
	public SmartInventory getModifyCurrentOverride() {
		return SmartInventory.builder().id("overrideModifyMenu")
				.provider(new MessagesOverrideModifyGui(currentOverrideName)).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + refs.getMsg("wwcConfigGUIChatMessagesModifyOverride"))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Set borders to orange */
			invManager.setBorders(contents, XMaterial.ORANGE_STAINED_GLASS_PANE);

			/* Middle Option: Change existing text */
			invManager.genericConversationButton(1, 4, player, contents, new ChatSettingsConvos.ModifyOverrideText(getModifyCurrentOverride(), currentOverrideName), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesOverrideChangeButton");
			
			/* Right Option: Delete override */
			ItemStack deleteOverrideButton = XMaterial.BARRIER.parseItem();
			ItemMeta deleteOverrideMeta = deleteOverrideButton.getItemMeta();
			deleteOverrideMeta.setDisplayName(ChatColor.RED
					+ refs.getMsg("wwcConfigGUIChatMessagesOverrideDeleteButton"));
			deleteOverrideButton.setItemMeta(deleteOverrideMeta);
			contents.set(1, 6, ClickableItem.of(deleteOverrideButton, e -> {
				BukkitRunnable saveMessages = new BukkitRunnable() {
					@Override
					public void run() {
						main.getConfigManager().getMsgsConfig().set("Overrides." + currentOverrideName, null);
						main.getConfigManager().saveMessagesConfig(false);
						final TextComponent successfulChange = Component.text()
										.content(refs.getMsg("wwcConfigConversationOverrideDeletionSuccess"))
										.color(NamedTextColor.GREEN)
								.build();
						refs.sendMsg(player, successfulChange);
						BukkitRunnable out = new BukkitRunnable() {
							@Override
							public void run() {
								new MessagesOverrideCurrentListGui().overrideMessagesSettings.open(player);
							}
						};
						refs.runSync(out);
					}
				};
				refs.runAsync(saveMessages);
			}));
			
			
			/* Left Option: Previous Page */
			invManager.setCommonButton(1, 2, player, contents, "Previous", new Object[] {new MessagesOverrideCurrentListGui().overrideMessagesSettings});
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
