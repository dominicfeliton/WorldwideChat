package com.dominicfeliton.worldwidechat.util;

import java.util.Comparator;
import java.util.Objects;

public class SupportedLang implements Comparable<SupportedLang> {
    private String langCode = "";
    private String langName = "";
    private String nativeLangName = "";

    // TODO: Fix redundant adding of SupportedLangs in each translator
    public SupportedLang(String langCode, String langName, String nativeLangName) {
        this.langCode = langCode;
        setLangName(langName);
        setNativeLangName(nativeLangName);
    }

    public SupportedLang(String langCode, String langName) {
        this.langCode = langCode;
        setLangName(langName);
        setNativeLangName("");
    }

    public SupportedLang(String langCode) {
        this.langCode = langCode;
        setLangName("");
        setNativeLangName("");
    }

    /* Getters */
    public String getLangCode() {
        return langCode;
    }

    public String getLangName() {
        return langName;
    }

    public String getNativeLangName() {
        return nativeLangName;
    }

    /* Setters */
    public void setLangCode(String i) {
        langCode = i;
    }

    public void setLangName(String i) {
        // Remove blank space for commands
        langName = i.replaceAll("\\s+", "-");
        ;
    }

    public void setNativeLangName(String i) {
        // Remove blank space for commands
        nativeLangName = i.replaceAll("\\s+", "-");
        ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SupportedLang other = (SupportedLang) obj;
        return Objects.equals(langCode, other.langCode) &&
                Objects.equals(langName, other.langName) &&
                Objects.equals(nativeLangName, other.nativeLangName);
    }

    @Override
    public int compareTo(SupportedLang other) {
        int langCodeComparison = Objects.compare(langCode, other.langCode, Comparator.nullsFirst(String::compareTo));
        if (langCodeComparison != 0) {
            return langCodeComparison;
        }
        int langNameComparison = Objects.compare(langName, other.langName, Comparator.nullsFirst(String::compareTo));
        if (langNameComparison != 0) {
            return langNameComparison;
        }
        return Objects.compare(nativeLangName, other.nativeLangName, Comparator.nullsFirst(String::compareTo));
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", langCode, langName, nativeLangName);
    }
}
