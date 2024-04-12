package com.badskater0729.worldwidechat.commands;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.configuration.ConfigurationHandler;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.PlayerRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class WWCLocalize extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    //private YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();

    public WWCLocalize(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        // Simple command to set yours or a user's localization.
        if (args.length == 0 || args.length > 2) {
            refs.sendFancyMsg("wwctInvalidArgs", "", "&c", sender);
            return false;
        }

        // TODO: Fix to be formatted like /wwctrl
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                return refs.sendNoConsoleChatMsg(sender);
            }

            PlayerRecord currRecord = main.getPlayerRecord((Player)sender, true);
            if (args[0].equalsIgnoreCase("stop") && !currRecord.getLocalizationCode().isEmpty()) {
                refs.sendFancyMsg("wwclLangStopped", sender);
                currRecord.setLocalizationCode("");
                return true;
            }

            if (checkIfValidLocalLang(args[0])) {
                //configHandler.generateMessagesConfig(args[0]);
                currRecord.setLocalizationCode(args[0]);
                refs.sendFancyMsg("wwclLangChanged", new String[] {"&6"+args[0]}, sender);
                return true;
            }
            // TODO: Better formatting for supportedPluginLangCodes?
            refs.sendFancyMsg("wwclLangInvalid", new String[] {"&6"+args[0], "&6"+ Arrays.toString(supportedPluginLangCodes)}, sender);
            return false;
        } else if (args.length == 2) {
            // First check if arg[0] is a player
            // TODO: perhaps convert to OfflinePlayer? Probably not...
            Player currPlayer = Bukkit.getPlayer(args[0]);
            if (currPlayer == null) {
                refs.sendFancyMsg("wwclLangPlayerNotValid", new String[] {"&6"+args[0]}, "&c", sender);
                return false;
            }

            PlayerRecord currRecord = main.getPlayerRecord(currPlayer, true);
            if (args[1].equalsIgnoreCase("stop") && !currRecord.getLocalizationCode().isEmpty()) {
                currRecord.setLocalizationCode("");
                refs.sendFancyMsg("wwclLangStoppedOtherPlayerSender", "&6"+args[0], sender);
                refs.sendFancyMsg("wwclLangStopped", sender);
                return true;
            }

            if (checkIfValidLocalLang(args[1])) {
                currRecord.setLocalizationCode(args[1]);
                refs.sendFancyMsg("wwclLangChangedOtherPlayerSender", new String[] {"&6"+args[0], "&6"+args[1]}, sender);
                refs.sendFancyMsg("wwclLangChanged", new String[] {"&6"+args[1]}, sender);
                return true;
            }

            // TODO: Better formatting for supportedPluginLangCodes?
            refs.sendFancyMsg("wwclLangInvalid", new String[] {"&6"+args[0], "&6"+ Arrays.toString(supportedPluginLangCodes)}, sender);
            return false;
        }
        return false;
    }

    public boolean checkIfValidLocalLang(String in) {
        for (String supportedPluginLangCode : supportedPluginLangCodes) {
            if (supportedPluginLangCode
                    .equalsIgnoreCase(in)) {
                return true;
            }
        }

        return false;
    }


}
