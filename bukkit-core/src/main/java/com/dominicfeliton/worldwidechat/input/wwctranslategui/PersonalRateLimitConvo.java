package com.dominicfeliton.worldwidechat.input.wwctranslategui;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.commands.WWCTranslateRateLimit;
import com.dominicfeliton.worldwidechat.inventory.wwctranslategui.WWCTranslateGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PersonalRateLimitConvo extends NumericInputPrompt {

    private ActiveTranslator currTranslator;

    private WorldwideChat main = WorldwideChat.instance;

    public PersonalRateLimitConvo(ActiveTranslator inTranslator) {
        currTranslator = inTranslator;
    }

    @Override
    public @NotNull String getPromptText(InputContext context) {
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
    protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
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
        return InputResult.complete();
    }

    @Override
    public InputResult cancelInput(InputContext context) {
        new WWCTranslateGuiMainMenu(currTranslator.getUUID(), context.getForWhom()).getTranslateMainMenu().open(context.getForWhom());
        return InputResult.complete();
    }

    @Override
    public String getUnavailableMessageKey() {
        return "wwcRateNoConvoFolia";
    }

}
