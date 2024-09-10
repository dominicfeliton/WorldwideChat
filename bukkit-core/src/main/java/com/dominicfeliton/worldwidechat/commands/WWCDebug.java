package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.runnables.SyncUserData;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.storage.DataStorageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
                    refs.sendMsg("wwcdDebugEnvWarn", "&6/wwcd debugenv enable", "&e", sender);
                    return true;
                case "reset":
                    // reset
                    refs.sendMsg("wwcdResetWarn", "&6/wwcd reset confirm", "&c", sender);
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
                    refs.sendMsg("wwcdDebugEnvEnabled", "", "&a", sender);
                    main.reload(sender, true);
                    return true;
                } else if (args[1].equalsIgnoreCase("disable")) {
                    mainConfig.set("General.enableDebugMode", false);
                    mainConfig.set("Translator.testModeTranslator", false);
                    refs.sendMsg("wwcdDebugEnvDisabled", "", "&a", sender);
                    main.reload(sender, true);
                    return true;
                }
                return invalidCmd(sender);
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (args[1].equalsIgnoreCase("confirm")) {
                    // Begin Reset
                    GenericRunnable run = new GenericRunnable() {
                        @Override
                        protected void execute() {
                            try {
                                DataStorageUtils.fullDataWipe();
                                refs.sendMsg("wwcdResetNotif", "", "&a", sender);
                            } catch (Exception e) {
                                refs.sendMsg("wwcdResetNotifError", "", "&c", sender);
                            }

                            // Reload on main thread
                            main.getCache().invalidateAll();
                            main.getCache().cleanUp();
                            main.getActiveTranslators().clear();
                            main.getPlayerRecords().clear();
                            GenericRunnable reload = new GenericRunnable() {
                                @Override
                                protected void execute() {
                                    main.reload(sender, true);
                                }
                            };
                            wwcHelper.runSync(reload, WorldwideChatHelper.SchedulerType.GLOBAL, null);
                        }
                    };
                    wwcHelper.runAsync(run, ASYNC, null);
                    return true;
                }
            }
        }

        // Requires valid settings
        if (isInvalid) {
            refs.sendMsg("wwcdDebugBadState", "", "&e", sender);
            return true;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "checkdb":
                    YamlConfiguration conf = main.getConfigManager().getMainConfig();
                    if (!main.isSQLConnValid(true) && !main.isPostgresConnValid(true)) {
                        refs.sendMsg("wwcdSQLAllSet", "", "&a", sender);
                        return true;
                    }

                    // Preserve original debug val
                    boolean debugBool = conf.getBoolean("General.enableDebugMode");

                    GenericRunnable run = new GenericRunnable() {
                        @Override
                        protected void execute() {
                            conf.set("General.enableDebugMode", true);
                            if (refs.detectOutdatedTable("activeTranslators") || refs.detectOutdatedTable("playerRecords") || refs.detectOutdatedTable("persistentCache")) {
                                refs.sendMsg("wwcdOutdatedSQLStruct", "", "&e", sender);
                            } else {
                                refs.sendMsg("wwcdSQLAllSet", "", "&a", sender);
                            }
                            conf.set("General.enableDebugMode", debugBool);
                        }
                    };
                    wwcHelper.runAsync(run, ASYNC, null);
                    return true;
                case "cache":
                    // print cache
                    Set<Map.Entry<CachedTranslation, String>> cache = main.getCache().asMap().entrySet();
                    refs.sendMsg("wwcdCacheSize", new String[]{"&6" + cache.size(), "&6" + mainConfig.getInt("Translator.translatorCacheSize")}, sender);
                    int count = 1;
                    for (Map.Entry<CachedTranslation, String> eaEntry : main.getCache().asMap().entrySet()) {
                        if (count >= 100) {
                            refs.debugMsg("Cutting off at 100 terms to not crash the server...");
                            return true;
                        }

                        CachedTranslation obj = eaEntry.getKey();
                        refs.sendMsg("wwcdCacheTerm", new String[]{"&7" + count, "&6" + obj.getInputLang(), "&6" + obj.getOutputLang(), "&6" + obj.getInputPhrase(), "&6" + eaEntry.getValue()}, sender);
                        count++;
                    }
                    return true;
                case "save":
                    // force save
                    GenericRunnable save = new GenericRunnable() {
                        @Override
                        protected void execute() {
                            // Preserve original debug val
                            YamlConfiguration conf = main.getConfigManager().getMainConfig();
                            boolean debugBool = conf.getBoolean("General.enableDebugMode");

                            conf.set("General.enableDebugMode", true);
                            if (sender instanceof Player) {
                                new SyncUserData((Player) sender);
                            } else {
                                new SyncUserData();
                            }

                            conf.set("General.enableDebugMode", debugBool);
                        }
                    };
                    wwcHelper.runAsync(save, ASYNC, null);
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
                    refs.sendMsg("wwcdCacheCleared", "", "&a", sender);
                    return true;
                }
                return invalidCmd(sender);
            }
        }

        // Invalid command
        return invalidCmd(sender);
    }

    private boolean invalidCmd(CommandSender sender) {
        refs.sendMsg("wwcdInvalidCmd", "", "&c", sender);
        return false;
    }
}

