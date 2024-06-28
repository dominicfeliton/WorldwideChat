package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
        if (args.length > 2) {
            refs.sendFancyMsg("wwctInvalidArgs", "", "&c", sender);
            return false;
        }

        if (args.length == 0) {
            if (isConsoleSender) {
                refs.sendFancyMsg("wwctInvalidArgs", "", "&c", sender);
                return false;
            }
            return changeLocalization(sender.getName(), "stop");
        } else if (args.length == 1) {
            if (isConsoleSender) {
                return changeLocalization(args[0], "stop");
            }
            if (Bukkit.getPlayerExact(args[0]) != null) {
                return changeLocalization(args[0], "stop");
            }
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

        if (!refs.isSupportedLang(locale, "local") && !locale.equalsIgnoreCase("stop")) {
            refs.sendFancyMsg("wwclLangInvalid", new String[] {"&6"+locale, "&6"+ refs.getFormattedLangCodes("local")}, sender);
            return false;
        }

        if (!inName.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("worldwidechat.wwcl.otherplayers")) {
            refs.badPermsMessage("worldwidechat.wwcl.otherplayers", sender);
            return false;
        }

        PlayerRecord currRecord = main.getPlayerRecord(inPlayer, true);
        if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
            // Changing our own localization
            if (!locale.equalsIgnoreCase("stop")) {
                changeLangMsg(sender, inName, locale);
            } else {
                if (!currRecord.getLocalizationCode().isEmpty()) {
                    stopLangMsg(sender);
                } else {
                    alreadyStoppedMsg(sender);
                }
            }
        } else {
            // Changing someone else's
            if (!locale.equalsIgnoreCase("stop")) {
                refs.sendFancyMsg("wwclLangChangedOtherPlayerSender", new String[] {"&6"+inName, "&6"+locale}, sender);
                changeLangMsg(inPlayer, inName, locale);
            } else {
                if (!currRecord.getLocalizationCode().isEmpty()) {
                    refs.sendFancyMsg("wwclLangStoppedOtherPlayerSender", "&6"+inName, sender);
                    stopLangMsg(inPlayer);
                } else {
                    refs.sendFancyMsg("wwclLangAlreadyStoppedOtherPlayerSender", "&6"+inName, sender);
                }
            }
        }

        // Convert to lang code
        locale = !locale.equalsIgnoreCase("stop") ? refs.getSupportedLang(locale, "local").getLangCode() : "";
        currRecord.setLocalizationCode(locale);

        return true;
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
