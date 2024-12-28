package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.wwcstatsgui.WWCStatsGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
                /* Get OfflinePlayer, this will allow us to get stats even if target is offline */
                final OfflinePlayer testPlayer;
                if (sender.getName().equals(inName)) {
                    testPlayer = (Player) sender;
                } else {
                    /* Don't run API against invalid long names */
                    if (inName.length() > 16 || inName.length() < 3) {
                        refs.sendMsg("wwcPlayerNotFound", "&6" + args[0], "&c", sender);
                        return;
                    }
                    testPlayer = Bukkit.getPlayer(inName);
                }

                GenericRunnable getStats = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        // Get Offline Player Async
                        OfflinePlayer inPlayer = testPlayer == null ?
                                Bukkit.getOfflinePlayer(inName) :
                                testPlayer;

                        if (!inPlayer.hasPlayedBefore()) {
                            // Target player not found
                            if (args.length > 1) {
                                refs.sendMsg("wwcPlayerNotFound", "&6" + args[0], "&c", sender);
                            } else {
                                refs.sendMsg("wwcPlayerNotFound", "&6"+sender.getName(), "&c", sender);
                            }
                            refs.playSound(CommonRefs.SoundType.STATS_FAIL, sender);
                            return;
                        }

                        /* Process stats of target */
                        // Check if PlayerRecord is valid
                        if (!main.isPlayerRecord(inPlayer.getUniqueId())) {
                            noRecordsMessage(inPlayer.getName());
                            refs.playSound(CommonRefs.SoundType.STATS_FAIL, sender);
                            return;
                        }
                        // Is on record; continue
                        if (sender instanceof Player) {
                            final String targetUUID = inPlayer.getUniqueId().toString();
                            GenericRunnable out = new GenericRunnable() {
                                @Override
                                protected void execute() {
                                    new WWCStatsGuiMainMenu(targetUUID, (Player) sender).getStatsMainMenu().open((Player) sender);
                                    refs.playSound(CommonRefs.SoundType.STATS_SUCCESS, sender);
                                }
                            };

                            wwcHelper.runSync(out, ENTITY, new Object[]{(Player) sender});
                        } else {
                            PlayerRecord record = main
                                    .getPlayerRecord(inPlayer.getUniqueId().toString(), false);
                            TextComponent stats = Component.text()
                                    .content(refs.getPlainMsg("wwcsTitle", inPlayer.getName(), sender))
                                    .color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true)
                                    .append(Component.text()
                                            .content("\n- " + refs.getPlainMsg("wwcsAttemptedTranslations", record.getAttemptedTranslations() + "", sender))
                                            .color(NamedTextColor.AQUA))
                                    .append(Component.text()
                                            .content("\n- " + refs.getPlainMsg("wwcsSuccessfulTranslations", record.getSuccessfulTranslations() + "", sender))
                                            .color(NamedTextColor.AQUA))
                                    .append(Component.text()
                                            .content("\n- " + refs.getPlainMsg("wwcsLocalization", record.getLocalizationCode().isEmpty()
                                                    ? refs.checkOrX(false)
                                                    : refs.getSupportedLang(record.getLocalizationCode(), CommonRefs.LangType.LOCAL).toString(), sender))
                                            .color(NamedTextColor.AQUA))
                                    .append(Component.text()
                                            .content("\n- " + refs.getPlainMsg("wwcsLastTranslationTime", record.getLastTranslationTime(), sender))
                                            .color(NamedTextColor.AQUA))
                                    .build();
                            // Add current translator stats if user is ActiveTranslator
                            TextComponent isActiveTranslator = Component.text()
                                    .content("\n- " + refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(false), sender))
                                    .color(NamedTextColor.AQUA)
                                    .build();
                            if (main.isActiveTranslator(inPlayer.getUniqueId())) {
                                // Is currently an active translator
                                // Therefore, append active translator stats
                                ActiveTranslator currTrans = main.getActiveTranslator(inPlayer.getUniqueId());
                                SupportedLang inLang = refs.getSupportedLang(currTrans.getInLangCode(), CommonRefs.LangType.INPUT);
                                SupportedLang outLang = refs.getSupportedLang(currTrans.getOutLangCode(), CommonRefs.LangType.OUTPUT);
                                isActiveTranslator = Component.text()
                                        .content("\n- " + refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(true), sender))
                                        .color(NamedTextColor.AQUA)
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransUUID", ChatColor.GOLD + currTrans.getUUID(), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransRateLimit", ChatColor.GOLD + "" + currTrans.getRateLimit(), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransInLang", ChatColor.GOLD + inLang.toString(), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransOutLang", ChatColor.GOLD + outLang.toString(), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransOutgoing", refs.checkOrX(currTrans.getTranslatingChatOutgoing()), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransIncoming", refs.checkOrX(currTrans.getTranslatingChatIncoming()), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransBook", refs.checkOrX(currTrans.getTranslatingBook()), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransSign", refs.checkOrX(currTrans.getTranslatingSign()), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransItem", refs.checkOrX(currTrans.getTranslatingItem()), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text()
                                                .content("\n  - " + refs.getPlainMsg("wwcsActiveTransEntity", refs.checkOrX(currTrans.getTranslatingEntity()), sender))
                                                .color(NamedTextColor.LIGHT_PURPLE))
                                        .build();
                                // If debug, append extra vars
                                if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
                                    TextComponent debugInfo = Component.text()
                                            .content("\n  - " + refs.getPlainMsg("wwcsActiveTransColorWarning", refs.checkOrX(currTrans.getCCWarning()), sender))
                                            .color(NamedTextColor.LIGHT_PURPLE)
                                            .append(Component.text()
                                                    .content("\n  - " + refs.getPlainMsg("wwcsActiveTransSignWarning", refs.checkOrX(currTrans.getSignWarning()), sender))
                                                    .color(NamedTextColor.LIGHT_PURPLE))
                                            .append(Component.text()
                                                    .content("\n  - " + refs.getPlainMsg("wwcsActiveTransSaved", refs.checkOrX(currTrans.getHasBeenSaved()), sender))
                                                    .color(NamedTextColor.LIGHT_PURPLE))
                                            .append(Component.text()
                                                    .content("\n  - " + refs.getPlainMsg("wwcsActiveTransPrevRate", ChatColor.GOLD + currTrans.getRateLimitPreviousTime(), sender))
                                                    .color(NamedTextColor.LIGHT_PURPLE))
                                            .build();
                                    isActiveTranslator = isActiveTranslator.append(debugInfo);
                                }
                            }
                            stats = stats.append(isActiveTranslator);
                            refs.sendMsg(sender, stats);
                        }
                    }
                };
                wwcHelper.runAsync(getStats, ASYNC);
            }
        };
        wwcHelper.runSync(getPlayer, GLOBAL);
    }

    private boolean noRecordsMessage(String name) {
        refs.sendMsg("wwcsNotATranslator", "&6" + name, "&c", sender);
        return true;
    }
}