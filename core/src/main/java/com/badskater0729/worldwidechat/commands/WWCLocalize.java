package com.badskater0729.worldwidechat.commands;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.configuration.ConfigurationHandler;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class WWCLocalize extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    //private YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
    private ConfigurationHandler configHandler = main.getConfigManager();

    public WWCLocalize(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        // Simple command to set yours or a user's localization.
        if (args.length == 0 || args.length > 2) {
            refs.sendFancyMsg("wwctInvalidArgs", new String[] {}, "&c", sender);
            return false;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                //refs.send NO CONSOLE SUPPORT
                return false;
            }

            if (!main.isActiveTranslator((Player)sender)) {
                // refs.send NOT AN ACTIVE TRANS
                return false;
            }

            ActiveTranslator currTranslator = main.getActiveTranslator((Player)sender);
            if (checkIfValidLang(args[0])) {
                configHandler.generateMessagesConfig(args[0]);
                currTranslator.setPersonalLangCode(args[0]);
                return true;
            }
            // TODO: send wwclInvalidLang
            return false;

            // Check if is a valid localization
            // Copy+Upgrade the localization if necessary to data dir
            // Set activeTranslator's localization
            // TODO: Add logic to getMsg since it is used everywhere
        } else if (args.length == 2) {
            // First check if arg[0] is a player


            // Then check if arg[1] is a valid LOCALIZATION LANG
            // Set activeTranslator's localization
            // TODO: Add logic to getMsg since it is used everywhere
        }

        return false;
    }

    public boolean checkIfValidLang(String in) {
        for (String supportedPluginLangCode : supportedPluginLangCodes) {
            if (supportedPluginLangCode
                    .equalsIgnoreCase(in)) {
                return true;
            }
        }

        return false;
    }


}
