package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Set;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class BlacklistModifyGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

	private WWCInventoryManager invManager = main.getInventoryManager();

	private String currentTerm = "";

	private Player inPlayer;

	public BlacklistModifyGui(String currentTerm, Player inPlayer) {
		this.currentTerm = currentTerm;
		this.inPlayer = inPlayer;
	}
	
	public SmartInventory modifyTerm() {
		return SmartInventory.builder().id("blacklistModifyMenu")
				.provider(this).size(3, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
					.title(refs.getPlainMsg("wwcConfigGUIBlacklistMessagesModify",
							currentTerm,
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
			//invManager.genericConversationButton(1, 4, player, contents, new ChatSettingsConvos.ModifyOverrideText(getModifyCurrentOverride(), currentOverrideName, inLang), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesOverrideChangeButton");
			
			/* Right Option: Delete override */
			ItemStack deleteTermButton = XMaterial.BARRIER.parseItem();
			ItemMeta deleteTermMeta = deleteTermButton.getItemMeta();
			deleteTermMeta.setDisplayName(refs.getPlainMsg("wwcConfigGUIChatMessagesBlacklistDeleteButton",
					"",
					"&c",
					inPlayer));
			deleteTermButton.setItemMeta(deleteTermMeta);
			contents.set(1, 5, ClickableItem.of(deleteTermButton, e -> {
				GenericRunnable save = new GenericRunnable() {
					@Override
					protected void execute() {
						YamlConfiguration config = main.getConfigManager().getBlacklistConfig();
						Set<String> bannedWords = main.getBlacklistTerms();
						bannedWords.remove(currentTerm); // Remove currentTerm from the list
						config.set("bannedWords", new ArrayList<>(bannedWords)); // Save the updated list back to the config

						main.getConfigManager().saveCustomConfig(config, main.getConfigManager().getBlacklistFile(), false);
						main.addPlayerUsingConfigurationGUI(inPlayer.getUniqueId());
						refs.sendMsg("wwcConfigConversationBlacklistDeletionSuccess", "", "&a", inPlayer);

						GenericRunnable out = new GenericRunnable() {
							@Override
							protected void execute() {
								new BlacklistGui(inPlayer).getBlacklist().open(player);
							}
						};
						wwcHelper.runSync(out, ENTITY, new Object[] {player});
					}
				};
				wwcHelper.runAsync(save, ASYNC, null);
			}));
			
			
			/* Left Option: Previous Page */
			invManager.setCommonButton(1, 3, player, contents, "Previous", new Object[] {new BlacklistGui(inPlayer).getBlacklist()});
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
