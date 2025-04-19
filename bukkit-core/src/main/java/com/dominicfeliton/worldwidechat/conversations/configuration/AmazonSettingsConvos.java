package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AmazonSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class AccessKey extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAmazonTranslateAccessKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateAccessKeySuccess",
                    new String[]{"Translator.amazonAccessKey", "Translator.useAmazonTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv.get());
        }
    }

    public static class Region extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAmazonTranslateRegionInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.amazonRegion"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateRegionSuccess",
                    new String[]{"Translator.amazonRegion", "Translator.useAmazonTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv.get());
        }
    }

    public static class SecretKey extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAmazonTranslateSecretKeyInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateSecretKeySuccess",
                    new String[]{"Translator.amazonSecretKey", "Translator.useAmazonTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv.get());
        }
    }


}
