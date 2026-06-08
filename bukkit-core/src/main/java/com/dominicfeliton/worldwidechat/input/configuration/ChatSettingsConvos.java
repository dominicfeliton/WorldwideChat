package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class ChatSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ChannelIcon extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            // TODO: Display color codes properly
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChannelIconInput",
                    new String[]{"&r" + main.getConfigManager().getMainConfig().getString("Chat.separateChatChannel.icon")},
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChannelIconSuccess",
                    "Chat.separateChatChannel.icon", input, MenuGui.CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.inv.get());
        }
    }

    public static class ChannelFormat extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();
        private String vars = "{prefix}, {username}, {suffix}, {local:XXX}";

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChannelFormatInput",
                    new String[]{"&r" + main.getConfigManager().getMainConfig().getString("Chat.separateChatChannel.format"),
                            "&6" + vars},
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChatFormatSuccess",
                    "Chat.separateChatChannel.format", input, MenuGui.CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.inv.get());
        }
    }

    public static class ChannelHoverFormat extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();
        private String vars = "{prefix}, {username}, {suffix}, {local:XXX}";

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChannelHoverFormatInput",
                    new String[]{"&r" + main.getConfigManager().getMainConfig().getString("Chat.separateChatChannel.hoverFormat"),
                            "&6" + vars},
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChatHoverFormatSuccess",
                    "Chat.separateChatChannel.hoverFormat", input, MenuGui.CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.inv.get());
        }
    }

    public static class ModifyChatPriority extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChatPriorityInput",
                    new String[]{"&6" + main.getConfigManager().getMainConfig().getString("Chat.chatListenerPriority"),
                            "&6" + Arrays.toString(EventPriority.values())},
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(InputContext context, String input) {
            Player currPlayer = ((Player) context.getForWhom());
            boolean valid = false;
            for (EventPriority eaPriority : EventPriority.values()) {
                if (eaPriority.name().equalsIgnoreCase(input) || input.equals("0")) {
                    valid = true;
                }
            }

            if (valid) {
                return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChatPrioritySuccess",
                        "Chat.chatListenerPriority", input, MenuGui.CONFIG_GUI_TAGS.CHAT_SET.inv.get());
            }
            refs.sendMsg("wwcConfigConversationChatPriorityBadInput",
                    "&6" + Arrays.toString(EventPriority.values()),
                    "&c",
                    currPlayer);
            return InputResult.repeat();
        }
    }

    public static class AddBlacklistTerm extends StringInputPrompt {
        private SmartInventory previousInventory;
        private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

        public AddBlacklistTerm(SmartInventory previousInventory) {
            this.previousInventory = previousInventory;
        }

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAddBlacklist", new String[]{}, "&b", currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
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
                        YamlConfiguration config = main.getConfigManager().getBlacklistConfig();
                        Player currPlayer = ((Player) context.getForWhom());
                        Set<String> bannedWords = main.getBlacklistTerms();
                        bannedWords.add(input); // Add currentTerm to the list
                        config.set("bannedWords", new ArrayList<String>(bannedWords)); // Save the updated list back to the config

                        main.addPlayerUsingConfigurationGUI(currPlayer.getUniqueId());
                        refs.sendMsg("wwcConfigConversationBlacklistAddSuccess",
                                "",
                                "&a",
                                currPlayer);

                        main.getConfigManager().saveCustomConfig(config, main.getConfigManager().getBlacklistFile(), false);
                    }
                    wwcHelper.runSync(open, ENTITY, new Object[]{(Player) context.getForWhom()});
                }
            };
            wwcHelper.runAsync(run, ASYNC, null);
            return InputResult.complete();
        }
    }

    public static class ModifyOverrideText extends StringInputPrompt {
        private SmartInventory previousInventory;

        private String currentOverrideName;

        private String inLang;

        private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

        public ModifyOverrideText(SmartInventory previousInventory, String currentOverrideName, String inLang) {
            this.previousInventory = previousInventory;
            this.currentOverrideName = currentOverrideName;
            this.inLang = inLang;
        }

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationOverrideTextChange", currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();

            GenericRunnable open = new GenericRunnable() {
                @Override
                protected void execute() {
                    previousInventory.open((Player) context.getForWhom());
                }
            };

            GenericRunnable async = new GenericRunnable() {
                @Override
                protected void execute() {
                    if (!input.equals("0")) {
                        Player currPlayer = ((Player) context.getForWhom());
                        YamlConfiguration msgConfig = main.getConfigManager().getCustomMessagesConfig(inLang);

                        msgConfig.set("Overrides." + currentOverrideName, input);
                        main.addPlayerUsingConfigurationGUI(currPlayer.getUniqueId());

                        refs.sendMsg("wwcConfigConversationOverrideTextChangeSuccess",
                                "",
                                "&a",
                                currPlayer);
                        main.getConfigManager().saveMessagesConfig(inLang, true);
                    }
                    wwcHelper.runSync(open, ENTITY, new Object[]{(Player) context.getForWhom()});
                }
            };
            wwcHelper.runAsync(async, ASYNC, null);
            return InputResult.complete();
        }
    }

}
