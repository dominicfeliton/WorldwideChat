package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.wwcstatsgui.WWCStatsGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.*;

public class WWCStats extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

    public WWCStats(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        /* Sanitize args */
        if (args.length > 1) {
            // Not enough/too many args
            refs.sendMsg("wwctInvalidArgs", "", "&c", sender);
        }

        /* Get Sender Stats */
        if (args.length == 0) {
            if (isConsoleSender) {
                return noRecordsMessage("Console");
            }
            translatorMessage(sender.getName());
            return true;
        }

        /* Get Target Stats */
        if (args.length == 1) {
            translatorMessage(args[0]);
            return true;
        }
        return false;
    }

    private void translatorMessage(String inName) {
        GenericRunnable getPlayer = new GenericRunnable() {
            @Override
            protected void execute() {
                StatsTarget target = resolveOnlineStatsTarget(inName);
                if (target == null) {
                    if (!isValidTargetName(inName)) {
                        sendPlayerNotFound(inName);
                        return;
                    }
                    processOfflineStatsTarget(inName);
                    return;
                }

                processStatsAsync(target);
            }
        };
        wwcHelper.runSync(getPlayer, GLOBAL);
    }

    private StatsTarget resolveOnlineStatsTarget(String inName) {
        if (sender instanceof Player senderPlayer && sender.getName().equalsIgnoreCase(inName)) {
            return new StatsTarget(senderPlayer.getUniqueId().toString(), senderPlayer.getName());
        }

        if (!isValidTargetName(inName)) {
            return null;
        }

        Player onlinePlayer = Bukkit.getPlayer(inName);
        if (onlinePlayer != null) {
            return new StatsTarget(onlinePlayer.getUniqueId().toString(), onlinePlayer.getName());
        }

        return null;
    }

    private void processOfflineStatsTarget(String inName) {
        GenericRunnable getOfflineTarget = new GenericRunnable() {
            @Override
            protected void execute() {
                /* Get OfflinePlayer async because this may hit Mojang/session APIs. */
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(inName);
                if (!offlinePlayer.hasPlayedBefore()) {
                    sendPlayerNotFound(inName);
                    return;
                }
                String targetName = offlinePlayer.getName() == null ? inName : offlinePlayer.getName();
                processStats(new StatsTarget(offlinePlayer.getUniqueId().toString(), targetName));
            }
        };
        wwcHelper.runAsync(getOfflineTarget, ASYNC);
    }

    private void processStatsAsync(StatsTarget target) {
        GenericRunnable getStats = new GenericRunnable() {
            @Override
            protected void execute() {
                processStats(target);
            }
        };
        wwcHelper.runAsync(getStats, ASYNC);
    }

    private void processStats(StatsTarget target) {
        /* Process stats of target */
        // Check if PlayerRecord is valid
        if (!main.isPlayerRecord(target.uuid())) {
            noRecordsMessage(target.name());
            refs.playSound(CommonRefs.SoundType.STATS_FAIL, sender);
            return;
        }
        // Is on record; continue
        if (sender instanceof Player) {
            final String targetUUID = target.uuid();
            final String targetName = target.name();
            GenericRunnable out = new GenericRunnable() {
                @Override
                protected void execute() {
                    new WWCStatsGuiMainMenu(targetUUID, targetName, (Player) sender).getStatsMainMenu().open((Player) sender);
                    refs.playSound(CommonRefs.SoundType.STATS_SUCCESS, sender);
                }
            };

            wwcHelper.runSync(out, ENTITY, new Object[]{(Player) sender});
        } else {
            PlayerRecord record = main
                    .getPlayerRecord(target.uuid(), false);
            Component stats = Component.text(refs.getPlainMsg("wwcsTitle", target.name(), sender), NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true)
                    .append(Component.text("\n- " + refs.getPlainMsg("wwcsAttemptedTranslations", record.getAttemptedTranslations() + "", sender), NamedTextColor.AQUA))
                    .append(Component.text("\n- " + refs.getPlainMsg("wwcsSuccessfulTranslations", record.getSuccessfulTranslations() + "", sender), NamedTextColor.AQUA))
                    .append(Component.text("\n- " + refs.getPlainMsg("wwcsLocalization", record.getLocalizationCode().isEmpty()
                                    ? refs.checkOrX(false)
                                    : refs.getSupportedLang(record.getLocalizationCode(), CommonRefs.LangType.LOCAL).toString(), sender), NamedTextColor.AQUA))
                    .append(Component.text("\n- " + refs.getPlainMsg("wwcsLastTranslationTime", record.getLastTranslationTime(), sender), NamedTextColor.AQUA));
            // Add current translator stats if user is ActiveTranslator
            Component isActiveTranslator = Component.text("\n- " + refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(false), sender), NamedTextColor.AQUA);
            if (main.isActiveTranslator(target.uuid())) {
                // Is currently an active translator
                // Therefore, append active translator stats
                ActiveTranslator currTrans = main.getActiveTranslator(target.uuid());
                SupportedLang inLang = refs.getSupportedLang(currTrans.getInLangCode(), CommonRefs.LangType.INPUT);
                SupportedLang outLang = refs.getSupportedLang(currTrans.getOutLangCode(), CommonRefs.LangType.OUTPUT);
                isActiveTranslator = Component.text("\n- " + refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(true), sender), NamedTextColor.AQUA)
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransUUID", ChatColor.GOLD + currTrans.getUUID(), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransRateLimit", ChatColor.GOLD + "" + currTrans.getRateLimit(), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransInLang", ChatColor.GOLD + inLang.toString(), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransOutLang", ChatColor.GOLD + outLang.toString(), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransOutgoing", refs.checkOrX(currTrans.getTranslatingChatOutgoing()), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransIncoming", refs.checkOrX(currTrans.getTranslatingChatIncoming()), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransBook", refs.checkOrX(currTrans.getTranslatingBook()), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransSign", refs.checkOrX(currTrans.getTranslatingSign()), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransItem", refs.checkOrX(currTrans.getTranslatingItem()), sender), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransEntity", refs.checkOrX(currTrans.getTranslatingEntity()), sender), NamedTextColor.LIGHT_PURPLE));
                // If debug, append extra vars
                if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
                    Component debugInfo = Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransColorWarning", refs.checkOrX(currTrans.getCCWarning()), sender), NamedTextColor.LIGHT_PURPLE)
                            .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransSignWarning", refs.checkOrX(currTrans.getSignWarning()), sender), NamedTextColor.LIGHT_PURPLE))
                            .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransSaved", refs.checkOrX(currTrans.getHasBeenSaved()), sender), NamedTextColor.LIGHT_PURPLE))
                            .append(Component.text("\n  - " + refs.getPlainMsg("wwcsActiveTransPrevRate", ChatColor.GOLD + currTrans.getRateLimitPreviousTime(), sender), NamedTextColor.LIGHT_PURPLE));
                    isActiveTranslator = isActiveTranslator.append(debugInfo);
                }
            }
            stats = stats.append(isActiveTranslator);
            refs.sendMsg(sender, stats);
        }
    }

    private boolean isValidTargetName(String inName) {
        /* Don't run API against invalid long names */
        return inName.length() <= 16 && inName.length() >= 3;
    }

    private void sendPlayerNotFound(String inName) {
        refs.sendMsg("wwcPlayerNotFound", "&6" + inName, "&c", sender);
        refs.playSound(CommonRefs.SoundType.STATS_FAIL, sender);
    }

    private boolean noRecordsMessage(String name) {
        refs.sendMsg("wwcsNotATranslator", "&6" + name, "&c", sender);
        return true;
    }

    private record StatsTarget(String uuid, String name) {
    }
}
