package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.wwctranslategui.WWCTranslateGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WWCTranslate extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private boolean isGlobal = this instanceof WWCGlobal;
    private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

    public WWCTranslate(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    /*
     * Correct Syntax: /wwct <id-in> <id-out> EX for id: en, ar, cs, nl, etc. id
     * MUST be valid, we will check with CommonRefs class
     */

    @Override
    public boolean processCommand() {
        // TODO: Add two test-cases.
        // 1) Add a ton of perm checks (such as making sure another user without perms cannot change/disable trans sessions
        // 2) Make sure that when you stop a different translation session yours is not stopped as well
        // TODO: Revamp GUI checks
        // TODO: Perhaps put /wwct without stopping first?
        // TODO: Fix bold calls
        /* Sanitize args */
        if ((isGlobal && args.length > 2) || (!isGlobal && args.length > 3)) {
            // Too many args
            refs.sendMsg("wwctInvalidArgs", "", "&c", sender);
            return false;
        }

        /* GUI Checks */
        if (args.length == 0 && isGlobal && !isConsoleSender) {
            new WWCTranslateGuiMainMenu("GLOBAL-TRANSLATE-ENABLED", (Player) sender).getTranslateMainMenu().open((Player) sender);
            return true;
        } else if (!isConsoleSender) {
            if ((args.length == 0 && !isGlobal) || (args.length == 1 && !isGlobal && args[0].equalsIgnoreCase(sender.getName()))) { /* User wants to see their own translation session */
                new WWCTranslateGuiMainMenu(((Player) sender).getUniqueId().toString(), (Player) sender).getTranslateMainMenu().open((Player) sender);
                return true;
            } else if (args.length == 1 && !isGlobal
                    && main.getServer().getPlayerExact(args[0]) != null) {
                if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
                    new WWCTranslateGuiMainMenu(main.getServer().getPlayerExact(args[0]).getUniqueId().toString(), (Player) sender)
                            .getTranslateMainMenu().open((Player) sender);
                    return true;
                } else {
                    refs.badPermsMessage("worldwidechat.wwct.otherplayers", sender);
                    return false;
                }
            }
        }

        /* Existing translator checks */
        if (!isGlobal) {
            // User wants to exit their own translation session.
            if (!isConsoleSender && args.length > 0 && (Bukkit.getServer().getPlayerExact(args[0]) == null
                    || (args[0] instanceof String && args[0].equalsIgnoreCase(sender.getName()))) && main.isActiveTranslator(((Player) sender))) {
                ActiveTranslator currTarget = main.getActiveTranslator(((Player) sender));
                main.removeActiveTranslator(currTarget);
                refs.sendMsg("wwctTranslationStopped", sender);
                stopSound();
                if ((args.length >= 1 && args[0].equalsIgnoreCase("Stop")) || (args.length >= 2 && args[1].equalsIgnoreCase("Stop"))) {
                    return true;
                }
                // User wants to delete another player's translation session.
            } else if (args.length > 0 && Bukkit.getServer().getPlayerExact(args[0]) != null && main.isActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]))) {
                if (!sender.hasPermission("worldwidechat.wwct.otherplayers")) {
                    refs.badPermsMessage("worldwidechat.wwct.otherplayers", sender);
                    return false;
                }

                Player testPlayer = Bukkit.getServer().getPlayerExact(args[0]);
                main.removeActiveTranslator(main.getActiveTranslator(testPlayer));
                refs.sendMsg("wwctTranslationStopped", testPlayer);
                refs.sendMsg("wwctTranslationStoppedOtherPlayer", "&6" + args[0], sender);
                stopSound();
                if ((isConsoleSender && args.length == 1) || (args.length >= 2 && args[1] instanceof String && args[1].equalsIgnoreCase("Stop"))) {
                    return true;
                }
            }
            // User wants to delete global translation session
        } else if (args.length > 0 && isGlobal && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")) {
            main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
            for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
                refs.sendMsg("wwcgTranslationStopped", eaPlayer);
            }
            stopSound();
            refs.sendMsg("wwcgTranslationStopped", main.getServer().getConsoleSender());
            if (args.length >= 1 && args[0] instanceof String && args[0].equalsIgnoreCase("Stop")) {
                return true;
            }
        }

        /* NEW TRANSLATION SESSION: Player has given us one argument */
        if (args.length == 1) {
            // If the sender is a player
            if (!isConsoleSender) {
                return startNewTranslationSession(isGlobal ? "GLOBAL-TRANSLATE-ENABLED" : ((Player) sender).getName(), "None", args[0]);
            }
            // If we are console...
            if (isGlobal) {
                return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", "None", args[0]);
            }
            return refs.sendNoConsoleChatMsg(sender);
        }

        /* Player has given us two arguments */
        if (args.length == 2) {
            if (!isGlobal) {
                // If we are passing two languages (or attempting to pass two languages)
                if (Bukkit.getPlayerExact(args[0]) == null) {
                    if (!isConsoleSender) {
                        return startNewTranslationSession(((Player) sender).getName(), args[0], args[1]);
                    }
                    return refs.sendNoConsoleChatMsg(sender);
                    // If we are attempting to pass a player and an outLang
                }
                return startNewTranslationSession(args[0], "None", args[1]);
            }
            return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", args[0], args[1]);
        }

        /* Player has given us three arguments */
        if (args.length == 3 && !isGlobal) {
            return startNewTranslationSession(args[0], args[1], args[2]);
        }
        return false;
    }

    private boolean startNewTranslationSession(String inName, String inLang, String outLang) {
        String fInLang = "";
        String fOutLang = "";

        // Check if inLang/outLang are the same
        if (inLang.equalsIgnoreCase(outLang) || refs.isSameLang(inLang, outLang, "all")) {
            refs.sendMsg("wwctSameLangError", refs.getFormattedLangCodes("in"), "&c", sender);
            return false;
        }
        /* NOTICE:
         * Do not let users use None inputLang with Amazon Translate.
         * Remove this if we ever find a workaround for each. */
        // Check if valid inLang
        if ((!inLang.equalsIgnoreCase("None") && !refs.isSupportedLang(inLang, "in")) ||
                (inLang.equalsIgnoreCase("None") && main.getTranslatorName().equalsIgnoreCase("Amazon Translate"))) {
            refs.sendMsg("wwctInvalidInputLangCode", refs.getFormattedLangCodes("in"), "&c", sender);
            return false;
        }
        // Check if valid outLang
        if (!refs.isSupportedLang(outLang, "out")) {
            refs.sendMsg("wwctInvalidOutputLangCode", refs.getFormattedLangCodes("out"), "&c", sender);
            return false;
        }

        // Check if target is valid player (if not global)
        // Set UUID if valid, else exit
        String inUUID = "";
        Player targetPlayer = null;

        // Format langs to send to player
        fInLang = refs.getSupportedLang(inLang, "in").getNativeLangName().isEmpty() ?
                refs.getSupportedLang(inLang, "in").getLangCode() : refs.getSupportedLang(inLang, "in").getNativeLangName();
        fOutLang = refs.getSupportedLang(outLang, "out").getNativeLangName().isEmpty() ?
                refs.getSupportedLang(inLang, "in").getLangCode() : refs.getSupportedLang(outLang, "out").getNativeLangName();

        if (!isGlobal) {
            targetPlayer = Bukkit.getPlayerExact(inName);
            if (targetPlayer == null) {
                refs.sendMsg("wwcPlayerNotFound", "&6" + inName, "&c", sender);
                return false;
            }
            inUUID = targetPlayer.getUniqueId().toString();
        } else {
            // If global, set UUID to reflect that
            inUUID = "GLOBAL-TRANSLATE-ENABLED";
        }

        /* Check if user is targetting themselves, which doesn't need this permission (or if we are console) */
        boolean targetIsSelf = !isConsoleSender && inUUID.equals((((Player) sender)).getUniqueId().toString());
        if (!isGlobal && !targetIsSelf && !sender.hasPermission("worldwidechat.wwct.otherplayers")) {
            refs.badPermsMessage("worldwidechat.wwct.otherplayers", sender);
            return false;
        }

        /* All input vars have been checked; create new ActiveTranslator */
        if (!isGlobal) {
            if (inLang.equalsIgnoreCase("None")) {
                if (targetIsSelf) {
                    refs.sendMsg("wwctAutoTranslateStart", "&6" + fOutLang, sender);
                } else {
                    refs.sendMsg("wwctAutoTranslateStartOtherPlayer", new String[]{"&6" + targetPlayer.getName(), "&6" + fOutLang}, sender);
                    refs.sendMsg("wwctAutoTranslateStart", "&6" + fOutLang, Bukkit.getPlayer(UUID.fromString(inUUID)));
                }
            } else {
                if (targetIsSelf) {
                    refs.sendMsg("wwctLangToLangStart", new String[]{"&6" + fInLang, "&6" + fOutLang}, sender);
                } else {
                    refs.sendMsg("wwctLangToLangStartOtherPlayer", new String[]{"&6" + targetPlayer.getName(), "&6" + fInLang, "&6" + fOutLang}, sender);
                    refs.sendMsg("wwctLangToLangStart", new String[]{"&6" + fInLang, "&6" + fOutLang}, Bukkit.getPlayer(UUID.fromString(inUUID)));
                }
            }
        } else {
            if (inLang.equalsIgnoreCase("None")) {
                for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
                    refs.sendMsg("wwcgAutoTranslateStart", "&6" + fOutLang, eaPlayer);
                }
                refs.sendMsg("wwcgAutoTranslateStart", "&6" + fOutLang, main.getServer().getConsoleSender());
            } else {
                for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
                    refs.sendMsg("wwcgLangToLangStart", new String[]{"&6" + fInLang, "&6" + fOutLang}, eaPlayer);
                }
                refs.sendMsg("wwcgLangToLangStart", new String[]{"&6" + fInLang, "&6" + fOutLang}, main.getServer().getConsoleSender());
            }
        }
        ActiveTranslator newTranslator = new ActiveTranslator(inUUID, "None", outLang);
        if (!inLang.equalsIgnoreCase("None")) {
            newTranslator.setInLangCode(inLang);
        }
        main.addActiveTranslator(newTranslator);
        if (!isConsoleSender) {
            ((Player)sender).playSound(((Player)sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
        return true;
    }

    private void stopSound() {
        if (!isConsoleSender) {
            ((Player)sender).playSound(((Player)sender).getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 1.0f, 1.0f);
        }
    }

}