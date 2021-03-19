package com.expl0itz.worldwidechat.misc;

import java.util.ArrayList;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslateSupportedLanguageObject;
import com.expl0itz.worldwidechat.watson.WatsonSupportedLanguageObject;

public class CommonDefinitions {

    private WorldwideChat main = WorldwideChat.getInstance();
    private ArrayList < WatsonSupportedLanguageObject > supportedWatsonLanguages = main.getSupportedWatsonLanguages();
    private ArrayList < GoogleTranslateSupportedLanguageObject > supportedGoogleTranslateLanguages = main.getSupportedGoogleTranslateLanguages();
    
    /* Important vars */
    private String[] supportedMCVersions = {
        "1.16",
        "1.15",
        "1.14"
    };
    
    private String[] supportedPluginLangCodes = {
        "en",
        "es"
    };

    /* Getters */
    public boolean isSupportedLangForSource(String in, String translator) {
        if (translator.equalsIgnoreCase("Watson")) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in)) 
                    && eaLang.getSupportedAsSource()) {
                    return true;
                }
            }
        } else if (translator.equalsIgnoreCase("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSupportedLangForTarget(String in, String translator) {
        if (translator.equalsIgnoreCase("Watson")) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in)) 
                    && eaLang.getSupportedAsTarget()) {
                    return true;
                }
            }
        } else if (translator.equalsIgnoreCase("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                if ((eaLang.getLangCode().equalsIgnoreCase(in)
                    || eaLang.getLangName().equalsIgnoreCase(in))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isSameLang(String first, String second, String translator) {
        if (translator.equalsIgnoreCase("Watson") ) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                if ((eaLang.getLangName().equals(getSupportedWatsonLang(first).getLangName()) 
                        && eaLang.getLangName().equals(getSupportedWatsonLang(second).getLangName()))) {
                        return true;
                    }
            }
        } else if (translator.equalsIgnoreCase("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                if ((eaLang.getLangName().equals(getSupportedGoogleTranslateLang(first).getLangName()) 
                    && eaLang.getLangName().equals(getSupportedGoogleTranslateLang(second).getLangName()))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public WatsonSupportedLanguageObject getSupportedWatsonLang(String in) {
        for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in))) {
                return eaLang;
            }
        }
        return null;
    }
    
    public GoogleTranslateSupportedLanguageObject getSupportedGoogleTranslateLang(String in) {
        for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in))) {
                return eaLang;
            }
        }
        return null;
    }
    
    public String getValidLangCodes() {
        String out = "\n";
        if (main.getTranslatorName().equals("Watson")) {
            for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
                out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), "; 
            }
        }
        else if (main.getTranslatorName().equals("Google Translate")) {
            for (GoogleTranslateSupportedLanguageObject eaLang : supportedGoogleTranslateLanguages) {
                out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), ";  
            }
        }
        if (out.indexOf(",") != -1) {
            out = out.substring(0, out.lastIndexOf(","));
        }
        return out;
    }
    
    public String[] getSupportedPluginLangCodes() {
        return supportedPluginLangCodes;
    }

    public String[] getSupportedMCVersions() {
        return supportedMCVersions;
    }
}