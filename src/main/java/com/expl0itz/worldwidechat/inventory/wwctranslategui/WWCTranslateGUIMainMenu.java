package com.expl0itz.worldwidechat.inventory.wwctranslategui;

import java.util.UUID;

import org.bukkit.Bukkit;
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
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
import com.expl0itz.worldwidechat.commands.WWCTranslateItem;
import com.expl0itz.worldwidechat.commands.WWCTranslateSign;
import com.expl0itz.worldwidechat.conversations.wwctranslategui.RateLimitConversation;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;
import com.expl0itz.worldwidechat.util.ActiveTranslator;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WWCTranslateGUIMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();

	private String targetPlayerUUID = null;

	public WWCTranslateGUIMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}

	/* Get translation info */
	public static SmartInventory getTranslateMainMenu(String targetPlayerUUID) {
		if (targetPlayerUUID == null) {
			return SmartInventory
					.builder().id("translateMainMenu").provider(new WWCTranslateGUIMainMenu(null)).size(6, 9)
					.manager(WorldwideChat.getInstance().getInventoryManager()).title(ChatColor.BLUE + WorldwideChat
							.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUIMainMenu"))
					.build();
		}
		String playerTitle = "";
		if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			playerTitle = ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig()
					.getString("Messages.wwctGUIMainMenuGlobal");
		} else {
			playerTitle = ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig()
					.getString("Messages.wwctGUIMainMenuPlayer").replace("%i", WorldwideChat.getInstance().getServer()
							.getPlayer(UUID.fromString(targetPlayerUUID)).getName());
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
					main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUITranslationButton"));
			translationButton.setItemMeta(translationMeta);
			contents.set(3, 4, ClickableItem.of(translationButton, e -> {
				if (targetPlayerUUID == null) {
					WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", null).open(player);
				} else {
					WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", targetPlayerUUID).open(player);
				}
			}));

			/* If target player exists, is active player */
			if (targetPlayerUUID != null && main.getActiveTranslator(targetPlayerUUID) != null) {
				ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);

				/* Make compass enchanted */
				EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
				translationMeta.addEnchant(glow, 1, true);
				translationMeta.setDisplayName(main.getConfigManager().getMessagesConfig()
						.getString("Messages.wwctGUIExistingTranslationButton"));
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
						+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIStopButton"));
				stopButton.setItemMeta(stopMeta);
				contents.set(2, 4, ClickableItem.of(stopButton, e -> {
					String[] args;
					if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						args = new String[] { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(),
								"stop" };
					} else {
						args = new String[] { "stop" };
					}
					WWCTranslate translate = new WWCTranslate((CommandSender) player, null, null, args);
					if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						translate.processCommand(true);
					} else {
						translate.processCommand(false);
					}
					getTranslateMainMenu(targetPlayerUUID).open(player);
				}));

				/* Rate Limit Button: Set a rate limit for the current translator */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctrl")
						&& player.hasPermission("worldwidechat.wwctrl.otherplayers")) {
					ConversationFactory rateConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new RateLimitConversation(main.getActiveTranslator(targetPlayerUUID)));
					ItemStack rateButton = new ItemStack(Material.SLIME_BLOCK);
					ItemMeta rateMeta = rateButton.getItemMeta();
					rateMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIRateButton"));
					if (targetTranslator.getRateLimit() > 0) {
						rateMeta.addEnchant(glow, 1, true);
					}
					rateButton.setItemMeta(rateMeta);
					contents.set(3, 2, ClickableItem.of(rateButton, e -> {
						rateConvo.buildConversation(player).begin();
					}));
				}

				/* Book Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctb")
						&& player.hasPermission("worldwidechat.wwctb.otherplayers")) {
					ItemStack bookButton = new ItemStack(Material.WRITABLE_BOOK);
					ItemMeta bookMeta = bookButton.getItemMeta();
					bookMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIBookButton"));
					if (targetTranslator.getTranslatingBook()) {
						bookMeta.addEnchant(glow, 1, true);
					}
					bookButton.setItemMeta(bookMeta);
					contents.set(3, 1, ClickableItem.of(bookButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender) player, null, null, args);
						translateBook.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
						// TODO: check if we need to open the menu again? (last line)
					}));
				}

				/* Sign Translation Button */
				if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcts")
						&& player.hasPermission("worldwidechat.wwcts.otherplayers")) {
					ItemStack signButton = new ItemStack(Material.OAK_SIGN);
					ItemMeta signMeta = signButton.getItemMeta();
					signMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUISignButton"));
					if (targetTranslator.getTranslatingSign()) {
						signMeta.addEnchant(glow, 1, true);
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
						&& player.hasPermission("worldwidechat.wwcti.otherplayers")) {
					ItemStack itemButton = new ItemStack(Material.NAME_TAG);
					ItemMeta itemMeta = itemButton.getItemMeta();
					itemMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIItemButton"));
					if (targetTranslator.getTranslatingItem()) {
						itemMeta.addEnchant(glow, 1, true);
					}
					itemButton.setItemMeta(itemMeta);
					contents.set(3, 6, ClickableItem.of(itemButton, e -> {
						String[] args = { main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName() };
						WWCTranslateItem translateItem = new WWCTranslateItem((CommandSender) player, null, null, args);
						translateItem.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
				/* Current player is active translator + target */
			} else if (targetPlayerUUID == null && main.getActiveTranslator(player.getUniqueId().toString()) != null) {
				ActiveTranslator currTranslator = main.getActiveTranslator(player.getUniqueId().toString());

				/* Make compass enchanted */
				EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
				translationMeta.addEnchant(glow, 1, true);
				translationMeta.setDisplayName(main.getConfigManager().getMessagesConfig()
						.getString("Messages.wwctGUIExistingTranslationButton"));
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
						+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIStopButton"));
				stopButton.setItemMeta(stopMeta);
				contents.set(2, 4, ClickableItem.of(stopButton, e -> {
					String[] args = { "stop" };
					WWCTranslate translate = new WWCTranslate((CommandSender) player, null, null, args);
					translate.processCommand(false);
					getTranslateMainMenu(targetPlayerUUID).open(player);
				}));

				/* Rate Limit Button */
				if (player.hasPermission("worldwidechat.wwctrl")) {
					ConversationFactory rateConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new RateLimitConversation(currTranslator));
					ItemStack rateButton = new ItemStack(Material.SLIME_BLOCK);
					ItemMeta rateMeta = rateButton.getItemMeta();
					rateMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIRateButton"));
					if (currTranslator.getRateLimit() > 0) {
						rateMeta.addEnchant(glow, 1, true);
					}
					rateButton.setItemMeta(rateMeta);
					contents.set(3, 2, ClickableItem.of(rateButton, e -> {
						rateConvo.buildConversation(player).begin();
					}));
				}

				/* Book Translation Button */
				if (player.hasPermission("worldwidechat.wwctb")) {
					ItemStack bookButton = new ItemStack(Material.WRITABLE_BOOK);
					ItemMeta bookMeta = bookButton.getItemMeta();
					bookMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIBookButton"));
					if (currTranslator.getTranslatingBook()) {
						bookMeta.addEnchant(glow, 1, true);
					}
					bookButton.setItemMeta(bookMeta);
					contents.set(3, 1, ClickableItem.of(bookButton, e -> {
						String[] args = {};
						WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender) player, null, null, args);
						translateBook.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}

				/* Sign Translation Button */
				if (player.hasPermission("worldwidechat.wwcts")) {
					ItemStack signButton = new ItemStack(Material.OAK_SIGN);
					ItemMeta signMeta = signButton.getItemMeta();
					signMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUISignButton"));
					if (currTranslator.getTranslatingSign()) {
						signMeta.addEnchant(glow, 1, true);
					}
					signButton.setItemMeta(signMeta);
					contents.set(3, 7, ClickableItem.of(signButton, e -> {
						String[] args = {};
						WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender) player, null, null, args);
						translateSign.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}

				/* Item Translation Button */
				if (player.hasPermission("worldwidechat.wwcti")) {
					ItemStack itemButton = new ItemStack(Material.NAME_TAG);
					ItemMeta itemMeta = itemButton.getItemMeta();
					itemMeta.setDisplayName(ChatColor.GREEN
							+ main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIItemButton"));
					if (currTranslator.getTranslatingItem()) {
						itemMeta.addEnchant(glow, 1, true);
					}
					itemButton.setItemMeta(itemMeta);
					contents.set(3, 6, ClickableItem.of(itemButton, e -> {
						String[] args = {};
						WWCTranslateItem translateItem = new WWCTranslateItem((CommandSender) player, null, null, args);
						translateItem.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
				}
			} else { /* Current player exists */
				return;
			}
		} catch (Exception e) {
			final TextComponent inventoryError = Component.text().append(main.getPluginPrefix().asComponent())
					.append(Component.text().content(
							main.getConfigManager().getMessagesConfig().getString("Messages.wwcInventoryErrorPlayer"))
							.color(NamedTextColor.RED))
					.build();
			main.adventure().sender(player).sendMessage(inventoryError);
			main.getLogger().severe(main.getConfigManager().getMessagesConfig().getString("Messages.wwcInventoryError")
					.replace("%i", player.getName()));
			e.printStackTrace();
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		if (targetPlayerUUID != null && !targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			if (Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) == null) {
				// Target player no longer online
				player.closeInventory();
				final TextComponent targetPlayerDC = Component.text().append(main.getPluginPrefix().asComponent())
						.append(Component.text()
								.content(main.getConfigManager().getMessagesConfig()
										.getString("Messages.wwctGUITargetPlayerNull"))
								.color(NamedTextColor.RED).decorate(TextDecoration.ITALIC))
						.build();
				main.adventure().sender(player).sendMessage(targetPlayerDC);
			}
		}
	}
}
