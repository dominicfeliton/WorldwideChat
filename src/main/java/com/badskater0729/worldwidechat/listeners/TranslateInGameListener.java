package com.badskater0729.worldwidechat.listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.TempItemInventory;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.OldVersionOpenBook;
import com.cryptomorin.xseries.XMaterial;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.translateText;
import static com.badskater0729.worldwidechat.util.CommonRefs.runSync;
import static com.badskater0729.worldwidechat.util.CommonRefs.runAsync;
import static com.badskater0729.worldwidechat.util.CommonRefs.serverIsStopping;

public class TranslateInGameListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;
	
	/* Custom Entity Name Translation */
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onInGameEntityTranslateRequest(PlayerInteractEntityEvent event) {
		if (main.isActiveTranslator(event.getPlayer()) && checkInventoryHand(event)) {
			/* Entity Names */
			try {
				if (main.getActiveTranslator(event.getPlayer()).getTranslatingEntity() && event.getRightClicked().isValid()) {
					final String customName = event.getRightClicked().getCustomName();
					BukkitRunnable out = new BukkitRunnable() {
						@Override
						public void run() {
							final TextComponent entityStart = Component.text()
									.append(Component.text()
											.content(getMsg("wwcEntityTranslateStart"))
											.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), entityStart);
							if (customName != null) {
								final TextComponent entityDone = Component.text()
										.append(Component.text()
												.content(getMsg("wwcEntityTranslateDone", new String[] {translateText(customName, event.getPlayer())}))
												.color(NamedTextColor.GREEN))
										.build();
								sendMsg(event.getPlayer(), entityDone);
							} else {
								final TextComponent entityStock = Component.text()
										.append(Component.text()
												.content(getMsg("wwcEntityTranslateNoName"))
												.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
										.build();
								sendMsg(event.getPlayer(), entityStock);
							}
						}
					};
					runAsync(out);
				}
			} catch (Exception e) {
				if (!serverIsStopping()) {
					throw e;
				}
				//debugMsg("We are reloading! Caught exception in Entity Translation...");
				//debugMsg(ExceptionUtils.getStackTrace(e));
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
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
			if (currTranslator.getTranslatingBook()
				&& checkInventoryHand(event) && event.getItem() != null
				&& XMaterial.WRITTEN_BOOK.parseItem().getType() == event.getItem().getType()
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				ItemStack currentBook = event.getItem().clone();
				BukkitRunnable out = new BukkitRunnable() {
					@Override
					public void run() {
						/* Init vars */
						BookMeta meta = (BookMeta) currentBook.getItemMeta();
						String outTitle = meta.getTitle();
						List<String> pages = meta.getPages();
						List<String> translatedPages = new ArrayList<String>();

						/* Send message */
						final TextComponent bookStart = Component.text()
								.append(Component.text()
										.content(getMsg("wwcBookTranslateStart"))
										.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
								.build();
						sendMsg(event.getPlayer(), bookStart);

						/* Translate title */
						outTitle = translateText(meta.getTitle(), event.getPlayer());
						final TextComponent bookTitleSuccess = Component.text()
								.append(Component.text()
										.content(getMsg("wwcBookTranslateTitleSuccess", new String[] {outTitle}))
										.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
								.build();
						sendMsg(event.getPlayer(), bookTitleSuccess);

						/* Translate pages */
						for (String eaPage : pages) {
							String out = translateText(eaPage, event.getPlayer());
							translatedPages.add(out);
						}

						if (currentBook != null) {
							/* Set completed message */
							final TextComponent bookDone = Component.text()
									.append(Component.text()
											.content(getMsg("wwcBookDone"))
											.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), bookDone);
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
									event.getPlayer().openBook(newBook);
								} catch (Exception e) {
									OldVersionOpenBook.openBook(newBook, event.getPlayer());
								}
							}
						};
						runSync(open);
					}
				};
				runAsync(out);
			}
			
			/* Sign Translation */
			else if (currTranslator.getTranslatingSign()
			    && event.getClickedBlock() != null 
			    && event.getClickedBlock().getType().name().contains("SIGN")
			    && checkInventoryHand(event)
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
						final TextComponent signStart = Component.text()
								.append(Component.text()
										.content(getMsg("wwcSignTranslateStart"))
										.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
								.build();
						sendMsg(event.getPlayer(), signStart);

						/* Translate each line of sign */
						for (int i = 0; i < changedSignText.length; i++) {
							String eaLine = translateText(signText[i], event.getPlayer());
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
							textLimit = true;
							// Always send the user the result via chat. sendSignChange() does not exist in Player.class before 1.14.
						}
						
						/*
						 * Change sign for this user only, if translationNotTooLong and sign still
						 * exists
						 */
						if (!textLimit && currentSign.getLocation() != null) {
							/* Set completed message */
							final TextComponent signDone = Component.text()
									.append(Component.text()
											.content(getMsg("wwcSignDone"))
											.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), signDone);
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
											.content(getMsg("wwcSignDeletedOrTooLong"))
											.color(NamedTextColor.LIGHT_PURPLE)
											.decoration(TextDecoration.ITALIC, true))
									.append(Component.text().content("\n" + "---------------")
											.color(NamedTextColor.GOLD)
											.append(Component.text().content(out).color(NamedTextColor.WHITE))
											.append(Component.text().content("---------------")
													.color(NamedTextColor.GOLD)))
									.build();

							sendMsg(event.getPlayer(), translationNoticeMsg);
							changedSignText = null;
						}
						
						/* Get update status */
						if (changedSignText != null && !serverIsStopping()) {
							final String[] finalText = changedSignText;
							BukkitRunnable sign = new BukkitRunnable() {
								@Override
								public void run() {
									event.getPlayer().sendSignChange(currentSign.getLocation(), finalText);
								}
							};
							runSync(sign);
						}
					}
				};
				runAsync(out);
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
						final TextComponent itemStart = Component.text()
								.append(Component.text()
										.content(getMsg("wwcItemTranslateStart"))
										.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
								.build();
						sendMsg(event.getPlayer(), itemStart);

						/* Translate item title */
						if (meta.hasDisplayName()) {
							translatedName = translateText(meta.getDisplayName(), event.getPlayer());
							/* Set completed message */
							final TextComponent itemTitleDone = Component.text()
									.append(Component.text()
											.content(getMsg("wwcItemTranslateTitleDone"))
											.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), itemTitleDone);
						} else {
							final TextComponent itemStockTitleFail = Component.text()
									.append(Component.text()
											.content(getMsg("wwcItemTranslateTitleStock"))
											.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), itemStockTitleFail);
							// Stock items not supported
						}

						/* Translate item lore */
						if (meta.hasLore()) {
							final TextComponent itemLoreStart = Component.text()
									.append(Component.text()
											.content(getMsg("wwcItemTranslateLoreStart"))
											.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), itemLoreStart);
							outLore = new ArrayList<String>();
							for (String eaLine : itemLore) {
								String translatedLine = translateText(eaLine, event.getPlayer());
								outLore.add(translatedLine);
							}
							/* Set completed message */
							final TextComponent itemLoreDone = Component.text()
									.append(Component.text()
											.content(getMsg("wwcItemTranslateLoreDone"))
											.color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
									.build();
							sendMsg(event.getPlayer(), itemLoreDone);
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
									TempItemInventory.getTempItemInventory(translatedItem).open(event.getPlayer());
								}
							};
							runSync(open);
						}
					}
				};
				runAsync(out);
			}
		} catch (Exception e) {
			if (!serverIsStopping()) {
				throw e;
			}
			debugMsg("We are reloading! Caught exception in Object Translation...");
			debugMsg(ExceptionUtils.getStackTrace(e));
		}
	}
	
	private boolean checkInventoryHand(PlayerInteractEvent event) {
		try {
			try {
				PlayerInteractEvent.class.getMethod("getHand");
				if (event.getHand().equals(EquipmentSlot.HAND)) {
					return true;
				}
				return false;
			} catch (NoSuchMethodException e) {
				debugMsg("getHand() method not found in PlayerInteractEvent!");
				return true;
			}
		} catch (Exception e) {
			debugMsg(ExceptionUtils.getStackTrace(e));
			return false;
		}
	}
	
	private boolean checkInventoryHand(PlayerInteractEntityEvent event) {
		try {
			try {
				PlayerInteractEvent.class.getMethod("getHand");
				if (event.getHand().equals(EquipmentSlot.HAND)) {
					return true;
				}
				return false;
			} catch (NoSuchMethodException e) {
				debugMsg("getHand() method not found in PlayerInteractEntityEvent!");
				return true;
			}
		} catch (Exception e) {
			debugMsg(ExceptionUtils.getStackTrace(e));
			return false;
		}
	}
	
	private Object getItemInMainHand(PlayerInteractEvent event) {
		// This sucks, removing this soon TODO
		if (event.getPlayer().getInventory() != null) {
			try {
				try {
					if (PlayerInventory.class.getMethod("getItemInMainHand") != null ) {
						if (event.getPlayer().getInventory().getItemInMainHand() != null
								&& event.getPlayer().getInventory().getItemInMainHand().getType() != XMaterial.AIR.parseItem().getType()
								&& event.getPlayer().getInventory().getItemInMainHand().getType() != XMaterial.WRITTEN_BOOK.parseItem().getType()) {
							return event.getPlayer().getInventory().getItemInMainHand();
						}
						return null;
					}
				} catch (NoSuchMethodException e) {
					debugMsg("getItemInMainHand() does not exist in PlayerInventory!");
					Method getItemInHand = PlayerInventory.class.getMethod("getItemInHand");
					Object getItemInHandObj = getItemInHand.invoke(event.getPlayer().getInventory());
					Object itemType = getItemInHandObj.getClass().getMethod("getType").invoke(getItemInHandObj);
					if (getItemInHandObj != null
							&& itemType != XMaterial.AIR.parseItem().getType()
							&& itemType != XMaterial.WRITTEN_BOOK.parseItem().getType()) {
					    return getItemInHandObj;	
					}
					return null;
				}
			} catch (Exception e) {
				debugMsg(ExceptionUtils.getStackTrace(e));
				return null;
			}
		}
		return null;
	}
}
