package com.expl0itz.worldwidechat.inventory.wwctranslategui;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
import com.expl0itz.worldwidechat.commands.WWCTranslateEntity;
import com.expl0itz.worldwidechat.commands.WWCTranslateItem;
import com.expl0itz.worldwidechat.commands.WWCTranslateSign;
import com.expl0itz.worldwidechat.conversations.wwctranslategui.RateLimitConversation;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class WWCTranslateGUIMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();

	private String targetPlayerUUID = "";

	public WWCTranslateGUIMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}

	/* Get translation info */
	public static SmartInventory getTranslateMainMenu(String targetPlayerUUID) {
		String playerTitle = "";
		if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			playerTitle = ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUIMainMenuGlobal");
		} else {
			playerTitle = ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUIMainMenuPlayer", new String[] {WorldwideChat.getInstance().getServer()
					.getPlayer(UUID.fromString(targetPlayerUUID)).getName()});
		}
		return SmartInventory.builder().id("translateMainMenu").provider(new WWCTranslateGUIMainMenu(targetPlayerUUID))
				.size(6, 9).manager(WorldwideChat.getInstance().getInventoryManager()).title(playerTitle).build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive */
			ItemStack customDefaultBorders = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
			ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
			defaultBorderMeta.setDisplayName(" ");
			customDefaultBorders.setItemMeta(defaultBorderMeta);
			contents.fillBorders(ClickableItem.empty(customDefaultBorders));

			/* New translation button */
			ItemStack translationButton = new ItemStack(Material.COMPASS);
			ItemMeta translationMeta = translationButton.getItemMeta();
			translationMeta.setDisplayName(
					CommonDefinitions.getMessage("wwctGUITranslationButton"));
			translationButton.setItemMeta(translationMeta);
			contents.set(3, 4, ClickableItem.of(translationButton, e -> {
				WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", targetPlayerUUID).open(player);
			}));

			/* Set active translator to our current target */
			ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			if (!main.getActiveTranslator(targetPlayerUUID).getUUID().equals("")) {
				/* Make compass enchanted */
				EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
				translationMeta.addEnchant(glow, 1, true);
				translationMeta.setDisplayName(CommonDefinitions.getMessage("wwctGUIExistingTranslationButton"));
				translationMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				translationButton.setItemMeta(translationMeta);

				/* Green stained glass borders for active */
				ItemStack customBorders = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				ItemMeta borderMeta = customBorders.getItemMeta();
				borderMeta.setDisplayName(" ");
				customBorders.setItemMeta(borderMeta);
				contents.fillBorders(ClickableItem.empty(customBorders));

				/* Stop Button: Stop translation if active */
				ItemStack stopButton = new ItemStack(Material.BARRIER);
				ItemMeta stopMeta = stopButton.getItemMeta();
				stopMeta.setDisplayName(ChatColor.RED
						+ CommonDefinitions.getMessage("wwctGUIStopButton"));
				stopButton.setItemMeta(stopMeta);
				contents.set(2, 4, ClickableItem.of(stopButton, e -> {
					String[] args;
					if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						args = new String[] { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(),
								"stop" };
					} else {
						args = new String[] { "stop" };
					}
					if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						WWCTranslate translate = new WWCGlobal((CommandSender) player, null, null, args);
						translate.processCommand();
					} else {
						WWCTranslate translate = new WWCTranslate((CommandSender) player, null, null, args);
						translate.processCommand();
					}
					getTranslateMainMenu(targetPlayerUUID).open(player);
				}));

				/* Rate Limit Button: Set a rate limit for the current translator */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctrl")
						&& (player.hasPermission("worldwidechat.wwctrl.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ConversationFactory rateConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new RateLimitConversation(targetTranslator));
					ItemStack rateButton = new ItemStack(Material.SLIME_BLOCK);
					ItemMeta rateMeta = rateButton.getItemMeta();
					if (targetTranslator.getRateLimit() > 0) {
						rateMeta.addEnchant(glow, 1, true);
						rateMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIRateButton"));
					} else {
						rateMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIRateButton"));
					}
					rateButton.setItemMeta(rateMeta);
					contents.set(3, 2, ClickableItem.of(rateButton, e -> {
						rateConvo.buildConversation(player).begin();
					}));
				}

				/* Book Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctb")
						&& (player.hasPermission("worldwidechat.wwctb.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack bookButton = new ItemStack(Material.WRITABLE_BOOK);
					ItemMeta bookMeta = bookButton.getItemMeta();
					if (targetTranslator.getTranslatingBook()) {
						bookMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIBookButton"));
						bookMeta.addEnchant(glow, 1, true);
					} else {
						bookMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIBookButton"));
					}
					bookButton.setItemMeta(bookMeta);
					contents.set(3, 1, ClickableItem.of(bookButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender) player, null, null, args);
						translateBook.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}

				/* Sign Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcts")
						&& (player.hasPermission("worldwidechat.wwcts.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack signButton = new ItemStack(Material.OAK_SIGN);
					ItemMeta signMeta = signButton.getItemMeta();
					if (targetTranslator.getTranslatingSign()) {
						signMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUISignButton"));
						signMeta.addEnchant(glow, 1, true);
					} else {
						signMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUISignButton"));
					}
					signButton.setItemMeta(signMeta);
					contents.set(3, 7, ClickableItem.of(signButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender) player, null, null, args);
						translateSign.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}

				/* Item Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcti")
						&& (player.hasPermission("worldwidechat.wwcti.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack itemButton = new ItemStack(Material.GRASS_BLOCK);
					ItemMeta itemMeta = itemButton.getItemMeta();
					if (targetTranslator.getTranslatingItem()) {
						itemMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIItemButton"));
						itemMeta.addEnchant(glow, 1, true);
					} else {
						itemMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIItemButton"));
					}
					itemButton.setItemMeta(itemMeta);
					contents.set(3, 6, ClickableItem.of(itemButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateItem translateItem = new WWCTranslateItem((CommandSender) player, null, null, args);
						translateItem.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
				
				/* Entity Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcte")
						&& (player.hasPermission("worldwidechat.wwcte.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack entityButton = new ItemStack(Material.NAME_TAG);
					ItemMeta entityMeta = entityButton.getItemMeta();
					if (targetTranslator.getTranslatingEntity()) {
						entityMeta.addEnchant(glow, 1, true);
						entityMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIEntityButton"));
					} else {
						entityMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIEntityButton"));
					}
					entityButton.setItemMeta(entityMeta);
					contents.set(2, 1, ClickableItem.of(entityButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateEntity translateEntity = new WWCTranslateEntity((CommandSender) player, null, null, args);
						translateEntity.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
			}
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		WWCInventoryManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}
}
