package com.dominicfeliton.worldwidechat.inventory;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WWCInventoryManager extends InventoryManager {
	
	private static WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	
	public WWCInventoryManager() {
		super(main);
	}
	
	public void checkIfPlayerIsMissing(Player player, String targetPlayerUUID) {
		if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) == null) {
			// Target player no longer online
			player.closeInventory();
			final TextComponent targetPlayerDC = Component.text()
							.content(refs.getMsg("wwctGUITargetPlayerNull", player))
							.color(NamedTextColor.RED).decorate(TextDecoration.ITALIC)
					.build();
			refs.sendMsg(player, targetPlayerDC);
		}
	}
	
	public void inventoryError(Player player, Exception e) {
		final TextComponent inventoryError = Component.text()
						.content(refs.getMsg("wwcInventoryErrorPlayer", player))
						.color(NamedTextColor.RED)
				.build();
		refs. sendMsg(player, inventoryError);
		main.getLogger().severe(refs.getMsg("wwcInventoryError", player.getName(), player));
		e.printStackTrace();
		player.closeInventory();
	}
	
	public void setCommonButton(int x, int y, Player player, InventoryContents contents, String buttonType) {
		setCommonButton(x, y, player, contents, buttonType, new String[0]);
	}
	
	public void setCommonButton(int x, int y, Player player, InventoryContents contents, String buttonType, Object[] args) {
		ItemStack pageButton = XMaterial.WHITE_STAINED_GLASS.parseItem();
		ItemMeta pageMeta = pageButton.getItemMeta();
		if (buttonType.equalsIgnoreCase("Previous")) {
			pageButton = XMaterial.RED_STAINED_GLASS.parseItem();
			pageMeta.setDisplayName(ChatColor.RED
					+ refs.getMsg("wwcConfigGUIPreviousPageButton", player));
			pageButton.setItemMeta(pageMeta);
			contents.set(x, y, ClickableItem.of(pageButton, e -> {
				if (!contents.pagination().isFirst()) {
					contents.inventory().open(player, contents.pagination().getPage() - 1);
				} else {
					((SmartInventory) args[0]).open(player);
				}
			}));
		} else if (buttonType.equalsIgnoreCase("Next")) {
			pageButton = XMaterial.GREEN_STAINED_GLASS.parseItem();
			pageMeta.setDisplayName(ChatColor.GREEN
					+ refs.getMsg("wwcConfigGUINextPageButton", player));
			pageButton.setItemMeta(pageMeta);
			contents.set(x, y, ClickableItem.of(pageButton, e -> {
				if (!contents.pagination().isLast()) {
					contents.inventory().open(player, contents.pagination().getPage() + 1);
				} else {
					((SmartInventory) args[0]).open(player);
				}
			}));
		} else if (buttonType.equalsIgnoreCase("Page Number")) {
			pageButton = XMaterial.LILY_PAD.parseItem();
			pageMeta.setDisplayName(ChatColor.AQUA
					+ refs.getMsg("wwcGUIPageNumber", (Arrays.copyOf(args, args.length, String[].class)), player));
			if (args[0].equals("1")) {
				addGlowEffect(pageMeta);
			}
			pageButton.setItemMeta(pageMeta);
			contents.set(x, y, ClickableItem.empty(pageButton));
		} else if (buttonType.equalsIgnoreCase("Quit")) {
			pageButton = XMaterial.BARRIER.parseItem();
			pageMeta = pageButton.getItemMeta();
			pageMeta.setDisplayName(ChatColor.RED
					+ refs.getMsg("wwcConfigGUIQuitButton", player));
			pageButton.setItemMeta(pageMeta);
			contents.set(x, y, ClickableItem.of(pageButton, e -> {
				main.reload(player, true);
			}));
		} else {
			pageMeta.setDisplayName(ChatColor.RED + "Not a valid button! This is a bug, please report it.");
			pageButton.setItemMeta(pageMeta);
		}
	}
	
	public void setBorders(InventoryContents contents, XMaterial inMaterial) {
		ItemStack customBorders = inMaterial.parseItem();
		ItemMeta borderMeta = customBorders.getItemMeta();
		borderMeta.setDisplayName(" ");
		customBorders.setItemMeta(borderMeta);
		contents.fillBorders(ClickableItem.empty(customBorders));
	}
	
	public void genericOpenSubmenuButton(int x, int y, Player player, InventoryContents contents, String buttonName, SmartInventory invToOpen) {
		genericOpenSubmenuButton(x, y, player, contents, null, buttonName, invToOpen);
	}
	
	public void genericOpenSubmenuButton(int x, int y, Player player, InventoryContents contents, Boolean preCondition, String buttonName, SmartInventory invToOpen) {
		ItemStack button;
		if (preCondition != null) {
			if (preCondition) {
				button = XMaterial.EMERALD_BLOCK.parseItem();
			} else {
				button = XMaterial.REDSTONE_BLOCK.parseItem();
			}
		} else {
			button = XMaterial.WRITABLE_BOOK.parseItem();
		}
		ItemMeta buttonMeta = button.getItemMeta();
		buttonMeta.setDisplayName(ChatColor.GOLD
				+ refs.getMsg(buttonName, player));
		button.setItemMeta(buttonMeta);
		contents.set(x, y, ClickableItem.of(button, e -> {
			invToOpen.open(player);
		}));
	}
	
	public void genericToggleButton(int x, int y, Player player, InventoryContents contents, String configButtonName, String messageOnChange, String configValueName, boolean serverRestartRequired) {
		genericToggleButton(x, y, player, contents, configButtonName, messageOnChange, configValueName, new ArrayList<>(), serverRestartRequired);
	}
	
	public void genericToggleButton(int x, int y, Player player, InventoryContents contents, String configButtonName, String messageOnChange, String configValueName, List<String> configValsToDisable, boolean serverRestartRequired) {
		ItemStack button = XMaterial.BEDROCK.parseItem();
		if (main.getConfigManager().getMainConfig().getBoolean(configValueName)) {
			button = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			button = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta buttonMeta = button.getItemMeta();
		buttonMeta.setDisplayName(ChatColor.GOLD + refs.getMsg(configButtonName, player));
		button.setItemMeta(buttonMeta);
		contents.set(x, y, ClickableItem.of(button, e -> {
			if (!serverRestartRequired) {
				main.addPlayerUsingConfigurationGUI(player);
			} else {
				refs.sendFancyMsg("wwcConfigGUIToggleRestartRequired", new String[]{}, "&e", player);
			}
			main.getConfigManager().getMainConfig().set(configValueName,
					!(main.getConfigManager().getMainConfig().getBoolean(configValueName)));
            for (String eaKey : configValsToDisable) {
				if (eaKey.equals(configValueName)) continue;
                refs.debugMsg("Disabling " + eaKey + "!");
                main.getConfigManager().getMainConfig().set(eaKey, false);
            }
            final TextComponent successfulChange = Component.text()
							.content(refs.getMsg(messageOnChange, player))
							.color(NamedTextColor.GREEN)
					.build();
			refs.sendMsg(player, successfulChange);
			genericToggleButton(x, y, player, contents, configButtonName, messageOnChange, configValueName, configValsToDisable, serverRestartRequired);
		}));
	}
	
	public void genericConversationButton(int x, int y, Player player, InventoryContents contents, Prompt inPrompt, XMaterial inMaterial, String buttonName) {
		ConversationFactory genericConversation = new ConversationFactory(main).withModality(true).withTimeout(600)
				.withFirstPrompt(inPrompt);
		ItemStack button = inMaterial.parseItem();
		ItemMeta buttonMeta = button.getItemMeta();
		buttonMeta.setDisplayName(ChatColor.GOLD
				+ refs.getMsg(buttonName, player));
		button.setItemMeta(buttonMeta);
		contents.set(x, y, ClickableItem.of(button, e -> {
			if (!main.getCurrPlatform().equals("Folia")) {
				genericConversation.buildConversation(player).begin();
			} else {
				refs.sendFancyMsg("wwcNoConvoFolia", "", "&c", player);
			}
		}));
	}
	
	public void addGlowEffect(ItemMeta meta) {
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
	}

	/**
	 * Returns the generic conversation for modifying values in our config.yml using the GUI.
	 * @param preCheck - The boolean that needs to be true for the change to proceed
	 * @param context - The conversation context obj
	 * @param successfulChangeMsg - Names of the message sent on successful change
	 * @param configValName - The names of the config value to be updated
	 * @param configVal - The new value
	 * @param prevInventory - The previous inventory to open up after the conversation is over
	 * @return Prompt.END_OF_CONVERSATION - This will ultimately be returned to end the conversation. If the length of configValName != the length of configVal, then null is returned.
	 */
	public Prompt genericConfigConvo(boolean preCheck, ConversationContext context, String successfulChangeMsg, String[] configValName, Object[] configVal, SmartInventory prevInventory) {
		Player currPlayer = ((Player)context.getForWhom());
		if (configValName.length != configVal.length) {
			return null;
		}
		// TODO: Add a fail message potentially if the precheck fails?
		if (preCheck) {
			for (int i = 0; i < configValName.length; i++) {
				main.getConfigManager().getMainConfig().set(configValName[i], configVal[i]);
			}
			main.addPlayerUsingConfigurationGUI((currPlayer.getUniqueId()));
			final TextComponent successfulChange = Component.text()
					.content(refs.getMsg(successfulChangeMsg, currPlayer))
					.color(NamedTextColor.GREEN)
					.build();
			refs.sendMsg(currPlayer, successfulChange);
		}
		/* Re-open previous GUI */
		prevInventory.open((Player)context.getForWhom());
		return Prompt.END_OF_CONVERSATION;
	}

	/**
	 * Returns the generic conversation for modifying values in our config.yml using the GUI.
	 * @param preCheck - The boolean that needs to be true for the change to proceed
	 * @param context - The conversation context obj
	 * @param successfulChangeMsg - Name of the message sent on successful change
	 * @param configValName - The name of the config value to be updated
	 * @param configVal - The new value
	 * @param prevInventory - The previous inventory to open up after the conversation is over
	 * @return Prompt.END_OF_CONVERSATION - This will ultimately be returned to end the conversation.
	 */
	public Prompt genericConfigConvo(boolean preCheck, ConversationContext context, String successfulChangeMsg, String configValName, Object configVal, SmartInventory prevInventory) {
		return genericConfigConvo(preCheck, context, successfulChangeMsg, new String[] {configValName}, new Object[] {configVal}, prevInventory);
	}
}
