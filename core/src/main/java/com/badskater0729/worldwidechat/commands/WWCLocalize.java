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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class WWCLocalize extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    //private YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
    private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

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

        if (args.length == 1) {
            return changeLocalization(sender.getName(), args[0]);
        } else if (args.length == 2) {
            return changeLocalization(args[0], args[1]);
        }
        return false;
    }

    private boolean changeLocalization(String inName, String locale) {
        Player inPlayer = Bukkit.getPlayerExact(inName);

        if (inPlayer == null) {
            playerNotFoundMsg(sender, inName);
            return false;
        }

        if (!refs.checkIfValidLocalLang(locale) && !locale.equalsIgnoreCase("stop")) {
            refs.sendFancyMsg("wwclLangInvalid", new String[] {"&6"+locale, "&6"+ Arrays.toString(supportedPluginLangCodes)}, sender);
            return false;
        }

        PlayerRecord currRecord = main.getPlayerRecord(inPlayer, true);
        if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
            // Changing our own localization
            if (!locale.equalsIgnoreCase("stop")) {
                currRecord.setLocalizationCode(locale);
                changeLangMsg(sender, inName, locale);
            } else {
                if (!currRecord.getLocalizationCode().isEmpty()) {
                    stopLangMsg(sender);
                    currRecord.setLocalizationCode("");
                } else {
                    alreadyStoppedMsg(sender);
                }
            }
            return true;
        } else {
            // Changing someone else's
            if (!locale.equalsIgnoreCase("stop")) {
                currRecord.setLocalizationCode(args[1]);
                refs.sendFancyMsg("wwclLangChangedOtherPlayerSender", new String[] {"&6"+inName, "&6"+locale}, sender);
                changeLangMsg(inPlayer, inName, locale);
            } else {
                if (!currRecord.getLocalizationCode().isEmpty()) {
                    currRecord.setLocalizationCode("");
                    refs.sendFancyMsg("wwclLangStoppedOtherPlayerSender", "&6"+inName, sender);
                    stopLangMsg(inPlayer);
                } else {
                    refs.sendFancyMsg("wwclLangAlreadyStoppedOtherPlayerSender", "&6"+inName, sender);
                }
            }
            return true;
        }
    }

    private void playerNotFoundMsg(CommandSender sender, String inName) {
        refs.sendFancyMsg("wwclLangPlayerNotValid", new String[] {"&6"+args[0]}, "&c", sender);
    }

    private void changeLangMsg(CommandSender sender, String inName, String locale) {
        refs.sendFancyMsg("wwclLangChanged", new String[] {"&6"+locale}, sender);
    }

    private void stopLangMsg(CommandSender sender) {
        refs.sendFancyMsg("wwclLangStopped", sender);
    }

    private void alreadyStoppedMsg(CommandSender sender) {
        refs.sendFancyMsg("wwclLangAlreadyStopped", sender);
    }
}
