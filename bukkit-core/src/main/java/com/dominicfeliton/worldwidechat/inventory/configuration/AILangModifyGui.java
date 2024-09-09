package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class AILangModifyGui implements InventoryProvider {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    private WWCInventoryManager invManager = main.getInventoryManager();

    private String currentLang = "";

    private Player inPlayer;

    public AILangModifyGui(String currentLang, Player inPlayer) {
        this.currentLang = currentLang;
        this.inPlayer = inPlayer;
    }

    public SmartInventory modifyAILang() {
        return SmartInventory.builder().id("aiLangModifyMenu")
                .provider(this).size(3, 9)
                .manager(WorldwideChat.instance.getInventoryManager())
                .title(refs.getPlainMsg("wwcConfigGUIAILangModify",
                        currentLang,
                        "&9",
                        inPlayer))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            /* Set borders to orange */
            invManager.setBorders(contents, XMaterial.ORANGE_STAINED_GLASS_PANE);

            /* Middle Option: Change existing text */
            //invManager.genericConversationButton(1, 4, player, contents, new ChatSettingsConvos.ModifyOverrideText(getModifyCurrentOverride(), currentOverrideName, inLang), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesOverrideChangeButton");

            /* Right Option: Delete override */
            ItemStack deleteLangButton = XMaterial.BARRIER.parseItem();
            ItemMeta deleteLangMeta = deleteLangButton.getItemMeta();
            deleteLangMeta.setDisplayName(refs.getPlainMsg("wwcConfigGUIAILangDeleteButton",
                    "",
                    "&c",
                    inPlayer));
            deleteLangButton.setItemMeta(deleteLangMeta);
            contents.set(1, 5, ClickableItem.of(deleteLangButton, e -> {
                GenericRunnable save = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        YamlConfiguration config = main.getConfigManager().getAIConfig();
                        SupportedLang fixed = refs.fixLangName(currentLang);
                        Set<String> codesOnly = new TreeSet<>();
                        for (SupportedLang eaLang : main.getSupportedInputLangs().values()) {
                            codesOnly.add(eaLang.getLangCode());
                        }

                        main.getSupportedInputLangs().remove(fixed.getLangCode());
                        main.getSupportedInputLangs().remove(fixed.getLangName());
                        main.getSupportedInputLangs().remove(fixed.getNativeLangName());

                        main.getSupportedOutputLangs().remove(fixed.getLangCode());
                        main.getSupportedOutputLangs().remove(fixed.getLangName());
                        main.getSupportedOutputLangs().remove(fixed.getNativeLangName());

                        codesOnly.remove(fixed.getLangCode());

                        config.set("supportedLangs", new ArrayList<>(codesOnly)); // Save the updated list back to the config

                        main.getConfigManager().saveCustomConfig(config, main.getConfigManager().getAIFile(), false);
                        main.addPlayerUsingConfigurationGUI(inPlayer.getUniqueId());
                        refs.sendMsg("wwcConfigConversationAILangDeletionSuccess", "", "&a", inPlayer);

                        GenericRunnable out = new GenericRunnable() {
                            @Override
                            protected void execute() {
                                new AILangGui(inPlayer).getAILangs().open(player);
                            }
                        };
                        wwcHelper.runSync(out, ENTITY, new Object[]{player});
                    }
                };
                wwcHelper.runAsync(save, ASYNC, null);
            }));


            /* Left Option: Previous Page */
            invManager.setCommonButton(1, 3, player, contents, "Previous", new Object[]{new AILangGui(inPlayer).getAILangs()});
        } catch (Exception e) {
            invManager.inventoryError(player, e);
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}

