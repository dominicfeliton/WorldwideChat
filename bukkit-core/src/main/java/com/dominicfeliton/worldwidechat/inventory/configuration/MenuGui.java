package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.input.InputPrompt;
import com.dominicfeliton.worldwidechat.input.configuration.*;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.dominicfeliton.worldwidechat.WorldwideChat.instance;

/**
 * Full MenuGui with lazy‑built sub‑inventories to remove first‑open lag.
 * Thanks to H***** for help with this class!
 */
public class MenuGui implements InventoryProvider {

    public enum CONFIG_GUI_TAGS {
        GEN_SET, STORAGE_SET, SQL_SET, MONGO_SET, POSTGRES_SET,
        CHAT_SET, CHAT_CHANNEL_SET,
        TRANS_SET, AMAZON_TRANS_SET, AZURE_TRANS_SET, CHATGPT_TRANS_SET, OPENAI_COMPATIBLE_TRANS_SET,
        DEEP_TRANS_SET, GOOGLE_TRANS_SET, LIBRE_TRANS_SET, OLLAMA_TRANS_SET,
        SYSTRAN_TRANS_SET,
        AI_SET;
        public LazySmartInventory inv;
    }

    /* ------------------------------------------------------------------ */
    /*  Static caches                                                     */
    /* ------------------------------------------------------------------ */

    private static final Map<String, String> CACHED_MSGS = new HashMap<>();

    private static final List<String> TRANSLATOR_TOGGLES;
    private static final List<String> STORAGE_TOGGLES =
            List.of("Storage.useSQL", "Storage.useMongoDB", "Storage.usePostgreSQL");

    static {
        List<String> tmp = new ArrayList<>(CommonRefs.translatorPairs.keySet());
        TRANSLATOR_TOGGLES = Collections.unmodifiableList(tmp);
    }

    private static String plain(CommonRefs refs, Player p, String key, String... args) {
        return CACHED_MSGS.computeIfAbsent(
                key + Arrays.toString(args),
                k -> refs.getPlainMsg(key, args, p)
        );
    }

    /* ------------------------------------------------------------------ */
    /*  Instance fields                                                   */
    /* ------------------------------------------------------------------ */

    private final WorldwideChat        main     = WorldwideChat.instance;
    private final CommonRefs           refs     = main.getServerFactory().getCommonRefs();
    private final WWCInventoryManager  invMgr   = instance.getInventoryManager();

    private final Player  inPlayer;
    private final String  transName;

    public MenuGui(Player inPlayer, String transName) {
        this.inPlayer  = inPlayer;
        this.transName = transName;
    }

    /* ------------------------------------------------------------------ */
    /*  Public entry – build root menus                                   */
    /* ------------------------------------------------------------------ */

    public void genAllConfigUIs() {
        refs.debugMsg("Generating root config GUI (lazy mode)!");

        /* ------------------------ General ------------------------- */
        MenuGui generalSet = new MenuGui(inPlayer, transName);
        CONFIG_GUI_TAGS.GEN_SET.inv = new LazySmartInventory(() -> {
            SmartInventory inv = generalSet.genSmartInv("generalSettingsMenu",
                    4, 9, ChatColor.BLUE, "wwcConfigGUIGeneralSettings");
            generalSet.buildGeneral();
            return inv;
        });

        /* ------------------------ Storage ------------------------- */
        MenuGui storageSet = new MenuGui(inPlayer, transName);
        CONFIG_GUI_TAGS.STORAGE_SET.inv = new LazySmartInventory(() -> {
            SmartInventory inv = storageSet.genSmartInv("storageSettingsMenu",
                    3, 9, ChatColor.BLUE, "wwcConfigGUIStorageSettings");
            storageSet.buildStorage();
            return inv;
        });

        /*  --------------------- Lazy pages ------------------------ */
        buildLazyMenus();
    }

    /* ------------------------------------------------------------------ */
    /*  Lazy menu declarations                                            */
    /* ------------------------------------------------------------------ */

