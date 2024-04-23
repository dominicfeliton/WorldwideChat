package com.badskater0729.worldwidechat.inventory.wwctranslategui;

import java.util.UUID;

import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.commands.WWCTranslateChatIncoming;
import com.badskater0729.worldwidechat.commands.WWCTranslateChatOutgoing;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class WWCTranslateGuiChatMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();

	private String targetPlayerUUID = "";
	private String targetPlayerName = "";
	private Player inPlayer;

	public WWCTranslateGuiChatMenu(String targetPlayerUUID, Player inPlayer) {
		this.inPlayer = inPlayer;
		this.targetPlayerUUID = targetPlayerUUID;
		this.targetPlayerName = ChatColor.BLUE + refs.getMsg("wwctGUIChatMenu", main.getServer()
				.getPlayer(UUID.fromString(targetPlayerUUID)).getName(), inPlayer);
	}

	/* Get translation info */
	public SmartInventory getTranslateChatMenu() {
		return SmartInventory.builder().id("translateChatMenu").provider(this)
				.size(4, 9).manager(main.getInventoryManager()).title(targetPlayerName).build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			/* White stained glass borders as default, Green stained glass borders for active */
			invManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			
			if (targetTranslator.getTranslatingChatOutgoing() || targetTranslator.getTranslatingChatIncoming()) {
				invManager.setBorders(contents, XMaterial.GREEN_STAINED_GLASS_PANE);
			}
			
			/* Outgoing Chat Button */
			if ((player.getUniqueId().toString().equals(targetPlayerUUID) && player.hasPermission("worldwidechat.wwctco"))
					|| (!player.getUniqueId().toString().equals(targetPlayerUUID)) && player.hasPermission("worldwidechat.wwctco.otherplayers")) {
				ItemStack outgoingChatButton = XMaterial.CHEST_MINECART.parseItem();
				ItemMeta outgoingChatMeta = outgoingChatButton.getItemMeta();
				if (targetTranslator.getTranslatingChatOutgoing()) {
					invManager.addGlowEffect(outgoingChatMeta);
					outgoingChatMeta.setDisplayName(ChatColor.GREEN
							+ refs.getMsg("wwctGUIChatOutgoingButton", inPlayer));
				} else {
					outgoingChatMeta.setDisplayName(ChatColor.YELLOW
							+ refs.getMsg("wwctGUIChatOutgoingButton", inPlayer));
				}
				outgoingChatButton.setItemMeta(outgoingChatMeta);
				contents.set(1, 3, ClickableItem.of(outgoingChatButton, e -> {
					String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
					WWCTranslateChatOutgoing translateChatOutgoing = new WWCTranslateChatOutgoing((CommandSender) player, null, null, args);
					translateChatOutgoing.processCommand();
					getTranslateChatMenu().open(player);
				}));	
			}
			
			/* Incoming Chat Button */
			if ((player.getUniqueId().toString().equals(targetPlayerUUID) && player.hasPermission("worldwidechat.wwctci"))
					|| (!player.getUniqueId().toString().equals(targetPlayerUUID)) && player.hasPermission("worldwidechat.wwctci.otherplayers")) {
            	ItemStack incomingChatButton = XMaterial.ANVIL.parseItem();
    			ItemMeta incomingChatMeta = incomingChatButton.getItemMeta();
    			if (targetTranslator.getTranslatingChatIncoming()) {
    				invManager.addGlowEffect(incomingChatMeta);
    				incomingChatMeta.setDisplayName(ChatColor.GREEN
    						+ refs.getMsg("wwctGUIChatIncomingButton", inPlayer));
    			} else {
    				incomingChatMeta.setDisplayName(ChatColor.YELLOW
    						+ refs.getMsg("wwctGUIChatIncomingButton", inPlayer));
    			}
    			incomingChatButton.setItemMeta(incomingChatMeta);
    			contents.set(1, 5, ClickableItem.of(incomingChatButton, e -> {
    				String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
    				WWCTranslateChatIncoming translateChatIncoming = new WWCTranslateChatIncoming((CommandSender) player, null, null, args);
    				translateChatIncoming.processCommand();
    				getTranslateChatMenu().open(player);
    			}));	
			}
			
			/* Bottom Left Option: Previous Page */
			invManager.setCommonButton(2, 4, player, contents, "Previous", new Object[] {new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu()});
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		invManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}
}
