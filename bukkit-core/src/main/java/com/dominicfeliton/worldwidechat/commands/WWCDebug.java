package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.runnables.SyncUserData;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;

public class WWCDebug extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    private YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
    public WWCDebug(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        boolean isInvalid = main.getTranslatorName().equals("Invalid");
        if (args.length < 1 || args.length > 3) {
            return false;
        }

        // Invalid settings are fine (not while starting tho)
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "debugenv":
                    refs.sendFancyMsg("wwcdDebugEnvWarn", new String[] {"&6/wwcd debugenv enable"}, "&e", sender);
                    return true;
                default:
                    break;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("debugenv")) {
                if (args[1].equalsIgnoreCase("enable")) {
                    mainConfig.set("General.enableDebugMode", true);
                    mainConfig.set("Translator.testModeTranslator", true);
                    refs.sendFancyMsg("wwcdDebugEnvEnabled", new String[]{}, "&a", sender);
                    main.reload(true);
                    return true;
                } else if (args[1].equalsIgnoreCase("disable")) {
                    mainConfig.set("General.enableDebugMode", false);
                    mainConfig.set("Translator.testModeTranslator", false);
                    refs.sendFancyMsg("wwcdDebugEnvDisabled", new String[]{}, "&a", sender);
                    main.reload(true);
                    return true;
                }
                return invalidCmd(sender);
            }
        }

        // Requires valid settings
        if (isInvalid) {
            refs.sendFancyMsg("wwcdDebugBadState", new String[]{}, "&e", sender);
            return true;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "checkdb":
                    YamlConfiguration conf = main.getConfigManager().getMainConfig();
                    if (!main.isSQLConnValid(true) && !main.isPostgresConnValid(true)) {
                        refs.sendFancyMsg("wwcdSQLAllSet", new String[] {}, "&a", sender);
                        return true;
                    }

                    // Preserve original debug val
                    boolean debugBool = conf.getBoolean("General.enableDebugMode");

                    conf.set("General.enableDebugMode", true);
                    if (refs.detectOutdatedTable("activeTranslators") || refs.detectOutdatedTable("playerRecords") || refs.detectOutdatedTable("persistentCache")) {
                        refs.sendFancyMsg("wwcdOutdatedSQLStruct", new String[] {}, "&e", sender);
                    } else {
                        refs.sendFancyMsg("wwcdSQLAllSet", new String[] {}, "&a", sender);
                    }
                    conf.set("General.enableDebugMode", debugBool);
                    return true;
                case "cache":
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
                case "save":
                    // force save
                    Runnable run = new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Preserve original debug val
                            YamlConfiguration conf = main.getConfigManager().getMainConfig();
                            boolean debugBool = conf.getBoolean("General.enableDebugMode");

                            conf.set("General.enableDebugMode", true);
                            if (sender instanceof Player) {
                                new SyncUserData((Player)sender).run();
                            } else {
                                new SyncUserData().run();
                            }

                            conf.set("General.enableDebugMode", debugBool);
                        }
                    };
                    wwcHelper.runAsync(run, ASYNC, null);
                    return true;
                default:
                    break;
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
                return invalidCmd(sender);
            }
        }

        // Invalid command
        return invalidCmd(sender);
    }

    private boolean invalidCmd(CommandSender sender) {
        refs.sendFancyMsg("wwcdInvalidCmd", "", "&c", sender);
        return false;
    }
}

