package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AmazonSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class AccessKey extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAmazonTranslateAccessKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateAccessKeySuccess",
                    new String[]{"Translator.amazonAccessKey", "Translator.useAmazonTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv.get());
        }
    }

    public static class Region extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAmazonTranslateRegionInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.amazonRegion"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateRegionSuccess",
                    new String[]{"Translator.amazonRegion", "Translator.useAmazonTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv.get());
        }
    }

    public static class SecretKey extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAmazonTranslateSecretKeyInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateSecretKeySuccess",
                    new String[]{"Translator.amazonSecretKey", "Translator.useAmazonTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv.get());
        }
    }


}
