package com.badskater0729.worldwidechat.inventory.configuration;

import com.badskater0729.worldwidechat.WorldwideChatHelper;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
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

import static com.badskater0729.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.badskater0729.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class MessagesOverrideModifyGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

	private WWCInventoryManager invManager = main.getInventoryManager();
	
	private String currentOverrideName = "";
	private String inLang = "";

	private Player inPlayer;
	
	public MessagesOverrideModifyGui(String currentOverrideName, String inLang, Player inPlayer) {
		this.currentOverrideName = currentOverrideName;
		this.inLang = inLang;
		this.inPlayer = inPlayer;
	}
	
	public SmartInventory getModifyCurrentOverride() {
		return SmartInventory.builder().id("overrideModifyMenu")
				.provider(this).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
					.title(ChatColor.BLUE + refs.getMsg("wwcConfigGUIChatMessagesModifyOverride", inPlayer))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Set borders to orange */
			invManager.setBorders(contents, XMaterial.ORANGE_STAINED_GLASS_PANE);

			/* Middle Option: Change existing text */
			invManager.genericConversationButton(1, 4, player, contents, new ChatSettingsConvos.ModifyOverrideText(getModifyCurrentOverride(), currentOverrideName, inLang), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesOverrideChangeButton");
			
			/* Right Option: Delete override */
			ItemStack deleteOverrideButton = XMaterial.BARRIER.parseItem();
			ItemMeta deleteOverrideMeta = deleteOverrideButton.getItemMeta();
			deleteOverrideMeta.setDisplayName(ChatColor.RED
					+ refs.getMsg("wwcConfigGUIChatMessagesOverrideDeleteButton", inPlayer));
			deleteOverrideButton.setItemMeta(deleteOverrideMeta);
			contents.set(1, 6, ClickableItem.of(deleteOverrideButton, e -> {
				BukkitRunnable saveMessages = new BukkitRunnable() {
					@Override
					public void run() {
						YamlConfiguration msgConfigCustom = main.getConfigManager().getCustomMessagesConfig(inLang);
						msgConfigCustom.set("Overrides." + currentOverrideName, null);
						main.getConfigManager().saveMessagesConfig(inLang, false);
						final TextComponent successfulChange = Component.text()
										.content(refs.getMsg("wwcConfigConversationOverrideDeletionSuccess", inPlayer))
										.color(NamedTextColor.GREEN)
								.build();
						refs.sendMsg(player, successfulChange);
						BukkitRunnable out = new BukkitRunnable() {
							@Override
							public void run() {
								new MessagesOverrideCurrentListGui(inLang, inPlayer).getOverrideMessagesSettings().open(player);
							}
						};
						wwcHelper.runSync(out, ENTITY, player);
					}
				};
				wwcHelper.runAsync(saveMessages, ASYNC, null);
			}));
			
			
			/* Left Option: Previous Page */
			invManager.setCommonButton(1, 2, player, contents, "Previous", new Object[] {new MessagesOverrideCurrentListGui(inLang, inPlayer).getOverrideMessagesSettings()});
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
