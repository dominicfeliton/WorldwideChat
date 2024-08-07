package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.conversations.configuration.*;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dominicfeliton.worldwidechat.WorldwideChat.instance;

public class MenuGui implements InventoryProvider {
	
	// Thank you, H***** for the help with this class!!
	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = instance.getInventoryManager();

	private Player inPlayer;

	private String transName;

	public MenuGui(Player inPlayer, String transName) {
		this.inPlayer = inPlayer;
		this.transName = transName;
	}

	public enum CONFIG_GUI_TAGS {
		GEN_SET, STORAGE_SET, SQL_SET, MONGO_SET, POSTGRES_SET, CHAT_SET, TRANS_SET, WATSON_TRANS_SET, GOOGLE_TRANS_SET, AMAZON_TRANS_SET, LIBRE_TRANS_SET, DEEP_TRANS_SET, AZURE_TRANS_SET, SYSTRAN_TRANS_SET;
		
		public SmartInventory smartInv;
	}

	public void genAllConfigUIs() {
		// TODO: Probably not likely, but is there a more efficient way?
		/* Generate inventories */
		refs.debugMsg("Generating config GUIs!");
		MenuGui generalSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.GEN_SET.smartInv = generalSet.genSmartInv("generalSettingsMenu", 3, 9, ChatColor.BLUE, "wwcConfigGUIGeneralSettings");
		
		MenuGui storageSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.STORAGE_SET.smartInv = storageSet.genSmartInv("storageSettingsMenu", "wwcConfigGUIStorageSettings");
		
		MenuGui sqlSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.SQL_SET.smartInv = sqlSet.genSmartInv("sqlSettingsMenu", 4, 9, ChatColor.BLUE, "wwcConfigGUIStorageTypeSettings", new String[] {"MySQL"});
		
		MenuGui mongoSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.MONGO_SET.smartInv = mongoSet.genSmartInv("mongoSettingsMenu", 3, 9, ChatColor.BLUE, "wwcConfigGUIStorageTypeSettings", new String[] {"MongoDB"});

		MenuGui postgresSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.POSTGRES_SET.smartInv = postgresSet.genSmartInv("postgresSettingsMenu", 4, 9, ChatColor.BLUE, "wwcConfigGUIStorageTypeSettings", new String[] {"PostgreSQL"});
		
		MenuGui chatSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.CHAT_SET.smartInv = chatSet.genSmartInv("chatSettingsMenu", "wwcConfigGUIChatSettings");
		
		MenuGui transSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.TRANS_SET.smartInv = transSet.genSmartInv("translatorSettingsMenu", 4, 9, ChatColor.BLUE, "wwcConfigGUITranslatorSettings");
		
		MenuGui transWatsonSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.WATSON_TRANS_SET.smartInv = transWatsonSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Watson"});
		
		MenuGui transGoogleSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.GOOGLE_TRANS_SET.smartInv = transGoogleSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Google"});
		
		MenuGui transAmazonSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv = transAmazonSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Amazon"});
		
		MenuGui transLibreSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.LIBRE_TRANS_SET.smartInv = transLibreSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Libre"});
		
		MenuGui transDeepSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.DEEP_TRANS_SET.smartInv = transDeepSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"DeepL"});

		MenuGui transAzureSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.AZURE_TRANS_SET.smartInv = transAzureSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Azure"});

		MenuGui transSystranSet = new MenuGui(inPlayer, transName);
		CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.smartInv = transSystranSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Systran"});

		/* Generate inventory contents */
		// General
		generalSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		generalSet.add(new ConvoElement(1, 1, "wwcConfigGUIPrefixButton", XMaterial.NAME_TAG,
				new GeneralSettingsConvos.Prefix()));
		generalSet.add(new ConvoElement(1, 2, "wwcConfigGUIFatalAsyncAbortButton", XMaterial.NAME_TAG,
				new GeneralSettingsConvos.FatalAsyncAbort()));
		generalSet.add(new ConvoElement(1, 3, "wwcConfigGUILangButton", XMaterial.NAME_TAG,
				new GeneralSettingsConvos.Lang()));
		generalSet.add(new ConvoElement(1, 4, "wwcConfigGUIUpdateCheckerButton", XMaterial.NAME_TAG,
				new GeneralSettingsConvos.UpdateChecker()));
		generalSet.add(new ConvoElement(1, 5, "wwcConfigGUISyncUserDataButton", XMaterial.NAME_TAG,
				new GeneralSettingsConvos.SyncUserData()));
		generalSet.add(new ToggleElement(1, 6, "wwcConfigGUIbStatsButton", "wwcConfigConversationbStatsSuccess", "General.enablebStats"));
		generalSet.add(new ToggleElement(1, 7, "wwcConfigGUIDebugModeButton", "wwcConfigConversationDebugModeSuccess", "General.enableDebugMode"));
		//generalSet.add(new ToggleElement(2, 1, "wwcConfigGUIVaultSupportButton", "wwcConfigConversationVaultSupportSuccess", "General.enableVaultSupport", true));
		generalSet.add(new CommonElement(2, 4, "Quit"));
		generalSet.add(new CommonElement(2, 6, "Next", new Object[] {CONFIG_GUI_TAGS.STORAGE_SET.smartInv}));
		generalSet.add(new CommonElement(2, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.GEN_SET.ordinal()+1 + ""}));
		
		// Storage
		ArrayList<String> storageToggles = new ArrayList<>(Arrays.asList("Storage.useSQL", "Storage.useMongoDB", "Storage.usePostgreSQL"));

		storageSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		storageSet.add(new SubMenuElement(1, 1, instance.isSQLConnValid(true), "wwcConfigGUISQLMenuButton", CONFIG_GUI_TAGS.SQL_SET.smartInv));
		storageSet.add(new SubMenuElement(1, 2, instance.isMongoConnValid(true), "wwcConfigGUIMongoMenuButton", CONFIG_GUI_TAGS.MONGO_SET.smartInv));
		storageSet.add(new SubMenuElement(1, 3, instance.isPostgresConnValid(true), "wwcConfigGUIPostgresMenuButton", CONFIG_GUI_TAGS.POSTGRES_SET.smartInv));
		storageSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.GEN_SET.smartInv}));
		storageSet.add(new CommonElement(2, 4, "Quit"));
		storageSet.add(new CommonElement(2, 6, "Next", new Object[] {CONFIG_GUI_TAGS.CHAT_SET.smartInv}));
		storageSet.add(new CommonElement(2, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.STORAGE_SET.ordinal()+1 + ""}));
		
		// SQL
		sqlSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		sqlSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleSQLButton", "wwcConfigConversationToggleSQLSuccess",
				"Storage.useSQL", storageToggles, false));
		sqlSet.add(new ConvoElement(1, 2, "wwcConfigGUISQLHostnameButton", XMaterial.NAME_TAG,
				new SQLSettingsConvos.Hostname()));
		sqlSet.add(new ConvoElement(1, 3, "wwcConfigGUISQLPortButton", XMaterial.NAME_TAG,
				new SQLSettingsConvos.Port()));
		sqlSet.add(new ConvoElement(1, 4, "wwcConfigGUISQLDatabaseNameButton", XMaterial.NAME_TAG,
				new SQLSettingsConvos.Database()));
		sqlSet.add(new ConvoElement(1, 5, "wwcConfigGUISQLUsernameButton", XMaterial.NAME_TAG,
				new SQLSettingsConvos.Username()));
		sqlSet.add(new ConvoElement(1, 6, "wwcConfigGUISQLPasswordButton", XMaterial.NAME_TAG,
				new SQLSettingsConvos.Password()));
		sqlSet.add(new ToggleElement(1, 7, "wwcConfigGUIToggleSQLSSLButton", "wwcConfigConversationToggleSQLSSLSuccess", "Storage.sqlUseSSL"));
		sqlSet.add(new ConvoElement(2, 1, "wwcConfigGUISQLOptionalArgsButton", XMaterial.NAME_TAG,
				new SQLSettingsConvos.OptionalArgs()));
		sqlSet.add(new CommonElement(3, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.STORAGE_SET.smartInv}));
		sqlSet.add(new CommonElement(3, 4, "Quit"));
		sqlSet.add(new CommonElement(3, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.STORAGE_SET.ordinal()+1 + ""}));
		
		// MongoDB
		mongoSet.add(new BorderElement(XMaterial.ORANGE_STAINED_GLASS_PANE));
		mongoSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleMongoButton", "wwcConfigConversationToggleMongoSuccess",
				"Storage.useMongoDB", storageToggles, false));
		mongoSet.add(new ConvoElement(1, 2, "wwcConfigGUIMongoHostnameButton", XMaterial.NAME_TAG,
				new MongoSettingsConvos.Hostname()));
		mongoSet.add(new ConvoElement(1, 3, "wwcConfigGUIMongoPortButton", XMaterial.NAME_TAG,
				new MongoSettingsConvos.Port()));
		mongoSet.add(new ConvoElement(1, 4, "wwcConfigGUIMongoDatabaseNameButton", XMaterial.NAME_TAG,
				new MongoSettingsConvos.Database()));
		mongoSet.add(new ConvoElement(1, 5, "wwcConfigGUIMongoUsernameButton", XMaterial.NAME_TAG,
				new MongoSettingsConvos.Username()));
		mongoSet.add(new ConvoElement(1, 6, "wwcConfigGUIMongoPasswordButton", XMaterial.NAME_TAG,
				new MongoSettingsConvos.Password()));
		mongoSet.add(new ConvoElement(1, 7, "wwcConfigGUIMongoOptionalArgsButton", XMaterial.NAME_TAG,
				new MongoSettingsConvos.OptionalArgs()));
		mongoSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.STORAGE_SET.smartInv}));
		mongoSet.add(new CommonElement(2, 4, "Quit"));
		mongoSet.add(new CommonElement(2, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.STORAGE_SET.ordinal()+1 + ""}));

		// PostgreSQL
		postgresSet.add(new BorderElement(XMaterial.GRAY_STAINED_GLASS_PANE));
		postgresSet.add(new ToggleElement(1, 1, "wwcConfigGUITogglePostgresButton", "wwcConfigConversationTogglePostgresSuccess",
				"Storage.usePostgreSQL", storageToggles, false));
		postgresSet.add(new ConvoElement(1, 2, "wwcConfigGUIPostgresHostnameButton", XMaterial.NAME_TAG,
				new PostgresSettingsConvos.Hostname()));
		postgresSet.add(new ConvoElement(1, 3, "wwcConfigGUIPostgresPortButton", XMaterial.NAME_TAG,
				new PostgresSettingsConvos.Port()));
		postgresSet.add(new ConvoElement(1, 4, "wwcConfigGUIPostgresDatabaseNameButton", XMaterial.NAME_TAG,
				new PostgresSettingsConvos.Database()));
		postgresSet.add(new ConvoElement(1, 5, "wwcConfigGUIPostgresUsernameButton", XMaterial.NAME_TAG,
				new PostgresSettingsConvos.Username()));
		postgresSet.add(new ConvoElement(1, 6, "wwcConfigGUIPostgresPasswordButton", XMaterial.NAME_TAG,
				new PostgresSettingsConvos.Password()));
		postgresSet.add(new ToggleElement(1, 7, "wwcConfigGUITogglePostgresSSLButton", "wwcConfigConversationTogglePostgresSSLSuccess", "Storage.postgresSSL"));
		postgresSet.add(new ConvoElement(2, 1, "wwcConfigGUIPostgresOptionalArgsButton", XMaterial.NAME_TAG,
				new PostgresSettingsConvos.OptionalArgs()));
		postgresSet.add(new CommonElement(3, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.STORAGE_SET.smartInv}));
		postgresSet.add(new CommonElement(3, 4, "Quit"));
		postgresSet.add(new CommonElement(3, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.STORAGE_SET.ordinal()+1 + ""}));

		// Chat 
		chatSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		chatSet.add(new ToggleElement(1, 1, "wwcConfigGUISendTranslationChatButton", "wwcConfigConversationSendTranslationChatSuccess", "Chat.sendTranslationChat"));
		chatSet.add(new ToggleElement(1, 2, "wwcConfigGUIPluginUpdateChatButton", "wwcConfigConversationPluginUpdateChatSuccess", "Chat.sendPluginUpdateChat"));
		chatSet.add(new ToggleElement(1, 3, "wwcConfigGUISendIncomingHoverTextChatButton", "wwcConfigConversationSendIncomingHoverTextChatSuccess", "Chat.sendIncomingHoverTextChat"));
		if (!main.getCurrPlatform().equals("Folia")) {
			chatSet.add(new SubMenuElement(1, 4, "wwcConfigGUIMessagesOverridePickChatButton", new MessagesOverridePickLangGui().getMessagesOverridePickLangGui()));
		}
		chatSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.STORAGE_SET.smartInv}));
		chatSet.add(new CommonElement(2, 4, "Quit"));
		chatSet.add(new CommonElement(2, 6, "Next", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		chatSet.add(new CommonElement(2, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.CHAT_SET.ordinal()+1 + ""}));
		
		// Translator
        List<String> translatorToggles = new ArrayList<>();
        for (Map.Entry<String, String> translatorPair : CommonRefs.translatorPairs.entrySet()) {
            String key = translatorPair.getKey();
            translatorToggles.add(key);
        }

        transSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
	    transSet.add(new SubMenuElement(1, 1, transName.equals("Amazon Translate"), "wwcConfigGUIAmazonTranslateButton", CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv));
	    transSet.add(new SubMenuElement(1, 2, transName.equals("Azure Translate"), "wwcConfigGUIAzureTranslateButton", CONFIG_GUI_TAGS.AZURE_TRANS_SET.smartInv));
		transSet.add(new SubMenuElement(1, 3, transName.equals("DeepL Translate"), "wwcConfigGUIDeepLTranslateButton", CONFIG_GUI_TAGS.DEEP_TRANS_SET.smartInv));
		transSet.add(new SubMenuElement(1, 4, transName.equals("Google Translate"), "wwcConfigGUIGoogleTranslateButton", CONFIG_GUI_TAGS.GOOGLE_TRANS_SET.smartInv));
		transSet.add(new SubMenuElement(1, 5, transName.equals("Libre Translate"), "wwcConfigGUILibreTranslateButton", CONFIG_GUI_TAGS.LIBRE_TRANS_SET.smartInv));
		transSet.add(new SubMenuElement(1, 6, transName.equals("Systran Translate"), "wwcConfigGUISystranTranslateButton", CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.smartInv));
		transSet.add(new SubMenuElement(1, 7, transName.equals("Watson"), "wwcConfigGUIWatsonButton", CONFIG_GUI_TAGS.WATSON_TRANS_SET.smartInv));
		transSet.add(new ConvoElement(2, 1, "wwcConfigGUITranslatorCacheButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsConvos.TranslationCache()));
	    transSet.add(new ConvoElement(2, 2, "wwcConfigGUIGlobalRateLimitButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsConvos.GlobalRateLimit()));
	    transSet.add(new ConvoElement(2, 3, "wwcConfigGUIErrorLimitButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsConvos.ErrorLimit()));
	    transSet.add(new ConvoElement(2, 4, "wwcConfigGUICharacterLimitButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsConvos.CharacterLimit()));
		transSet.add(new ConvoElement(2, 5, "wwcConfigGUIIgnoreErrorsButton", XMaterial.NAME_TAG,
						new TranslatorSettingsConvos.IgnoreErrors()));
		transSet.add(new ToggleElement(2, 6, "wwcConfigGUIPersistentCacheButton", "wwcConfigConversationPersistentCacheSuccess", "Translator.enablePersistentCache"));
	    transSet.add(new CommonElement(3, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.CHAT_SET.smartInv}));
		transSet.add(new CommonElement(3, 4, "Quit"));
		transSet.add(new CommonElement(3, 8, "Page Number", new String[] {CONFIG_GUI_TAGS.CHAT_SET.ordinal()+1 + ""}));
		
		// Watson Translator
		transWatsonSet.add(new BorderElement(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));
		transWatsonSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleWatsonTranslateButton", "wwcConfigConversationWatsonTranslateToggleSuccess",
				"Translator.useWatsonTranslate", translatorToggles, false));
		transWatsonSet.add(new ConvoElement(1, 2, "wwcConfigGUIWatsonAPIKeyButton", XMaterial.NAME_TAG, 
				new WatsonSettingsConvos.ApiKey()));
		transWatsonSet.add(new ConvoElement(1, 3, "wwcConfigGUIWatsonURLButton", XMaterial.NAME_TAG,
				new WatsonSettingsConvos.ServiceUrl()));
		transWatsonSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transWatsonSet.add(new CommonElement(2, 4, "Quit"));
		transWatsonSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
		
		// Google Translator
		transGoogleSet.add(new BorderElement(XMaterial.RED_STAINED_GLASS_PANE));
		transGoogleSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleGoogleTranslateButton", "wwcConfigConversationGoogleTranslateToggleSuccess",
				"Translator.useGoogleTranslate", translatorToggles, false));
		transGoogleSet.add(new ConvoElement(1, 2, "wwcConfigGUIGoogleTranslateAPIKeyButton", XMaterial.NAME_TAG, 
				new GoogleSettingsConvos.ApiKey()));
		transGoogleSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transGoogleSet.add(new CommonElement(2, 4, "Quit"));
		transGoogleSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
		
		// Amazon Translator
		transAmazonSet.add(new BorderElement(XMaterial.YELLOW_STAINED_GLASS_PANE));
		transAmazonSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleAmazonTranslateButton", "wwcConfigConversationAmazonTranslateToggleSuccess",
				"Translator.useAmazonTranslate", translatorToggles, false));
		transAmazonSet.add(new ConvoElement(1, 2, "wwcConfigGUIAmazonTranslateAccessKeyButton", XMaterial.NAME_TAG,
				new AmazonSettingsConvos.AccessKey()));
		transAmazonSet.add(new ConvoElement(1, 3, "wwcConfigGUIAmazonTranslateSecretKeyButton", XMaterial.NAME_TAG, 
				new AmazonSettingsConvos.SecretKey()));
		transAmazonSet.add(new ConvoElement(1, 4, "wwcConfigGUIAmazonTranslateRegionButton", XMaterial.NAME_TAG, 
				new AmazonSettingsConvos.Region()));
		transAmazonSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transAmazonSet.add(new CommonElement(2, 4, "Quit"));
		transAmazonSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
		
		// Libre Translator
		transLibreSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		transLibreSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleLibreTranslateButton", "wwcConfigConversationLibreTranslateToggleSuccess",
				"Translator.useLibreTranslate", translatorToggles, false));
	    transLibreSet.add(new ConvoElement(1, 2, "wwcConfigGUILibreTranslateURLButton", XMaterial.NAME_TAG,
	    		new LibreSettingsConvos.Url()));
		transLibreSet.add(new ConvoElement(1, 3, "wwcConfigGUILibreTranslateApiKeyButton", XMaterial.NAME_TAG,
	    		new LibreSettingsConvos.ApiKey()));
		transLibreSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transLibreSet.add(new CommonElement(2, 4, "Quit"));
		transLibreSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
		
		// DeepL Translator
		transDeepSet.add(new BorderElement(XMaterial.BLUE_STAINED_GLASS_PANE));
		transDeepSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleDeepLTranslateButton", "wwcConfigConversationDeepLTranslateToggleSuccess",
				"Translator.useDeepLTranslate", translatorToggles, false));
		transDeepSet.add(new ConvoElement(1, 2, "wwcConfigGUIDeepLTranslateApiKeyButton", XMaterial.NAME_TAG,
	    		new DeepLSettingsConvos.ApiKey()));
		transDeepSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transDeepSet.add(new CommonElement(2, 4, "Quit"));
		transDeepSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));

		// Azure Translator
		transAzureSet.add(new BorderElement(XMaterial.GREEN_STAINED_GLASS_PANE));
		transAzureSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleAzureTranslateButton", "wwcConfigConversationAzureTranslateToggleSuccess",
				"Translator.useAzureTranslate", translatorToggles, false));
		transAzureSet.add(new ConvoElement(1, 2, "wwcConfigGUIAzureTranslateApiKeyButton", XMaterial.NAME_TAG,
				new AzureSettingsConvos.ApiKey()));
		transAzureSet.add(new ConvoElement(1, 3, "wwcConfigGUIAzureTranslateRegionButton", XMaterial.NAME_TAG,
				new AzureSettingsConvos.Region()));
		transAzureSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transAzureSet.add(new CommonElement(2, 4, "Quit"));
		transAzureSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));

		// Systran Translator
		transSystranSet.add(new BorderElement(XMaterial.CYAN_STAINED_GLASS_PANE));
		transSystranSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleSystranTranslateButton", "wwcConfigConversationSystranTranslateToggleSuccess",
				"Translator.useSystranTranslate", translatorToggles, false));
		transSystranSet.add(new ConvoElement(1, 2, "wwcConfigGUISystranTranslateApiKeyButton", XMaterial.NAME_TAG,
				new SystranSettingsConvos.ApiKey()));
		transSystranSet.add(new CommonElement(2, 2, "Previous", new Object[] {CONFIG_GUI_TAGS.TRANS_SET.smartInv}));
		transSystranSet.add(new CommonElement(2, 4, "Quit"));
		transSystranSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
	}
	
	static abstract class Element {
		public int x;
		public int y;
		
		public Player player;
		public InventoryContents contents;
		
		public String buttonName;
		public XMaterial blockIcon;
		
		public Element(int x_, int y_, String buttonName_, XMaterial blockIcon_) {
			x = x_;
			y = y_;
			
			buttonName = buttonName_;
			blockIcon = blockIcon_;
		}
		
		// Draw element to inventory grid
		abstract public void rasterize(Player player, InventoryContents contents);
	}
	
	static class ConvoElement extends Element {
		
		public Prompt prompt;

		private WWCInventoryManager invManager = instance.getInventoryManager();
		
		public ConvoElement(int x_, int y_, String buttonName_, XMaterial blockIcon_, Prompt prompt_) {
			super(x_, y_, buttonName_, blockIcon_);
			
			prompt = prompt_;
		}
		
		@Override
		public void rasterize(Player player, InventoryContents contents) {
			invManager.genericConversationButton(x, y, player, contents, prompt, blockIcon, buttonName);
		}
		
	}
	
	static class ToggleElement extends Element {
		
		public String onSuccess;
		public String configName;
		public List<String> configValsToDisable = new ArrayList<>();

		private WWCInventoryManager invManager = instance.getInventoryManager();

		private boolean restartRequired;

		public ToggleElement(int x_, int y_, String buttonName_, String onSuccess_, String configName_, boolean serverRestartRequired_) {
			this(x_, y_, buttonName_, onSuccess_, configName_, null, serverRestartRequired_);
		}

		public ToggleElement(int x_, int y_, String buttonName_, String onSuccess_, String configName_) {
			this(x_, y_, buttonName_, onSuccess_, configName_, null, false);
		}
		
		public ToggleElement(int x_, int y_, String buttonName_, String onSuccess_, String configName_, List<String> configValsToDisable_, boolean serverRestartRequired_) {
			super(x_, y_, buttonName_, null);

			restartRequired = serverRestartRequired_;
			configName = configName_;
			onSuccess = onSuccess_;
			if (configValsToDisable_ != null) {
				// Make sure that config vals to disable list doesn't contain the value we're trying to enable
				configValsToDisable = configValsToDisable_;
			}
		}
		
		@Override
		public void rasterize(Player player, InventoryContents contents) {
			invManager.genericToggleButton(x, y, player, contents, buttonName,
					onSuccess, configName, configValsToDisable, restartRequired);
		}
		
	}
	
	static class CommonElement extends Element {
		
		public Object[] args = new String[0];

		private WWCInventoryManager invManager = instance.getInventoryManager();
		
		public CommonElement(int x_, int y_, String buttonName_, Object[] args_) {
			super(x_, y_, buttonName_, null);

			if (args_ != null) {
				args = args_;
			}
		}
		
		public CommonElement(int x_, int y_, String buttonName_) {
			super(x_, y_, buttonName_, null);
		}

		@Override
		public void rasterize(Player player, InventoryContents contents) {
			invManager.setCommonButton(x, y, player, contents, buttonName, args);
		}
		
	}
	
	static class BorderElement extends Element {

		private WWCInventoryManager invManager = instance.getInventoryManager();
		public BorderElement(XMaterial blockIcon_) {
			super(0, 0, "", blockIcon_);
		}

		@Override
		public void rasterize(Player player, InventoryContents contents) {
			invManager.setBorders(contents, blockIcon);
		}
	}
	
	static class SubMenuElement extends Element {

		public Boolean preCondition = null;
		public SmartInventory invToOpen;

		private WWCInventoryManager invManager = instance.getInventoryManager();
		
		public SubMenuElement(int x_, int y_, Boolean preCondition_, String buttonName_, SmartInventory invToOpen_) {
			super(x_, y_, buttonName_, null);
			
			preCondition = preCondition_;
			invToOpen = invToOpen_;
		}
		
		public SubMenuElement(int x_, int y_, String buttonName_, SmartInventory invToOpen_) {
			super(x_, y_, buttonName_, null);
			
			invToOpen = invToOpen_;
		}

		@Override
		public void rasterize(Player player, InventoryContents contents) {
			invManager.genericOpenSubmenuButton(x, y, player, contents, preCondition, buttonName, invToOpen);
		}
		
	}
	
	private List<Element> elements = new ArrayList<>();

	public void add(Element e) {elements.add(e); }
	
	@Override
	public void init(Player player, InventoryContents contents) {

		try {
			for (Element e : elements) e.rasterize(player, contents);
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
	
	private SmartInventory genSmartInv(String id, int x, int y, ChatColor col, String titleTag, String[] args) {
		CommonRefs refs = main.getServerFactory().getCommonRefs();
		return SmartInventory.builder().id(id)
				.provider(this).size(x, y)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(col + refs.getMsg(titleTag, args,inPlayer))
				.build();
	}
	
	private SmartInventory genSmartInv(String id, int x, int y, ChatColor col, String titleTag) {
		CommonRefs refs = main.getServerFactory().getCommonRefs();
		return SmartInventory.builder().id(id)
		.provider(this).size(x, y)
		.manager(WorldwideChat.instance.getInventoryManager())
		.title(col + refs.getMsg(titleTag, inPlayer))
		.build();
	}
	
	// Use default config inventory settings
	private SmartInventory genSmartInv(String id, String titleTag) {
		return genSmartInv(id, 3, 9, ChatColor.BLUE, titleTag);
	}
	
	private SmartInventory genSmartInv(String id, String titleTag, String[] args) {
		return genSmartInv(id, 3, 9, ChatColor.BLUE, titleTag, args);
	}

}