    private void buildLazyMenus() {

        CONFIG_GUI_TAGS.SQL_SET.inv = lazy("sqlSettingsMenu", 4, 9,
                "wwcConfigGUIStorageTypeSettings", ChatColor.BLUE, MenuGui::buildSQL, "SQL");

        CONFIG_GUI_TAGS.MONGO_SET.inv = lazy("mongoSettingsMenu", 3, 9,
                "wwcConfigGUIStorageTypeSettings", ChatColor.BLUE, MenuGui::buildMongo, "MongoDB");

        CONFIG_GUI_TAGS.POSTGRES_SET.inv = lazy("postgresSettingsMenu", 4, 9,
                "wwcConfigGUIStorageTypeSettings", ChatColor.BLUE, MenuGui::buildPostgres, "PostgreSQL");

        CONFIG_GUI_TAGS.CHAT_SET.inv = lazy("chatSettingsMenu", 4, 9,
                "wwcConfigGUIChatSettings", ChatColor.BLUE, MenuGui::buildChat);

        CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.inv = lazy("chatChannelSettingsMenu", 3, 9,
                "wwcConfigGUIChatChannelSettings", ChatColor.BLUE, MenuGui::buildChatChannel);

        CONFIG_GUI_TAGS.TRANS_SET.inv = lazy("translatorSettingsMenu", 5, 9,
                "wwcConfigGUITranslatorSettings", ChatColor.BLUE, MenuGui::buildTranslatorRoot);

        CONFIG_GUI_TAGS.GOOGLE_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("googleTranslator", "wwcConfigGUIEachTranslatorSettings", "Google");
            g.buildGoogle();
            return inv;
        });

        CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("amazonTranslator", "wwcConfigGUIEachTranslatorSettings", "Amazon");
            g.buildAmazon();
            return inv;
        });

        CONFIG_GUI_TAGS.LIBRE_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("libreTranslator", "wwcConfigGUIEachTranslatorSettings", "Libre");
            g.buildLibre();
            return inv;
        });

        CONFIG_GUI_TAGS.DEEP_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("deepLTranslator", "wwcConfigGUIEachTranslatorSettings", "DeepL");
            g.buildDeepL();
            return inv;
        });

        CONFIG_GUI_TAGS.AZURE_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("azureTranslator", "wwcConfigGUIEachTranslatorSettings", "Azure");
            g.buildAzure();
            return inv;
        });

        CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("systranTranslator", "wwcConfigGUIEachTranslatorSettings", "Systran");
            g.buildSystran();
            return inv;
        });

        CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("chatgptTranslator", "wwcConfigGUIEachTranslatorSettings", "ChatGPT");
            g.buildChatGPT();
            return inv;
        });

        CONFIG_GUI_TAGS.OPENAI_COMPATIBLE_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("openAICompatibleTranslator", "wwcConfigGUIEachTranslatorSettings", "OpenAI Compatible");
            g.buildOpenAICompatible();
            return inv;
        });

        CONFIG_GUI_TAGS.OLLAMA_TRANS_SET.inv = new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv("ollamaTranslator", "wwcConfigGUIEachTranslatorSettings", "Ollama");
            g.buildOllama();
            return inv;
        });

        CONFIG_GUI_TAGS.AI_SET.inv = lazy("aiSettings", 3, 9,
                "wwcConfigGUIAISettings", ChatColor.BLUE, MenuGui::buildAI);
    }

    /* ------------------------------------------------------------------ */
    /*  Lazy helpers                                                      */
    /* ------------------------------------------------------------------ */

    private LazySmartInventory lazy(String id,
                                    int rows, int cols,
                                    String titleKey, ChatColor color,
                                    Consumer<MenuGui> bodyBuilder,
                                    String... titleArgs) {
        return new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv(id, rows, cols, color, titleKey, titleArgs);
            bodyBuilder.accept(g);
            return inv;
        });
    }

    private LazySmartInventory lazy(String id,
                                    String titleKey,
                                    Consumer<MenuGui> bodyBuilder,
                                    String... titleArgs) {
        return new LazySmartInventory(() -> {
            MenuGui g = new MenuGui(inPlayer, transName);
            SmartInventory inv = g.genSmartInv(id, 3, 9, ChatColor.BLUE, titleKey, titleArgs);
            bodyBuilder.accept(g);
            return inv;
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Page builders                                                     */
    /* ------------------------------------------------------------------ */

    private void buildGeneral() {
        elements.clear();
        add(new BorderElement(Material.WHITE_STAINED_GLASS_PANE));
        add(new ConvoElement(1, 1, "wwcConfigGUIPrefixButton", Material.NAME_TAG,
                new GeneralSettingsConvos.Prefix()));
        add(new ConvoElement(1, 2, "wwcConfigGUIFatalAsyncAbortButton", Material.NAME_TAG,
                new GeneralSettingsConvos.FatalAsyncAbort()));
        add(new ConvoElement(1, 3, "wwcConfigGUILangButton", Material.NAME_TAG,
                new GeneralSettingsConvos.Lang()));
        add(new ConvoElement(1, 4, "wwcConfigGUIUpdateCheckerButton", Material.NAME_TAG,
                new GeneralSettingsConvos.UpdateChecker()));
        add(new ConvoElement(1, 5, "wwcConfigGUISyncUserDataButton", Material.NAME_TAG,
                new GeneralSettingsConvos.SyncUserData()));
        add(new ToggleElement(1, 6, "wwcConfigGUIbStatsButton", "wwcConfigConversationbStatsSuccess",
                "General.enablebStats"));
        add(new ToggleElement(1, 7, "wwcConfigGUIDebugModeButton", "wwcConfigConversationDebugModeSuccess",
                "General.enableDebugMode"));
        add(new ToggleElement(2, 1, "wwcConfigGUILocalizeSyncButton", "wwcConfigConversationLocalizeSyncSuccess",
                "General.syncUserLocalization"));
        add(new ToggleElement(2, 2, "wwcConfigGUIEnableSoundsButton", "wwcConfigConversationEnableSoundsSuccess",
                "General.enableSounds"));
        add(new CommonElement(3, 4, "Quit"));
        add(new CommonElement(3, 8, "Page Number", new String[]{"1"}));
        add(new CommonElement(3, 6, "Next",
                new Object[]{CONFIG_GUI_TAGS.STORAGE_SET.inv}));
    }

    private void buildStorage() {
        elements.clear();
        add(new BorderElement(Material.WHITE_STAINED_GLASS_PANE));
        add(new SubMenuElement(1, 1, instance.isSQLConnValid(true), "wwcConfigGUISQLMenuButton",
                CONFIG_GUI_TAGS.SQL_SET.inv));
        add(new SubMenuElement(1, 2, instance.isMongoConnValid(true), "wwcConfigGUIMongoMenuButton",
                CONFIG_GUI_TAGS.MONGO_SET.inv));
        add(new SubMenuElement(1, 3, instance.isPostgresConnValid(true), "wwcConfigGUIPostgresMenuButton",
                CONFIG_GUI_TAGS.POSTGRES_SET.inv));
        add(new CommonElement(2, 4, "Quit"));
        add(new CommonElement(2, 2, "Previous", new Object[]{ CONFIG_GUI_TAGS.GEN_SET.inv}));
        add(new CommonElement(2, 6, "Next", new Object[]{CONFIG_GUI_TAGS.CHAT_SET.inv}));
        add(new CommonElement(2, 8, "Page Number", new String[]{"2"}));
    }

    private void buildSQL() {
        elements.clear();
        add(new BorderElement(Material.WHITE_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleSQLButton", "wwcConfigConversationToggleSQLSuccess",
                "Storage.useSQL", STORAGE_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUISQLHostnameButton", Material.NAME_TAG,
                new SQLSettingsConvos.Hostname()));
        add(new ConvoElement(1, 3, "wwcConfigGUISQLPortButton", Material.NAME_TAG,
                new SQLSettingsConvos.Port()));
        add(new ConvoElement(1, 4, "wwcConfigGUISQLDatabaseNameButton", Material.NAME_TAG,
                new SQLSettingsConvos.Database()));
        add(new ConvoElement(1, 5, "wwcConfigGUISQLUsernameButton", Material.NAME_TAG,
                new SQLSettingsConvos.Username()));
        add(new ConvoElement(1, 6, "wwcConfigGUISQLPasswordButton", Material.NAME_TAG,
                new SQLSettingsConvos.Password()));
        add(new ToggleElement(1, 7, "wwcConfigGUIToggleSQLSSLButton", "wwcConfigConversationToggleSQLSSLSuccess",
                "Storage.sqlUseSSL"));
        add(new ConvoElement(2, 1, "wwcConfigGUISQLOptionalArgsButton", Material.NAME_TAG,
                new SQLSettingsConvos.OptionalArgs()));
        add(new CommonElement(3, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.STORAGE_SET.inv}));
        add(new CommonElement(3, 4, "Quit"));
    }

    private void buildMongo() {
        elements.clear();
        add(new BorderElement(Material.ORANGE_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleMongoButton", "wwcConfigConversationToggleMongoSuccess",
                "Storage.useMongoDB", STORAGE_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIMongoHostnameButton", Material.NAME_TAG,
                new MongoSettingsConvos.Hostname()));
        add(new ConvoElement(1, 3, "wwcConfigGUIMongoPortButton", Material.NAME_TAG,
                new MongoSettingsConvos.Port()));
        add(new ConvoElement(1, 4, "wwcConfigGUIMongoDatabaseNameButton", Material.NAME_TAG,
                new MongoSettingsConvos.Database()));
        add(new ConvoElement(1, 5, "wwcConfigGUIMongoUsernameButton", Material.NAME_TAG,
                new MongoSettingsConvos.Username()));
        add(new ConvoElement(1, 6, "wwcConfigGUIMongoPasswordButton", Material.NAME_TAG,
                new MongoSettingsConvos.Password()));
        add(new ConvoElement(1, 7, "wwcConfigGUIMongoOptionalArgsButton", Material.NAME_TAG,
                new MongoSettingsConvos.OptionalArgs()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.STORAGE_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildPostgres() {
        elements.clear();
        add(new BorderElement(Material.GRAY_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUITogglePostgresButton", "wwcConfigConversationTogglePostgresSuccess",
                "Storage.usePostgreSQL", STORAGE_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIPostgresHostnameButton", Material.NAME_TAG,
                new PostgresSettingsConvos.Hostname()));
        add(new ConvoElement(1, 3, "wwcConfigGUIPostgresPortButton", Material.NAME_TAG,
                new PostgresSettingsConvos.Port()));
        add(new ConvoElement(1, 4, "wwcConfigGUIPostgresDatabaseNameButton", Material.NAME_TAG,
                new PostgresSettingsConvos.Database()));
        add(new ConvoElement(1, 5, "wwcConfigGUIPostgresUsernameButton", Material.NAME_TAG,
                new PostgresSettingsConvos.Username()));
        add(new ConvoElement(1, 6, "wwcConfigGUIPostgresPasswordButton", Material.NAME_TAG,
                new PostgresSettingsConvos.Password()));
        add(new ToggleElement(1, 7, "wwcConfigGUITogglePostgresSSLButton", "wwcConfigConversationTogglePostgresSSLSuccess",
                "Storage.postgresSSL"));
        add(new ConvoElement(2, 1, "wwcConfigGUIPostgresOptionalArgsButton", Material.NAME_TAG,
                new PostgresSettingsConvos.OptionalArgs()));
        add(new CommonElement(3, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.STORAGE_SET.inv}));
        add(new CommonElement(3, 4, "Quit"));
    }

    private void buildChat() {
        elements.clear();
        add(new BorderElement(Material.WHITE_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUISendTranslationChatButton", "wwcConfigConversationSendTranslationChatSuccess",
                "Chat.sendTranslationChat"));
        add(new ToggleElement(1, 2, "wwcConfigGUIPluginUpdateChatButton", "wwcConfigConversationPluginUpdateChatSuccess",
                "Chat.sendPluginUpdateChat"));
        add(new ToggleElement(1, 3, "wwcConfigGUISendIncomingHoverTextChatButton", "wwcConfigConversationSendIncomingHoverTextChatSuccess",
                "Chat.sendIncomingHoverTextChat"));
        add(new ToggleElement(1, 4, "wwcConfigGUISendOutgoingHoverTextChatButton", "wwcConfigConversationSendOutgoingHoverTextChatSuccess",
                "Chat.sendOutgoingHoverTextChat"));
        add(new ToggleElement(1, 5, "wwcConfigGUIChatBlacklistButton", "wwcConfigConversationChatBlacklistSuccess",
                "Chat.enableBlacklist"));
        add(new ToggleElement(1, 6, "wwcConfigGUIVaultSupportButton", "wwcConfigConversationVaultSupportSuccess",
                "Chat.useVault"));
        add(new ToggleElement(1, 7, "wwcConfigGUISendActionBarButton", "wwcConfigConversationSendActionBarSuccess",
                "Chat.sendActionBar"));
        add(new ConvoElement(2, 1, "wwcConfigGUIChatListenerPriorityButton", Material.NAME_TAG,
                new ChatSettingsConvos.ModifyChatPriority()));
        add(new InputMethodElement(2, 5));

        if (!main.getCurrPlatform().equals("Folia")) {
            add(new SubMenuElement(2, 2, "wwcConfigGUIMessagesOverridePickChatButton",
                    new LazySmartInventory(() -> new MessagesOverridePickLangGui(inPlayer).getMessagesOverridePickLangGui())));
            if (main.getConfigManager().getBlacklistConfig() != null) {
                add(new SubMenuElement(2, 3, "wwcConfigGUIMessagesModifyBlacklistButton",
                        new LazySmartInventory(() -> new BlacklistGui(inPlayer).getBlacklist())));
            }
            add(new SubMenuElement(2, 4, "wwcConfigGUIChatChannelButton",
                    CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.inv));
        } else {
            add(new SubMenuElement(2, 2, "wwcConfigGUIChatChannelButton",
                    CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.inv));
        }

        add(new CommonElement(3, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.STORAGE_SET.inv}));
        add(new CommonElement(3, 4, "Quit"));
        add(new CommonElement(3, 6, "Next", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(3, 8, "Page Number", new String[]{"3"}));
    }

    private void buildChatChannel() {
        elements.clear();
        add(new BorderElement(Material.PURPLE_STAINED_GLASS_PANE));
        add(new ConvoElement(1, 1, "wwcConfigGUISeparateChatChannelIconButton", Material.NAME_TAG,
                new ChatSettingsConvos.ChannelIcon()));
        add(new ConvoElement(1, 2, "wwcConfigGUISeparateChatChannelFormatButton", Material.NAME_TAG,
                new ChatSettingsConvos.ChannelFormat()));
        add(new ConvoElement(1, 3, "wwcConfigGUISeparateChatChannelHoverFormatButton", Material.NAME_TAG,
                new ChatSettingsConvos.ChannelHoverFormat()));
        add(new ToggleElement(1, 4, "wwcConfigGUISeparateChatChannelForceButton", "wwcConfigConversationSeparateChatChannelForceSuccess",
                "Chat.separateChatChannel.force"));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.CHAT_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildTranslatorRoot() {
        elements.clear();
        add(new BorderElement(Material.WHITE_STAINED_GLASS_PANE));
        add(new SubMenuElement(1, 1, transName.equals("Amazon Translate"), "wwcConfigGUIAmazonTranslateButton",
                CONFIG_GUI_TAGS.AMAZON_TRANS_SET.inv));
        add(new SubMenuElement(1, 2, transName.equals("Azure Translate"), "wwcConfigGUIAzureTranslateButton",
                CONFIG_GUI_TAGS.AZURE_TRANS_SET.inv));
        add(new SubMenuElement(1, 3, transName.equals("ChatGPT"), "wwcConfigGUIChatGPTButton",
                CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.inv));
        add(new SubMenuElement(1, 4, transName.equals("DeepL Translate"), "wwcConfigGUIDeepLTranslateButton",
                CONFIG_GUI_TAGS.DEEP_TRANS_SET.inv));
        add(new SubMenuElement(1, 5, transName.equals("Google Translate"), "wwcConfigGUIGoogleTranslateButton",
                CONFIG_GUI_TAGS.GOOGLE_TRANS_SET.inv));
        add(new SubMenuElement(1, 6, transName.equals("Libre Translate"), "wwcConfigGUILibreTranslateButton",
                CONFIG_GUI_TAGS.LIBRE_TRANS_SET.inv));
        add(new SubMenuElement(1, 7, transName.equals("Ollama"), "wwcConfigGUIOllamaButton",
                CONFIG_GUI_TAGS.OLLAMA_TRANS_SET.inv));
        add(new SubMenuElement(2, 1, transName.equals("OpenAI Compatible"), "wwcConfigGUIOpenAICompatibleButton",
                CONFIG_GUI_TAGS.OPENAI_COMPATIBLE_TRANS_SET.inv));
        add(new SubMenuElement(2, 2, transName.equals("Systran Translate"), "wwcConfigGUISystranTranslateButton",
                CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.inv));
        add(new ConvoElement(3, 1, "wwcConfigGUITranslatorCacheButton", Material.NAME_TAG,
                new TranslatorSettingsConvos.TranslationCache()));
        add(new ConvoElement(3, 2, "wwcConfigGUIGlobalRateLimitButton", Material.NAME_TAG,
                new TranslatorSettingsConvos.GlobalRateLimit()));
        add(new ConvoElement(3, 3, "wwcConfigGUIErrorLimitButton", Material.NAME_TAG,
                new TranslatorSettingsConvos.ErrorLimit()));
        add(new ConvoElement(3, 4, "wwcConfigGUICharacterLimitButton", Material.NAME_TAG,
                new TranslatorSettingsConvos.CharacterLimit()));
        add(new ConvoElement(3, 5, "wwcConfigGUIIgnoreErrorsButton", Material.NAME_TAG,
                new TranslatorSettingsConvos.IgnoreErrors()));
        add(new ToggleElement(3, 6, "wwcConfigGUIPersistentCacheButton", "wwcConfigConversationPersistentCacheSuccess",
                "Translator.enablePersistentCache"));
        add(new CommonElement(4, 6, "Next", new Object[]{CONFIG_GUI_TAGS.AI_SET.inv}));
        add(new CommonElement(4, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.CHAT_SET.inv}));
        add(new CommonElement(4, 4, "Quit"));
        add(new CommonElement(4, 8, "Page Number", new String[]{"4"}));
    }

    private void buildGoogle() {
        elements.clear();
        add(new BorderElement(Material.RED_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleGoogleTranslateButton", "wwcConfigConversationGoogleTranslateToggleSuccess",
                "Translator.useGoogleTranslate", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIGoogleTranslateAPIKeyButton", Material.NAME_TAG,
                new GoogleSettingsConvos.ApiKey()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildAmazon() {
        elements.clear();
        add(new BorderElement(Material.YELLOW_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleAmazonTranslateButton", "wwcConfigConversationAmazonTranslateToggleSuccess",
                "Translator.useAmazonTranslate", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIAmazonTranslateAccessKeyButton", Material.NAME_TAG,
                new AmazonSettingsConvos.AccessKey()));
        add(new ConvoElement(1, 3, "wwcConfigGUIAmazonTranslateSecretKeyButton", Material.NAME_TAG,
                new AmazonSettingsConvos.SecretKey()));
        add(new ConvoElement(1, 4, "wwcConfigGUIAmazonTranslateRegionButton", Material.NAME_TAG,
                new AmazonSettingsConvos.Region()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildLibre() {
        elements.clear();
        add(new BorderElement(Material.WHITE_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleLibreTranslateButton", "wwcConfigConversationLibreTranslateToggleSuccess",
                "Translator.useLibreTranslate", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUILibreTranslateURLButton", Material.NAME_TAG,
                new LibreSettingsConvos.Url()));
        add(new ConvoElement(1, 3, "wwcConfigGUILibreTranslateApiKeyButton", Material.NAME_TAG,
                new LibreSettingsConvos.ApiKey()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildDeepL() {
        elements.clear();
        add(new BorderElement(Material.BLUE_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleDeepLTranslateButton", "wwcConfigConversationDeepLTranslateToggleSuccess",
                "Translator.useDeepLTranslate", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIDeepLTranslateApiKeyButton", Material.NAME_TAG,
                new DeepLSettingsConvos.ApiKey()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildAzure() {
        elements.clear();
        add(new BorderElement(Material.GREEN_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleAzureTranslateButton", "wwcConfigConversationAzureTranslateToggleSuccess",
                "Translator.useAzureTranslate", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIAzureTranslateApiKeyButton", Material.NAME_TAG,
                new AzureSettingsConvos.ApiKey()));
        add(new ConvoElement(1, 3, "wwcConfigGUIAzureTranslateRegionButton", Material.NAME_TAG,
                new AzureSettingsConvos.Region()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildSystran() {
        elements.clear();
        add(new BorderElement(Material.CYAN_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleSystranTranslateButton", "wwcConfigConversationSystranTranslateToggleSuccess",
                "Translator.useSystranTranslate", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUISystranTranslateApiKeyButton", Material.NAME_TAG,
                new SystranSettingsConvos.ApiKey()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildChatGPT() {
        elements.clear();
        add(new BorderElement(Material.BLACK_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleChatGPTButton", "wwcConfigGUIChatGPTToggleSuccess",
                "Translator.useChatGPT", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIChatGPTURLButton", Material.NAME_TAG,
                new ChatGPTSettingsConvos.Url()));
        add(new ConvoElement(1, 3, "wwcConfigGUIChatGPTAPIKeyButton", Material.NAME_TAG,
                new ChatGPTSettingsConvos.ApiKey()));
        add(new ConvoElement(1, 4, "wwcConfigGUIChatGPTModelButton", Material.NAME_TAG,
                new ChatGPTSettingsConvos.Model()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildOpenAICompatible() {
        elements.clear();
        add(new BorderElement(Material.GRAY_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleOpenAICompatibleButton", "wwcConfigGUIOpenAICompatibleToggleSuccess",
                "Translator.useOpenAICompatible", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIOpenAICompatibleURLButton", Material.NAME_TAG,
                new OpenAICompatibleSettingsConvos.Url()));
        add(new ConvoElement(1, 3, "wwcConfigGUIOpenAICompatibleAPIKeyButton", Material.NAME_TAG,
                new OpenAICompatibleSettingsConvos.ApiKey()));
        add(new ConvoElement(1, 4, "wwcConfigGUIOpenAICompatibleModelButton", Material.NAME_TAG,
                new OpenAICompatibleSettingsConvos.Model()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildOllama() {
        elements.clear();
        add(new BorderElement(Material.LIME_STAINED_GLASS_PANE));
        add(new ToggleElement(1, 1, "wwcConfigGUIToggleOllamaButton", "wwcConfigGUIOllamaToggleSuccess",
                "Translator.useOllama", TRANSLATOR_TOGGLES, false));
        add(new ConvoElement(1, 2, "wwcConfigGUIOllamaURLButton", Material.NAME_TAG,
                new OllamaSettingsConvos.Url()));
        add(new ConvoElement(1, 3, "wwcConfigGUIOllamaModelButton", Material.NAME_TAG,
                new OllamaSettingsConvos.Model()));
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
    }

    private void buildAI() {
        elements.clear();
        add(new BorderElement(Material.RED_STAINED_GLASS_PANE));
        add(new BulkInputElement(1, 1, "wwcConfigGUIChatGPTPromptButton", Material.NAME_TAG,
                main.getConfigManager().getAIConfig(), "chatGPTOverrideSystemPrompt"));
        add(new BulkInputElement(1, 2, "wwcConfigGUIOllamaPromptButton", Material.NAME_TAG,
                main.getConfigManager().getAIConfig(), "ollamaOverrideSystemPrompt"));
        add(new BulkInputElement(1, 3, "wwcConfigGUIOpenAICompatiblePromptButton", Material.NAME_TAG,
                main.getConfigManager().getAIConfig(), "openAICompatibleOverrideSystemPrompt"));
        add(new ConvoElement(1, 4, "wwcConfigGUIGuidelinesAIModelButton", Material.NAME_TAG,
                new AISettingsConvos.GuidelinesAIModel()));
        add(new BulkInputElement(1, 5, "wwcConfigGUIGuidelinesAIPromptButton", Material.NAME_TAG,
                main.getConfigManager().getAIConfig(), "guidelinesAIOverridePrompt"));
        add(new ToggleElement(1, 6, "wwcConfigGUIToggleGuidelinesAIChecksButton", "wwcConfigGUIGuidelinesAIChecksToggleSuccess",
                "Translator.enableGuidelinesAIChecks"));
        if (!main.getCurrPlatform().equals("Folia")) {
            add(new SubMenuElement(1, 7, "wwcConfigGUIAIChangeLangs",
                    new LazySmartInventory(() -> new AILangGui(inPlayer).getAILangs())));
        }
        add(new CommonElement(2, 2, "Previous", new Object[]{CONFIG_GUI_TAGS.TRANS_SET.inv}));
        add(new CommonElement(2, 4, "Quit"));
        add(new CommonElement(2, 8, "Page Number", new String[]{"5"}));
    }

    /* ------------------------------------------------------------------ */
    /*  Element hierarchy (unchanged behaviour)                           */
    /* ------------------------------------------------------------------ */

    static abstract class Element {
        public final int      x;
        public final int      y;
        public final String   buttonName;
        public final Material blockIcon;

        Element(int x, int y, String buttonName, Material icon) {
            this.x = x;
            this.y = y;
            this.buttonName = buttonName;
            this.blockIcon = icon;
        }
        abstract void rasterize(Player player, InventoryContents contents);
    }

    static class ConvoElement extends Element {
        public final InputPrompt prompt;
        private final WWCInventoryManager invMgr = instance.getInventoryManager();

        ConvoElement(int x, int y, String buttonName, Material icon, InputPrompt prompt) {
            super(x, y, buttonName, icon);
            this.prompt = prompt;
        }
        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.genericInputButton(x, y, player, contents, prompt, blockIcon, buttonName);
        }
    }

    static class BulkInputElement extends Element {
        public final YamlConfiguration cfg;
        public final String            key;
        private final WWCInventoryManager invMgr = instance.getInventoryManager();

        BulkInputElement(int x, int y, String buttonName, Material icon,
                         YamlConfiguration cfg, String key) {
            super(x, y, buttonName, icon);
            this.cfg = cfg; this.key = key;
        }
        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.genericBookButton(x, y, player, contents, cfg, key, blockIcon, buttonName);
        }
    }

    static class ToggleElement extends Element {
        public final String onSuccess;
        public final String cfgName;
        public final List<String> disableList;
        public final boolean restart;
        private final WWCInventoryManager invMgr = instance.getInventoryManager();

        ToggleElement(int x, int y, String button, String success, String cfgName) {
            this(x, y, button, success, cfgName, null, false);
        }
        ToggleElement(int x, int y, String button, String success, String cfgName,
                      List<String> disable, boolean restart) {
            super(x, y, button, null);
            this.onSuccess = success; this.cfgName = cfgName;
            this.disableList = disable == null ? List.of() : disable;
            this.restart = restart;
        }
        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.genericToggleButton(x, y, player, contents, buttonName,
                    onSuccess, cfgName, disableList, restart);
        }
    }

    static class InputMethodElement extends Element {
        private final WWCInventoryManager invMgr = instance.getInventoryManager();

        InputMethodElement(int x, int y) {
            super(x, y, "wwcConfigGUIInputMethodButton", null);
        }

        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.genericInputMethodButton(x, y, player, contents);
        }
    }

    static class CommonElement extends Element {
        public final Object[] args;
        private final WWCInventoryManager invMgr = instance.getInventoryManager();

        CommonElement(int x, int y, String buttonName, Object[] args) {
            super(x, y, buttonName, null);
            this.args = args == null ? new Object[0] : args;
        }
        CommonElement(int x, int y, String buttonName) {
            this(x, y, buttonName, null);
        }
        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.setCommonButton(x, y, player, contents, buttonName, args);
        }
    }

    static class BorderElement extends Element {
        private final WWCInventoryManager invMgr = instance.getInventoryManager();
        BorderElement(Material icon) { super(0, 0, "", icon); }
        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.setBorders(contents, blockIcon);
        }
    }

    static class SubMenuElement extends Element {
        public final Boolean preCond;
        public final LazySmartInventory target;
        private final WWCInventoryManager invMgr = instance.getInventoryManager();

        SubMenuElement(int x, int y, Boolean preCond, String button, LazySmartInventory target) {
            super(x, y, button, null);
            this.preCond = preCond; this.target = target;
        }
        SubMenuElement(int x, int y, String button, LazySmartInventory target) {
            this(x, y, null, button, target);
        }
        @Override
        void rasterize(Player player, InventoryContents contents) {
            invMgr.genericOpenSubmenuButton(x, y, player, contents, preCond, buttonName, target.get());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  InventoryProvider implementation                                  */
    /* ------------------------------------------------------------------ */

    private final List<Element> elements = new ArrayList<>();
    void add(Element e) { elements.add(e); }

    @Override public void init(Player p, InventoryContents c) {
        ClickableItem blank = ClickableItem.empty(new ItemStack(Material.AIR));
        c.fill(blank);

        try { for (Element e : new ArrayList<>(elements)) {
            e.rasterize(p, c);
        } }
        catch (Exception ex) { invMgr.inventoryError(p, ex); }
    }
    @Override public void update(Player p, InventoryContents c) { }

    /* ------------------------------------------------------------------ */
    /*  SmartInventory helpers                                            */
    /* ------------------------------------------------------------------ */

    private SmartInventory genSmartInv(String id, int rows, int cols, ChatColor color,
                                       String titleKey, String... args) {
        return SmartInventory.builder()
                .id(id).provider(this).size(rows, cols)
                .manager(WorldwideChat.instance.getInventoryManager())
                .title(color + plain(refs, inPlayer, titleKey, args))
                .build();
    }
    private SmartInventory genSmartInv(String id, String titleKey, String... args) {
        return genSmartInv(id, 3, 9, ChatColor.BLUE, titleKey, args);
    }

    /* ------------------------------------------------------------------ */
    /*  Lazy wrapper                                                      */
    /* ------------------------------------------------------------------ */

    public static final class LazySmartInventory {
        private final Supplier<SmartInventory> factory;
        LazySmartInventory(Supplier<SmartInventory> f) { this.factory = f; }

        public SmartInventory get() {
            return factory.get();
        }
    }
}
