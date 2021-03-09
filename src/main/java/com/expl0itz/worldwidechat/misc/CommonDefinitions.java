package com.expl0itz.worldwidechat.misc;

public class CommonDefinitions {

    /* Important vars */
    private String[] supportedMCVersions = {
        "1.16"
    };
    
    private String[] supportedPluginLangCodes = {
        "en"
    };

    private String[] supportedWatsonLangCodes = {
            //eu, ca are not completely translatable by watson
            //TODO: Fetch all supported langs from Watson, get translatable languages in json, check if they can be used as input/output
        "en",
        "es",
        "ar",
        "de",
        "fr",
        "it",
        "ja",
        "ko",
        "pt-br",
        "zh",
        "zh-tw",
        "bn",
        "bs",
        "bg",
        "hr",
        "cs",
        "da",
        "nl",
        "et",
        "fi",
        "fr-CA",
        "de",
        "el",
        "gu",
        "he",
        "hi",
        "hu",
        "ga",
        "id",
        "it",
        "ja",
        "ko",
        "lv",
        "lt",
        "ms",
        "ml",
        "mt",
        "cnr",
        "ne",
        "nb",
        "pl",
        "pt",
        "ro",
        "ru",
        "sr",
        "si",
        "sk",
        "sl",
        "sv",
        "ta",
        "te",
        "th",
        "tr",
        "uk",
        "ur",
        "vi",
        "cy"

    };

    /* Getters */
    public String[] getSupportedWatsonLangCodes() {
        return supportedWatsonLangCodes;
    }

    public String[] getSupportedPluginLangCodes() {
        return supportedPluginLangCodes;
    }

    public String[] getSupportedMCVersions() {
        return supportedMCVersions;
    }
}