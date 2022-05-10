package com.expl0itz.worldwidechat.inventory.configuration;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsFatalAsyncAbortConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsLangConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsPrefixConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsSyncUserDataConversation;
import com.expl0itz.worldwidechat.conversations.configuration.GeneralSettingsUpdateCheckerConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsDatabaseConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsHostnameConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsOptionalArgsConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsPasswordConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsPortConversation;
import com.expl0itz.worldwidechat.conversations.configuration.SQLSettingsUsernameConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateAccessKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateRegionConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsAmazonTranslateSecretKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsCharacterLimitConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsErrorLimitConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGlobalRateConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsGoogleTranslateApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsTranslationCacheConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonApiKeyConversation;
import com.expl0itz.worldwidechat.conversations.configuration.TranslatorSettingsWatsonServiceUrlConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.SQLUtils;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class MenuGui implements InventoryProvider {
	
	public static enum TAGS {
		GEN_SET, STORAGE_SET, SQL_SET, CHAT_SET, TRANS_SET, WATSON_TRANS_SET, GOOGLE_TRANS_SET, AMAZON_TRANS_SET;
		
		public SmartInventory smartInv;
	}
	
	static public void genAllConfigUIs() {
		//TODO: Add a convo class with a similar structure to this one. Combine everything :)
		/* Generate inventories */
		MenuGui generalSet = new MenuGui();
		TAGS.GEN_SET.smartInv = generalSet.genSmartInv("generalSettingsMenu", "wwcConfigGUIGeneralSettings");
		
		MenuGui storageSet = new MenuGui();
		TAGS.STORAGE_SET.smartInv = storageSet.genSmartInv("storageSettingsMenu", "wwcConfigGUIStorageSettings");
		
		MenuGui sqlSet = new MenuGui();
		TAGS.SQL_SET.smartInv = sqlSet.genSmartInv("sqlSettingsMenu", 4, 9, ChatColor.BLUE, "wwcConfigGUISQLSettings");
		
		MenuGui chatSet = new MenuGui();
		TAGS.CHAT_SET.smartInv = chatSet.genSmartInv("chatSettingsMenu", "wwcConfigGUIChatSettings");
		
		MenuGui transSet = new MenuGui();
		TAGS.TRANS_SET.smartInv = transSet.genSmartInv("translatorSettingsMenu", "wwcConfigGUITranslatorSettings");
		
		MenuGui transWatsonSet = new MenuGui();
		TAGS.WATSON_TRANS_SET.smartInv = transWatsonSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Watson"});
		
		MenuGui transGoogleSet = new MenuGui();
		TAGS.GOOGLE_TRANS_SET.smartInv = transGoogleSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Google"});
		
		MenuGui transAmazonSet = new MenuGui();
		TAGS.AMAZON_TRANS_SET.smartInv = transAmazonSet.genSmartInv("eachTranslatorSettings", "wwcConfigGUIEachTranslatorSettings", new String[] {"Amazon"});
		
		/* Generate inventory contents */
		// General
		generalSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		generalSet.add(new ConvoElement(1, 1, "wwcConfigGUIPrefixButton", XMaterial.NAME_TAG,
				new GeneralSettingsPrefixConversation()));
		generalSet.add(new ToggleElement(1, 2, "wwcConfigGUIbStatsButton", "wwcConfigConversationbStatsSuccess", "General.enablebStats"));
		generalSet.add(new ConvoElement(1, 3, "wwcConfigGUILangButton", XMaterial.NAME_TAG,
				new GeneralSettingsLangConversation()));
		generalSet.add(new ConvoElement(1, 4, "wwcConfigGUIUpdateCheckerButton", XMaterial.NAME_TAG,
				new GeneralSettingsUpdateCheckerConversation()));
		generalSet.add(new ConvoElement(1, 5, "wwcConfigGUISyncUserDataButton", XMaterial.NAME_TAG,
				new GeneralSettingsSyncUserDataConversation()));
		generalSet.add(new ConvoElement(1, 6, "wwcConfigGUIFatalAsyncAbortButton", XMaterial.NAME_TAG,
				new GeneralSettingsFatalAsyncAbortConversation()));
		generalSet.add(new ToggleElement(1, 7, "wwcConfigGUIDebugModeButton", "wwcConfigConversationDebugModeSuccess", "General.enableDebugMode"));
		generalSet.add(new CommonElement(2, 4, "Quit", null));
		generalSet.add(new CommonElement(2, 6, "Next", new Object[] {TAGS.STORAGE_SET.smartInv}));
		generalSet.add(new CommonElement(2, 8, "Page Number", new String[] {TAGS.GEN_SET.ordinal()+1 + ""}));
		
		// Storage
		storageSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		storageSet.add(new SubMenuElement(1, 1, SQLUtils.isConnected(), "wwcConfigGUISQLMenuButton", TAGS.SQL_SET.smartInv));
		storageSet.add(new CommonElement(2, 2, "Previous", new Object[] {TAGS.GEN_SET.smartInv}));
		storageSet.add(new CommonElement(2, 4, "Quit", null));
		storageSet.add(new CommonElement(2, 6, "Next", new Object[] {TAGS.CHAT_SET.smartInv}));
		storageSet.add(new CommonElement(2, 8, "Page Number", new String[] {TAGS.STORAGE_SET.ordinal()+1 + ""}));
		
		// SQL
		sqlSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		sqlSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleSQLButton", "wwcConfigConversationToggleSQLSuccess", "Storage.useSQL"));
		sqlSet.add(new ConvoElement(1, 2, "wwcConfigGUISQLHostnameButton", XMaterial.NAME_TAG,
				new SQLSettingsHostnameConversation()));
		sqlSet.add(new ConvoElement(1, 3, "wwcConfigGUISQLPortButton", XMaterial.NAME_TAG,
				new SQLSettingsPortConversation()));
		sqlSet.add(new ConvoElement(1, 4, "wwcConfigGUISQLDatabaseNameButton", XMaterial.NAME_TAG,
				new SQLSettingsDatabaseConversation()));
		sqlSet.add(new ConvoElement(1, 5, "wwcConfigGUISQLUsernameButton", XMaterial.NAME_TAG,
				new SQLSettingsUsernameConversation()));
		sqlSet.add(new ConvoElement(1, 6, "wwcConfigGUISQLPasswordButton", XMaterial.NAME_TAG,
				new SQLSettingsPasswordConversation()));
		sqlSet.add(new ToggleElement(1, 7, "wwcCOnfigGUIToggleSQLSSLButton", "wwcConfigConversationToggleSQLSSLSuccess", "Storage.sqlUseSSL"));
		sqlSet.add(new ConvoElement(2, 1, "wwcConfigGUISQLOptionalArgsButton", XMaterial.NAME_TAG,
				new SQLSettingsOptionalArgsConversation()));
		sqlSet.add(new CommonElement(3, 2, "Previous", new Object[] {TAGS.STORAGE_SET.smartInv}));
		sqlSet.add(new CommonElement(3, 4, "Quit", null));
		sqlSet.add(new CommonElement(3, 8, "Page Number", new String[] {TAGS.STORAGE_SET.ordinal()+1 + ""}));
		
		// Chat 
		chatSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		chatSet.add(new ToggleElement(1, 1, "wwcConfigGUISendTranslationChatButton", "wwcConfigConversationSendTranslationChatSuccess", "Chat.sendTranslationChat"));
		chatSet.add(new ToggleElement(1, 2, "wwcConfigGUIPluginUpdateChatButton", "wwcConfigConversationPluginUpdateChatSuccess", "Chat.sendPluginUpdateChat"));
		chatSet.add(new ToggleElement(1, 3, "wwcConfigGUISendFailedTranslationChatButton", "wwcConfigConversationSendFailedTranslationChatSuccess", "Chat.sendFailedTranslationChat"));
		chatSet.add(new ToggleElement(1, 4, "wwcConfigGUISendIncomingHoverTextChatButton", "wwcConfigConversationSendIncomingHoverTextChatSuccess", "Chat.sendIncomingHoverTextChat"));
		chatSet.add(new SubMenuElement(1, 5, "wwcConfigGUIMessagesOverrideChatButton", MessagesOverrideCurrentListGUI.overrideMessagesSettings));
		chatSet.add(new CommonElement(2, 2, "Previous", new Object[] {TAGS.STORAGE_SET.smartInv}));
		chatSet.add(new CommonElement(2, 4, "Quit", null));
		chatSet.add(new CommonElement(2, 6, "Next", new Object[] {TAGS.TRANS_SET.smartInv}));
		chatSet.add(new CommonElement(2, 8, "Page Number", new String[] {TAGS.CHAT_SET.ordinal()+1 + ""}));
		
		// Translator
		transSet.add(new BorderElement(XMaterial.WHITE_STAINED_GLASS_PANE));
		transSet.add(new SubMenuElement(1, 1, WorldwideChat.instance.getTranslatorName().equals("Watson"), "wwcConfigGUIWatsonButton", TAGS.WATSON_TRANS_SET.smartInv));
	    transSet.add(new SubMenuElement(1, 2, WorldwideChat.instance.getTranslatorName().equals("Google Translate"), "wwcConfigGUIGoogleTranslateButton", TAGS.GOOGLE_TRANS_SET.smartInv));
	    transSet.add(new SubMenuElement(1, 3, WorldwideChat.instance.getTranslatorName().equals("Amazon Translate"), "wwcConfigGUIAmazonTranslateButton", TAGS.AMAZON_TRANS_SET.smartInv));
	    transSet.add(new ConvoElement(1, 4, "wwcConfigGUITranslatorCacheButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsTranslationCacheConversation()));
	    transSet.add(new ConvoElement(1, 5, "wwcConfigGUIGlobalRateLimitButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsGlobalRateConversation()));
	    transSet.add(new ConvoElement(1, 6, "wwcConfigGUIErrorLimitButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsErrorLimitConversation()));
	    transSet.add(new ConvoElement(1, 7, "wwcConfigGUICharacterLimitButton", XMaterial.NAME_TAG,
	    		new TranslatorSettingsCharacterLimitConversation()));
	    transSet.add(new CommonElement(2, 2, "Previous", new Object[] {TAGS.CHAT_SET.smartInv}));
		transSet.add(new CommonElement(2, 4, "Quit", null));
		transSet.add(new CommonElement(2, 8, "Page Number", new String[] {TAGS.CHAT_SET.ordinal()+1 + ""}));
		
		// Watson Translator
		transWatsonSet.add(new BorderElement(XMaterial.BLUE_STAINED_GLASS_PANE));
		transWatsonSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleWatsonTranslateButton", "wwcConfigConversationWatsonTranslateToggleSuccess", "Translator.useWatsonTranslate", new String[] {"Translator.useGoogleTranslate", "Translator.useAmazonTranslate"}));
		transWatsonSet.add(new ConvoElement(1, 2, "wwcConfigGUIWatsonAPIKeyButton", XMaterial.NAME_TAG, 
				new TranslatorSettingsWatsonApiKeyConversation()));
		transWatsonSet.add(new ConvoElement(1, 3, "wwcConfigGUIWatsonURLButton", XMaterial.NAME_TAG,
				new TranslatorSettingsWatsonServiceUrlConversation()));
		transWatsonSet.add(new CommonElement(2, 2, "Previous", new Object[] {TAGS.TRANS_SET.smartInv}));
		transWatsonSet.add(new CommonElement(2, 4, "Quit", null));
		transWatsonSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
		
		// Google Translator
		transGoogleSet.add(new BorderElement(XMaterial.RED_STAINED_GLASS_PANE));
		transGoogleSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleGoogleTranslateButton", "wwcConfigConversationGoogleTranslateToggleSuccess", "Translator.useGoogleTranslate", new String[] {"Translator.useWatsonTranslate", "Translator.useAmazonTranslate"}));
		transGoogleSet.add(new ConvoElement(1, 2, "wwcConfigGUIGoogleTranslateAPIKeyButton", XMaterial.NAME_TAG, 
				new TranslatorSettingsGoogleTranslateApiKeyConversation()));
		transGoogleSet.add(new CommonElement(2, 2, "Previous", new Object[] {TAGS.TRANS_SET.smartInv}));
		transGoogleSet.add(new CommonElement(2, 4, "Quit", null));
		transGoogleSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
		
		// Amazon Translator
		transAmazonSet.add(new BorderElement(XMaterial.YELLOW_STAINED_GLASS_PANE));
		transAmazonSet.add(new ToggleElement(1, 1, "wwcConfigGUIToggleAmazonTranslateButton", "wwcConfigConversationAmazonTranslateToggleSuccess", "Translator.useAmazonTranslate", new String[] {"Translator.useGoogleTranslate", "Translator.useWatsonTranslate"}));
		transAmazonSet.add(new ConvoElement(1, 2, "wwcConfigGUIAmazonTranslateAccessKeyButton", XMaterial.NAME_TAG, 
				new TranslatorSettingsAmazonTranslateAccessKeyConversation()));
		transAmazonSet.add(new ConvoElement(1, 3, "wwcConfigGUIAmazonTranslateSecretKeyButton", XMaterial.NAME_TAG, 
				new TranslatorSettingsAmazonTranslateSecretKeyConversation()));
		transAmazonSet.add(new ConvoElement(1, 4, "wwcConfigGUIAmazonTranslateRegionButton", XMaterial.NAME_TAG, 
				new TranslatorSettingsAmazonTranslateRegionConversation()));
		transAmazonSet.add(new CommonElement(2, 2, "Previous", new Object[] {TAGS.TRANS_SET.smartInv}));
		transAmazonSet.add(new CommonElement(2, 4, "Quit", null));
		transAmazonSet.add(new CommonElement(2, 8, "Page Number", new String[] {"1"}));
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
		
		public ConvoElement(int x_, int y_, String buttonName_, XMaterial blockIcon_, Prompt prompt_) {
			super(x_, y_, buttonName_, blockIcon_);
			
			prompt = prompt_;
		}
		
		@Override
		public void rasterize(Player player, InventoryContents contents) {
			WWCInventoryManager.genericConversationButton(x, y, player, contents, prompt, blockIcon, buttonName);
		}
		
	}
	
	static class ToggleElement extends Element {
		
		public String onSuccess;
		public String configName;
		public String[] args = new String[0];
		
		public ToggleElement(int x_, int y_, String buttonName_, String onSuccess_, String configName_) {
			this(x_, y_, buttonName_, onSuccess_, configName_, null);
		}
		
		public ToggleElement(int x_, int y_, String buttonName_, String onSuccess_, String configName_, String[] args_) {
			super(x_, y_, buttonName_, null);
			
			configName = configName_;
			onSuccess = onSuccess_;
			if (args != null) {
				args = args_;
			}
		}
		
		@Override
		public void rasterize(Player player, InventoryContents contents) {
			WWCInventoryManager.genericToggleButton(x, y, player, contents, buttonName, 
					onSuccess, configName, args);
		}
		
	}
	
	static class CommonElement extends Element {
		
		public Object[] args = new String[0];
		
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
			//DEBUG
			WWCInventoryManager.setCommonButton(x, y, player, contents, buttonName, args);
		}
		
	}
	
	static class BorderElement extends Element {
		public BorderElement(XMaterial blockIcon_) {
			super(0, 0, "", blockIcon_);
		}

		@Override
		public void rasterize(Player player, InventoryContents contents) {
			WWCInventoryManager.setBorders(contents, blockIcon);
		}
	}
	
	static class SubMenuElement extends Element {

		public Object preCondition = null;
		public SmartInventory invToOpen;
		
		public SubMenuElement(int x_, int y_, Object preCondition_, String buttonName_, SmartInventory invToOpen_) {
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
			WWCInventoryManager.genericOpenSubmenuButton(x, y, player, contents, preCondition, buttonName, invToOpen);
		}
		
	}
	
	private List<Element> elements = new ArrayList<>();
	
	public void add(Element e) {elements.add(e); }
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			for (Element e : elements) e.rasterize(player, contents);
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}
	
	private SmartInventory genSmartInv(String id, int x, int y, ChatColor col, String titleTag, String[] args) {
		return SmartInventory.builder().id(id)
				.provider(this).size(x, y)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(col + CommonDefinitions.getMessage(titleTag, args))
				.build();
	}
	
	private SmartInventory genSmartInv(String id, int x, int y, ChatColor col, String titleTag) {
		return SmartInventory.builder().id(id)
		.provider(this).size(x, y)
		.manager(WorldwideChat.instance.getInventoryManager())
		.title(col + CommonDefinitions.getMessage(titleTag))
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
