package com.dominicfeliton.worldwidechat.inventory;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class WWCInventoryManager extends InventoryManager {

    private static WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    public WWCInventoryManager() {
        super(main);
    }

    public void checkIfPlayerIsMissing(Player player, String targetPlayerUUID) {
        if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED") && Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) == null) {
            // Target player no longer online
            player.closeInventory();
            refs.sendMsg("wwctGUITargetPlayerNull",
                    "",
                    "&c&o",
                    player);
        }
    }

    public void inventoryError(Player player, Exception e) {
        refs.sendMsg("wwcInventoryErrorPlayer",
                "",
                "&c",
                player);

        main.getLogger().severe(refs.getPlainMsg("wwcInventoryError",
                "&6" + player.getName(),
                player));
        e.printStackTrace();
        player.closeInventory();
    }

    public void setCommonButton(int x, int y, Player player, InventoryContents contents, String buttonType) {
        setCommonButton(x, y, player, contents, buttonType, new String[0]);
    }

    public void setCommonButton(int x, int y, Player player, InventoryContents contents, String buttonType, Object[] args) {
        ItemStack pageButton = XMaterial.WHITE_STAINED_GLASS.parseItem();
        ItemMeta pageMeta = pageButton.getItemMeta();
        if (buttonType.equalsIgnoreCase("Previous")) {
            pageButton = XMaterial.RED_STAINED_GLASS.parseItem();
            pageMeta.setDisplayName(refs.getPlainMsg("wwcConfigGUIPreviousPageButton",
                    "",
                    "&c",
                    player));
            pageButton.setItemMeta(pageMeta);
            contents.set(x, y, ClickableItem.of(pageButton, e -> {
                if (!contents.pagination().isFirst()) {
                    contents.inventory().open(player, contents.pagination().getPage() - 1);
                } else {
                    SmartInventory target = (args[0] instanceof MenuGui.LazySmartInventory)
                            ? ((MenuGui.LazySmartInventory) args[0]).get()
                            : (SmartInventory) args[0];
                    target.open(player);
                }
            }));
        } else if (buttonType.equalsIgnoreCase("Next")) {
            pageButton = XMaterial.GREEN_STAINED_GLASS.parseItem();
            pageMeta.setDisplayName(refs.getPlainMsg("wwcConfigGUINextPageButton",
                    "",
                    "&a",
                    player));
            pageButton.setItemMeta(pageMeta);
            contents.set(x, y, ClickableItem.of(pageButton, e -> {
                if (!contents.pagination().isLast()) {
                    contents.inventory().open(player, contents.pagination().getPage() + 1);
                } else {
                    SmartInventory target = (args[0] instanceof MenuGui.LazySmartInventory)
                        ? ((MenuGui.LazySmartInventory) args[0]).get()
                        : (SmartInventory) args[0];
                    target.open(player);
                }
            }));
        } else if (buttonType.equalsIgnoreCase("Page Number")) {
            pageButton = XMaterial.LILY_PAD.parseItem();
            pageMeta.setDisplayName(refs.getPlainMsg("wwcGUIPageNumber",
                    "&6" + args[0],
                    "&b",
                    player));

            addGlowEffect(pageMeta);
            pageButton.setItemMeta(pageMeta);
            contents.set(x, y, ClickableItem.empty(pageButton));
        } else if (buttonType.equalsIgnoreCase("Quit")) {
            pageButton = XMaterial.BARRIER.parseItem();
            pageMeta = pageButton.getItemMeta();
            pageMeta.setDisplayName(refs.getPlainMsg("wwcConfigGUIQuitButton",
                    "",
                    "&c",
                    player));
            pageButton.setItemMeta(pageMeta);
            contents.set(x, y, ClickableItem.of(pageButton, e -> {
                main.reload(player, true);
            }));
        } else {
            pageMeta.setDisplayName(ChatColor.RED + "Not a valid button! This is a bug, please report it.");
            pageButton.setItemMeta(pageMeta);
        }
    }

    public void setBorders(InventoryContents contents, XMaterial inMaterial) {
        ItemStack customBorders = inMaterial.parseItem();
        ItemMeta borderMeta = customBorders.getItemMeta();
        borderMeta.setDisplayName(" ");
        customBorders.setItemMeta(borderMeta);
        contents.fillBorders(ClickableItem.empty(customBorders));
    }

    public void genericOpenSubmenuButton(int x, int y, Player player, InventoryContents contents, String buttonName, SmartInventory invToOpen) {
        genericOpenSubmenuButton(x, y, player, contents, null, buttonName, invToOpen);
    }

    public void genericOpenSubmenuButton(int x, int y, Player player, InventoryContents contents, Boolean preCondition, String buttonName, SmartInventory invToOpen) {
        ItemStack button;
        if (preCondition != null) {
            if (preCondition) {
                button = XMaterial.EMERALD_BLOCK.parseItem();
            } else {
                button = XMaterial.REDSTONE_BLOCK.parseItem();
            }
        } else {
            button = XMaterial.WRITABLE_BOOK.parseItem();
        }
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(refs.getPlainMsg(buttonName,
                "",
                "&6",
                player));
        button.setItemMeta(buttonMeta);
        contents.set(x, y, ClickableItem.of(button, e -> {
            invToOpen.open(player);
        }));
    }

    public void genericToggleButton(int x, int y, Player player, InventoryContents contents, String configButtonName, String messageOnChange, String configValueName, boolean serverRestartRequired) {
        genericToggleButton(x, y, player, contents, configButtonName, messageOnChange, configValueName, new ArrayList<>(), serverRestartRequired);
    }

    public void genericToggleButton(int x, int y, Player player, InventoryContents contents, String configButtonName, String messageOnChange, String configValueName, List<String> configValsToDisable, boolean serverRestartRequired) {
        ItemStack button = XMaterial.BEDROCK.parseItem();
        if (main.getConfigManager().getMainConfig().getBoolean(configValueName)) {
            button = XMaterial.EMERALD_BLOCK.parseItem();
        } else {
            button = XMaterial.REDSTONE_BLOCK.parseItem();
        }
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(refs.getPlainMsg(configButtonName,
                "",
                "&6",
                player));
        button.setItemMeta(buttonMeta);
        contents.set(x, y, ClickableItem.of(button, e -> {
            if (!serverRestartRequired) {
                main.addPlayerUsingConfigurationGUI(player);
            } else {
                refs.sendMsg("wwcConfigGUIToggleRestartRequired",
                        "",
                        "&e",
                        player);
            }
            main.getConfigManager().getMainConfig().set(configValueName,
                    !(main.getConfigManager().getMainConfig().getBoolean(configValueName)));
            for (String eaKey : configValsToDisable) {
                if (eaKey.equals(configValueName)) continue;
                refs.debugMsg("Disabling " + eaKey + "!");
                main.getConfigManager().getMainConfig().set(eaKey, false);
            }
            refs.sendMsg(messageOnChange,
                    "",
                    "&a",
                    player);

            genericToggleButton(x, y, player, contents, configButtonName, messageOnChange, configValueName, configValsToDisable, serverRestartRequired);
        }));
    }

    public void genericConversationButton(int x, int y, Player player, InventoryContents contents, Prompt inPrompt, XMaterial inMaterial, String buttonName) {
        ConversationFactory genericConversation = new ConversationFactory(main).withModality(true).withTimeout(600)
                .withFirstPrompt(inPrompt);
        ItemStack button = inMaterial.parseItem();
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(refs.getPlainMsg(buttonName,
                "",
                "&6",
                player));
        button.setItemMeta(buttonMeta);
        contents.set(x, y, ClickableItem.of(button, e -> {
            if (!main.getCurrPlatform().equals("Folia")) {
                genericConversation.buildConversation(player).begin();
            } else {
                refs.sendMsg("wwcNoConvoFolia", "", "&c", player);
            }
        }));
    }

    public void genericBookButton(int x, int y, Player player, InventoryContents contents, YamlConfiguration inConfig, String inConfigVal, XMaterial inMaterial, String buttonName) {
        ItemStack button = inMaterial.parseItem();
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(refs.getPlainMsg(buttonName,
                "",
                "&6",
                player));
        button.setItemMeta(buttonMeta);
        contents.set(x, y, ClickableItem.of(button, e -> {
            ItemStack book = XMaterial.WRITABLE_BOOK.parseItem();
            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            char[] array = inConfig.getString(inConfigVal).toCharArray();
            StringBuilder currPage = new StringBuilder();

            ArrayList<String> pages = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                currPage.append(array[i]);
                if (currPage.length() >= 234) {
                    pages.add(currPage.toString());
                    currPage = new StringBuilder();
                }
            }

            // add last page
            if (currPage.length() > 0) {
                pages.add(currPage.toString());
            }

            bookMeta.setAuthor(player.getName());
            try {
                /* Older MC versions do not have generation data */
                bookMeta.setGeneration(bookMeta.getGeneration());
            } catch (NoSuchMethodError no) {
            }
            bookMeta.setTitle(inConfigVal);
            bookMeta.setPages(pages);
            book.setItemMeta(bookMeta);

            // Queue auto-cleanup after 5 minutes
            GenericRunnable cleanup = new GenericRunnable() {
                @Override
                protected void execute() {
                    if (!main.isPlayerUsingGUI(player) || !(main.getPlayerDataUsingGUI(player).length > 0)) {
                        refs.debugMsg("No cleanup for genericBookButton required");
                        return;
                    }
                    refs.debugMsg("Cleaning up genericBookButton...");

                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.hasItemMeta() && item.getItemMeta().equals(bookMeta)) {
                            player.getInventory().removeItem(item);
                            break;
                        }
                    }

                    main.removePlayerUsingConfigurationGUI(player);
                }
            };
            cleanup.setName("generic bulk book input");
            wwcHelper.runSync(true, 6000, cleanup, ENTITY, new Object[]{player});

            main.addPlayerUsingConfigurationGUI(player.getUniqueId(), new Object[]{inConfig, inConfigVal, contents.inventory(), cleanup});
            player.closeInventory();

            refs.sendMsg("wwcConfigInstructionsBulkBook",
                    "",
                    "&a",
                    player);

            // Drop the writable book at the player's location
            player.getWorld().dropItemNaturally(player.getLocation(), book);
        }));
    }

    public void addGlowEffect(ItemMeta meta) {
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
    }

    /**
     * Returns the generic conversation for modifying values in our config.yml using the GUI.
     *
     * @param exitCheck           - If this is false, then we will exit
     * @param context             - The conversation context obj
     * @param successfulChangeMsg - Names of the message sent on successful change
     * @param configValName       - The names of the config value to be updated
     * @param configVal           - The new value
     * @param prevInventory       - The previous inventory to open up after the conversation is over
     * @return Prompt.END_OF_CONVERSATION - This will ultimately be returned to end the conversation. If the length of configValName != the length of configVal, then null is returned.
     */
    public Prompt genericConfigConvo(boolean exitCheck, ConversationContext context, String successfulChangeMsg, String[] configValName, Object[] configVal, SmartInventory prevInventory) {
        Player currPlayer = ((Player) context.getForWhom());
        if (configValName.length != configVal.length) {
            return null;
        }

        if (exitCheck) {
            for (int i = 0; i < configValName.length; i++) {
                main.getConfigManager().getMainConfig().set(configValName[i], configVal[i]);
            }
            main.addPlayerUsingConfigurationGUI((currPlayer.getUniqueId()));
            refs.sendMsg(successfulChangeMsg,
                    "",
                    "&a",
                    currPlayer);
        }
        /* Re-open previous GUI */
        prevInventory.open((Player) context.getForWhom());
        return Prompt.END_OF_CONVERSATION;
    }

    /**
     * Returns the generic conversation for modifying values in our config.yml using the GUI.
     *
     * @param exitCheck           - If this is false, then we will exit
     * @param context             - The conversation context obj
     * @param successfulChangeMsg - Name of the message sent on successful change
     * @param configValName       - The name of the config value to be updated
     * @param configVal           - The new value
     * @param prevInventory       - The previous inventory to open up after the conversation is over
     * @return Prompt.END_OF_CONVERSATION - This will ultimately be returned to end the conversation.
     */
    public Prompt genericConfigConvo(boolean exitCheck, ConversationContext context, String successfulChangeMsg, String configValName, Object configVal, SmartInventory prevInventory) {
        return genericConfigConvo(exitCheck, context, successfulChangeMsg, new String[]{configValName}, new Object[]{configVal}, prevInventory);
    }
}
