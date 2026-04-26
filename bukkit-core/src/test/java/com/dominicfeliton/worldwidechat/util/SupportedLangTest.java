package com.dominicfeliton.worldwidechat.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupportedLangTest {

    @Test
    void normalizesWhitespaceInDisplayNames() {
        SupportedLang lang = new SupportedLang("pt-BR", "Brazilian Portuguese", "Portugues Brasileiro");

        assertEquals("pt-BR", lang.getLangCode());
        assertEquals("Brazilian-Portuguese", lang.getLangName());
        assertEquals("Portugues-Brasileiro", lang.getNativeLangName());
    }

    @Test
    void equalityAndHashCodeUseCodeAndNames() {
        SupportedLang first = new SupportedLang("es", "Spanish", "Espanol");
        SupportedLang second = new SupportedLang("es", "Spanish", "Espanol");
        SupportedLang differentNativeName = new SupportedLang("es", "Spanish", "");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, differentNativeName);
    }

    @Test
    void comparableSortsByCodeThenNameThenNativeName() {
        List<SupportedLang> langs = new ArrayList<>(List.of(
                new SupportedLang("fr", "French", "Francais"),
                new SupportedLang("en", "English", "English"),
                new SupportedLang("en", "American English", "English")
        ));

        langs.sort(null);

        assertEquals("American-English", langs.get(0).getLangName());
        assertEquals("English", langs.get(1).getLangName());
        assertEquals("fr", langs.get(2).getLangCode());
    }
}
