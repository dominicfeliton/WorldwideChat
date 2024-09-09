package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class AISettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class AddLang extends StringPrompt {
        private SmartInventory previousInventory;
        private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

        public AddLang(SmartInventory previousInventory) {
            this.previousInventory = previousInventory;
        }

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAddLang", new String[]{}, "&b", currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();

            GenericRunnable open = new GenericRunnable() {
                @Override
                protected void execute() {
                    previousInventory.open((Player) context.getForWhom());
                }
            };

            GenericRunnable run = new GenericRunnable() {
                @Override
                protected void execute() {
                    if (!input.equals("0")) {
                        YamlConfiguration config = main.getConfigManager().getAIConfig();
                        Player currPlayer = ((Player) context.getForWhom());
                        Set<String> codesOnly = new TreeSet<>();
                        for (SupportedLang eaLang : main.getSupportedInputLangs().values()) {
                            codesOnly.add(eaLang.getLangCode());
                        }
                        SupportedLang fixed = refs.fixLangName(input);

                        main.getSupportedInputLangs().put(fixed.getLangCode(), fixed);
                        if (!fixed.getLangName().isEmpty()) {
                            main.getSupportedInputLangs().put(fixed.getLangName(), fixed);
                        }
                        if (!fixed.getNativeLangName().isEmpty()) {
                            main.getSupportedInputLangs().put(fixed.getNativeLangName(), fixed);
                        }

                        main.getSupportedOutputLangs().put(fixed.getLangCode(), fixed);
                        if (!fixed.getLangName().isEmpty()) {
                            main.getSupportedOutputLangs().put(fixed.getLangName(), fixed);
                        }
                        if (!fixed.getNativeLangName().isEmpty()) {
                            main.getSupportedOutputLangs().put(fixed.getNativeLangName(), fixed);
                        }

                        codesOnly.add(fixed.getLangCode());
                        config.set("supportedLangs", new ArrayList<>(codesOnly)); // Save the updated list back to the config

                        // TODO: Move addPlayerUsingConfigurationGUI to invMan
                        main.addPlayerUsingConfigurationGUI(currPlayer.getUniqueId());
                        refs.sendMsg("wwcConfigConversationAILangAddSuccess",
                                "",
                                "&a",
                                currPlayer);

                        main.getConfigManager().saveCustomConfig(config, main.getConfigManager().getAIFile(), false);
                    }
                    wwcHelper.runSync(open, ENTITY, new Object[]{(Player) context.getForWhom()});
                }
            };
            wwcHelper.runAsync(run, ASYNC, null);
            return END_OF_CONVERSATION;
        }
    }

}
