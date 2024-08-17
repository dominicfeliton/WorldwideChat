package com.dominicfeliton.worldwidechat.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.TempItemInventory;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
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

import java.util.ArrayList;
import java.util.List;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;

public class TranslateInGameListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;

	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

	/* Custom Entity Name Translation */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInGameEntityTranslateRequest(PlayerInteractEntityEvent event) {
		if (main.isActiveTranslator(event.getPlayer()) && event.getHand().equals(EquipmentSlot.HAND)) {
			/* Entity Names */
			try {
				if (main.getActiveTranslator(event.getPlayer()).getTranslatingEntity() && event.getRightClicked().isValid()) {
					final String customName = event.getRightClicked().getCustomName();
					BukkitRunnable out = new BukkitRunnable() {
						@Override
						public void run() {
							refs.sendFancyMsg("wwcEntityTranslateStart", "", "&d&l", event.getPlayer());
							if (customName != null) {
								refs.sendFancyMsg("wwcEntityTranslateDone", "&a&o" + refs.translateText(customName, event.getPlayer()), "&2&l", event.getPlayer());
							} else {
								refs.sendFancyMsg("wwcEntityTranslateNoName", "", "&e&o", event.getPlayer());
							}
						}
					};

					wwcHelper.runAsync(out, ENTITY, new Object[] {event.getPlayer()});
				}
			} catch (Exception e) {
				if (!refs.serverIsStopping()) {
					throw e;
				}
				//refs.debugMsg("We are reloading! Caught exception in Entity Translation...");
				//refs.debugMsg(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/* Items (+ Lore), Books, Signs Translation */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInGameObjTranslateRequest(PlayerInteractEvent event) {
		try {
			if (!main.isActiveTranslator(event.getPlayer())) {
				return;
			}
			/* Book Translation */
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer());
			if (currTranslator.getTranslatingBook()
					&& event.getHand().equals(EquipmentSlot.HAND) && event.getItem() != null
					&& XMaterial.WRITTEN_BOOK.parseItem().getType() == event.getItem().getType()
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				ItemStack currentBook = event.getItem().clone();
				BukkitRunnable out = new BukkitRunnable() {
					@Override
					public void run() {
						/* Init vars */
						Player currPlayer = event.getPlayer();
						BookMeta meta = (BookMeta) currentBook.getItemMeta();
						String outTitle = meta.getTitle();
						List<String> pages = meta.getPages();
						List<String> translatedPages = new ArrayList<String>();

						/* Send message */
						refs.sendFancyMsg("wwcBookTranslateStart", "", "&d&l", currPlayer);

						/* Translate title */
						outTitle = refs.translateText(meta.getTitle(), currPlayer);
						refs.sendFancyMsg("wwcBookTranslateTitleSuccess", "&a&o" + outTitle, "&2&l", currPlayer);

						/* Translate pages */
						for (String eaPage : pages) {
							String out = refs.translateText(eaPage, currPlayer);
							translatedPages.add(out);
						}

						if (currentBook != null) {
							/* Set completed message */
							refs.sendFancyMsg("wwcBookDone", "", "&a&o", currPlayer);
						}

						/* Create the modified book */
						ItemStack newBook = XMaterial.WRITTEN_BOOK.parseItem();
						BookMeta newMeta = (BookMeta) newBook.getItemMeta();
						newMeta.setAuthor(meta.getAuthor());
						try {
							/* Older MC versions do not have generation data */
							newMeta.setGeneration(meta.getGeneration());
						} catch (NoSuchMethodError e) {}
						newMeta.setTitle(outTitle);
						newMeta.setPages(translatedPages);
						newBook.setItemMeta(newMeta);

						/* Get update status */
						BukkitRunnable open = new BukkitRunnable() {
							@Override
							public void run() {
								/* Version Check: For 1.13 and below compatibility */
								try {
									Player.class.getMethod("openBook", ItemStack.class);
									currPlayer.openBook(newBook);
								} catch (Exception e) {
									// Old version
									int page = 1;
									String dashes = "---------------------";
									int middleIndex = dashes.length() / 2;
									for (String eachPage: translatedPages) {
										String result = dashes.substring(0, middleIndex) + " &2&l(&a&l" + page + "&2&l)&r&6 " + dashes.substring(middleIndex);
										refs.sendMsg(currPlayer, "&6" + result);
										refs.sendMsg(currPlayer, eachPage);
										page++;
									}
									refs.sendMsg(currPlayer, "&6" + dashes + "----");
								}
							}
						};

						wwcHelper.runSync(open, ENTITY, new Object[] {currPlayer});
					}
				};
				wwcHelper.runAsync(out, GLOBAL, null);
			}

			/* Sign Translation */
			else if (currTranslator.getTranslatingSign()
					&& event.getClickedBlock() != null
					&& event.getClickedBlock().getType().name().contains("SIGN")
					&& event.getHand().equals(EquipmentSlot.HAND)
					&& event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				/* Start sign translation */
				Sign currentSign = (Sign) event.getClickedBlock().getState();

				BukkitRunnable out = new BukkitRunnable() {
					@Override
					public void run() {
						/* Init vars */
						String[] signText = currentSign.getLines();
						String[] changedSignText = new String[signText.length];
						boolean textLimit = false;

						/* Send message */
						refs.sendFancyMsg("wwcSignTranslateStart", "", "&d&l", event.getPlayer());

						/* Translate each line of sign */
						for (int i = 0; i < changedSignText.length; i++) {
							String eaLine = refs.translateText(signText[i], event.getPlayer());
							/* Save translated line */
							if (eaLine.length() > 15) {
								textLimit = true;
							}
							changedSignText[i] = eaLine;
						}

						/* Version Check: For 1.13 and below compatibility */
						try {
							Player.class.getMethod("sendSignChange", Location.class, String[].class);
						} catch (Exception e) {
							refs.debugMsg("No sendSignChange method found!");
							textLimit = true;
							// Always send the user the result via chat. sendSignChange() does not exist in Player.class before 1.14.
						}

						/*
						 * Change sign for this user only, if translationNotTooLong and sign still
						 * exists
						 */
						Location currLoc = currentSign.getLocation();

						// Prep message if sign is too long
						String out = "\n";
						for (String eaLine : changedSignText) {
							if (eaLine.length() > 1)
								out += eaLine + "\n";
						}
						final TextComponent translationNoticeMsg = Component.text()
								.content(refs.getMsg("wwcSignDeletedOrTooLong", event.getPlayer()))
								.color(NamedTextColor.LIGHT_PURPLE)
								.decoration(TextDecoration.ITALIC, true)
								.append(Component.text().content("\n" + "---------------")
										.color(NamedTextColor.GOLD)
										.append(Component.text().content(out).color(NamedTextColor.WHITE))
										.append(Component.text().content("---------------")
												.color(NamedTextColor.GOLD)))
								.build();

						if (!textLimit && currLoc != null) {
							/* Set completed message */
							refs.sendFancyMsg("wwcSignDone", "", "&a&o", event.getPlayer());
						} else {
							/* If we are here, sign is too long or deleted msg */
							refs.sendMsg(event.getPlayer(), translationNoticeMsg);
							changedSignText = null;
						}

						/* Get update status */
						if (changedSignText != null && !refs.serverIsStopping()) {
							final String[] finalText = changedSignText;
							BukkitRunnable sign = new BukkitRunnable() {
								@Override
								public void run() {
									try {
										event.getPlayer().sendSignChange(currLoc, finalText);
									} catch (Exception e) {
										// This might happen sometimes, send message until this is fixed.
										refs.debugMsg("sendSignChange is broken?? cringe, fixme");
										refs.sendMsg(event.getPlayer(), translationNoticeMsg);
									}
								}
							};

							wwcHelper.runSync(sign, ENTITY, new Object[] {event.getPlayer()});
						}
					}
				};
				wwcHelper.runAsync(out, GLOBAL, null);
			}

			/* Item Translation */
			else if (currTranslator.getTranslatingItem()
					&& getItemInMainHand(event) != null
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				/* Start item translation */
				ItemStack currentItem = (ItemStack) getItemInMainHand(event);

				BukkitRunnable out = new BukkitRunnable() {
					@Override
					public void run() {
						/* Init vars */
						ItemMeta meta = currentItem.getItemMeta();
						String translatedName = "";
						List<String> itemLore = meta.getLore();
						ArrayList<String> outLore = new ArrayList<String>();

						/* Send message */
						refs.sendFancyMsg("wwcItemTranslateStart", "", "&d&l", event.getPlayer());

						/* Translate item title */
						if (meta.hasDisplayName()) {
							translatedName = refs.translateText(meta.getDisplayName(), event.getPlayer());
							/* Set completed message */
							refs.sendFancyMsg("wwcItemTranslateTitleDone","", "&a&o", event.getPlayer());
						} else {
							refs.sendFancyMsg("wwcItemTranslateTitleStock","", "&e&o", event.getPlayer());
							// Stock items not supported
						}

						// If we are stock item with no lore
						if (!meta.hasLore() && !meta.hasDisplayName()) return;

						/* Translate item lore */
						if (meta.hasLore()) {
							refs.sendFancyMsg("wwcItemTranslateLoreStart","", "&d&l", event.getPlayer());
							outLore = new ArrayList<String>();
							for (String eaLine : itemLore) {
								String translatedLine = refs.translateText(eaLine, event.getPlayer());
								outLore.add(translatedLine);
							}
							/* Set completed message */
							refs.sendFancyMsg("wwcItemTranslateLoreDone", "", "&a&o", event.getPlayer());
						}

						/* Create "fake" item to be displayed to user */
						ItemStack translatedItem = currentItem.clone();
						ItemMeta translatedMeta = translatedItem.getItemMeta();
						translatedMeta.setDisplayName(translatedName);
						translatedMeta.setLore(outLore);
						translatedItem.setItemMeta(translatedMeta);

						/* Return fake item */
						if ((!translatedItem.getItemMeta().getDisplayName().equals("") || (!translatedItem.hasItemMeta() || !translatedItem.getItemMeta().getLore().isEmpty()))) {
							BukkitRunnable open = new BukkitRunnable() {
								@Override
								public void run() {
									new TempItemInventory(translatedItem, event.getPlayer()).getTempItemInventory().open(event.getPlayer());
								}
							};

							wwcHelper.runSync(open, ENTITY, new Object[] {event.getPlayer()});
						}
					}
				};
				wwcHelper.runAsync(out, GLOBAL, null);
			}
		} catch (Exception e) {
			if (!refs.serverIsStopping()) {
				throw e;
			}
			refs.debugMsg("We are reloading! Caught exception in Object Translation...");
			refs.debugMsg(ExceptionUtils.getStackTrace(e));
		}
	}
	
	private Object getItemInMainHand(PlayerInteractEvent event) {
		boolean hasItem = event.getPlayer().getInventory() != null
				&& event.getPlayer().getInventory().getItemInMainHand() != null
				&& event.getPlayer().getInventory().getItemInMainHand().getType() != XMaterial.AIR.parseItem().getType()
				&& event.getPlayer().getInventory().getItemInMainHand().getType() != XMaterial.WRITTEN_BOOK.parseItem().getType();
		if (hasItem) {return event.getPlayer().getInventory().getItemInMainHand();}

		return null;
	}
}
