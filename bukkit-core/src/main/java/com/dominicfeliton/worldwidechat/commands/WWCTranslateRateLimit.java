package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class WWCTranslateRateLimit extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

    public WWCTranslateRateLimit(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        /* Sanitize args */
        if (args.length > 2) {
            // Not enough/too many args
            refs.sendMsg("wwctInvalidArgs", "", "&c", sender);
            return false;
        }

        /* Disable existing personal rate limit */
        if (args.length == 0 && isConsoleSender) {
            refs.sendMsg("wwctInvalidArgs", "", "&c", null);
            return false;
        } else if ((args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(sender.getName())))) {
            return changeRateLimit(sender.getName(), 0);
        }

        /* Set personal rate limit */
        if (args.length == 1 && StringUtils.isNumeric(args[0])) {
            if (isConsoleSender) {
                return refs.sendNoConsoleChatMsg(sender);
            }
            if (StringUtils.isNumeric(args[0])) {
                return changeRateLimit(sender.getName(), Integer.parseInt(args[0]));
            }
            /* If rate limit is not valid/user wants to disable it */
            rateLimitBadIntMessage(sender);
            return false;
        }

        /* Disable another user's existing rate limit */
        if (args.length == 1 && args[0] != null) {
            return changeRateLimit(args[0], 0);
        }

        /* Set rate limit for another user */
        if (args.length == 2 && args[0] != null) {
            if (StringUtils.isNumeric(args[1])) {
                return changeRateLimit(args[0], Integer.parseInt(args[1]));
            }
            rateLimitBadIntMessage(sender);
            return false;
        }
        return false;
    }

    private boolean changeRateLimit(String inName, int newLimit) {
        /* Player check, translator check */
        Player inPlayer = Bukkit.getPlayerExact(inName);
        /* If player is null */
        if (inPlayer == null) {
            playerNotFoundMessage(sender, inName);
            return false;
        } else if (!main.isActiveTranslator(inPlayer)) {
            /* If translator is null, determine sender and send correct message */
            if (!isConsoleSender && inName.equalsIgnoreCase(sender.getName())) {
                refs.sendMsg("wwctrlNotATranslator", "", "&c", sender);
                return false;
            }
            playerNotFoundMessage(sender, inName);
            return false;
        }
        ActiveTranslator currTranslator = main.getActiveTranslator(inPlayer);
        if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
            /* If we are changing our own rate limit */
            if (newLimit > 0) {
                /* Enable rate limit */
                refs.playSound(CommonRefs.SoundType.SUBMENU_TOGGLE_OFF, sender);
                currTranslator.setRateLimit(newLimit);
                enableRateLimitMessage(inPlayer, newLimit);
            } else {
                /* Disable rate limit */
                if (!(currTranslator.getRateLimit() > 0)) {
                    /* Rate limit already disabled */
                    refs.sendMsg("wwctrlRateLimitAlreadyOffSender", "", "&e", inPlayer);
                } else {
                    /* Disable rate limit */
                    refs.playSound(CommonRefs.SoundType.SUBMENU_TOGGLE_OFF, sender);
                    currTranslator.setRateLimit(0);
                    disableRateLimitMessage(inPlayer);
                }
            }
            return true;
        } else {
            /* If we are changing somebody else's rate limit */
            if (newLimit > 0) {
                /* Enable rate limit */
                refs.playSound(CommonRefs.SoundType.SUBMENU_TOGGLE_OFF, sender);
                currTranslator.setRateLimit(newLimit);
                refs.sendMsg("wwctrlRateLimitSetTarget", new String[]{"&6" + inPlayer.getName(), "&6" + newLimit}, "&d", sender);
                enableRateLimitMessage(inPlayer, newLimit);
            } else {
                /* Disable rate limit */
                if (!(currTranslator.getRateLimit() > 0)) {
                    /* Rate limit already disabled */
                    refs.sendMsg("wwctrlRateLimitAlreadyOffTarget", "&6" + inPlayer.getName(), "&e", sender);
                } else {
                    /* Disable rate limit */
                    refs.playSound(CommonRefs.SoundType.SUBMENU_TOGGLE_OFF, sender);
                    currTranslator.setRateLimit(0);
                    refs.sendMsg("wwctrlRateLimitOffTarget", "&6" + inPlayer.getName(), "&d", sender);
                    disableRateLimitMessage(inPlayer);
                }
            }
            return true;
        }
    }

    private void disableRateLimitMessage(Player inPlayer) {
        refs.sendMsg("wwctrlRateLimitOffSender", "&d", inPlayer);
    }

    private void enableRateLimitMessage(Player inPlayer, int limit) {
        refs.sendMsg("wwctrlRateLimitSetSender", "&6" + limit, "&d", inPlayer);
    }

    private void rateLimitBadIntMessage(CommandSender sender) {
        refs.sendMsg("wwctrlRateLimitBadInt", "&c", sender);
    }

    private void playerNotFoundMessage(CommandSender sender, String inName) {
        refs.sendMsg("wwctrlPlayerNotFound", "&6" + inName, "&c", sender);
    }

}