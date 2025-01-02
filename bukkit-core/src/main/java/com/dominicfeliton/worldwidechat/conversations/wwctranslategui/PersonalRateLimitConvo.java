package com.dominicfeliton.worldwidechat.conversations.wwctranslategui;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.commands.WWCTranslateRateLimit;
import com.dominicfeliton.worldwidechat.inventory.wwctranslategui.WWCTranslateGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PersonalRateLimitConvo extends NumericPrompt {

    private ActiveTranslator currTranslator;

    private WorldwideChat main = WorldwideChat.instance;

    public PersonalRateLimitConvo(ActiveTranslator inTranslator) {
        currTranslator = inTranslator;
    }

    @Override
    public @NotNull String getPromptText(ConversationContext context) {
        /* Close any open inventories */
        CommonRefs refs = main.getServerFactory().getCommonRefs();
        Player currPlayer = ((Player) context.getForWhom());
        currPlayer.closeInventory();
        return refs.getPlainMsg("wwctGUIConversationRateLimit",
                "&6" + currTranslator.getRateLimit(),
                "&b",
                currPlayer);
    }

    @Override
    protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
        WWCTranslateRateLimit rateCommand;
        if (input.intValue() > 0) { // Enable rate limit
            rateCommand = new WWCTranslateRateLimit(((CommandSender) context.getForWhom()), null,
                    null, new String[]{Bukkit.getPlayer(UUID.fromString(currTranslator.getUUID())).getName(), input.intValue() + ""});
            rateCommand.processCommand();
        } else if (input.intValue() == 0) { // Disable rate limit
            rateCommand = new WWCTranslateRateLimit(((CommandSender) context.getForWhom()), null,
                    null, new String[]{Bukkit.getPlayer(UUID.fromString(currTranslator.getUUID())).getName()});
            rateCommand.processCommand();
        } // Go back
        new WWCTranslateGuiMainMenu(currTranslator.getUUID(), (Player) context.getForWhom()).getTranslateMainMenu().open((Player) context.getForWhom());
        return END_OF_CONVERSATION;
    }

}
