package com.expl0itz.worldwidechat.misc;

import java.util.ArrayList;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.watson.WatsonSupportedLanguageObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.Languages;

public class CommonDefinitions {

    private WorldwideChat main = WorldwideChat.getInstance();
    private ArrayList<WatsonSupportedLanguageObject> supportedWatsonLanguages = main.getSupportedWatsonLanguages();
    
    /* Important vars */
    private String[] supportedMCVersions = {
        "1.16"
    };
    
    private String[] supportedPluginLangCodes = {
        "en"
    };

    /* Getters */
    public boolean isSupportedWatsonLangForSource(String in) {
        for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in)) 
                && eaLang.getSupportedAsSource()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSupportedWatsonLangForTarget(String in) {
        for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in))
                && eaLang.getSupportedAsTarget()) { //Add native language support?
                return true;
            }
        }
        return false;
    }
    
    public WatsonSupportedLanguageObject getSupportedWatsonLang(String in) {
        for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
            if ((eaLang.getLangCode().equalsIgnoreCase(in)
                || eaLang.getLangName().equalsIgnoreCase(in))
                && eaLang.getSupportedAsTarget()) { //Add native language support?
                return eaLang;
            }
        }
        return null;
    }
    
    public String getValidLangCodes() {
        String out = "\n";
        for (WatsonSupportedLanguageObject eaLang : supportedWatsonLanguages) {
            out += "(" + eaLang.getLangCode() + " - " + ((main.getPluginLang().equalsIgnoreCase("en") ? eaLang.getLangName() : eaLang.getNativeLangName()) + "), "); 
        }
        out = out.substring(0, out.lastIndexOf(","));
        return out;
    }
    
    public String[] getSupportedPluginLangCodes() {
        return supportedPluginLangCodes;
    }

    public String[] getSupportedMCVersions() {
        return supportedMCVersions;
    }
}