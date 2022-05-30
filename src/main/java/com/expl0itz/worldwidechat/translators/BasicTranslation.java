package com.expl0itz.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;

public class BasicTranslation {

	//TODO: Make translators download their languages from out GitHub repo, and not their websites
	//In case the websites change, we won't break
	
	public WorldwideChat main = WorldwideChat.instance;
	
	public String textToTranslate;
	public String inputLang;
	public String outputLang;
	
	public boolean isInitializing;
	
	public BasicTranslation(String textToTranslate, String inputLang, String outputLang) {
		isInitializing = false;
		this.textToTranslate = textToTranslate;
		this.inputLang = inputLang;
		this.outputLang = outputLang;
	}
	
	public BasicTranslation(boolean isInitializing) {
		this.isInitializing = isInitializing;
	}
	
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		return textToTranslate;
	}
	
	public void setBackupCodes() {
		List<SupportedLanguageObject> supportedLangs = new ArrayList<SupportedLanguageObject>();
		if (this instanceof AmazonTranslation) {
			supportedLangs.add(new SupportedLanguageObject("af", "Afrikaans"));
			supportedLangs.add(new SupportedLanguageObject("sq", "Albanian"));
			supportedLangs.add(new SupportedLanguageObject("am", "Amharic"));
			supportedLangs.add(new SupportedLanguageObject("ar", "Arabic"));
			supportedLangs.add(new SupportedLanguageObject("hy", "Armenian"));
			supportedLangs.add(new SupportedLanguageObject("az", "Azerbaijani"));
			supportedLangs.add(new SupportedLanguageObject("bn", "Bengali"));
			supportedLangs.add(new SupportedLanguageObject("bs", "Bosnian"));
			supportedLangs.add(new SupportedLanguageObject("bg", "Bulgarian"));
			supportedLangs.add(new SupportedLanguageObject("ca", "Catalan"));
			supportedLangs.add(new SupportedLanguageObject("zh", "Chinese (Simplified)"));
			supportedLangs.add(new SupportedLanguageObject("zh-TW", "Chinese (Traditional)"));
			supportedLangs.add(new SupportedLanguageObject("hr", "Croatian"));
			supportedLangs.add(new SupportedLanguageObject("cs", "Czech"));
			supportedLangs.add(new SupportedLanguageObject("da", "Danish"));
			supportedLangs.add(new SupportedLanguageObject("fa-AF", "Dari"));
			supportedLangs.add(new SupportedLanguageObject("nl", "Dutch"));
			supportedLangs.add(new SupportedLanguageObject("en", "English"));
			supportedLangs.add(new SupportedLanguageObject("et", "Estonian"));
			supportedLangs.add(new SupportedLanguageObject("fa", "Farsi (Persian)"));
			supportedLangs.add(new SupportedLanguageObject("tl", "Filipino, Tagalog"));
			supportedLangs.add(new SupportedLanguageObject("fi", "Finnish"));
			supportedLangs.add(new SupportedLanguageObject("fr", "French"));
			supportedLangs.add(new SupportedLanguageObject("fr-CA", "French (Canada)"));
			supportedLangs.add(new SupportedLanguageObject("ka", "Georgian"));
			supportedLangs.add(new SupportedLanguageObject("de", "German"));
			supportedLangs.add(new SupportedLanguageObject("el", "Greek"));
			supportedLangs.add(new SupportedLanguageObject("gu", "Gujarati"));
			supportedLangs.add(new SupportedLanguageObject("ht", "Haitian Creole"));
			supportedLangs.add(new SupportedLanguageObject("ha", "Hausa"));
			supportedLangs.add(new SupportedLanguageObject("he", "Hebrew"));
			supportedLangs.add(new SupportedLanguageObject("hi", "Hindi"));
			supportedLangs.add(new SupportedLanguageObject("hu", "Hungarian"));
			supportedLangs.add(new SupportedLanguageObject("is", "Icelandic"));
			supportedLangs.add(new SupportedLanguageObject("id", "Indonesian"));
			supportedLangs.add(new SupportedLanguageObject("ga", "Irish"));
			supportedLangs.add(new SupportedLanguageObject("it", "Italian"));
			supportedLangs.add(new SupportedLanguageObject("ja", "Japanese"));
			supportedLangs.add(new SupportedLanguageObject("kn", "Kannada"));
			supportedLangs.add(new SupportedLanguageObject("kk", "Kazakh"));
			supportedLangs.add(new SupportedLanguageObject("ko", "Korean"));
			supportedLangs.add(new SupportedLanguageObject("lv", "Latvian"));
			supportedLangs.add(new SupportedLanguageObject("lt", "Lithuanian"));
			supportedLangs.add(new SupportedLanguageObject("mk", "Macedonian"));
			supportedLangs.add(new SupportedLanguageObject("ms", "Malay"));
			supportedLangs.add(new SupportedLanguageObject("ml", "Malayalam"));
			supportedLangs.add(new SupportedLanguageObject("mt", "Maltese"));
			supportedLangs.add(new SupportedLanguageObject("mr", "Marathi"));
			supportedLangs.add(new SupportedLanguageObject("mn", "Mongolian"));
			supportedLangs.add(new SupportedLanguageObject("no", "Norwegian"));
			supportedLangs.add(new SupportedLanguageObject("ps", "Pashto"));
			supportedLangs.add(new SupportedLanguageObject("pl", "Polish"));
			supportedLangs.add(new SupportedLanguageObject("pt", "Portuguese (Brazil)"));
			supportedLangs.add(new SupportedLanguageObject("pt-PT", "Portuguese (Portugal)"));
			supportedLangs.add(new SupportedLanguageObject("pa", "Punjabi"));
			supportedLangs.add(new SupportedLanguageObject("ro", "Romanian"));
			supportedLangs.add(new SupportedLanguageObject("ru", "Russian"));
			supportedLangs.add(new SupportedLanguageObject("sr", "Serbian"));
			supportedLangs.add(new SupportedLanguageObject("si", "Sinhala"));
			supportedLangs.add(new SupportedLanguageObject("sk", "Slovak"));
			supportedLangs.add(new SupportedLanguageObject("sl", "Slovenian"));
			supportedLangs.add(new SupportedLanguageObject("so", "Somali"));
			supportedLangs.add(new SupportedLanguageObject("es", "Spanish"));
			supportedLangs.add(new SupportedLanguageObject("es-MX", "Spanish (Mexico)"));
			supportedLangs.add(new SupportedLanguageObject("sw", "Swahili"));
			supportedLangs.add(new SupportedLanguageObject("sv", "Swedish"));
			supportedLangs.add(new SupportedLanguageObject("ta", "Tamil"));
			supportedLangs.add(new SupportedLanguageObject("te", "Telugu"));
			supportedLangs.add(new SupportedLanguageObject("th", "Thai"));
			supportedLangs.add(new SupportedLanguageObject("tr", "Turkish"));
			supportedLangs.add(new SupportedLanguageObject("uk", "Ukrainian"));
			supportedLangs.add(new SupportedLanguageObject("ur", "Urdu"));
			supportedLangs.add(new SupportedLanguageObject("uz", "Uzbek"));
			supportedLangs.add(new SupportedLanguageObject("vi", "Vietnamese"));
			supportedLangs.add(new SupportedLanguageObject("cy", "Welsh"));
		} else if (this instanceof WatsonTranslation) {
			supportedLangs.add(new SupportedLanguageObject("ar", "Arabic", "العربية"));
			supportedLangs.add(new SupportedLanguageObject("bg", "Bulgarian", "български език"));
			supportedLangs.add(new SupportedLanguageObject("bn", "Bengali", "বাংলা"));
			supportedLangs.add(new SupportedLanguageObject("bs", "Bosnian", "Bosanski"));
			supportedLangs.add(new SupportedLanguageObject("ca", "Catalan", "Català"));
			supportedLangs.add(new SupportedLanguageObject("cnr", "Montenegrin", "Crnogorski"));
			supportedLangs.add(new SupportedLanguageObject("cs", "Czech", "Český Jazyk"));
			supportedLangs.add(new SupportedLanguageObject("cy", "Welsh", "Cymraeg"));
			supportedLangs.add(new SupportedLanguageObject("da", "Danish", "Dansk"));
			supportedLangs.add(new SupportedLanguageObject("de", "German", "Deutsch"));
			supportedLangs.add(new SupportedLanguageObject("el", "Greek", "Νέα Ελληνικά"));
			supportedLangs.add(new SupportedLanguageObject("en", "English", "English"));
			supportedLangs.add(new SupportedLanguageObject("es", "Spanish", "Español"));
			supportedLangs.add(new SupportedLanguageObject("et", "Estonian", "Eesti Keel"));
			supportedLangs.add(new SupportedLanguageObject("eu", "Basque", "Euskara"));
			supportedLangs.add(new SupportedLanguageObject("fi", "Finnish", "Suomen Kieli"));
			supportedLangs.add(new SupportedLanguageObject("fr", "French", "Français"));
			supportedLangs.add(new SupportedLanguageObject("fr-CA", "French (Canada)", "Français Québécois"));
			supportedLangs.add(new SupportedLanguageObject("ga", "Irish", "Gaeilge"));
			supportedLangs.add(new SupportedLanguageObject("gu", "Gujarati", "ગુજરાતી"));
			supportedLangs.add(new SupportedLanguageObject("he", "Hebrew", "עברית"));
			supportedLangs.add(new SupportedLanguageObject("hi", "Hindi", "हिन्दी"));
			supportedLangs.add(new SupportedLanguageObject("hr", "Croatian", "Hrvatski"));
			supportedLangs.add(new SupportedLanguageObject("hu", "Hungarian", "Magyar Nyelv"));
			supportedLangs.add(new SupportedLanguageObject("id", "Indonesian", "Bahasa Indonesia"));
			supportedLangs.add(new SupportedLanguageObject("it", "Italian", "Italiano"));
			supportedLangs.add(new SupportedLanguageObject("ja", "Japanese", "日本語"));
			supportedLangs.add(new SupportedLanguageObject("kn", "Kannada", "ಕನ್ನಡ "));
			supportedLangs.add(new SupportedLanguageObject("ko", "Korean", "한국어"));
			supportedLangs.add(new SupportedLanguageObject("lt", "Lithuanian", "Lietuvių Kalba"));
			supportedLangs.add(new SupportedLanguageObject("lv", "Latvian", "Latviešu Valoda"));
			supportedLangs.add(new SupportedLanguageObject("ml", "Malayalam", "മലയാളം"));
			supportedLangs.add(new SupportedLanguageObject("mr", "Marathi", "मराठी"));
			supportedLangs.add(new SupportedLanguageObject("ms", "Malay", "Bahasa Melayu"));
			supportedLangs.add(new SupportedLanguageObject("mt", "Maltese", "Malti"));
			supportedLangs.add(new SupportedLanguageObject("nb", "Norwegian Bokmal", "Bokmål"));
			supportedLangs.add(new SupportedLanguageObject("ne", "Nepali", "नेपाली भाषा"));
			supportedLangs.add(new SupportedLanguageObject("nl", "Dutch", "Nederlands"));
			supportedLangs.add(new SupportedLanguageObject("pa", "Punjabi", "ਪੰਜਾਬੀ"));
			supportedLangs.add(new SupportedLanguageObject("pl", "Polish", "Język polski"));
			supportedLangs.add(new SupportedLanguageObject("pt", "Portuguese", "Português"));
			supportedLangs.add(new SupportedLanguageObject("ro", "Romanian", "Limba Română"));
			supportedLangs.add(new SupportedLanguageObject("ru", "Russian", "Русский язык"));
			supportedLangs.add(new SupportedLanguageObject("si", "Sinhala", "සිංහල"));
			supportedLangs.add(new SupportedLanguageObject("sk", "Slovakian", "Slovenčina"));
			supportedLangs.add(new SupportedLanguageObject("sl", "Slovenian", "Slovenščina"));
			supportedLangs.add(new SupportedLanguageObject("sr", "Serbian", "Српски"));
			supportedLangs.add(new SupportedLanguageObject("sv", "Swedish", "Svenska"));
			supportedLangs.add(new SupportedLanguageObject("ta", "Tamil", "தமிழ்"));
			supportedLangs.add(new SupportedLanguageObject("te", "Telugu", "తెలుగు"));
			supportedLangs.add(new SupportedLanguageObject("th", "Thai", "ภาษาไทย"));
			supportedLangs.add(new SupportedLanguageObject("tr", "Turkish", "Türkçe"));
			supportedLangs.add(new SupportedLanguageObject("uk", "Ukrainian", "Українська мова"));
			supportedLangs.add(new SupportedLanguageObject("ur", "Urdu", "اُردُو"));
			supportedLangs.add(new SupportedLanguageObject("vi", "Vietnamese", "Tiếng Việt"));
			supportedLangs.add(new SupportedLanguageObject("zh", "Simplified Chinese", "中文"));
			supportedLangs.add(new SupportedLanguageObject("zh-TW", "Traditional Chinese", "古文"));
		} else {
			supportedLangs.add(new SupportedLanguageObject("en", "English"));
			supportedLangs.add(new SupportedLanguageObject("es", "Spanish"));
		}
		main.setSupportedTranslatorLanguages(supportedLangs);
	}
}
