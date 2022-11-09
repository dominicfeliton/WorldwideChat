package com.badskater0729.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.commands.WWCGlobal;
import com.badskater0729.worldwidechat.commands.WWCTranslate;
import com.badskater0729.worldwidechat.commands.WWCTranslateBook;
import com.badskater0729.worldwidechat.commands.WWCTranslateEntity;
import com.badskater0729.worldwidechat.commands.WWCTranslateItem;
import com.badskater0729.worldwidechat.commands.WWCTranslateSign;
import com.badskater0729.worldwidechat.conversations.wwctranslategui.PersonalRateLimitConvo;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonDefinitions;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class WWCTranslateGuiMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	private String targetPlayerUUID = "";

	public WWCTranslateGuiMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}

	/* Get translation info */
	public static SmartInventory getTranslateMainMenu(String targetPlayerUUID) {
		String playerTitle = "";
		if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			playerTitle = ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUIMainMenuGlobal");
		} else {
			playerTitle = ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUIMainMenuPlayer", new String[] {WorldwideChat.instance.getServer()
					.getPlayer(UUID.fromString(targetPlayerUUID)).getName()});
		}
		return SmartInventory.builder().id("translateMainMenu").provider(new WWCTranslateGuiMainMenu(targetPlayerUUID))
				.size(5, 9).manager(WorldwideChat.instance.getInventoryManager()).title(playerTitle).build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			if (main.isActiveTranslator(targetPlayerUUID)) {
				WWCInventoryManager.setBorders(contents, XMaterial.GREEN_STAINED_GLASS_PANE);
			}

			/* New translation button */
			ItemStack translationButton = XMaterial.COMPASS.parseItem();
			ItemMeta translationMeta = translationButton.getItemMeta();
			translationMeta.setDisplayName(
					CommonDefinitions.getMessage("wwctGUITranslationButton"));
			translationButton.setItemMeta(translationMeta);
			contents.set(2, 4, ClickableItem.of(translationButton, e -> {
				WWCTranslateGuiSourceLanguage.getSourceLanguageInventory("", targetPlayerUUID).open(player);
			}));

			/* Set active translator to our current target */
			ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			if (main.isActiveTranslator(targetPlayerUUID)) {
				/* Make compass enchanted */
				WWCInventoryManager.addGlowEffect(translationMeta);
				translationMeta.setDisplayName(CommonDefinitions.getMessage("wwctGUIExistingTranslationButton"));
				List<String> outLore = new ArrayList<>();
				outLore.add(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwctGUIExistingTranslationInput", new String[] {ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getInLangCode()}));
				outLore.add(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwctGUIExistingTranslationOutput", new String[] {ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getOutLangCode()}));
				translationMeta.setLore(outLore);
				translationButton.setItemMeta(translationMeta);

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
							.withFirstPrompt(new PersonalRateLimitConvo(targetTranslator));
					ItemStack rateButton = XMaterial.SLIME_BLOCK.parseItem();
					ItemMeta rateMeta = rateButton.getItemMeta();
					if (targetTranslator.getRateLimit() > 0) {
						WWCInventoryManager.addGlowEffect(rateMeta);
						rateMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIRateButton"));
					} else {
						rateMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIRateButton"));
					}
					rateButton.setItemMeta(rateMeta);
					contents.set(1, 1, ClickableItem.of(rateButton, e -> {
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
						WWCInventoryManager.addGlowEffect(bookMeta);
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
						WWCInventoryManager.addGlowEffect(signMeta);
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
						WWCInventoryManager.addGlowEffect(itemMeta);
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
						WWCInventoryManager.addGlowEffect(entityMeta);
						entityMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIEntityButton"));
					} else {
						entityMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIEntityButton"));
					}
					entityButton.setItemMeta(entityMeta);
					contents.set(2, 2, ClickableItem.of(entityButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateEntity translateEntity = new WWCTranslateEntity((CommandSender) player, null, null, args);
						translateEntity.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
				
				/* Chat Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")
						&& ((targetPlayerUUID.equals(player.getUniqueId().toString()) && (player.hasPermission("worldwidechat.wwctco") || player.hasPermission("worldwidechat.wwctci"))) 
								|| (!targetPlayerUUID.equals(player.getUniqueId().toString()) && (player.hasPermission("worldwidechat.wwctco.otherplayers") || player.hasPermission("worldwidechat.wwctci.otherplayers"))))) {
					ItemStack chatButton = XMaterial.PAINTING.parseItem();
					ItemMeta chatMeta = chatButton.getItemMeta();
					if (targetTranslator.getTranslatingChatOutgoing() || targetTranslator.getTranslatingChatIncoming()) {
						WWCInventoryManager.addGlowEffect(chatMeta);
						List<String> outLoreChat = new ArrayList<>();
						outLoreChat.add(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwctGUIExistingChatIncomingEnabled", new String[] {ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getTranslatingChatIncoming()}));
						outLoreChat.add(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwctGUIExistingChatOutgoingEnabled", new String[] {ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getTranslatingChatOutgoing()}));
						chatMeta.setLore(outLoreChat);
						chatMeta.setDisplayName(ChatColor.GREEN
								+ CommonDefinitions.getMessage("wwctGUIChatButton"));
					} else {
						chatMeta.setDisplayName(ChatColor.YELLOW
								+ CommonDefinitions.getMessage("wwctGUIChatButton"));
					}
					chatButton.setItemMeta(chatMeta);
					contents.set(3, 4, ClickableItem.of(chatButton, e -> {
						WWCTranslateGuiChatMenu.getTranslateChatMenu(targetPlayerUUID).open(player);
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
