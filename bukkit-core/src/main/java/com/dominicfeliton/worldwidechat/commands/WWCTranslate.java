package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.wwctranslategui.WWCTranslateGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WWCTranslate extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private final boolean isGlobal;
    private final boolean isConsoleSender;

    public WWCTranslate(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
        this.isGlobal = this instanceof WWCGlobal;
        this.isConsoleSender = sender instanceof ConsoleCommandSender;
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
        /* Sanitize args */
        if ((isGlobal && args.length > 2) || (!isGlobal && args.length > 3)) {
            // Too many args
            refs.sendMsg("wwctInvalidArgs", "", "&c", sender);
            return false;
        }

        // GUI Checks
        if (handleGuiRequests()) {
            return true;
        }

        /* Existing translator checks */
        if (!isGlobal) {
            // User wants to exit their own translation session.
            if (!isConsoleSender && args.length > 0 && (Bukkit.getServer().getPlayerExact(args[0]) == null
                    || args[0].equalsIgnoreCase(sender.getName())) && main.isActiveTranslator((Player) sender)) {
                ActiveTranslator currTarget = main.getActiveTranslator((Player) sender);
                main.removeActiveTranslator(currTarget);
                refs.sendMsg("wwctTranslationStopped", sender);
                refs.playSound(CommonRefs.SoundType.STOP_TRANSLATION, sender);
                if ((args.length >= 1 && args[0].equalsIgnoreCase("Stop")) || (args.length >= 2 && args[1].equalsIgnoreCase("Stop"))) {
                    return true;
                }
            } else if (args.length > 0 && Bukkit.getServer().getPlayerExact(args[0]) != null
                    && main.isActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]))) {
                if (!sender.hasPermission("worldwidechat.wwct.otherplayers")) {
                    refs.badPermsMessage("worldwidechat.wwct.otherplayers", sender);
                    return false;
                }
                Player testPlayer = Bukkit.getServer().getPlayerExact(args[0]);
                main.removeActiveTranslator(main.getActiveTranslator(testPlayer));
                refs.sendMsg("wwctTranslationStopped", testPlayer);
                refs.sendMsg("wwctTranslationStoppedOtherPlayer", "&6" + args[0], sender);
                refs.playSound(CommonRefs.SoundType.STOP_TRANSLATION, sender);
                if ((isConsoleSender && args.length == 1) || (args.length >= 2 && args[1].equalsIgnoreCase("Stop"))) {
                    return true;
                }
            }
        } else if (args.length > 0 && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")) {
            main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
            for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
                refs.sendMsg("wwcgTranslationStopped", eaPlayer);
            }
            refs.playSound(CommonRefs.SoundType.STOP_TRANSLATION, sender);
            refs.sendMsg("wwcgTranslationStopped", main.getServer().getConsoleSender());
            if (args[0].equalsIgnoreCase("Stop")) {
                return true;
            }
        }

        /* NEW TRANSLATION SESSION: Player has given us one argument */
        if (args.length == 1) {
            if (!isConsoleSender) {
                return startNewTranslationSession(isGlobal ? "GLOBAL-TRANSLATE-ENABLED" : ((Player) sender).getName(), "None", args[0]);
            }
            if (isGlobal) {
                return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", "None", args[0]);
            }
            return refs.sendNoConsoleChatMsg(sender);
        }

        /* Player has given us two arguments */
        if (args.length == 2) {
            if (!isGlobal) {
                if (Bukkit.getPlayerExact(args[0]) == null) {
                    if (!isConsoleSender) {
                        return startNewTranslationSession(((Player) sender).getName(), args[0], args[1]);
                    }
                    return refs.sendNoConsoleChatMsg(sender);
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

    private boolean handleGuiRequests() {
        if (isConsoleSender) {
            return false;
        }
        if (args.length == 0) {
            openGui((Player) sender,
                    isGlobal ? "GLOBAL-TRANSLATE-ENABLED" : ((Player) sender).getUniqueId().toString());
            return true;
        }
        if (args.length == 1 && !isGlobal) {
            if (args[0].equalsIgnoreCase(sender.getName())) {
                openGui((Player) sender, ((Player) sender).getUniqueId().toString());
                return true;
            }
            Player target = Bukkit.getServer().getPlayerExact(args[0]);
            if (target != null) {
                if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
                    openGui((Player) sender, target.getUniqueId().toString());
                } else {
                    refs.badPermsMessage("worldwidechat.wwct.otherplayers", sender);
                }
                return true;
            }
        }
        return false;
    }

    private void openGui(Player viewer, String id) {
        new WWCTranslateGuiMainMenu(id, viewer)
                .getTranslateMainMenu()
                .open(viewer);
    }

    private boolean startNewTranslationSession(String inName, String inLang, String outLang) {
        String fInLang = "";
        String fOutLang = "";

        if (inLang.equalsIgnoreCase(outLang) || refs.isSameLang(inLang, outLang, CommonRefs.LangType.ALL)) {
            refs.sendMsg("wwctSameLangError", refs.getFormattedLangCodes("in"), "&c", sender);
            return false;
        }
        if ((!inLang.equalsIgnoreCase("None") && !refs.isSupportedLang(inLang, CommonRefs.LangType.INPUT)) ||
                (inLang.equalsIgnoreCase("None") && main.getTranslatorName().equalsIgnoreCase("Amazon Translate"))) {
            refs.sendMsg("wwctInvalidInputLangCode", refs.getFormattedLangCodes("in"), "&c", sender);
            return false;
        }
        if (!refs.isSupportedLang(outLang, CommonRefs.LangType.OUTPUT)) {
            refs.sendMsg("wwctInvalidOutputLangCode", refs.getFormattedLangCodes("out"), "&c", sender);
            return false;
        }

        String inUUID;
        Player targetPlayer = null;

        fInLang = refs.getSupportedLang(inLang, CommonRefs.LangType.INPUT).getNativeLangName().isEmpty()
                ? refs.getSupportedLang(inLang, CommonRefs.LangType.INPUT).getLangCode()
                : refs.getSupportedLang(inLang, CommonRefs.LangType.INPUT).getNativeLangName();
        fOutLang = refs.getSupportedLang(outLang, CommonRefs.LangType.OUTPUT).getNativeLangName().isEmpty()
                ? refs.getSupportedLang(outLang, CommonRefs.LangType.OUTPUT).getLangCode()
                : refs.getSupportedLang(outLang, CommonRefs.LangType.OUTPUT).getNativeLangName();

        if (!isGlobal) {
            targetPlayer = Bukkit.getPlayerExact(inName);
            if (targetPlayer == null) {
                refs.sendMsg("wwcPlayerNotFound", "&6" + inName, "&c", sender);
                return false;
            }
            inUUID = targetPlayer.getUniqueId().toString();
        } else {
            inUUID = "GLOBAL-TRANSLATE-ENABLED";
        }

        boolean targetIsSelf = !isConsoleSender
                && inUUID.equals(((Player) sender).getUniqueId().toString());
        if (!isGlobal && !targetIsSelf && !sender.hasPermission("worldwidechat.wwct.otherplayers")) {
            refs.badPermsMessage("worldwidechat.wwct.otherplayers", sender);
            return false;
        }

        if (!isGlobal) {
            if (inLang.equalsIgnoreCase("None")) {
                if (targetIsSelf) {
                    refs.sendMsg("wwctAutoTranslateStart", "&6" + fOutLang, sender);
                } else {
                    refs.sendMsg("wwctAutoTranslateStartOtherPlayer",
                            new String[]{"&6" + targetPlayer.getName(), "&6" + fOutLang}, sender);
                    refs.sendMsg("wwctAutoTranslateStart", "&6" + fOutLang,
                            Bukkit.getPlayer(UUID.fromString(inUUID)));
                }
            } else {
                if (targetIsSelf) {
                    refs.sendMsg("wwctLangToLangStart",
                            new String[]{"&6" + fInLang, "&6" + fOutLang}, sender);
                } else {
                    refs.sendMsg("wwctLangToLangStartOtherPlayer",
                            new String[]{"&6" + targetPlayer.getName(), "&6" + fInLang, "&6" + fOutLang}, sender);
                    refs.sendMsg("wwctLangToLangStart",
                            new String[]{"&6" + fInLang, "&6" + fOutLang},
                            Bukkit.getPlayer(UUID.fromString(inUUID)));
                }
            }
        } else {
            if (inLang.equalsIgnoreCase("None")) {
                for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
                    refs.sendMsg("wwcgAutoTranslateStart", "&6" + fOutLang, eaPlayer);
                }
                refs.sendMsg("wwcgAutoTranslateStart", "&6" + fOutLang,
                        main.getServer().getConsoleSender());
            } else {
                for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
                    refs.sendMsg("wwcgLangToLangStart",
                            new String[]{"&6" + fInLang, "&6" + fOutLang}, eaPlayer);
                }
                refs.sendMsg("wwcgLangToLangStart",
                        new String[]{"&6" + fInLang, "&6" + fOutLang},
                        main.getServer().getConsoleSender());
            }
        }

        ActiveTranslator newTranslator = new ActiveTranslator(inUUID, "None", outLang);
        if (!inLang.equalsIgnoreCase("None")) {
            newTranslator.setInLangCode(inLang);
        }
        main.addActiveTranslator(newTranslator);
        refs.playSound(CommonRefs.SoundType.START_TRANSLATION, sender);
        return true;
    }

}