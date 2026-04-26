package com.dominicfeliton.worldwidechat.util;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ActiveTranslatorTest {

    @Test
    void defaultsRepresentNewTranslationSession() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");

        assertEquals("player-uuid", translator.getUUID());
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());
        assertEquals(0, translator.getRateLimit());
        assertEquals("None", translator.getRateLimitPreviousTime());
        assertTrue(translator.getTranslatingChatOutgoing());
        assertFalse(translator.getTranslatingChatIncoming());
        assertFalse(translator.getTranslatingBook());
        assertFalse(translator.getTranslatingSign());
        assertFalse(translator.getTranslatingItem());
        assertFalse(translator.getTranslatingEntity());
        assertFalse(translator.getHasBeenSaved());
    }

    @Test
    void mutatingPersistedFieldsMarksTranslatorUnsaved() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");

        translator.setHasBeenSaved(true);
        translator.setRateLimit(10);
        assertFalse(translator.getHasBeenSaved());
        assertEquals(10, translator.getRateLimit());

        translator.setHasBeenSaved(true);
        translator.setTranslatingBook(true);
        assertFalse(translator.getHasBeenSaved());
        assertTrue(translator.getTranslatingBook());

        translator.setHasBeenSaved(true);
        Instant previousTime = Instant.parse("2026-01-02T03:04:05Z");
        translator.setRateLimitPreviousTime(previousTime);
        assertFalse(translator.getHasBeenSaved());
        assertEquals(previousTime.toString(), translator.getRateLimitPreviousTime());
    }
}
