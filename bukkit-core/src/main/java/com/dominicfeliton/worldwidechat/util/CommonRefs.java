package com.dominicfeliton.worldwidechat.util;

import com.cryptomorin.xseries.XSound;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.translators.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import fr.minuskube.inv.SmartInventory;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.zone.ZoneRulesException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;

public class CommonRefs {

    /* Important vars */
    private static WorldwideChat main = WorldwideChat.instance;

    private static WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    public static String[] supportedMCVersions = {"1.21", "1.20", "1.19", "1.18", "1.17", "1.16", "1.15", "1.14", "1.13"};

    public static final Map<String, SupportedLang> supportedPluginLangCodes = new LinkedHashMap<>();

    static {
        Map<String, SupportedLang> tempMap = new LinkedHashMap<>();
        List.of("af", "am", "ar", "az", "bg", "bn", "bs", "ca", "cs", "cy", "da", "de", "el", "en", "es", "es-MX", "et", "fa", "fa-AF", "fi", "fr", "fr-CA",
                        "ga", "gu", "ha", "he", "hi", "hr", "ht", "hu", "hy", "id", "is", "it", "ja", "ka", "kk", "kn", "ko", "lt", "lv", "mk", "ml", "mn", "mr", "ms", "mt", "nl", "no", "pa", "pl", "ps", "pt",
                        "pt-PT", "ro", "ru", "si", "sk", "sl", "so", "sq", "sr", "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "ur", "uz", "vi", "zh", "zh-TW")
                .forEach(langCode -> tempMap.put(langCode, new SupportedLang(langCode, "", "")));

        Map<String, SupportedLang> fixedMap = new CommonRefs().fixLangNames(tempMap, false, true);
        supportedPluginLangCodes.putAll(fixedMap);
    }

    public static final Map<String, String> translatorPairs = new HashMap<>();

    static {
        translatorPairs.put("Translator.useGoogleTranslate", "Google Translate");
        translatorPairs.put("Translator.useAmazonTranslate", "Amazon Translate");
        translatorPairs.put("Translator.useLibreTranslate", "Libre Translate");
        translatorPairs.put("Translator.useDeepLTranslate", "DeepL Translate");
        translatorPairs.put("Translator.useAzureTranslate", "Azure Translate");
        translatorPairs.put("Translator.useSystranTranslate", "Systran Translate");
        translatorPairs.put("Translator.useChatGPT", "ChatGPT");
        translatorPairs.put("Translator.useOllama", "Ollama");

        // For testing only!
        translatorPairs.put("Translator.testModeTranslator", "JUnit/MockBukkit Testing Translator");
    }

    public static final Map<String, Map<String, String>> tableSchemas = new HashMap<>();

    static {
        // Linked hashmaps to preserve order
        // PRIMARY KEY == playerUUID, randomUUID on persistentCache
        HashMap<String, String> activeTranslatorsSchema = new LinkedHashMap<>();
        activeTranslatorsSchema.put("creationDate", "VARCHAR(40)");
        activeTranslatorsSchema.put("playerUUID", "VARCHAR(40)");
        activeTranslatorsSchema.put("inLangCode", "VARCHAR(10)");
        activeTranslatorsSchema.put("outLangCode", "VARCHAR(10)");
        activeTranslatorsSchema.put("rateLimit", "INT");
        activeTranslatorsSchema.put("rateLimitPreviousTime", "VARCHAR(40)");
        activeTranslatorsSchema.put("translatingChatOutgoing", "BOOLEAN");
        activeTranslatorsSchema.put("translatingChatIncoming", "BOOLEAN");
        activeTranslatorsSchema.put("translatingBook", "BOOLEAN");
        activeTranslatorsSchema.put("translatingSign", "BOOLEAN");
        activeTranslatorsSchema.put("translatingItem", "BOOLEAN");
        activeTranslatorsSchema.put("translatingEntity", "BOOLEAN");
        tableSchemas.put("activeTranslators", activeTranslatorsSchema);

        Map<String, String> playerRecordsSchema = new LinkedHashMap<>();
        playerRecordsSchema.put("creationDate", "VARCHAR(40)");
        playerRecordsSchema.put("playerUUID", "VARCHAR(40)");
        playerRecordsSchema.put("attemptedTranslations", "INT");
        playerRecordsSchema.put("successfulTranslations", "INT");
        playerRecordsSchema.put("lastTranslationTime", "VARCHAR(40)");
        playerRecordsSchema.put("localizationCode", "VARCHAR(10)");
        tableSchemas.put("playerRecords", playerRecordsSchema);

        Map<String, String> cachedTermsSchema = new LinkedHashMap<>();
        cachedTermsSchema.put("randomUUID", "VARCHAR(40)");
        cachedTermsSchema.put("inputLang", "VARCHAR(10)");
        cachedTermsSchema.put("outputLang", "VARCHAR(10)");
        cachedTermsSchema.put("inputPhrase", "VARCHAR(260)");
        cachedTermsSchema.put("outputPhrase", "VARCHAR(260)");
        tableSchemas.put("persistentCache", cachedTermsSchema);
    }

    public enum LangType {
        INPUT("in"),
        OUTPUT("out"),
        ALL("all"),
        LOCAL("local");

        private final String type;

        LangType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static LangType fromString(String type) {
            for (LangType langType : LangType.values()) {
                if (langType.type.equalsIgnoreCase(type)) {
                    return langType;
                }
            }
            throw new IllegalArgumentException("Unknown langType: " + type);
        }
    }

    public enum SoundType {
        SUBMENU_TOGGLE_ON("SUBMENU_TOGGLE_ON", XSound.matchXSound(Sound.BLOCK_NOTE_BLOCK_HAT).parseSound(), 0.5f, 1.0f),
        SUBMENU_TOGGLE_OFF("SUBMENU_TOGGLE_OFF", XSound.matchXSound(Sound.BLOCK_NOTE_BLOCK_SNARE).parseSound(), 0.5f, 1.0f),
        START_TRANSLATION("START_TRANSLATION", XSound.matchXSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).parseSound(), 1.0f, 1.0f),
        STOP_TRANSLATION("STOP_TRANSLATION", XSound.matchXSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF).parseSound(), 1.0f, 1.0f),
        RELOAD_SUCCESS("RELOAD_SUCCESS", XSound.matchXSound(Sound.BLOCK_BEACON_ACTIVATE).parseSound(), 1.0f, 1.0f),
        RELOAD_ERROR("RELOAD_ERROR", XSound.matchXSound(Sound.BLOCK_DISPENSER_FAIL).parseSound(), 1.0f, 1.0f),
        STATS_SUCCESS("STATS_SUCCESS",
                main.getCurrMCVersion().contains("1.13") ?
                        XSound.matchXSound(Sound.BLOCK_NOTE_BLOCK_PLING).parseSound() :
                        XSound.matchXSound(Sound.ITEM_BOOK_PAGE_TURN).parseSound(),
                1.0f, 1.0f),
        STATS_FAIL("STATS_FAIL", XSound.matchXSound(Sound.BLOCK_NOTE_BLOCK_BASS).parseSound(), 1.0f, 1.0f),
        WWC_VERSION("WWC_VERSION", XSound.matchXSound(Sound.ENTITY_PLAYER_LEVELUP).parseSound(), 1.0f, 1.0f),
        PENDING_RELOAD("PENDING_RELOAD", XSound.matchXSound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE).parseSound(), 1.0f, 1.0f);

        private final String name;
        private final Sound sound;
        private final Float float1;
        private final Float float2;

        SoundType(String name, Sound sound, Float float1, Float float2) {
            this.name = name;
            this.sound = sound;
            this.float1 = float1;
            this.float2 = float2;
        }

        public String getName() {
            return name;
        }

        public Sound getSound() {
            return sound;
        }

        public Float getFloat1() {
            return float1;
        }

        public Float getFloat2() {
            return float2;
        }

        public static SoundType fromString(String name) {
            for (SoundType soundType : SoundType.values()) {
                if (soundType.name.equalsIgnoreCase(name)) {
                    return soundType;
                }
            }
            throw new IllegalArgumentException("Unknown soundType: " + name);
        }
    }

    /**
     * Compares two strings to check if they are the same language under the current translator.
     *
     * @param first    - A valid language name
     * @param second   - A valid language name
     * @param langType - 'out' or 'in' or 'local' are three valid inputs for this
     * @return Boolean - Whether languages are the same or not
     */
    public boolean isSameLang(String first, String second, LangType langType) {
        return isSupportedLang(first, langType) && isSupportedLang(second, langType)
                && getSupportedLang(first, langType).equals(getSupportedLang(second, langType));
    }

    /**
     * Gets a supported language under the current translator.
     *
     * @param langName - A valid language name
     * @param langType - 'out' or 'in' or 'local' are three valid inputs for this;
     *                 'out' will check if in is a valid output lang, 'in' will check the input lang list
     *                 'all' will check both lists
     *                 'local' will check local lang list
     * @return SupportedLanguageObject - Will be completely empty if the language is invalid
     */
    public SupportedLang getSupportedLang(String langName, LangType langType) {
        // Setup vars
        SupportedLang invalidLang = new SupportedLang("", "", "");
        SupportedLang outLang;

        // Return None if none
        if (langName.equalsIgnoreCase("None") || langName.equalsIgnoreCase("auto")) {
            outLang = new SupportedLang("auto", "None", "None");
            return outLang;
        }

        // Check langType using enum
        switch (langType) {
            case INPUT:
                outLang = main.getSupportedInputLangs().get(langName);
                break;
            case OUTPUT:
                outLang = main.getSupportedOutputLangs().get(langName);
                break;
            case ALL:
                outLang = main.getSupportedInputLangs().get(langName);
                if (outLang == null) {
                    outLang = main.getSupportedOutputLangs().get(langName);
                }
                break;
            case LOCAL:
                outLang = supportedPluginLangCodes.get(langName);
                break;
            default:
                debugMsg("Invalid langType for getSupportedTranslatorLang()! langType: " + langType + " ...returning invalid, not checking language. Fix this!!!");
                outLang = null;
                break;
        }

        if (outLang == null) {
            debugMsg("Lang " + langName + " not found in " + langType + "!");
            return invalidLang;
        }

        return outLang;
    }

    public void playSound(SoundType type, CommandSender sender) {
        if (!main.isSoundEnabled() || !(sender instanceof Player) || main.getTranslatorName().equalsIgnoreCase("JUnit/MockBukkit Testing Translator")) {
            debugMsg("No sound - not enabled or not a player!");
            return;
        }

        GenericRunnable run = new GenericRunnable() {
            @Override
            protected void execute() {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), type.sound, type.float1, type.float2);
            }
        };

        wwcHelper.runSync(run, WorldwideChatHelper.SchedulerType.ENTITY, new Object[] {(Player)sender});
    }

    /**
     * Checks if a language is supported under the current translator.
     *
     * @param in       - A valid language name
     * @param langType - 'out' or 'in' are two valid inputs for this;
     *                 'out' will check if in is a valid output lang, 'in' will check the input lang list
     *                 'all' will check both lists
     * @return true if supported, false otherwise
     */
    public boolean isSupportedLang(String in, LangType langType) {
        return !getSupportedLang(in, langType).getLangCode().isEmpty();
    }

    /**
     * Gets a list of properly formatted, supported language codes.
     *
     * @param langType - 'out' or 'in' are two valid inputs for this;
     *                 'out' will check if in is a valid output lang, 'in' will check the input lang list
     * @return String - Formatted language codes
     */
    public String getFormattedLangCodes(String langType) {
        /* Setup vars */
        Map<String, SupportedLang> langMap;
        StringBuilder out = new StringBuilder("\n");

        /* Check langType */
        switch (langType.toLowerCase()) {
            case "in":
                langMap = main.getSupportedInputLangs();
                break;
            case "out":
                langMap = main.getSupportedOutputLangs();
                break;
            case "local":
                langMap = CommonRefs.supportedPluginLangCodes;
                break;
            default:
                debugMsg("Invalid langType for getFormattedValidLangCodes()! langType: " + langType + " ...returning invalid, not checking language. Fix this!!!");
                return "&cInvalid language type specified";
        }

        /* Use a TreeSet to eliminate duplicates and sort */
        TreeSet<SupportedLang> sortedUniqueLangs = new TreeSet<>(langMap.values());

        /* Format the output nicely */
        for (SupportedLang lang : sortedUniqueLangs) {
            if (lang == null) {
                debugMsg("Lang codes not set for " + langType + "! FIX THIS");
                out.append("N/A");
                break;
            }
            out.append("&b").append(lang.getLangCode())
                    .append(" &f- ")
                    .append("&e").append(lang.getLangName()).append("&6/&e").append(lang.getNativeLangName())
                    .append("&r, ");
        }

        // Remove the last comma and space if present
        if (out.length() > 2) {
            out.setLength(out.length() - 2);
        }

        return out.toString();
    }

    /**
     * Closes all inventories registered by WorldwideChat.
     */
    public void closeAllInvs() {
        // Close all active GUIs
        main.getPlayersUsingGUI().clear();
        for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
            try {
                SmartInventory currInventory = main.getInventoryManager().getInventory(eaPlayer).get();
                if (currInventory instanceof SmartInventory
                        && currInventory.getManager()
                        .equals(main.getInventoryManager())) {
                    eaPlayer.closeInventory();
                }
            } catch (NoSuchElementException e) {
                continue;
            }
        }
    }

    /**
     * Sends a debug message to console. Will only work when debug mode is set to true in the Console.
     *
     * @param inMessage - The debug message that will be sent to the Console.
     */
    public void debugMsg(String inMessage) {
        if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
            main.getLogger().warning("DEBUG: " + inMessage);
        }
    }

    public String getPlainMsg(String messageName) {
        return serial(getCompMsg(messageName, new String[]{}, "", null));
    }

    public String getPlainMsg(String messageName, CommandSender sender) {
        return serial(getCompMsg(messageName, new String[]{}, "", sender));
    }

    public String getPlainMsg(String messageName, String replacement) {
        return serial(getCompMsg(messageName, new String[]{replacement}, "", null));
    }

    public String getPlainMsg(String messageName, String replacement, String resetCode) {
        return serial(getCompMsg(messageName, new String[]{replacement}, resetCode, null));
    }

    public String getPlainMsg(String messageName, String replacement, CommandSender sender) {
        return serial(getCompMsg(messageName, new String[]{replacement}, "", sender));
    }

    public String getPlainMsg(String messageName, String[] replacements, CommandSender sender) {
        return serial(getCompMsg(messageName, replacements, "", sender));
    }

    public String getPlainMsg(String messageName, String replacement, String resetCode, CommandSender sender) {
        return serial(getCompMsg(messageName, new String[]{replacement}, resetCode, sender));
    }

    public String getPlainMsg(String messageName, String[] replacements, String resetCode) {
        return serial(getCompMsg(messageName, replacements, resetCode, null));
    }

    /**
     * Gets a plaintext message from the config with proper formatting
     *
     * @param messageName
     * @param replacements
     * @param resetCode
     * @param sender
     * @return
     */
    public String getPlainMsg(String messageName, String[] replacements, String resetCode, CommandSender sender) {
        return serial(getCompMsg(messageName, replacements, resetCode, sender));
    }

    /**
     * Gets a message from the currently selected messages-XX.yml.
     *
     * @param messageName  - The name of the message from messages-XX.yml.
     * @param replacements - The list of replacement values that replace variables in the selected message. There is no sorting system; the list must be already sorted.
     * @param resetCode    - The color code sequence (&4&l, etc.) that the rest of the message should use besides the replacement values.
     * @param sender       - The person/entity to be sent this message. Can be null for nobody in particular.
     * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
     */
    public TextComponent getCompMsg(String messageName, String[] replacements, String resetCode, CommandSender sender) {
        YamlConfiguration messagesConfig = main.getConfigManager().getMsgsConfig();
        String code = "";
        String globalCode = main.getConfigManager().getMainConfig().getString("General.pluginLang");
        if (sender instanceof Player && main.isPlayerRecord((Player) sender)) {
            code = main.getPlayerRecord((Player) sender, false).getLocalizationCode();
            if (!code.isEmpty()) {
                messagesConfig = main.getConfigManager().getCustomMessagesConfig(code);
            }
        }

        for (int i = 0; i < replacements.length; i++) {
            // Translate color codes in replacements
            replacements[i] = ChatColor.translateAlternateColorCodes('&', replacements[i] + resetCode);
        }

        /* Get message from messages.yml */
        String convertedOriginalMessage = resetCode;
        if (messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)) != null) {
            convertedOriginalMessage += ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)));
        } else {
            if (messagesConfig.getString("Messages." + ChatColor.stripColor(messageName)) == null) {
                if (code.isEmpty()) {
                    main.getLogger().severe("Bad message (" + messageName + ")! Please fix your messages-" + globalCode + ".yml.");
                    return Component.text().content(ChatColor.RED + "Bad message (" + messageName + ")! Please fix your messages-" + globalCode + ".yml.").build();
                } else {
                    main.getLogger().severe("Bad message (" + messageName + ")! Please fix your messages-" + code + ".yml.");
                    return Component.text().content(ChatColor.RED + "Bad message (" + messageName + ")! Please fix your messages-" + code + ".yml.").build();
                }
            }
            convertedOriginalMessage += messagesConfig.getString("Messages." + ChatColor.stripColor(messageName));
        }

        // Translate color codes in the original message
        convertedOriginalMessage = ChatColor.translateAlternateColorCodes('&', convertedOriginalMessage);

        // Escape single quotes for MessageFormat
        convertedOriginalMessage = convertedOriginalMessage.replace("'", "''").trim();

        // Return fixedMessage with replaced vars
        return Component.text().content(MessageFormat.format(convertedOriginalMessage, (Object[]) replacements)).build();
    }

    public void sendMsg(String messageName, CommandSender sender) {
        sendMsg(sender, getCompMsg(messageName, new String[]{}, "&r&d", sender));
    }

    public void sendMsg(String messageName, String[] replacements, CommandSender sender) {
        // Default WWC color is usually LIGHT_PURPLE (&d)
        sendMsg(sender, getCompMsg(messageName, replacements, "&r&d", sender));
    }

    public void sendMsg(String messageName, String replacement, CommandSender sender) {
        sendMsg(sender, getCompMsg(messageName, new String[]{replacement}, "&r&d", sender));
    }

    public void sendMsg(String messageName, String replacement, String resetCode, CommandSender sender) {
        sendMsg(sender, getCompMsg(messageName, new String[]{replacement}, "&r" + resetCode, sender));
    }

    public void sendMsg(String messageName, String[] replacements, String resetCode, CommandSender sender) {
        sendMsg(sender, getCompMsg(messageName, replacements, "&r" + resetCode, sender));
    }

    /**
     * Sends the user a properly formatted message through our adventure instance.
     *
     * @param sender          - The target sender. Can be any entity that can receive messages. CANNOT BE NULL.
     * @param originalMessage - The unformatted Component that should be sent to sender.
     * @return
     */
    public void sendMsg(CommandSender sender, Component originalMessage) {
        try {
            Audience adventureSender = main.adventure().sender(sender);
            final TextComponent outMessage = Component.text().append(main.getPluginPrefix().asComponent())
                    .append(Component.space())
                    .append(originalMessage.asComponent())
                    .build();
            if (sender instanceof ConsoleCommandSender) {
                main.getServer().getConsoleSender().sendMessage((ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(outMessage))));
            } else {
                adventureSender.sendMessage(outMessage);
            }
        } catch (IllegalStateException e) {
        }
    }

    public void sendMsg(CommandSender sender, String stringMsg) {
        sendMsg(sender, deserial(stringMsg));
    }

    /**
     * Shorthand for component to str
     *
     * @param comp - TextComponent
     * @return string version
     */
    public String serial(Component comp) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(comp);
    }

    /**
     * Shorthand for str to component
     *
     * @param str
     * @return textcomponent version
     */
    public Component deserial(String str) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
    }

    /**
     * Translates array of Strings.
     * All of them can be marked as "1" translation and bypass the rate limit.
     * @param arrayOfMsgs
     * @param currPlayer
     * @param countAsOneRequest
     * @return
     */
    public String[] translateText(String[] arrayOfMsgs, Player currPlayer, boolean countAsOneRequest) {
        // Don't translate if 1) we care about the rate limit and 2) they have a rate limit blocker
        boolean inCache = false;
        debugMsg(Arrays.toString(arrayOfMsgs));
        for (String msg : arrayOfMsgs) {
            ActiveTranslator trans = main.getActiveTranslator(currPlayer);
            if (main.hasCacheTerm(new CachedTranslation(trans.getInLangCode(), trans.getOutLangCode(), msg))) {
                inCache = true;
            } else if (!msg.isEmpty()) {
                inCache = false;
                break;
            }
        }
        debugMsg(inCache + " <-- array in cache?");
        if (!inCache && countAsOneRequest && shouldRateLimit(false, currPlayer)) return arrayOfMsgs;

        // Either we are ignoring the rate limit or the user is not being rate limited here.
        String[] out = new String[arrayOfMsgs.length];
        for (int i = 0; i < arrayOfMsgs.length; i++) {
            out[i] = (translateText(arrayOfMsgs[i], currPlayer, countAsOneRequest));
        }
        return out;
    }

    /**
     * Translates list of Strings.
     * All of them can be marked as "1" translation and bypass the rate limit.
     * @param listOfMsgs
     * @param currPlayer
     * @param countAsOneRequest
     * @return
     */
    // TODO: if phrases are in cache,
    public List<String> translateText(List<String> listOfMsgs, Player currPlayer, boolean countAsOneRequest) {
        // Don't translate if 1) we care about the rate limit and 2) they have a rate limit blocker
        boolean inCache = false;
        for (String msg : listOfMsgs) {
            ActiveTranslator trans = main.getActiveTranslator(currPlayer);
            if (main.hasCacheTerm(new CachedTranslation(trans.getInLangCode(), trans.getOutLangCode(), msg))) {
                inCache = true;
            } else if (!msg.isEmpty()) {
                inCache = false;
                break;
            }
        }
        if (!inCache && countAsOneRequest && shouldRateLimit(false, currPlayer)) return listOfMsgs;

        // Either we are ignoring the rate limit or the user is not being rate limited here.
        List<String> out = new ArrayList<>();
        for (String str : listOfMsgs) {
            out.add(translateText(str, currPlayer, countAsOneRequest));
        }
        return out;
    }

    public String translateText(String inMessage, Player currPlayer) {
        return translateText(inMessage, currPlayer, false);
    }

    /**
     * Translates text using the selected translator.
     *
     * @param inMessage  - The original message to be translated.
     * @param currPlayer - The player who wants this message to be translated.
     * @return String - The translated message. If this is equal to inMessage, the translation failed.
     */
    public String translateText(String inMessage, Player currPlayer, boolean ignoreRateLimit) {
        /* If translator settings are invalid, do not do this... */
        debugMsg("translateText() call using " + main.getTranslatorName());
        if (inMessage.isEmpty() || serverIsStopping() || main.getTranslatorName().equals("Starting") || main.getTranslatorName().equals("Invalid")) {
            return inMessage;
        }
        YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();

        /* Main logic callback */
        Callable<String> result = () -> {
            // Init vars
            boolean isExempt = ignoreRateLimit;
            boolean isBlacklistExempt = false;
            int personalRateLimit = 0;
            String permissionCheck = "";

            /* Detect color codes in message */
            detectColorCodes(inMessage, currPlayer);

            /* Modify or create new player record */
            PlayerRecord currPlayerRecord = main
                    .getPlayerRecord(currPlayer, true);
            if (main.getServer().getPluginManager().getPlugin("DeluxeChat") == null)
                currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations() + 1);

            /* Initialize current vars + ActiveTranslator, sanity checks */
            ActiveTranslator currActiveTranslator;
            if (!main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
                    && (main.isActiveTranslator(currPlayer))) {
                currActiveTranslator = main.getActiveTranslator(currPlayer);
            } else if (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
                    && (main.isActiveTranslator(currPlayer))) {
                currActiveTranslator = main.getActiveTranslator(currPlayer);
            } else {
                currActiveTranslator = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
            }

            /* Char limit check */
            int limit = main.getMessageCharLimit();
            if (inMessage.length() > limit) {
                sendMsg("wwcCharLimit", "&c" + limit, "&6", currPlayer);
                return inMessage;
            }

            /* Check blacklist */
            if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") && !serverIsStopping() && !main.getCurrPlatform().equals("Folia")) {
                try {
                    isBlacklistExempt = Bukkit.getScheduler().callSyncMethod(main, () -> currPlayer.hasPermission("worldwidechat.blacklist.exempt")).get(3, TimeUnit.SECONDS);
                } catch (TimeoutException | InterruptedException e) {
                    debugMsg("Timeout from permission checks should never happen, unless the server is stopping or /reloading. "
                            + "If it isn't, and we can't fetch a user permission in less than ~2.5 seconds, we have a problem.");
                    return inMessage;
                }
            } else if (main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") || main.getCurrPlatform().equals("Folia")) {
                isBlacklistExempt = currPlayer.hasPermission("worldwidechat.blacklist.exempt");
            }

            // Run blacklist if user is not exempt
            if (!isBlacklistExempt) {
                debugMsg("Checking blacklist!");
                for (String eaWord : main.getBlacklistTerms()) {
                    if (inMessage.contains(eaWord)) {
                        sendMsg("wwcBlacklistedMsg", new String[]{}, "&c", currPlayer);
                        debugMsg(getPlainMsg("wwcBlacklistedMsgDetected", new String[]{eaWord, inMessage}, ""));
                        return inMessage;
                    }
                }
            }

            /* Check cache */
            CachedTranslation testTranslation = new CachedTranslation(currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), inMessage);
            String testCache = main.getCacheTerm(testTranslation);

            if (testCache != null) {
                currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
                currPlayerRecord.setLastTranslationTime();
                return StringEscapeUtils.unescapeJava(
                        ChatColor.translateAlternateColorCodes('&', testCache));
            }

            /* Check rate limit */
            if (shouldRateLimit(ignoreRateLimit, currPlayer)) {
                return inMessage;
            }

            /* Begin actual translation, set message to output */
            String out = inMessage;
            debugMsg("Translating a message (in " + currActiveTranslator.getInLangCode() + ") from user " + currActiveTranslator.getUUID() + " to " + currActiveTranslator.getOutLangCode() + ".");
            out = getTranslatorResult(main.getTranslatorName(), inMessage, currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), false);

            /* Update stats */
            currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
            currPlayerRecord.setLastTranslationTime();

            /* Add to cache */
            if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
                main.addCacheTerm(testTranslation, out);
            }
            return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', out));
        };

        /* Start Callback Process */
        Future<String> process = main.getCallbackExecutor().submit(result);
        String finalOut = inMessage;
        try {
            /* Get translation */
            finalOut = process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            /* Sanitize error before proceeding to write it to errorLog */
            if (e instanceof InterruptedException || main.getTranslatorName().equals("Starting")) {
                // If we are getting stopped by onDisable, end this immediately.
                debugMsg("Interrupted translateText(), or server state is changing...");
                return inMessage;
            } else if (e instanceof ExecutionException && e.getCause() != null && isErrorToIgnore(e.getCause())) {
                // If the translator has low confidence
                debugMsg("Low confidence from current translator!");
                return inMessage;
            } else if (e instanceof TimeoutException) {
                // If we get a timeoutexception
                sendTimeoutExceptionMsg(currPlayer);
                return inMessage;
            }

            /* Add 1 to error count */
            main.setTranslatorErrorCount(main.getTranslatorErrorCount() + 1);
            sendMsg("wwcTranslatorError", "", "&c", currPlayer);
            main.getLogger()
                    .severe(getPlainMsg("wwcTranslatorErrorConsole", "&6" + currPlayer.getName(), "&c", null));
            debugMsg(ExceptionUtils.getStackTrace(e));

            /* Write to log file */
            File errorLog = new File(main.getDataFolder(), "errorLog.txt");
            try {
                FileWriter fw = new FileWriter(errorLog, true);
                String dateStr = "";
                try {
                    debugMsg(ZoneId.systemDefault().toString());
                    LocalDate date = LocalDate.now();
                    LocalTime time = LocalTime.now();
                    dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - "))
                            + time.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                } catch (ZoneRulesException noTZ) {
                    // This error occurs when we can't fetch the local TZ for whatever reason.
                    // If this error happens on all machines then it may be something I have to further investigate.
                    // For now, only seems to happen on my macOS device.
                    dateStr = "(Unable to fetch local TZ, so here's an ugly date) - " + Instant.now();
                }

                fw.write("========== " + dateStr + " ==========");
                fw.write(System.getProperty("line.separator"));
                fw.write(ExceptionUtils.getStackTrace(e));
                fw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            debugMsg("Error Count: " + main.getTranslatorErrorCount());

            /* If error count is greater than threshold set in config.yml, reload on this thread (we are already async) */
            if (main.getTranslatorErrorCount() >= main.getErrorLimit()) {
                main.getLogger().severe(getPlainMsg("wwcTranslatorErrorThresholdReached"));
                main.getLogger().severe(getPlainMsg("wwcTranslatorErrorThresholdReachedCheckLogs"));
                wwcHelper.runSync(new GenericRunnable() {
                    @Override
                    protected void execute() {
                        main.reload();
                    }
                }, GLOBAL, null);
            }
        }

        /* Return final result */
        return finalOut;
    }

    public String getTranslatorResult(String translatorName, boolean isInitializing) throws ExecutionException, InterruptedException, TimeoutException {
        return getTranslatorResult(translatorName, "", "", "", true);
    }

    /**
     * Gets a response from the currently selected translator, throws exceptions on failure
     *
     * @param translatorName - Name of translator to target
     * @param inMessage      - The message to translate
     * @param inLangCode     - The input language code
     * @param outLangCode    - The output language code
     * @param isInitializing - Whether the plugin is starting up or not
     * @return
     * @throws ExecutionException   - If there is a normal exception
     * @throws InterruptedException - If we interrupt the callback
     * @throws TimeoutException     - If the callback times out
     */
    public String getTranslatorResult(String translatorName, String inMessage, String inLangCode, String outLangCode, boolean isInitializing) throws ExecutionException, InterruptedException, TimeoutException {
        String out = inMessage;
        YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();

        switch (translatorName) {
            case "Google Translate":
                GoogleTranslation googleTranslateInstance = new GoogleTranslation(
                        mainConfig.getString("Translator.googleTranslateAPIKey"),
                        isInitializing,
                        main.getCallbackExecutor()
                );
                out = googleTranslateInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "Amazon Translate":
                AmazonTranslation amazonTranslateInstance = new AmazonTranslation(
                        mainConfig.getString("Translator.amazonAccessKey"),
                        mainConfig.getString("Translator.amazonSecretKey"),
                        mainConfig.getString("Translator.amazonRegion"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = amazonTranslateInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "Libre Translate":
                LibreTranslation libreTranslateInstance = new LibreTranslation(
                        mainConfig.getString("Translator.libreAPIKey"),
                        mainConfig.getString("Translator.libreURL"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = libreTranslateInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "DeepL Translate":
                DeepLTranslation deeplTranslateInstance = new DeepLTranslation(
                        mainConfig.getString("Translator.deepLAPIKey"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = deeplTranslateInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "Azure Translate":
                AzureTranslation azureTranslateInstance = new AzureTranslation(
                        mainConfig.getString("Translator.azureAPIKey"),
                        mainConfig.getString("Translator.azureRegion"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = azureTranslateInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "Systran Translate":
                SystranTranslation systranTranslateInstance = new SystranTranslation(
                        mainConfig.getString("Translator.systranAPIKey"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = systranTranslateInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "ChatGPT":
                OpenAITranslation openAITranslationInstance = new OpenAITranslation(
                        mainConfig.getString("Translator.chatGPTAPIKey"),
                        mainConfig.getString("Translator.chatGPTURL"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = openAITranslationInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "Ollama":
                OllamaTranslation ollamaInstance = new OllamaTranslation(
                        mainConfig.getString("Translator.ollamaURL"),
                        isInitializing,
                        main.getCallbackExecutor());
                out = ollamaInstance.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            case "JUnit/MockBukkit Testing Translator":
                TestTranslation testTranslator = new TestTranslation(
                        isInitializing,
                        main.getCallbackExecutor()
                );
                out = testTranslator.useTranslator(inMessage, inLangCode, outLangCode);
                break;
            default:
                // Get here if we are adding a new translation service
                debugMsg("No valid translator currently in use, according to getTranslatorResult(). Returning original message...");
                return inMessage;
        }
        return out;
    }

    /**
     * Sends a message that console cannot have a translation session for itself.
     *
     * @param sender - Who will receive the message
     * @return Returns false, so that a command can return this method.
     */
    public boolean sendNoConsoleChatMsg(CommandSender sender) {
        sendMsg("wwctCannotTranslateConsole", "", "&c", sender);
        return false;
    }

    /**
     * Sends a message to console and a sender that a timeout exception has occurred.
     *
     * @param sender - Who will receive the message besides console.
     * @return Returns true, so that a command can return this method.
     */
    public boolean sendTimeoutExceptionMsg(CommandSender sender) {
        if (sender instanceof Player) {
            main.getLogger().warning(getPlainMsg("wwcTimeoutExceptionConsole", sender.getName(), (Player) sender));
        }
        sendMsg("wwcTimeoutException", sender);
        return true;
    }

    /**
     * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
     * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
     *
     * @return Boolean - Whether the server is reloading/stopping or not
     */
    public boolean serverIsStopping() {
        boolean stopping = !main.isEnabled();
        debugMsg("Bukkit initial stop check: " + stopping);
        if (stopping) return true;

        try {
            Bukkit.getScheduler().runTaskLater(main, () -> {
            }, 0L);
        } catch (IllegalPluginAccessException | IllegalStateException e) {
            debugMsg("Server is stopping! Don't run a task/do any weird stuff.");
            return true;
        }

        debugMsg("Bukkit final stop check: false");
        return false;
    }

    /**
     * Returns a check or an X if true/false with RED/GREEN/BOLD coloring.
     *
     * @param inBool
     * @return X or check mark
     */
    public String checkOrX(boolean inBool) {
        if (inBool) {
            return ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713";
        }
        return ChatColor.BOLD + "" + ChatColor.RED + "\u2717";
    }

    /**
     * Detects an "outdated" MySQL/PostgreSQL table.
     * This means if the table has an old format or is somehow improperly formatted
     *
     * @param tableName
     * @return
     */
    public boolean detectOutdatedTable(String tableName) {
        YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
        Map<String, String> tableSchema = CommonRefs.tableSchemas.get(tableName);
        if (tableSchema == null) {
            main.getLogger().severe(getPlainMsg("wwcdBadTable", new String[]{"&6" + tableName}, "&c", null));
            return false;
        }

        if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
            try (Connection sqlConnection = main.getSqlSession().getConnection()) {
                DatabaseMetaData metaData = sqlConnection.getMetaData();
                ResultSet columns = metaData.getColumns(null, null, tableName, null);

                Map<String, String> existingColumns = new HashMap<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    existingColumns.put(columnName, columnType);
                }

                for (Map.Entry<String, String> column : tableSchema.entrySet()) {
                    String columnName = column.getKey();
                    String expectedColumnType = column.getValue();
                    String actualColumnType = existingColumns.get(columnName);

                    if (actualColumnType == null) {
                        // Column is missing
                        debugMsg(getPlainMsg("wwcdColumnMissing", new String[]{"&b" + columnName, "&b" + tableName}, "&e", null));
                        main.getLogger().severe(getPlainMsg("wwcOldDatabaseStruct"));
                        return true;
                    }

                    // Add case exceptions here
                    if (actualColumnType.equals("BIT") && expectedColumnType.equals("BOOLEAN")) {
                        actualColumnType = "BOOLEAN";
                    }

                    if (!expectedColumnType.contains(actualColumnType)) {
                        // Column type doesn't match the expected type
                        debugMsg(getPlainMsg("wwcdColumnBadType", new String[]{"&b" + columnName, "&b" + tableName, "&b" + actualColumnType, "&b" + expectedColumnType}, "&e"));
                        main.getLogger().severe(getPlainMsg("wwcOldDatabaseStruct"));
                        return true;
                    }
                }

                // Check for extra columns in the table that are not defined in the schema
				/* who cares...
				for (String columnName : existingColumns.keySet()) {
					if (!tableSchema.containsKey(columnName)) {
						debugMsg(String.format("Extra column '%s' found in table '%s' that is not defined in the schema",
								columnName, tableName));
						main.getLogger().severe(getMsg("wwcOldDatabaseStruct", null));
						return true;
					}
				}
				*/

                debugMsg(getPlainMsg("wwcdGoodTable", new String[]{"&6MySQL", "&6" + tableName}, "&a", null));
                return false; // Table structure matches the schema
            } catch (SQLException e) {
                e.printStackTrace();
                return false; // Play it safe, probably corrupted anyway
            }
        } else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false)) {
            try (Connection postgresConnection = main.getPostgresSession().getConnection()) {
                DatabaseMetaData metaData = postgresConnection.getMetaData();
                // PostgreSQL converts unquoted identifiers to lowercase
                String adjustedTableName = tableName.toLowerCase();
                ResultSet columns = metaData.getColumns(null, null, adjustedTableName, null);

                Map<String, String> existingColumns = new HashMap<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME").toUpperCase(); // Normalize the type name to uppercase for comparison.
                    existingColumns.put(columnName, columnType);
                }

                for (Map.Entry<String, String> column : tableSchema.entrySet()) {
                    String columnName = column.getKey().toLowerCase(); // Adjust for PostgreSQL's default behavior.
                    String expectedColumnType = column.getValue().toUpperCase();
                    String actualColumnType = existingColumns.get(columnName);

                    if (actualColumnType == null) {
                        // Column is missing
                        debugMsg(getPlainMsg("wwcdColumnMissing", new String[]{"&b" + columnName, "&b" + tableName}, "&e", null));
                        main.getLogger().severe(getPlainMsg("wwcOldDatabaseStruct"));
                        return true;
                    }

                    // Add case exceptions here
                    if ("BIT".equals(actualColumnType) && "BOOLEAN".equals(expectedColumnType)) {
                        actualColumnType = "BOOLEAN";
                    }

                    if ("INT4".equals(actualColumnType) && "INT".equals(expectedColumnType)) {
                        actualColumnType = "INT";
                    }

                    if (!expectedColumnType.contains(actualColumnType)) {
                        // Column type doesn't match the expected type
                        debugMsg(getPlainMsg("wwcdColumnBadType", new String[]{"&b" + columnName, "&b" + tableName, "&b" + actualColumnType, "&b" + expectedColumnType}, "&e", null));
                        main.getLogger().severe(getPlainMsg("wwcOldDatabaseStruct"));
                        return true;
                    }
                }

                debugMsg(getPlainMsg("wwcdGoodTable", new String[]{"&6PostgreSQL", "&6" + tableName}, "&a", null));
                return false; // Table structure matches the schema
            } catch (SQLException e) {
                e.printStackTrace();
                return false; // Play it safe, probably corrupted anyway
            }

        }
        return true;
    }

    /**
     * Checks if a provided exception is a no confidence one/one to be ignored from our target translator.
     *
     * @param throwable - The exception to be checked
     * @return Boolean - If exception is no confidence, true; false otherwise
     */
    private boolean isErrorToIgnore(Throwable throwable) {
        // TOOD: Make sure this works properly again
        String exceptionMessage = StringUtils.lowerCase(throwable.getMessage());
        if (exceptionMessage == null) {
            // Usually just a timeout error. If a user gets this frequently they'll know something's wrong anyways
            return true;
        }

        if (main.getErrorsToIgnore().isEmpty()) {
            debugMsg("Errors to ignore is empty!");
            return false;
        }

        // This is a special character. If the user puts this character, then we ignore all errors.
        if (main.getErrorsToIgnore().contains("*")) return true;

        // Check if the exception message contains any of the strings in our low confidence dictionary
        for (String eaStr : main.getErrorsToIgnore()) {
            if (exceptionMessage.contains(eaStr)) {
                debugMsg("Ignoring error thrown by translator: " + exceptionMessage);
                return true;
            }
        }
        return false;
    }

    /**
     * Detects presence of color codes in a given string
     *
     * @param inMessage  - Message to be checked
     * @param currPlayer - Player that sent the message
     */
    private void detectColorCodes(String inMessage, Player currPlayer) {
        if (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")) {
            // Too much
            return;
        }

        if (!main.isActiveTranslator(currPlayer) || main.getActiveTranslator(currPlayer).getCCWarning()) {
            // if player is not an active translator, or they already saw the CC warning
            return;
        }

        if ((inMessage.contains("&") && main.isActiveTranslator(currPlayer) && !(main.getActiveTranslator(currPlayer)
                .getCCWarning()))) // check if user has already been sent CC warning
        {
            sendMsg("colorCodeWarning", "", "&d&o", currPlayer);
            main.getActiveTranslator(currPlayer)
                    .setCCWarning(true);
            // we're still gonna translate it but it won't look pretty
        }
    }

    private boolean shouldRateLimit(boolean skip, Player currPlayer) {
        // Get permission from Bukkit API synchronously, since we do not want to risk
        // concurrency problems
        if (skip) return false;
        boolean exempt = false;
        int personalRateLimit = 0;
        String permissionCheck = "";
        ActiveTranslator currActiveTranslator = main.getActiveTranslator(currPlayer);

        if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") && !serverIsStopping() && !main.getCurrPlatform().equals("Folia")) {
            try {
                permissionCheck = Bukkit.getScheduler().callSyncMethod(main, () -> checkForRateLimitPermissions(currPlayer)).get(3, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                debugMsg("Timeout from permission checks should never happen, unless the server is stopping or /reloading. "
                        + "If it isn't, and we can't fetch a user permission in less than ~3 seconds, we have a problem.");
                //return inMessage;
                return true;
            }
        } else if (main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") || main.getCurrPlatform().equals("Folia")) {
            // MockBukkit does not support callSyncMethod, not necessary on Folia
            debugMsg("Checking permissions in translateText() WITHOUT callSyncMethod()...");
            permissionCheck = checkForRateLimitPermissions(currPlayer);
        }
        debugMsg("rateLimit: " + permissionCheck);

        // If exempt, set exempt to true; else, get the delay from the end of the
        // permission string
        if (permissionCheck.equalsIgnoreCase("worldwidechat.ratelimit.exempt")) {
            exempt = true;
        } else {
            String delayStr = CharMatcher.inRange('0', '9').retainFrom(permissionCheck);
            if (!delayStr.isEmpty()) {
                personalRateLimit = Integer.parseInt(delayStr);
            }
        }

        // Get user's personal rate limit, if permission is not set and they are an
        // active translator.
        if (!exempt && personalRateLimit == 0 && main.isActiveTranslator(currPlayer)) {
            personalRateLimit = main
                    .getActiveTranslator(currPlayer).getRateLimit();
        }

        // Personal Limits (Override Global)
        if (!exempt && personalRateLimit > 0) {
            if (!isRateLimited(personalRateLimit, currActiveTranslator, currPlayer)) {
                //return inMessage;
                return true;
            }
            // Global Limits
        } else if (!exempt && main.getGlobalRateLimit() > 0) {
            if (!isRateLimited(main.getGlobalRateLimit(), currActiveTranslator, currPlayer)) {
                //return inMessage;
                return true;
            }
        }

        // No rate limit
        return false;
    }

    /**
     * Ensures that an ActiveTranslator does not currently need to be rate limited.
     *
     * @param delay                - Exact rate limit that is being looked for
     * @param currActiveTranslator - The valid ActiveTranslator target
     * @param sender               - The sender of the original command
     * @return Boolean - Returns false if the user should currently be rate limited, and true otherwise.
     */
    private boolean isRateLimited(int delay, ActiveTranslator currActiveTranslator, CommandSender sender) {
        if (!(currActiveTranslator.getRateLimitPreviousTime().equals("None"))) {
            Instant previous = Instant.parse(currActiveTranslator.getRateLimitPreviousTime());
            Instant currTime = Instant.now();
            if (currTime.compareTo(previous.plus(delay, ChronoUnit.SECONDS)) < 0) {
                sendMsg("wwcRateLimit", "" + ChronoUnit.SECONDS.between(currTime,
                                previous.plus(delay, ChronoUnit.SECONDS)),
                        "&e",
                        sender);
                return false;
            } else {
                currActiveTranslator.setRateLimitPreviousTime(Instant.now());
            }
        } else {
            currActiveTranslator.setRateLimitPreviousTime(Instant.now());
        }
        return true;
    }

    /**
     * Ensures that a player does not have a rate limit permission.
     *
     * @param currPlayer - Player that is being checked.
     * @return String - Returns an empty string if no permission was found, or the permission name if it is
     */
    private String checkForRateLimitPermissions(Player currPlayer) {
        Set<PermissionAttachmentInfo> perms = currPlayer.getEffectivePermissions();
        if (perms.contains("worldwidechat.ratelimit.exempt")) {
            return "worldwidechat.ratelimit.exempt";
        }

        for (PermissionAttachmentInfo perm : perms) {
            if (perm.getPermission().startsWith("worldwidechat.ratelimit.")) {
                return perm.getPermission();
            }
        }
        return "";
    }

    /**
     * Sends a bad permissions error
     *
     * @param correctPerm
     * @param sender
     */
    public void badPermsMessage(String correctPerm, CommandSender sender) {
        sendMsg("wwcBadPerms", "&6" + correctPerm, "&c", sender);
    }

    /**
     * Returns the chat channel format for the current player(s) given vault/the config
     *
     * @param translateIcon
     * @param translateFormat
     * @param prefix
     * @param username
     * @param suffix
     * @param originPlayer
     * @param targetPlayer
     * @return The finalized component
     */
    public Component getChatChannelFormat(Component translateIcon, String translateFormat, String prefix, String username, String suffix, Player originPlayer, Player targetPlayer) {
        Component out = Component.empty();
        if (translateIcon != null) {
            out = out.append(translateIcon);
        }
        String parsedFormat = translateFormat;

        // Handle PAPI
        int count = parsedFormat.length() - parsedFormat.replace("%", "").length();
        if (count > 1 && main.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (originPlayer != null) {
                debugMsg("Papi for player who sent message!");
                parsedFormat = PlaceholderAPI.setPlaceholders(originPlayer, translateFormat);
            } else {
                debugMsg("Removing papi placeholders for console.");
                parsedFormat = translateFormat.replaceAll("%[^%]+%", "");
            }
        }

        // Handle Localizations
        // If the targetPlayer is null, we use the originPlayer to get local
        // If originPlayer is null, server lang will be used
        Pattern local = Pattern.compile("\\{local:([^}]+)}");
        Matcher match = local.matcher(parsedFormat);
        while (match.find()) {
            String extracted = match.group(1);
            parsedFormat = parsedFormat.replace(match.group(0), getPlainMsg(extracted, targetPlayer == null ? originPlayer : targetPlayer));
        }

        // Handle Default Placeholders
        out = out.append(deserial(parsedFormat));
        TextReplacementConfig p = TextReplacementConfig.builder()
                .matchLiteral("{prefix}")
                .replacement(deserial(prefix))
                .build();

        TextReplacementConfig u = TextReplacementConfig.builder()
                .matchLiteral("{username}")
                .replacement(deserial(username))
                .build();

        TextReplacementConfig s = TextReplacementConfig.builder()
                .matchLiteral("{suffix}")
                .replacement(deserial(suffix))
                .build();

        out = out.replaceText(p);
        out = out.replaceText(s);
        out = out.replaceText(u);

        return out;
    }

    /**
     * Returns a SupportedLang obj with nativeLang/langName fields filled out
     *
     * @param code - the language code to search for
     * @return Supported Lang Object, or an unfixed obj if it was not found in the JSON
     */
    public SupportedLang fixLangName(String code) {
        String isoJsonFilePath = "ISO_639-WWC-Modified.json";
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ISOLanguage> languageMap;
        SupportedLang out = new SupportedLang(code);

        try {
            languageMap = objectMapper.readValue(main.getResource(isoJsonFilePath), new TypeReference<Map<String, ISOLanguage>>() {
            });
            // hashSet means less dupes
            debugMsg("Trying to fix " + code + " from JSON...");
            ISOLanguage jsonLang = languageMap.get(code);

            if (jsonLang != null) {
                out.setLangName(jsonLang.getIntName());
                out.setNativeLangName(jsonLang.getNativeName());
                return out;
            } else {
                debugMsg("Could not find " + code + " in JSON!");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            main.getLogger().warning(getPlainMsg("wwcISOJSONFail"));
        }

        // Return original
        return out;
    }

    /**
     * Fixes a given map of SupportedLangs to include native names/language names
     *
     * @param in          - Map of SupportedLang objs
     * @param nativesOnly - Whether we should add regular lang names as well as native langs
     * @param preInit     - Whether we are initializing or not (if not, do not send ANY messages; will not work in this state)
     * @return - The fixed map of supportedLang objs
     */
    public Map<String, SupportedLang> fixLangNames(Map<String, SupportedLang> in, boolean nativesOnly, boolean preInit) {
        // Adjust the file path as necessary
        // TODO: Take in hashSet instead of converting? Seems really unnecessary...
        String isoJsonFilePath = "ISO_639-WWC-Modified.json";
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ISOLanguage> languageMap;

        try {
            languageMap = objectMapper.readValue(main.getResource(isoJsonFilePath), new TypeReference<Map<String, ISOLanguage>>() {
            });
            // hashSet means less dupes
            for (SupportedLang currLang : new HashSet<>(in.values())) {
                String currCode = currLang.getLangCode();
                if (!preInit) {
                    debugMsg("Trying to fix " + currCode + " from JSON...");
                }
                ISOLanguage jsonLang = languageMap.get(currCode);

                if (jsonLang != null) {
                    currLang.setNativeLangName(jsonLang.getNativeName());
                    in.put(currLang.getNativeLangName(), currLang);
                    if (!nativesOnly) {
                        // If we want to fix language names as well...
                        currLang.setLangName(jsonLang.getIntName());
                        in.put(currLang.getLangName(), currLang);
                    }
                } else {
                    if (!preInit) debugMsg("Could not find " + currCode + " in JSON!");
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            if (!preInit) main.getLogger().warning(getPlainMsg("wwcISOJSONFail"));
        }

        return in;
    }

    static class ISOLanguage {
        @JsonProperty("int")
        private List<String> intNames;
        @JsonProperty("native")
        private List<String> nativeNames;

        // Getters and Setters
        public List<String> getIntNames() {
            return intNames;
        }

        public void setIntNames(List<String> intNames) {
            this.intNames = intNames;
        }

        public List<String> getNativeNames() {
            return nativeNames;
        }

        public void setNativeNames(List<String> nativeNames) {
            this.nativeNames = nativeNames;
        }

        public String getIntName() {
            return intNames.get(0);
        }

        public String getNativeName() {
            return nativeNames.get(0);
        }
    }
}