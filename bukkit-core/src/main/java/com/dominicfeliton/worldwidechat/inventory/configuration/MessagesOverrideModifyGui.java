package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.conversations.configuration.ChatSettingsConvos;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

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
					.title(refs.getPlainMsg("wwcConfigGUIChatMessagesModifyOverride",
							"",
							"&9",
							inPlayer))
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
					+ refs.getPlainMsg("wwcConfigGUIChatMessagesOverrideDeleteButton", inPlayer));
			deleteOverrideButton.setItemMeta(deleteOverrideMeta);
			contents.set(1, 6, ClickableItem.of(deleteOverrideButton, e -> {
				GenericRunnable saveMessages = new GenericRunnable() {
					@Override
					protected void execute() {
						YamlConfiguration msgConfigCustom = main.getConfigManager().getCustomMessagesConfig(inLang);
						msgConfigCustom.set("Overrides." + currentOverrideName, null);
						main.getConfigManager().saveMessagesConfig(inLang, false);

						main.addPlayerUsingConfigurationGUI(inPlayer.getUniqueId());
						refs.sendMsg("wwcConfigConversationOverrideDeletionSuccess",
								"",
								"&a",
								inPlayer);

						GenericRunnable out = new GenericRunnable() {
							@Override
							protected void execute() {
								new MessagesOverrideCurrentListGui(inLang, inPlayer).getOverrideMessagesSettings().open(player);
							}
						};
						wwcHelper.runSync(out, ENTITY, new Object[] {player});
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
