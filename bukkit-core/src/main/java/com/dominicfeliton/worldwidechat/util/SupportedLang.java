package com.dominicfeliton.worldwidechat.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class SupportedLang implements Comparable<SupportedLang> {

    private final AtomicReference<String> langCode = new AtomicReference<>("");
    private final AtomicReference<String> langName = new AtomicReference<>("");
    private final AtomicReference<String> nativeLangName = new AtomicReference<>("");

    public SupportedLang(String langCode, String langName, String nativeLangName) {
        this.langCode.set(langCode);
        setLangName(langName);
        setNativeLangName(nativeLangName);
    }

    public SupportedLang(String langCode, String langName) {
        this.langCode.set(langCode);
        setLangName(langName);
        setNativeLangName("");
    }

    public SupportedLang(String langCode) {
        this.langCode.set(langCode);
        setLangName("");
        setNativeLangName("");
    }

    public String getLangCode() {
        return langCode.get();
    }

    public String getLangName() {
        return langName.get();
    }

    public String getNativeLangName() {
        return nativeLangName.get();
    }

    public void setLangCode(String i) {
        langCode.set(i);
    }

    public void setLangName(String i) {
        langName.set(i.replaceAll("\\s+", "-"));
    }

    public void setNativeLangName(String i) {
        nativeLangName.set(i.replaceAll("\\s+", "-"));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SupportedLang other = (SupportedLang) obj;
        return Objects.equals(langCode.get(), other.langCode.get()) &&
                Objects.equals(langName.get(), other.langName.get()) &&
                Objects.equals(nativeLangName.get(), other.nativeLangName.get());
    }

    @Override
    public int compareTo(SupportedLang other) {
        int c1 = Objects.compare(langCode.get(), other.langCode.get(), Comparator.nullsFirst(String::compareTo));
        if (c1 != 0) return c1;
        int c2 = Objects.compare(langName.get(), other.langName.get(), Comparator.nullsFirst(String::compareTo));
        if (c2 != 0) return c2;
        return Objects.compare(nativeLangName.get(), other.nativeLangName.get(), Comparator.nullsFirst(String::compareTo));
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", langCode.get(), langName.get(), nativeLangName.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(langCode.get(), langName.get(), nativeLangName.get());
    }
}