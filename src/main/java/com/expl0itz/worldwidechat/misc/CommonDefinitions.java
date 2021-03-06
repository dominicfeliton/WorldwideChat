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
        "eu",
        "bn",
        "bs",
        "bg",
        "ca",
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
        "it"

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