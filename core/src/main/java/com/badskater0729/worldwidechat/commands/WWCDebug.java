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

            if (args[0].equalsIgnoreCase("convert")) {
                // convert shitty struct in SQL
                if (refs.detectOutdatedTransTable() || refs.detectOutdatedRecordTable()) {
                    refs.sendFancyMsg("wwcOldSqlStructLongIntro", "", "&e", sender);
                    refs.sendFancyMsg("wwcOldSqlStructLongTodo", "&c/wwcd convert yes", "&e", sender);
                } else {
                    refs.sendFancyMsg("wwcNewSqlStruct", sender);
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
            if (args[0].equalsIgnoreCase("convert")) {
                if (args[1].equalsIgnoreCase("yes")) {
                    if (!refs.detectOutdatedTransTable() && !refs.detectOutdatedRecordTable()) {
                        refs.sendFancyMsg("wwcNewSqlStruct", sender);
                        return true;
                    }

                    refs.sendFancyMsg("wwcNewSqlConvertBegin", "", "&e", sender);
                    String originalTranslator = main.getTranslatorName();

                    // Disable command invoking + translating, close invs
                    main.setTranslatorName("Starting");
                    refs.closeAllInvs();

                    try (Connection sqlConnection = main.getSqlSession().getConnection()) {
                        /* Adjusting the `activeTranslators` table */
                        try (PreparedStatement alterActiveTranslators = sqlConnection.prepareStatement(
                                "ALTER TABLE activeTranslators " +
                                        "MODIFY creationDate VARCHAR(40), " +
                                        "MODIFY playerUUID VARCHAR(40), " +
                                        "MODIFY inLangCode VARCHAR(10), " +
                                        "MODIFY outLangCode VARCHAR(10), " +
                                        "MODIFY rateLimit INT, " +
                                        "MODIFY rateLimitPreviousTime VARCHAR(40), " +
                                        "MODIFY translatingChatOutgoing BOOLEAN, " +
                                        "MODIFY translatingChatIncoming BOOLEAN, " +
                                        "MODIFY translatingBook BOOLEAN, " +
                                        "MODIFY translatingSign BOOLEAN, " +
                                        "MODIFY translatingItem BOOLEAN, " +
                                        "MODIFY translatingEntity BOOLEAN")) {
                            alterActiveTranslators.executeUpdate();
                        }

                        /* Adjusting the `playerRecords` table */
                        try (PreparedStatement alterPlayerRecords = sqlConnection.prepareStatement(
                                "ALTER TABLE playerRecords " +
                                        "MODIFY creationDate VARCHAR(40), " +
                                        "MODIFY playerUUID VARCHAR(40), " +
                                        "MODIFY attemptedTranslations INT, " +
                                        "MODIFY successfulTranslations INT, " +
                                        "MODIFY lastTranslationTime VARCHAR(40)")) {
                            alterPlayerRecords.executeUpdate();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        refs.sendFancyMsg("wwcNewSqlConvertFail", "", "&c", sender);
                        main.setTranslatorName("Invalid");
                        main.setSqlSession(null);
                        main.getServer().getPluginManager().disablePlugin(main);
                        return false;
                    }

                    refs.sendFancyMsg("wwcNewSqlConvertFinish", sender);
                    main.setTranslatorName(originalTranslator);
                    main.reload(sender, false);
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

