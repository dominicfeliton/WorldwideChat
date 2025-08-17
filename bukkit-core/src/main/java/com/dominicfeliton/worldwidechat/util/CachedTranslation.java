package com.dominicfeliton.worldwidechat.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CachedTranslation implements Comparable<CachedTranslation> {

    private final AtomicReference<String> inputLang = new AtomicReference<>();
    private final AtomicReference<String> outputLang = new AtomicReference<>();
    private final AtomicReference<String> inputPhrase = new AtomicReference<>();
    private final AtomicBoolean hasBeenSaved = new AtomicBoolean(false);

    public CachedTranslation(String inputLang, String outputLang, String inputPhrase) {
        this.inputLang.set(inputLang);
        this.outputLang.set(outputLang);
        this.inputPhrase.set(inputPhrase);
        this.hasBeenSaved.set(false);
    }

    public String getInputLang() {
        return inputLang.get();
    }

    public String getOutputLang() {
        return outputLang.get();
    }

    public String getInputPhrase() {
        return inputPhrase.get();
    }

    public boolean hasBeenSaved() {
        return hasBeenSaved.get();
    }

    public void setInputLang(String i) {
        inputLang.set(i);
    }

    public void setOutputLang(String i) {
        outputLang.set(i);
    }

    public void setInputPhrase(String i) {
        inputPhrase.set(i);
    }

    public void setHasBeenSaved(boolean b) {
        hasBeenSaved.set(b);
    }

    @Override
    public int compareTo(CachedTranslation o) {
        String a1 = inputLang.get();
        String b1 = o.getInputLang();
        int c1 = a1.compareTo(b1);
        if (c1 != 0) return c1;

        String a2 = outputLang.get();
        String b2 = o.getOutputLang();
        int c2 = a2.compareTo(b2);
        if (c2 != 0) return c2;

        String a3 = inputPhrase.get();
        String b3 = o.getInputPhrase();
        return a3.compareTo(b3);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CachedTranslation other = (CachedTranslation) obj;
        return Objects.equals(inputLang.get(), other.inputLang.get()) &&
                Objects.equals(outputLang.get(), other.outputLang.get()) &&
                Objects.equals(inputPhrase.get(), other.inputPhrase.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputLang.get(), outputLang.get(), inputPhrase.get());
    }
}