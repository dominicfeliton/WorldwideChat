package com.expl0itz.worldwidechat.inventory;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import org.bukkit.scheduler.BukkitRunnable;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WWCInventoryManager extends InventoryManager {
	
	private static WorldwideChat main = WorldwideChat.instance;
	
	public WWCInventoryManager(JavaPlugin plugin) {
		super(plugin);
	}
	
	public static void checkIfPlayerIsMissing(Player player, String targetPlayerUUID) {
		if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) == null) {
			// Target player no longer online
			player.closeInventory();
			final TextComponent targetPlayerDC = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctGUITargetPlayerNull"))
							.color(NamedTextColor.RED).decorate(TextDecoration.ITALIC))
					.build();
			CommonDefinitions.sendMessage(player, targetPlayerDC);
		}
	}
	
	public static void inventoryError(Player player, Exception e) {
		final TextComponent inventoryError = Component.text()
				.append(Component.text().content(
						CommonDefinitions.getMessage("wwcInventoryErrorPlayer"))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(player, inventoryError);
		main.getLogger().severe(CommonDefinitions.getMessage("wwcInventoryError", new String[] {player.getName()}));
		e.printStackTrace();
		player.closeInventory();
	}
	
	public static void setCommonButton(int x, int y, Player player, InventoryContents contents, String buttonType) {
		setCommonButton(x, y, player, contents, buttonType, new String[0]);
	}
	
	public static void setCommonButton(int x, int y, Player player, InventoryContents contents, String buttonType, Object[] args) {
		ItemStack pageButton = XMaterial.WHITE_STAINED_GLASS.parseItem();
		ItemMeta pageMeta = pageButton.getItemMeta();
		pageMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		if (buttonType.equalsIgnoreCase("Previous")) {
			pageButton = XMaterial.RED_STAINED_GLASS.parseItem();
			pageMeta.setDisplayName(ChatColor.RED
					+ CommonDefinitions.getMessage("wwcConfigGUIPreviousPageButton"));
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
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
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
					+ CommonDefinitions.getMessage("wwcGUIPageNumber", (Arrays.copyOf(args, args.length, String[].class))));
			if (args[0].equals("1")) {
				pageMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
			}
			pageButton.setItemMeta(pageMeta);
			contents.set(x, y, ClickableItem.of(pageButton, e -> {}));
		} else if (buttonType.equalsIgnoreCase("Quit")) {
			pageButton = XMaterial.BARRIER.parseItem();
			pageMeta = pageButton.getItemMeta();
			pageMeta.setDisplayName(ChatColor.RED
					+ CommonDefinitions.getMessage("wwcConfigGUIQuitButton"));
			pageButton.setItemMeta(pageMeta);
			contents.set(x, y, ClickableItem.of(pageButton, e -> {
				saveMainConfigAndReload(player, contents);
			}));
		} else {
			pageMeta.setDisplayName(ChatColor.RED + "Not a valid button! This is a bug, please report it.");
			pageButton.setItemMeta(pageMeta);
		}
	}

	private static void saveMainConfigAndReload(Player player, InventoryContents content) {
		main.removePlayerUsingConfigurationGUI(player);
		player.closeInventory();
		
		// Ensure that /wwcr is not ran while this is running
		main.setTranslatorName("Starting");
		
		if (!CommonDefinitions.serverIsStopping()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					// Save config sync/in the same thread because we are already in another thread thanks to Bukkit Scheduler
					main.getConfigManager().saveMainConfig(false);
					
					// Reload
					main.reload(player);
				}
			}.runTaskAsynchronously(main);	
		}
	}
	
	public static void setBorders(InventoryContents contents, XMaterial inMaterial) {
		ItemStack customBorders = inMaterial.parseItem();
		ItemMeta borderMeta = customBorders.getItemMeta();
		borderMeta.setDisplayName(" ");
		customBorders.setItemMeta(borderMeta);
		contents.fillBorders(ClickableItem.empty(customBorders));
	}
	
	public static void genericOpenSubmenuButton(int x, int y, Player player, InventoryContents contents, String buttonName, SmartInventory invToOpen) {
		genericOpenSubmenuButton(x, y, player, contents, null, buttonName, invToOpen);
	}
	
	public static void genericOpenSubmenuButton(int x, int y, Player player, InventoryContents contents, Object preCondition, String buttonName, SmartInventory invToOpen) {
		ItemStack button;
		if (preCondition != null && preCondition instanceof Boolean) {
			if ((Boolean) preCondition) {
				button = XMaterial.EMERALD_BLOCK.parseItem();
			} else {
				button = XMaterial.REDSTONE_BLOCK.parseItem();
			}
		} else {
			button = XMaterial.WRITABLE_BOOK.parseItem();
		}
		ItemMeta buttonMeta = button.getItemMeta();
		buttonMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage(buttonName));
		button.setItemMeta(buttonMeta);
		contents.set(x, y, ClickableItem.of(button, e -> {
			invToOpen.open(player);
		}));
	}
	
	public static void genericToggleButton(int x, int y, Player player, InventoryContents contents, String configButtonName, String messageOnChange, String configValueName) {
		genericToggleButton(x, y, player, contents, configButtonName, messageOnChange, configValueName, new String[0]);
	}
	
	public static void genericToggleButton(int x, int y, Player player, InventoryContents contents, String configButtonName, String messageOnChange, String configValueName, String[] configValsToDisable) {
		ItemStack button = XMaterial.BEDROCK.parseItem();
		if (main.getConfigManager().getMainConfig().getBoolean(configValueName)) {
			button = XMaterial.EMERALD_BLOCK.parseItem();
		} else {
			button = XMaterial.REDSTONE_BLOCK.parseItem();
		}
		ItemMeta buttonMeta = button.getItemMeta();
		buttonMeta.setDisplayName(ChatColor.GOLD + CommonDefinitions.getMessage(configButtonName));
		button.setItemMeta(buttonMeta);
		contents.set(x, y, ClickableItem.of(button, e -> {
			main.addPlayerUsingConfigurationGUI(player);
			main.getConfigManager().getMainConfig().set(configValueName,
					!(main.getConfigManager().getMainConfig().getBoolean(configValueName)));
			if (configValsToDisable.length > 0) {
				for (String eaKey : configValsToDisable) {
					main.getConfigManager().getMainConfig().set(eaKey, false);
				}
			}
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage(messageOnChange))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage(player, successfulChange);
			genericToggleButton(x, y, player, contents, configButtonName, messageOnChange, configValueName);
		}));
	}
	
	public static void genericConversationButton(int x, int y, Player player, InventoryContents contents, Prompt inPrompt, XMaterial inMaterial, String buttonName) {
		ConversationFactory genericConversation = new ConversationFactory(main).withModality(true).withTimeout(600)
				.withFirstPrompt(inPrompt);
		ItemStack button = inMaterial.parseItem();
		ItemMeta buttonMeta = button.getItemMeta();
		buttonMeta.setDisplayName(ChatColor.GOLD
				+ CommonDefinitions.getMessage(buttonName));
		button.setItemMeta(buttonMeta);
		contents.set(x, y, ClickableItem.of(button, e -> {
			genericConversation.buildConversation(player).begin();
		}));
	}
}
