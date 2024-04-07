package com.badskater0729.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.badskater0729.worldwidechat.util.CommonRefs;
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
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class WWCTranslateGuiMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();

	private String targetPlayerUUID = "";

	public WWCTranslateGuiMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}

	/* Get translation info */
	// TODO: Rewrite for being able to switch between localizations??
	// TODO: Also fix getMsg calls to respect user's localization
	public SmartInventory getTranslateMainMenu() {
		String playerTitle = "";
		if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			playerTitle = ChatColor.BLUE + refs.getMsg("wwctGUIMainMenuGlobal", null);
		} else {
			playerTitle = ChatColor.BLUE + refs.getMsg("wwctGUIMainMenuPlayer", WorldwideChat.instance.getServer()
					.getPlayer(UUID.fromString(targetPlayerUUID)).getName(), null);
		}
		return SmartInventory.builder().id("translateMainMenu").provider(new WWCTranslateGuiMainMenu(targetPlayerUUID))
				.size(5, 9).manager(WorldwideChat.instance.getInventoryManager()).title(playerTitle).build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive */
			invManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			if (main.isActiveTranslator(targetPlayerUUID)) {
				invManager.setBorders(contents, XMaterial.GREEN_STAINED_GLASS_PANE);
			}

			/* New translation button */
			ItemStack translationButton = XMaterial.COMPASS.parseItem();
			ItemMeta translationMeta = translationButton.getItemMeta();
			translationMeta.setDisplayName(
					refs.getMsg("wwctGUITranslationButton", null));
			translationButton.setItemMeta(translationMeta);
			contents.set(2, 4, ClickableItem.of(translationButton, e -> {
				new WWCTranslateGuiSourceLanguage("", targetPlayerUUID).getSourceLanguageInventory().open(player);
			}));

			/* Set active translator to our current target */
			ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			if (main.isActiveTranslator(targetPlayerUUID)) {
				/* Make compass enchanted */
				invManager.addGlowEffect(translationMeta);
				translationMeta.setDisplayName(refs.getMsg("wwctGUIExistingTranslationButton", null));
				List<String> outLore = new ArrayList<>();
				outLore.add(ChatColor.LIGHT_PURPLE + refs.getMsg("wwctGUIExistingTranslationInput", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getInLangCode(), null));
				outLore.add(ChatColor.LIGHT_PURPLE + refs.getMsg("wwctGUIExistingTranslationOutput", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getOutLangCode(), null));
				translationMeta.setLore(outLore);
				translationButton.setItemMeta(translationMeta);

				/* Stop Button: Stop translation if active */
				ItemStack stopButton = XMaterial.BARRIER.parseItem();
				ItemMeta stopMeta = stopButton.getItemMeta();
				stopMeta.setDisplayName(ChatColor.RED
						+ refs.getMsg("wwctGUIStopButton", null));
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
					getTranslateMainMenu().open(player);
				}));

				/* Rate Limit Button: Set a rate limit for the current translator */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctrl")
						&& (player.hasPermission("worldwidechat.wwctrl.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ConversationFactory rateConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new PersonalRateLimitConvo(targetTranslator));
					ItemStack rateButton = XMaterial.SLIME_BLOCK.parseItem();
					ItemMeta rateMeta = rateButton.getItemMeta();
					if (targetTranslator.getRateLimit() > 0) {
						invManager.addGlowEffect(rateMeta);
						rateMeta.setDisplayName(ChatColor.GREEN
								+ refs.getMsg("wwctGUIRateButton", null));
					} else {
						rateMeta.setDisplayName(ChatColor.YELLOW
								+ refs.getMsg("wwctGUIRateButton", null));
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
								+ refs.getMsg("wwctGUIBookButton", null));
						invManager.addGlowEffect(bookMeta);
					} else {
						bookMeta.setDisplayName(ChatColor.YELLOW
								+ refs.getMsg("wwctGUIBookButton", null));
					}
					bookButton.setItemMeta(bookMeta);
					contents.set(2, 1, ClickableItem.of(bookButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender) player, null, null, args);
						translateBook.processCommand();
						getTranslateMainMenu().open(player);
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
								+ refs.getMsg("wwctGUISignButton", null));
						invManager.addGlowEffect(signMeta);
					} else {
						signMeta.setDisplayName(ChatColor.YELLOW
								+ refs.getMsg("wwctGUISignButton", null));
					}
					signButton.setItemMeta(signMeta);
					contents.set(2, 7, ClickableItem.of(signButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender) player, null, null, args);
						translateSign.processCommand();
						getTranslateMainMenu().open(player);
					}));
				}

				/* Item Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcti")
						&& (player.hasPermission("worldwidechat.wwcti.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack itemButton = XMaterial.GRASS_BLOCK.parseItem();
					ItemMeta itemMeta = itemButton.getItemMeta();
					if (targetTranslator.getTranslatingItem()) {
						itemMeta.setDisplayName(ChatColor.GREEN
								+ refs.getMsg("wwctGUIItemButton", null));
						invManager.addGlowEffect(itemMeta);
					} else {
						itemMeta.setDisplayName(ChatColor.YELLOW
								+ refs.getMsg("wwctGUIItemButton", null));
					}
					itemButton.setItemMeta(itemMeta);
					contents.set(2, 6, ClickableItem.of(itemButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateItem translateItem = new WWCTranslateItem((CommandSender) player, null, null, args);
						translateItem.processCommand();
						getTranslateMainMenu().open(player);
					}));
				}
				
				/* Entity Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcte")
						&& (player.hasPermission("worldwidechat.wwcte.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
					ItemStack entityButton = XMaterial.NAME_TAG.parseItem();
					ItemMeta entityMeta = entityButton.getItemMeta();
					if (targetTranslator.getTranslatingEntity()) {
						invManager.addGlowEffect(entityMeta);
						entityMeta.setDisplayName(ChatColor.GREEN
								+ refs.getMsg("wwctGUIEntityButton", null));
					} else {
						entityMeta.setDisplayName(ChatColor.YELLOW
								+ refs.getMsg("wwctGUIEntityButton", null));
					}
					entityButton.setItemMeta(entityMeta);
					contents.set(2, 2, ClickableItem.of(entityButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateEntity translateEntity = new WWCTranslateEntity((CommandSender) player, null, null, args);
						translateEntity.processCommand();
						getTranslateMainMenu().open(player);
					}));
				}
				
				/* Chat Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")
						&& ((targetPlayerUUID.equals(player.getUniqueId().toString()) && (player.hasPermission("worldwidechat.wwctco") || player.hasPermission("worldwidechat.wwctci"))) 
								|| (!targetPlayerUUID.equals(player.getUniqueId().toString()) && (player.hasPermission("worldwidechat.wwctco.otherplayers") || player.hasPermission("worldwidechat.wwctci.otherplayers"))))) {
					ItemStack chatButton = XMaterial.PAINTING.parseItem();
					ItemMeta chatMeta = chatButton.getItemMeta();
					if (targetTranslator.getTranslatingChatOutgoing() || targetTranslator.getTranslatingChatIncoming()) {
						invManager.addGlowEffect(chatMeta);
						List<String> outLoreChat = new ArrayList<>();
						outLoreChat.add(ChatColor.LIGHT_PURPLE + refs.getMsg("wwctGUIExistingChatIncomingEnabled", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getTranslatingChatIncoming(), null));
						outLoreChat.add(ChatColor.LIGHT_PURPLE + refs.getMsg("wwctGUIExistingChatOutgoingEnabled", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + targetTranslator.getTranslatingChatOutgoing(), null));
						chatMeta.setLore(outLoreChat);
						chatMeta.setDisplayName(ChatColor.GREEN
								+ refs.getMsg("wwctGUIChatButton", null));
					} else {
						chatMeta.setDisplayName(ChatColor.YELLOW
								+ refs.getMsg("wwctGUIChatButton", null));
					}
					chatButton.setItemMeta(chatMeta);
					contents.set(3, 4, ClickableItem.of(chatButton, e -> {
						new WWCTranslateGuiChatMenu(targetPlayerUUID).getTranslateChatMenu().open(player);
					}));
				}
			}
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		invManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}
}
