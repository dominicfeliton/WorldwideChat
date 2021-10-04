package com.expl0itz.worldwidechat.inventory.wwctranslategui;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
import com.expl0itz.worldwidechat.commands.WWCTranslateChat;
import com.expl0itz.worldwidechat.commands.WWCTranslateEntity;
import com.expl0itz.worldwidechat.commands.WWCTranslateItem;
import com.expl0itz.worldwidechat.commands.WWCTranslateSign;
import com.expl0itz.worldwidechat.conversations.wwctranslategui.RateLimitConversation;
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
				.size(5, 9).manager(WorldwideChat.getInstance().getInventoryManager()).title(playerTitle).build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive */
			ItemStack customDefaultBorders = XMaterial.WHITE_STAINED_GLASS_PANE.parseItem();
			ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
			defaultBorderMeta.setDisplayName(" ");
			customDefaultBorders.setItemMeta(defaultBorderMeta);
			contents.fillBorders(ClickableItem.empty(customDefaultBorders));

			/* New translation button */
			ItemStack translationButton = XMaterial.COMPASS.parseItem();
			ItemMeta translationMeta = translationButton.getItemMeta();
			translationMeta.setDisplayName(
					CommonDefinitions.getMessage("wwctGUITranslationButton"));
			translationButton.setItemMeta(translationMeta);
			contents.set(2, 4, ClickableItem.of(translationButton, e -> {
				WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", targetPlayerUUID).open(player);
			}));

			/* Set active translator to our current target */
			ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			if (!main.getActiveTranslator(targetPlayerUUID).getUUID().equals("")) {
				/* Make compass enchanted */
				translationMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				translationMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
				translationMeta.setDisplayName(CommonDefinitions.getMessage("wwctGUIExistingTranslationButton"));
				translationButton.setItemMeta(translationMeta);

				/* Green stained glass borders for active */
				ItemStack customBorders = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem();
				ItemMeta borderMeta = customBorders.getItemMeta();
				borderMeta.setDisplayName(" ");
				customBorders.setItemMeta(borderMeta);
				contents.fillBorders(ClickableItem.empty(customBorders));

				/* Stop Button: Stop translation if active */
				ItemStack stopButton = XMaterial.BARRIER.parseItem();
				ItemMeta stopMeta = stopButton.getItemMeta();
				stopMeta.setDisplayName(ChatColor.RED
						+ CommonDefinitions.getMessage("wwctGUIStopButton"));
				stopButton.setItemMeta(stopMeta);
				contents.set(1, 4, ClickableItem.of(stopButton, e -> {
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
					ItemStack rateButton = XMaterial.SLIME_BLOCK.parseItem();
					ItemMeta rateMeta = rateButton.getItemMeta();
					if (targetTranslator.getRateLimit() > 0) {
						rateMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						rateMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
						rateMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIRateButton"));
					} else {
						rateMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIRateButton"));
					}
					rateButton.setItemMeta(rateMeta);
					contents.set(2, 2, ClickableItem.of(rateButton, e -> {
						rateConvo.buildConversation(player).begin();
					}));
				}

				/* Book Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctb")
						&& (player.hasPermission("worldwidechat.wwctb.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack bookButton = XMaterial.WRITABLE_BOOK.parseItem();
					ItemMeta bookMeta = bookButton.getItemMeta();
					if (targetTranslator.getTranslatingBook()) {
						bookMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIBookButton"));
						bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						bookMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
					} else {
						bookMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIBookButton"));
					}
					bookButton.setItemMeta(bookMeta);
					contents.set(2, 1, ClickableItem.of(bookButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender) player, null, null, args);
						translateBook.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}

				/* Sign Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcts")
						&& (player.hasPermission("worldwidechat.wwcts.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					/* Init item, ensure pre-1.14 compatibility */
					ItemStack signButton = XMaterial.OAK_SIGN.parseItem();
					ItemMeta signMeta = signButton.getItemMeta();
					if (targetTranslator.getTranslatingSign()) {
						signMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUISignButton"));
						signMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						signMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
					} else {
						signMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUISignButton"));
					}
					signButton.setItemMeta(signMeta);
					contents.set(2, 7, ClickableItem.of(signButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender) player, null, null, args);
						translateSign.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}

				/* Item Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcti")
						&& (player.hasPermission("worldwidechat.wwcti.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack itemButton = XMaterial.GRASS_BLOCK.parseItem();
					ItemMeta itemMeta = itemButton.getItemMeta();
					if (targetTranslator.getTranslatingItem()) {
						itemMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIItemButton"));
						itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						itemMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
					} else {
						itemMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIItemButton"));
					}
					itemButton.setItemMeta(itemMeta);
					contents.set(2, 6, ClickableItem.of(itemButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateItem translateItem = new WWCTranslateItem((CommandSender) player, null, null, args);
						translateItem.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
				
				/* Entity Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcte")
						&& (player.hasPermission("worldwidechat.wwcte.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack entityButton = XMaterial.NAME_TAG.parseItem();
					ItemMeta entityMeta = entityButton.getItemMeta();
					if (targetTranslator.getTranslatingEntity()) {
						entityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						entityMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
						entityMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIEntityButton"));
					} else {
						entityMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIEntityButton"));
					}
					entityButton.setItemMeta(entityMeta);
					contents.set(1, 1, ClickableItem.of(entityButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateEntity translateEntity = new WWCTranslateEntity((CommandSender) player, null, null, args);
						translateEntity.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
				
				/* Chat Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctc")
						&& (player.hasPermission("worldwidechat.wwctc.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack chatButton = XMaterial.PAINTING.parseItem();
					ItemMeta chatMeta = chatButton.getItemMeta();
					if (targetTranslator.getTranslatingChat()) {
						chatMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						chatMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().parseEnchantment(), 1, false);
						chatMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIChatButton"));
					} else {
						chatMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIChatButton"));
					}
					chatButton.setItemMeta(chatMeta);
					contents.set(3, 4, ClickableItem.of(chatButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateChat translateChat = new WWCTranslateChat((CommandSender) player, null, null, args);
						translateChat.processCommand();
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
