package com.dominicfeliton.worldwidechat.inventory.wwctranslategui;

import org.bukkit.Material;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.commands.*;
import com.dominicfeliton.worldwidechat.input.InputRequest;
import com.dominicfeliton.worldwidechat.input.wwctranslategui.PersonalRateLimitConvo;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WWCTranslateGuiMainMenu implements InventoryProvider {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private WWCInventoryManager invManager = main.getInventoryManager();

    private String targetPlayerUUID = "";

    private Player inPlayer;

    public WWCTranslateGuiMainMenu(String targetPlayerUUID, Player inPlayer) {
        this.targetPlayerUUID = targetPlayerUUID;
        this.inPlayer = inPlayer;
    }

    /* Get translation info */
    public SmartInventory getTranslateMainMenu() {
        String playerTitle = "";
        if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
            playerTitle = refs.getPlainMsg("wwctGUIMainMenuGlobal",
                    "",
                    "&9",
                    inPlayer);
        } else {
            playerTitle = refs.getPlainMsg("wwctGUIMainMenuPlayer",
                    main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(),
                    "&9",
                    inPlayer);
        }
        return SmartInventory.builder().id("translateMainMenu").provider(new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer))
                .size(5, 9).manager(WorldwideChat.instance.getInventoryManager()).title(playerTitle).build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            /* Default white stained glass borders for inactive */
            invManager.setBorders(contents, Material.WHITE_STAINED_GLASS_PANE);
            if (main.isActiveTranslator(targetPlayerUUID)) {
                invManager.setBorders(contents, Material.GREEN_STAINED_GLASS_PANE);
            }

            /* New translation button */
            ItemStack translationButton = new ItemStack(Material.COMPASS);
            ItemMeta translationMeta = translationButton.getItemMeta();
            translationMeta.setDisplayName(
                    refs.getPlainMsg("wwctGUITranslationButton", inPlayer));
            translationButton.setItemMeta(translationMeta);
            contents.set(2, 4, ClickableItem.of(translationButton, e -> {
                new WWCTranslateGuiSourceLanguage("", targetPlayerUUID, inPlayer).getSourceLanguageInventory().open(player);
            }));

            /* Localization button */
            if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcl")
                    && (player.hasPermission("worldwidechat.wwcl.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
                PlayerRecord currRecord = main.getPlayerRecord(targetPlayerUUID, true);

                ItemStack localizationButton = new ItemStack(Material.PAPER);
                ItemMeta localizationMeta = localizationButton.getItemMeta();
                List<String> outLore = new ArrayList<>();
                if (!currRecord.getLocalizationCode().isEmpty()) {
                    localizationMeta.setDisplayName(refs.getPlainMsg("wwctGUILocalizationButton",
                            "",
                            "&a",
                            inPlayer));

                    SupportedLang lang = refs.getSupportedLang(currRecord.getLocalizationCode(), CommonRefs.LangType.LOCAL);
                    String out = lang.getNativeLangName().isEmpty() ?
                            currRecord.getLocalizationCode() :
                            currRecord.getLocalizationCode() + "/" + lang.getNativeLangName();
                    outLore.add(refs.getPlainMsg("wwctGUILocalizeExistingValue",
                            "&6&l" + out,
                            "&d",
                            inPlayer));
                    invManager.addGlowEffect(localizationMeta);
                } else {
                    localizationMeta.setDisplayName(refs.getPlainMsg("wwctGUILocalizationButton",
                            "",
                            "&e",
                            inPlayer));
                }
                localizationMeta.setLore(outLore);
                localizationButton.setItemMeta(localizationMeta);
                contents.set(3, 4, ClickableItem.of(localizationButton, e -> {
                    new WWCTranslateGuiLocalizationMenu(targetPlayerUUID, inPlayer).getLocalizationInventory().open(player);
                }));
            }

            /* Set active translator to our current target */
            ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);

            if (main.isActiveTranslator(targetPlayerUUID)) {
                /* Make compass enchanted */
                invManager.addGlowEffect(translationMeta);
                translationMeta.setDisplayName(refs.getPlainMsg("wwctGUIExistingTranslationButton", inPlayer));
                List<String> outLore = new ArrayList<>();
                SupportedLang inLang = refs.getSupportedLang(targetTranslator.getInLangCode(), CommonRefs.LangType.INPUT);
                SupportedLang outLang = refs.getSupportedLang(targetTranslator.getOutLangCode(), CommonRefs.LangType.OUTPUT);
                refs.debugMsg("Input lang for /wwct: " + inLang);
                if (!inLang.getLangCode().equalsIgnoreCase("auto")
                    && !inLang.getLangCode().equalsIgnoreCase("None")) {
                    String out = inLang.getNativeLangName().isEmpty() ?
                            inLang.getLangCode() :
                            inLang.getLangCode() + "/" + inLang.getNativeLangName();
                    outLore.add(refs.getPlainMsg("wwctGUIExistingTranslationInput",
                            "&6&l" + out,
                            "&d",
                            inPlayer));
                }
                String out = outLang.getNativeLangName().isEmpty() ?
                        outLang.getLangCode() :
                        outLang.getLangCode() + "/" + outLang.getNativeLangName();
                outLore.add(refs.getPlainMsg("wwctGUIExistingTranslationOutput",
                        "&6&l" + out,
                        "&d",
                        inPlayer));
                translationMeta.setLore(outLore);
                translationButton.setItemMeta(translationMeta);

                /* Stop Button: Stop translation if active */
                ItemStack stopButton = new ItemStack(Material.BARRIER);
                ItemMeta stopMeta = stopButton.getItemMeta();
                stopMeta.setDisplayName(refs.getPlainMsg("wwctGUIStopButton",
                        "",
                        "&c",
                        inPlayer));
                stopButton.setItemMeta(stopMeta);
                contents.set(1, 4, ClickableItem.of(stopButton, e -> {
                    String[] args;
                    if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
                        args = new String[]{main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(),
                                "stop"};
                    } else {
                        args = new String[]{"stop"};
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
                    ItemStack rateButton = new ItemStack(Material.SLIME_BLOCK);
                    ItemMeta rateMeta = rateButton.getItemMeta();
                    ArrayList<String> lore = new ArrayList<>();
                    if (targetTranslator.getRateLimit() > 0) {
                        invManager.addGlowEffect(rateMeta);
                        lore.add(refs.getPlainMsg("wwctGUIRateButtonLore",
                                "&6&l" + targetTranslator.getRateLimit(),
                                "&d",
                                inPlayer));
                        rateMeta.setDisplayName(refs.getPlainMsg("wwctGUIRateButton",
                                "",
                                "&a",
                                inPlayer));
                    } else {
                        rateMeta.setDisplayName(refs.getPlainMsg("wwctGUIRateButton",
                                "",
                                "&e",
                                inPlayer));
                    }
                    rateMeta.setLore(lore);
                    rateButton.setItemMeta(rateMeta);
                    contents.set(1, 1, ClickableItem.of(rateButton, e -> {
                        main.getInputService().open(player, InputRequest.fromPrompt(new PersonalRateLimitConvo(targetTranslator)));
                    }));
                }

                /* Book Translation Button */
                if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwctb")
                        && (player.hasPermission("worldwidechat.wwctb.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
                    ItemStack bookButton = new ItemStack(Material.WRITABLE_BOOK);
                    ItemMeta bookMeta = bookButton.getItemMeta();
                    if (targetTranslator.getTranslatingBook()) {
                        bookMeta.setDisplayName(refs.getPlainMsg("wwctGUIBookButton",
                                "",
                                "&a",
                                inPlayer));
                        invManager.addGlowEffect(bookMeta);
                    } else {
                        bookMeta.setDisplayName(refs.getPlainMsg("wwctGUIBookButton",
                                "",
                                "&e",
                                inPlayer));
                    }
                    bookButton.setItemMeta(bookMeta);
                    contents.set(2, 1, ClickableItem.of(bookButton, e -> {
                        String[] args = {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName()};
                        WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender) player, null, null, args);
                        translateBook.processCommand();
                        getTranslateMainMenu().open(player);
                    }));
                }

                /* Sign Translation Button */
                if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcts")
                        && (player.hasPermission("worldwidechat.wwcts.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
                    /* Init item */
                    ItemStack signButton = new ItemStack(Material.OAK_SIGN);
                    ItemMeta signMeta = signButton.getItemMeta();
                    if (targetTranslator.getTranslatingSign()) {
                        signMeta.setDisplayName(refs.getPlainMsg("wwctGUISignButton",
                                "",
                                "&a",
                                inPlayer));
                        invManager.addGlowEffect(signMeta);
                    } else {
                        signMeta.setDisplayName(refs.getPlainMsg("wwctGUISignButton",
                                "",
                                "&e",
                                inPlayer));
                    }
                    signButton.setItemMeta(signMeta);
                    contents.set(2, 7, ClickableItem.of(signButton, e -> {
                        String[] args = {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName()};
                        WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender) player, null, null, args);
                        translateSign.processCommand();
                        getTranslateMainMenu().open(player);
                    }));
                }

                /* Item Translation Button */
                if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcti")
                        && (player.hasPermission("worldwidechat.wwcti.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
                    ItemStack itemButton = new ItemStack(Material.GRASS_BLOCK);
                    ItemMeta itemMeta = itemButton.getItemMeta();
                    if (targetTranslator.getTranslatingItem()) {
                        itemMeta.setDisplayName(refs.getPlainMsg("wwctGUIItemButton",
                                "",
                                "&a",
                                inPlayer));
                        invManager.addGlowEffect(itemMeta);
                    } else {
                        itemMeta.setDisplayName(refs.getPlainMsg("wwctGUIItemButton",
                                "",
                                "&e",
                                inPlayer));
                    }
                    itemButton.setItemMeta(itemMeta);
                    contents.set(1, 7, ClickableItem.of(itemButton, e -> {
                        String[] args = {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName()};
                        WWCTranslateItem translateItem = new WWCTranslateItem((CommandSender) player, null, null, args);
                        translateItem.processCommand();
                        getTranslateMainMenu().open(player);
                    }));
                }

                /* Entity Translation Button */
                if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && player.hasPermission("worldwidechat.wwcte")
                        && (player.hasPermission("worldwidechat.wwcte.otherplayers") || player.getUniqueId().toString().equals(targetPlayerUUID))) {
                    ItemStack entityButton = new ItemStack(Material.NAME_TAG);
                    ItemMeta entityMeta = entityButton.getItemMeta();
                    if (targetTranslator.getTranslatingEntity()) {
                        invManager.addGlowEffect(entityMeta);
                        entityMeta.setDisplayName(refs.getPlainMsg("wwctGUIEntityButton",
                                "",
                                "&a",
                                inPlayer));
                    } else {
                        entityMeta.setDisplayName(refs.getPlainMsg("wwctGUIEntityButton",
                                "",
                                "&e",
                                inPlayer));
                    }
                    entityButton.setItemMeta(entityMeta);
                    contents.set(2, 2, ClickableItem.of(entityButton, e -> {
                        String[] args = {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName()};
                        WWCTranslateEntity translateEntity = new WWCTranslateEntity((CommandSender) player, null, null, args);
                        translateEntity.processCommand();
                        getTranslateMainMenu().open(player);
                    }));
                }

                /* Chat Translation Button */
                if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")
                        && ((targetPlayerUUID.equals(player.getUniqueId().toString()) && (player.hasPermission("worldwidechat.wwctco") || player.hasPermission("worldwidechat.wwctci")))
                        || (!targetPlayerUUID.equals(player.getUniqueId().toString()) && (player.hasPermission("worldwidechat.wwctco.otherplayers") || player.hasPermission("worldwidechat.wwctci.otherplayers"))))) {
                    ItemStack chatButton = new ItemStack(Material.PAINTING);
                    ItemMeta chatMeta = chatButton.getItemMeta();
                    if (targetTranslator.getTranslatingChatOutgoing() || targetTranslator.getTranslatingChatIncoming()) {
                        invManager.addGlowEffect(chatMeta);
                        List<String> outLoreChat = new ArrayList<>();
                        outLoreChat.add(refs.getPlainMsg("wwctGUIExistingChatIncomingEnabled", refs.checkOrX(targetTranslator.getTranslatingChatIncoming()), "&d", inPlayer));
                        outLoreChat.add(refs.getPlainMsg("wwctGUIExistingChatOutgoingEnabled", refs.checkOrX(targetTranslator.getTranslatingChatOutgoing()), "&d", inPlayer));
                        chatMeta.setLore(outLoreChat);
                        chatMeta.setDisplayName(refs.getPlainMsg("wwctGUIChatButton",
                                "",
                                "&a",
                                inPlayer));
                    } else {
                        chatMeta.setDisplayName(refs.getPlainMsg("wwctGUIChatButton",
                                "",
                                "&e",
                                inPlayer));
                    }
                    chatButton.setItemMeta(chatMeta);
                    contents.set(2, 6, ClickableItem.of(chatButton, e -> {
                        new WWCTranslateGuiChatMenu(targetPlayerUUID, inPlayer).getTranslateChatMenu().open(player);
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
