package com.badskater0729.worldwidechat.inventory.configuration;

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

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.runSync;
import static com.badskater0729.worldwidechat.util.CommonRefs.runAsync;

public class MessagesOverrideModifyGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	
	private String currentOverrideName = "";
	
	public MessagesOverrideModifyGui(String currentOverrideName) {
		this.currentOverrideName = currentOverrideName;
	}
	
	public static SmartInventory getModifyCurrentOverride(String currentOverrideName) {
		return SmartInventory.builder().id("overrideModifyMenu")
				.provider(new MessagesOverrideModifyGui(currentOverrideName)).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + getMsg("wwcConfigGUIChatMessagesModifyOverride"))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Set borders to orange */
			WWCInventoryManager.setBorders(contents, XMaterial.ORANGE_STAINED_GLASS_PANE);

			/* Middle Option: Change existing text */
			WWCInventoryManager.genericConversationButton(1, 4, player, contents, new ChatSettingsConvos.ModifyOverrideText(getModifyCurrentOverride(currentOverrideName), currentOverrideName), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesOverrideChangeButton");
			
			/* Right Option: Delete override */
			ItemStack deleteOverrideButton = XMaterial.BARRIER.parseItem();
			ItemMeta deleteOverrideMeta = deleteOverrideButton.getItemMeta();
			deleteOverrideMeta.setDisplayName(ChatColor.RED
					+ getMsg("wwcConfigGUIChatMessagesOverrideDeleteButton"));
			deleteOverrideButton.setItemMeta(deleteOverrideMeta);
			contents.set(1, 6, ClickableItem.of(deleteOverrideButton, e -> {
				BukkitRunnable saveMessages = new BukkitRunnable() {
					@Override
					public void run() {
						main.getConfigManager().getMsgsConfig().set("Overrides." + currentOverrideName, null);
						main.getConfigManager().saveMessagesConfig(false);
						final TextComponent successfulChange = Component.text()
								.append(Component.text()
										.content(getMsg("wwcConfigConversationOverrideDeletionSuccess"))
										.color(NamedTextColor.GREEN))
								.build();
						sendMsg(player, successfulChange);
						BukkitRunnable out = new BukkitRunnable() {
							@Override
							public void run() {
								MessagesOverrideCurrentListGui.overrideMessagesSettings.open(player);
							}
						};
						runSync(out);
					}
				};
				runAsync(saveMessages);
			}));
			
			
			/* Left Option: Previous Page */
			WWCInventoryManager.setCommonButton(1, 2, player, contents, "Previous", new Object[] {MessagesOverrideCurrentListGui.overrideMessagesSettings});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
