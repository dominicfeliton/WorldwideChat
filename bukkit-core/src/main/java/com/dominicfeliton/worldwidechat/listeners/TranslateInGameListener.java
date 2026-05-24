package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.TempItemInventory;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.TranslationProgressIndicator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class TranslateInGameListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;

    protected CommonRefs refs = main.getServerFactory().getCommonRefs();
    protected WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    /* Custom Entity Name Translation */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInGameEntityTranslateRequest(PlayerInteractEntityEvent event) {
        if (main.isActiveTranslator(event.getPlayer()) && event.getHand().equals(EquipmentSlot.HAND)) {
            /* Entity Names */
            try {
                if (main.getActiveTranslator(event.getPlayer()).getTranslatingEntity() && event.getRightClicked().isValid()) {
                    if (isCitizensNpc(event.getRightClicked())) {
                        refs.debugMsg("Skipping generic entity-name translation for Citizens NPC; Citizens speech handles the translated line.");
                        return;
                    }

                    Player player = event.getPlayer();
                    final String customName = event.getRightClicked().getCustomName();
                    TranslationProgressIndicator.Handle status = refs.beginStatusMsg("wwcEntityTranslateStart", "", "&d&l", player);
                    GenericRunnable out = new GenericRunnable() {
                        @Override
                        protected void execute() {
                            try {
                                if (customName != null) {
                                    String translatedName = refs.translateText(customName, player);
                                    refs.finishStatusMsg(status, "wwctTranslationFinishActionBar", "", "&o&a", player);
                                    refs.sendMsg("wwcEntityTranslateDone", "&a&o" + translatedName, "&2&l", player);
                                } else {
                                    refs.failStatusMsg(status, player);
                                    refs.sendMsg("wwcEntityTranslateNoName", "", "&e&o", player);
                                }
                            } catch (RuntimeException | LinkageError e) {
                                refs.failStatusMsg(status, player);
                                throw e;
                            }
                        }
                    };

                    wwcHelper.runAsync(out, ASYNC, null);
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

    private boolean isCitizensNpc(Entity entity) {
        return main.getServer().getPluginManager().getPlugin("Citizens") != null
                && entity.hasMetadata("NPC");
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
                    && Material.WRITTEN_BOOK == event.getItem().getType()
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                Player currPlayer = event.getPlayer();
                BookMeta meta = (BookMeta) event.getItem().getItemMeta();
                String title = meta.getTitle();
                String author = meta.getAuthor();
                BookMeta.Generation generation = meta.getGeneration();
                List<String> pages = new ArrayList<>(meta.getPages());
                TranslationProgressIndicator.Handle status = refs.beginObjectStatusMsg("wwcBookTranslateStart", "", "&d&l", currPlayer);
                GenericRunnable out = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        String outTitle;
                        List<String> translatedPages;

                        /* Translate title */
                        try {
                            List<String> bookText = new ArrayList<>();
                            bookText.add(title);
                            bookText.addAll(pages);
                            List<String> translatedBookText = refs.translateObjectText(bookText, currPlayer);

                            outTitle = translatedBookText.get(0);
                            if (outTitle.equalsIgnoreCase(title)) {
                                refs.sendMsg("wwcBookTranslateTitleFail", "", "&e&o", currPlayer);
                            } else {
                                refs.sendMsg("wwcBookTranslateTitleSuccess", "&a&o" + outTitle, "&2&l", currPlayer);
                            }

                            /* Translate pages */
                            translatedPages = new ArrayList<>(translatedBookText.subList(1, translatedBookText.size()));

                            if (!translatedPages.equals(pages)) {
                                /* Set completed message */
                                refs.finishStatusMsg(status, "wwcBookDone", "", "&a&o", currPlayer);
                            } else {
                                refs.failStatusMsg(status, currPlayer);
                                refs.sendMsg("wwcBookFail", "", "&e&o", currPlayer);
                            }
                        } catch (RuntimeException | LinkageError e) {
                            refs.failStatusMsg(status, currPlayer);
                            throw e;
                        }

                        /* Get update status */
                        GenericRunnable open = new GenericRunnable() {
                            @Override
                            protected void execute() {
                                /* Create the modified book */
                                ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
                                BookMeta newMeta = (BookMeta) newBook.getItemMeta();
                                newMeta.setAuthor(author);
                                newMeta.setGeneration(generation);
                                newMeta.setTitle(outTitle);
                                newMeta.setPages(translatedPages);
                                newBook.setItemMeta(newMeta);
                                try {
                                    currPlayer.openBook(newBook);
                                } catch (RuntimeException | LinkageError ignored) {
                                }
                            }
                        };

                        wwcHelper.runSync(open, ENTITY, new Object[]{currPlayer});
                    }
                };
                wwcHelper.runAsync(out, ASYNC, null);
            }

            /* Sign Translation */
            else if (currTranslator.getTranslatingSign()
                    && event.getClickedBlock() != null
                    && event.getClickedBlock().getType().name().contains("SIGN")
                    && event.getHand().equals(EquipmentSlot.HAND)
                    && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                /* Start sign translation */
                Player player = event.getPlayer();
                Sign currentSign = (Sign) event.getClickedBlock().getState();
                String[] currentSignText = currentSign.getLines();
                final String[] signText = Arrays.copyOf(currentSignText, currentSignText.length);
                Location currLoc = currentSign.getLocation();
                TranslationProgressIndicator.Handle status = refs.beginObjectStatusMsg("wwcSignTranslateStart", "", "&d&l", player);

                GenericRunnable out = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        /* Init vars */
                        String[] changedSignText;
                        boolean textLimit = false;

                        try {
                            changedSignText = refs.translateObjectText(signText, player);
                        } catch (RuntimeException | LinkageError e) {
                            refs.failStatusMsg(status, player);
                            throw e;
                        }

                        // Check each translated line to make sure length fits in sign
                        for (String eaStr : changedSignText) {
                            if (eaStr.length() > 15) {
                                textLimit = true;
                            }
                        }

                        /*
                         * Change sign for this user only, if translationNotTooLong and sign still
                         * exists
                         */
                        // Prep message if sign is too long
                        String out = "\n";
                        for (String eaLine : changedSignText) {
                            if (eaLine.length() > 1)
                                out += eaLine + "\n";
                        }
                        final Component translationNoticeMsg = Component.text(refs.getPlainMsg("wwcSignDeletedOrTooLong", player), NamedTextColor.LIGHT_PURPLE)
                                .decoration(TextDecoration.ITALIC, true)
                                .append(Component.text("\n" + "---------------", NamedTextColor.GOLD)
                                        .append(Component.text(out, NamedTextColor.WHITE))
                                        .append(Component.text("---------------", NamedTextColor.GOLD)));

                        boolean transFail = Arrays.equals(signText, changedSignText);
                        if (transFail) {
                            refs.failStatusMsg(status, player);
                            refs.sendMsg("wwcSignNotTranslated", "", "&e&o", player);
                            changedSignText = null;
                        } else if (textLimit || currLoc == null) {
                            /* If we are here, sign is too long or deleted msg */
                            refs.sendMsg(player, translationNoticeMsg);
                            refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
                            changedSignText = null;
                        }

                        /* Get update status */
                        if (changedSignText != null && !refs.serverIsStopping()) {
                            final String[] finalText = changedSignText;
                            GenericRunnable sign = new GenericRunnable() {
                                @Override
                                protected void execute() {
                                    try {
                                        if (!currLoc.getBlock().getType().name().contains("SIGN")) {
                                            refs.debugMsg("Sign no longer exists; sending translated sign text in chat.");
                                            refs.sendMsg(player, translationNoticeMsg);
                                            refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
                                            return;
                                        }
                                        player.sendSignChange(currLoc, finalText);
                                        refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
                                    } catch (IllegalArgumentException e) {
                                        refs.debugMsg("sendSignChange rejected translated sign text; sending it in chat.");
                                        refs.sendMsg(player, translationNoticeMsg);
                                        refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
                                    } catch (RuntimeException e) {
                                        refs.failStatusMsg(status, player);
                                        throw e;
                                    }
                                }
                            };

                            wwcHelper.runSync(sign, ENTITY, new Object[]{player});
                        }
                    }
                };
                wwcHelper.runAsync(out, ASYNC, null);
            }

            /* Item Translation */
            else if (currTranslator.getTranslatingItem()
                    && getItemInMainHand(event) != null
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                /* Start item translation */
                Player player = event.getPlayer();
                ItemStack currentItem = ((ItemStack) getItemInMainHand(event)).clone();
                ItemMeta meta = currentItem.getItemMeta();
                boolean hasDisplayName = meta.hasDisplayName();
                String displayName = hasDisplayName ? meta.getDisplayName() : "";
                boolean hasLore = meta.hasLore();
                List<String> itemLore = hasLore ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                TranslationProgressIndicator.Handle status = refs.beginObjectStatusMsg("wwcItemTranslateStart", "", "&d&l", player);

                GenericRunnable out = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        /* Init vars */
                        String translatedName = "";
                        ArrayList<String> outLore = new ArrayList<String>();

                        /* Translate item title */
                        try {
                            if (!hasLore && !hasDisplayName) {
                                refs.sendMsg("wwcItemTranslateTitleStock", "", "&e&o", player);
                                refs.failStatusMsg(status, player);
                                return;
                            }

                            List<String> itemText = new ArrayList<>();
                            int displayNameIndex = -1;
                            if (hasDisplayName) {
                                displayNameIndex = itemText.size();
                                itemText.add(displayName);
                            } else {
                                refs.sendMsg("wwcItemTranslateTitleStock", "", "&e&o", player);
                                // Stock item names are not supported.
                            }

                            int loreStartIndex = itemText.size();
                            if (hasLore) {
                                refs.sendStatusMsg("wwcItemTranslateLoreStart", "", "&d&l", player);
                                itemText.addAll(itemLore);
                            }

                            List<String> translatedItemText = refs.translateObjectText(itemText, player);

                            if (hasDisplayName) {
                                translatedName = translatedItemText.get(displayNameIndex);
                                if (!hasLore) {
                                    refs.finishStatusMsg(status, "wwcItemTranslateTitleDone", "", "&a&o", player);
                                } else {
                                    refs.sendStatusMsg("wwcItemTranslateTitleDone", "", "&a&o", player);
                                }
                            }

                            /* Translate item lore */
                            if (hasLore) {
                                outLore = new ArrayList<>(translatedItemText.subList(loreStartIndex, translatedItemText.size()));
                                /* Set completed message */
                                refs.finishStatusMsg(status, "wwcItemTranslateLoreDone", "", "&a&o", player);
                            }
                        } catch (RuntimeException | LinkageError e) {
                            refs.failStatusMsg(status, player);
                            throw e;
                        }

                        /* Return fake item */
                        if (!translatedName.equals("") || !outLore.isEmpty()) {
                            final String finalTranslatedName = translatedName;
                            final List<String> finalOutLore = new ArrayList<>(outLore);
                            GenericRunnable open = new GenericRunnable() {
                                @Override
                                protected void execute() {
                                    /* Create "fake" item to be displayed to user */
                                    ItemStack translatedItem = currentItem.clone();
                                    ItemMeta translatedMeta = translatedItem.getItemMeta();
                                    if (hasDisplayName) {
                                        translatedMeta.setDisplayName(finalTranslatedName);
                                    }
                                    if (hasLore) {
                                        translatedMeta.setLore(finalOutLore);
                                    }
                                    translatedItem.setItemMeta(translatedMeta);
                                    try {
                                        new TempItemInventory(translatedItem, player).getTempItemInventory().open(player);
                                    } catch (RuntimeException | LinkageError ignored) {
                                    }
                                }
                            };

                            wwcHelper.runSync(open, ENTITY, new Object[]{player});
                        }
                    }
                };
                wwcHelper.runAsync(out, ASYNC, null);
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
                && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR
                && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK;
        if (hasItem) {
            return event.getPlayer().getInventory().getItemInMainHand();
        }

        return null;
    }
}
