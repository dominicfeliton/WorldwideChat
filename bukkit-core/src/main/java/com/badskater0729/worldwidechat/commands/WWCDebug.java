package com.badskater0729.worldwidechat.commands;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.configuration.ConfigurationHandler;
import com.badskater0729.worldwidechat.util.CachedTranslation;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class WWCDebug extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
    public WWCDebug(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        if (args.length < 1 || args.length > 3) {
            return false;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("checkdb")) {
                YamlConfiguration conf = main.getConfigManager().getMainConfig();
                boolean debugBool = conf.getBoolean("General.debugModeEnabled");

                conf.set("General.enableDebugMode", true);
                if (refs.detectOutdatedTable("activeTranslators") || refs.detectOutdatedTable("playerRecords") || refs.detectOutdatedTable("persistentCache")) {
                    refs.sendFancyMsg("wwcdOutdatedSQLStruct", new String[] {}, "&e", sender);
                } else {
                    refs.sendFancyMsg("wwcdSQLAllSet", new String[] {}, "&a", sender);
                }
                conf.set("General.enableDebugMode", debugBool);
                return true;
            }
            if (args[0].equalsIgnoreCase("cache")) {
                // print cache
                Set<Map.Entry<CachedTranslation, String>> cache = main.getCache().asMap().entrySet();
                refs.sendFancyMsg("wwcdCacheSize", new String[] {"&6" + cache.size(), "&6" + mainConfig.getInt("Translator.translatorCacheSize")}, sender);
                int count = 1;
                for (Map.Entry<CachedTranslation, String> eaEntry : main.getCache().asMap().entrySet()) {
                    CachedTranslation obj = eaEntry.getKey();
                    refs.sendFancyMsg("wwcdCacheTerm", new String[] {"&7" + count, "&6" + obj.getInputLang(), "&6" + obj.getOutputLang(), "&6" + obj.getInputPhrase(), "&6" + eaEntry.getValue()}, sender);
                    count++;
                }
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("cache")) {
                if (args[1].equalsIgnoreCase("clear")) {
                    main.getCache().invalidateAll();
                    main.getCache().cleanUp();
                    refs.sendFancyMsg("wwcdCacheCleared", new String[]{}, "&a", sender);
                    return true;
                }
                return false;
            }
            return false;
        }

        // Invalid command
        refs.sendFancyMsg("wwcdInvalidCmd", "", "&c", sender);
        return false;
    }
}

