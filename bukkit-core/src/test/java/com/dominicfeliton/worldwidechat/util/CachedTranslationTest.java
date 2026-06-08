package com.dominicfeliton.worldwidechat.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CachedTranslationTest {

    @Test
    void equalityAndHashCodeUseLanguagesAndInputPhrase() {
        CachedTranslation first = new CachedTranslation("en", "es", "Hello");
        CachedTranslation second = new CachedTranslation("en", "es", "Hello");
        CachedTranslation differentPhrase = new CachedTranslation("en", "es", "Goodbye");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, differentPhrase);
    }

    @Test
    void comparableSortsByInputLangThenOutputLangThenPhrase() {
        List<CachedTranslation> translations = new ArrayList<>(List.of(
                new CachedTranslation("fr", "en", "Bonjour"),
                new CachedTranslation("en", "fr", "Hello"),
                new CachedTranslation("en", "es", "Hello"),
                new CachedTranslation("en", "es", "Apple")
        ));

        translations.sort(null);

        assertEquals("Apple", translations.get(0).getInputPhrase());
        assertEquals("es", translations.get(0).getOutputLang());
        assertEquals("Hello", translations.get(1).getInputPhrase());
        assertEquals("fr", translations.get(2).getOutputLang());
        assertEquals("fr", translations.get(3).getInputLang());
    }

    @Test
    void savedFlagCanBeUpdated() {
        CachedTranslation translation = new CachedTranslation("en", "es", "Hello");

        assertFalse(translation.hasBeenSaved());
        translation.setHasBeenSaved(true);

        assertTrue(translation.hasBeenSaved());
    }
}
