package com.expl0itz.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
    
    /* Book Translation */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookTranslate(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingBook() 
                && event.getHand() != null 
                && event.getItem() != null
                && Material.WRITTEN_BOOK == event.getItem().getType()
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            /* Get vars, start book translation */
            try {
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
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslateStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                            .build();
                        main.adventure().sender(event.getPlayer()).sendMessage(bookStart);
                        
                        /* Translate title */
                        outTitle = CommonDefinitions.translateText(meta.getTitle(), event.getPlayer());
                        if (!outTitle.equals(meta.getTitle())) {
                        	final TextComponent bookTitleSuccess = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslateTitleSuccess").replace("%i", meta.getTitle())).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                                    .build();
                                main.adventure().sender(event.getPlayer()).sendMessage(bookTitleSuccess);
                        } else {
                        	final TextComponent bookTitleFail = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslateTitleFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                                    .build();
                                main.adventure().sender(event.getPlayer()).sendMessage(bookTitleFail);
                        }
                        
                        /* Translate pages */
                        for (String eaPage : pages) {
                            String out = CommonDefinitions.translateText(eaPage, event.getPlayer());
                            if (out.equals("") || out.equalsIgnoreCase(eaPage)) {
                                sameResult = true;
                                out = main.getConfigManager().getMessagesConfig().getString("Messages.wwctbTranslatePageFail").replace("%i", eaPage);
                            }
                            translatedPages.add(out);
                        }
                        
                        if (!sameResult && currentBook != null) {
                            /* Set completed message */
                            final TextComponent bookDone = Component.text()
                                .append(main.getPluginPrefix().asComponent())
                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                                .build();
                            main.adventure().sender(event.getPlayer()).sendMessage(bookDone);
                        }
                        else if (sameResult) {
                            /* If we are here, one or more translations was unsuccessful */
                            final TextComponent translationNoticeMsg = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslationFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                                    .build();
                                main.adventure().sender(event.getPlayer()).sendMessage(translationNoticeMsg);
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
            } catch (Exception e) {
                final TextComponent translationFailMsg = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBookTranslationFailCatastrophic")).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
            }
        }
    }
    
    /* Sign Translation */
    @EventHandler(priority = EventPriority.HIGHEST) 
    public void onSignTranslate(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingSign() 
                && event.getClickedBlock() != null) {
            if ((event.getClickedBlock().getType().name().contains("SIGN") && event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                try {
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
                                .append(main.getPluginPrefix().asComponent())
                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslateStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                                .build();
                            main.adventure().sender(event.getPlayer()).sendMessage(signStart);
                            
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
                            /* Change sign for this user only, if translationNotTooLong and sign still exists */
                            if (!textLimit && !oneOrMoreLinesFailed && currentSign.getLocation() != null) {
                                /* Set completed message */
                                final TextComponent signDone = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                                    .build();
                                main.adventure().sender(event.getPlayer()).sendMessage(signDone);
                            } 
                            else if (oneOrMoreLinesFailed && !textLimit && currentSign.getLocation() != null) {
                            	/* Even though one or more lines failed, set completed message */
                                final TextComponent signDone = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslationFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                                    .build();
                                main.adventure().sender(event.getPlayer()).sendMessage(signDone);
                            } else {
                                /* Format sign for chat, if translation exceeds 15 chars or sign was already deleted */
                                String out = "\n";
                                for (String eaLine : changedSignText) {
                                    if (eaLine.length() > 1)
                                        out += eaLine + "\n";
                                }
                                
                                /* If we are here, sign is too long or deleted msg */
                                final TextComponent translationNoticeMsg = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignDeletedOrTooLong")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                                    .append(Component.text().content("\n" + "---------------").color(NamedTextColor.GOLD)
                                    .append(Component.text().content(out).color(NamedTextColor.WHITE))
                                    .append(Component.text().content("---------------").color(NamedTextColor.GOLD)))
                                .build();
                                
                                main.adventure().sender(event.getPlayer()).sendMessage(translationNoticeMsg);
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
                } catch (Exception e) {
                    final TextComponent translationFailMsg = Component.text()
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslationFailCatastrophic")).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                            .build();
                        main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
                }
            }
        }
    }
    
    /* Item Name + Lore Translation */
    @EventHandler(priority = EventPriority.HIGHEST)
	public void onItemTranslateRequest(PlayerInteractEvent event) {
		if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
				&& main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingItem() 
				&& event.getPlayer().getInventory() != null && event.getPlayer().getInventory().getItemInMainHand() != null 
				&& event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR
				&& event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				) {
			try {
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
				            .append(main.getPluginPrefix().asComponent())
				            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
				            .build();
				        main.adventure().sender(event.getPlayer()).sendMessage(itemStart);
				    	
				    	/* Translate item title */
				    	if (meta.hasDisplayName()) {
				    		translatedName = CommonDefinitions.translateText(meta.getDisplayName(), event.getPlayer());
				    		if (!translatedName.equalsIgnoreCase(meta.getDisplayName())) {
				    			/* Set completed message */
				                final TextComponent itemTitleDone = Component.text()
				                    .append(main.getPluginPrefix().asComponent())
				                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateTitleDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
				                    .build();
				                main.adventure().sender(event.getPlayer()).sendMessage(itemTitleDone);
				    		} else {
				    			translatedName = "";
				    			final TextComponent itemTitleFail = Component.text()
				                        .append(main.getPluginPrefix().asComponent())
				                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateTitleFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
				                        .build();
				                main.adventure().sender(event.getPlayer()).sendMessage(itemTitleFail);
				    		}
				    	} else {
				    		final TextComponent itemStockTitleFail = Component.text()
				                    .append(main.getPluginPrefix().asComponent())
				                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateTitleStock")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
				                    .build();
				                main.adventure().sender(event.getPlayer()).sendMessage(itemStockTitleFail);
				    		// We cannot translate stock items to a custom name, as all item enums are in English.
				    		// If the user is using another language in their respective Minecraft client, there is no way that
				    		// The server can know this. Therefore, vanilla items are not supported for translation.
				    	}
						
						/* Translate item lore */
						if (meta.hasLore()) {
							final TextComponent itemLoreStart = Component.text()
						            .append(main.getPluginPrefix().asComponent())
						            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateLoreStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
						            .build();
						        main.adventure().sender(event.getPlayer()).sendMessage(itemLoreStart);
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
					                .append(main.getPluginPrefix().asComponent())
					                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateLoreDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
					                .build();
					            main.adventure().sender(event.getPlayer()).sendMessage(itemLoreDone);
							} else {
								final TextComponent itemLoreFail = Component.text()
					                    .append(main.getPluginPrefix().asComponent())
					                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslateLoreFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
					                    .build();
					                main.adventure().sender(event.getPlayer()).sendMessage(itemLoreFail);
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
			} catch (Exception e) {
				final TextComponent translationFailMsg = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcItemTranslationFailCatastrophic").replace("%i", main.getTranslatorName())).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                        .build();
                    main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
			}
		}
	}
}
