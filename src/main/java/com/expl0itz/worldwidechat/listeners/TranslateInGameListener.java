package com.expl0itz.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.TempItemInventory;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TranslateInGameListener implements Listener {

	private WorldwideChat main = WorldwideChat.getInstance();

	/* Custom Entity Name Translation */
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onInGameEntityTranslateRequest(PlayerInteractEntityEvent event) {
		if (!main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getUUID().equals("") && event.getHand().equals(EquipmentSlot.HAND)) {
			try {
				/* Entity Names */
				if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingEntity()) {
					if (event.getRightClicked().isValid()) {
						final String customName = event.getRightClicked().getCustomName();
						new BukkitRunnable() {
							@Override
							public void run() {
								final TextComponent entityStart = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcEntityTranslateStart"))
												.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), entityStart);
								if (customName != null) {
									final TextComponent entityDone = Component.text()
											.append(Component.text()
													.content(CommonDefinitions.getMessage("wwcEntityTranslateDone", new String[] {CommonDefinitions.translateText(customName, event.getPlayer())}))
													.color(NamedTextColor.GREEN))
											.build();
									CommonDefinitions.sendMessage(event.getPlayer(), entityDone);
								} else {
									final TextComponent entityStock = Component.text()
											.append(Component.text()
													.content(CommonDefinitions.getMessage("wwcEntityTranslateNoName"))
													.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
											.build();
									CommonDefinitions.sendMessage(event.getPlayer(), entityStock);
								}
							}
						}.runTaskAsynchronously(main);
					}
				}	
			} catch (Exception e) {
				final TextComponent translationFailMsg = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcInGameEntityTranslationFailCatastrophic"))
								.color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), translationFailMsg);
			}
		}
	}
	
	/* Items (+ Lore), Books, Signs Translation */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInGameObjTranslateRequest(PlayerInteractEvent event) {
		if (!main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getUUID().equals("")) {
			try {
				/* Book Translation */
				if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingBook()
					&& event.getHand() != null && event.getItem() != null
					&& Material.WRITTEN_BOOK == event.getItem().getType()
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					ItemStack currentBook = event.getItem().clone();
					new BukkitRunnable() {
						@Override
						public void run() {
							/* Init vars */
							BookMeta meta = (BookMeta) currentBook.getItemMeta();
							String outTitle = meta.getTitle();
							List<String> pages = meta.getPages();
							List<String> translatedPages = new ArrayList<String>();
							boolean sameResult = false;

							/* Send message */
							final TextComponent bookStart = Component.text()
									.append(Component.text()
											.content(CommonDefinitions.getMessage("wwcBookTranslateStart"))
											.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
									.build();
							CommonDefinitions.sendMessage(event.getPlayer(), bookStart);

							/* Translate title */
							outTitle = CommonDefinitions.translateText(meta.getTitle(), event.getPlayer());
							if (!outTitle.equals(meta.getTitle())) {
								final TextComponent bookTitleSuccess = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcBookTranslateTitleSuccess", new String[] {meta.getTitle()}))
												.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), bookTitleSuccess);
							} else {
								final TextComponent bookTitleFail = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcBookTranslateTitleFail"))
												.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), bookTitleFail);
							}

							/* Translate pages */
							for (String eaPage : pages) {
								String out = CommonDefinitions.translateText(eaPage, event.getPlayer());
								if (out.equals("") || out.equalsIgnoreCase(eaPage)) {
									sameResult = true;
									out = CommonDefinitions.getMessage("Messages.wwctbTranslatePageFail", new String[] {eaPage});
								}
								translatedPages.add(out);
							}

							if (!sameResult && currentBook != null) {
								/* Set completed message */
								final TextComponent bookDone = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcBookDone"))
												.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), bookDone);
							} else if (sameResult) {
								/* If we are here, one or more translations was unsuccessful */
								final TextComponent translationNoticeMsg = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcBookTranslationFail"))
												.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), translationNoticeMsg);
							}

							/* Create the modified book */
							ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
							BookMeta newMeta = (BookMeta) newBook.getItemMeta();
							newMeta.setAuthor(meta.getAuthor());
							newMeta.setGeneration(meta.getGeneration());
							newMeta.setTitle(outTitle);
							newMeta.setPages(translatedPages);
							newBook.setItemMeta(newMeta);

							new BukkitRunnable() {
								@Override
								public void run() {
									event.getPlayer().openBook(newBook);
								}
							}.runTask(main);
						}
					}.runTaskAsynchronously(main);
				}
				
				/* Sign Translation */
				else if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingSign()
				    && event.getClickedBlock() != null 
				    && event.getClickedBlock().getType().name().contains("SIGN")
				    && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					/* Start sign translation */
					Sign currentSign = (Sign) event.getClickedBlock().getState();

					new BukkitRunnable() {
						@Override
						public void run() {
							/* Init vars */
							String[] signText = currentSign.getLines();
							String[] changedSignText = new String[signText.length];
							boolean textLimit = false;
							boolean oneOrMoreLinesFailed = false;

							/* Send message */
							final TextComponent signStart = Component.text()
									.append(Component.text()
											.content(CommonDefinitions.getMessage("wwcSignTranslateStart"))
											.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
									.build();
							CommonDefinitions.sendMessage(event.getPlayer(), signStart);

							/* Translate each line of sign */
							for (int i = 0; i < changedSignText.length; i++) {
								String eaLine = CommonDefinitions.translateText(signText[i], event.getPlayer());
								/* Save translated line */
								if (eaLine.length() > 15) {
									textLimit = true;
								}
								if (eaLine.equals(signText[i]) && !signText[i].equals("")) {
									oneOrMoreLinesFailed = true;
								}
								changedSignText[i] = eaLine;
							}
							/*
							 * Change sign for this user only, if translationNotTooLong and sign still
							 * exists
							 */
							if (!textLimit && !oneOrMoreLinesFailed && currentSign.getLocation() != null) {
								/* Set completed message */
								final TextComponent signDone = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcSignDone"))
												.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), signDone);
							} else if (oneOrMoreLinesFailed && !textLimit && currentSign.getLocation() != null) {
								/* Even though one or more lines failed, set completed message */
								final TextComponent signDone = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcSignTranslationFail"))
												.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), signDone);
							} else {
								/*
								 * Format sign for chat, if translation exceeds 15 chars or sign was already
								 * deleted
								 */
								String out = "\n";
								for (String eaLine : changedSignText) {
									if (eaLine.length() > 1)
										out += eaLine + "\n";
								}

								/* If we are here, sign is too long or deleted msg */
								final TextComponent translationNoticeMsg = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcSignDeletedOrTooLong"))
												.color(NamedTextColor.LIGHT_PURPLE)
												.decoration(TextDecoration.ITALIC, true))
										.append(Component.text().content("\n" + "---------------")
												.color(NamedTextColor.GOLD)
												.append(Component.text().content(out).color(NamedTextColor.WHITE))
												.append(Component.text().content("---------------")
														.color(NamedTextColor.GOLD)))
										.build();

								CommonDefinitions.sendMessage(event.getPlayer(), translationNoticeMsg);
								this.cancel();
								return;
							}
							new BukkitRunnable() {
								@Override
								public void run() {
									event.getPlayer().sendSignChange(currentSign.getLocation(), changedSignText);
								}
							}.runTask(main);
						}
					}.runTaskAsynchronously(main);
				}
				
				/* Item Translation */
				else if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingItem()
					&& event.getPlayer().getInventory() != null
					&& event.getPlayer().getInventory().getItemInMainHand() != null
					&& event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR
					&& event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					/* Start item translation */
					ItemStack currentItem = event.getPlayer().getInventory().getItemInMainHand();

					new BukkitRunnable() {
						@Override
						public void run() {
							/* Init vars */
							ItemMeta meta = currentItem.getItemMeta();
							String translatedName = "";
							List<String> itemLore = meta.getLore();
							ArrayList<String> outLore = new ArrayList<String>();
							boolean sameResult = false;

							/* Send message */
							final TextComponent itemStart = Component.text()
									.append(Component.text()
											.content(CommonDefinitions.getMessage("wwcItemTranslateStart"))
											.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
									.build();
							CommonDefinitions.sendMessage(event.getPlayer(), itemStart);

							/* Translate item title */
							if (meta.hasDisplayName()) {
								translatedName = CommonDefinitions.translateText(meta.getDisplayName(), event.getPlayer());
								if (!translatedName.equalsIgnoreCase(meta.getDisplayName())) {
									/* Set completed message */
									final TextComponent itemTitleDone = Component.text()
											.append(Component.text()
													.content(CommonDefinitions.getMessage("wwcItemTranslateTitleDone"))
													.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
											.build();
									CommonDefinitions.sendMessage(event.getPlayer(), itemTitleDone);
								} else {
									translatedName = "";
									final TextComponent itemTitleFail = Component.text()
											.append(Component.text()
													.content(CommonDefinitions.getMessage("wwcItemTranslateTitleFail"))
													.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
											.build();
									CommonDefinitions.sendMessage(event.getPlayer(), itemTitleFail);
								}
							} else {
								final TextComponent itemStockTitleFail = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcItemTranslateTitleStock"))
												.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), itemStockTitleFail);
								// We cannot translate stock items to a custom name, as all item enums are in
								// English.
								// If the user is using another language in their respective Minecraft client,
								// there is no way that
								// The server can know this. Therefore, vanilla items are not supported for
								// translation.
							}

							/* Translate item lore */
							if (meta.hasLore()) {
								final TextComponent itemLoreStart = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcItemTranslateLoreStart"))
												.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
										.build();
								CommonDefinitions.sendMessage(event.getPlayer(), itemLoreStart);
								outLore = new ArrayList<String>();
								for (String eaLine : itemLore) {
									String translatedLine = CommonDefinitions.translateText(eaLine, event.getPlayer());
									if (eaLine.equals(translatedLine)) {
										sameResult = true;
									}
									outLore.add(translatedLine);
								}
								if (!sameResult) {
									/* Set completed message */
									final TextComponent itemLoreDone = Component.text()
											.append(Component.text()
													.content(CommonDefinitions.getMessage("wwcItemTranslateLoreDone"))
													.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
											.build();
									CommonDefinitions.sendMessage(event.getPlayer(), itemLoreDone);
								} else {
									final TextComponent itemLoreFail = Component.text()
											.append(Component.text()
													.content(CommonDefinitions.getMessage("wwcItemTranslateLoreFail"))
													.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
											.build();
									CommonDefinitions.sendMessage(event.getPlayer(), itemLoreFail);
								}
							}

							/* Create "fake" item to be displayed to user */
							ItemStack translatedItem = currentItem.clone();
							ItemMeta translatedMeta = translatedItem.getItemMeta();
							translatedMeta.setDisplayName(translatedName);
							translatedMeta.setLore(outLore);
							translatedItem.setItemMeta(translatedMeta);

							/* Return fake item */
							if (!translatedName.equals("") || !outLore.isEmpty()) {
								new BukkitRunnable() {
									@Override
									public void run() {
										TempItemInventory.getTempItemInventory(translatedItem).open(event.getPlayer());
									}
								}.runTask(main);
							}
						}
					}.runTaskAsynchronously(main);
				}
				
			} catch (Exception e) {
				final TextComponent translationFailMsg = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcInGameObjectTranslationFailCatastrophic"))
								.color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), translationFailMsg);
			}
		}
	}
}
