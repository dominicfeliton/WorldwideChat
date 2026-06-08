package com.dominicfeliton.worldwidechat.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRecordTest {

    @Test
    void initializesWithStoredStats() {
        PlayerRecord record = new PlayerRecord("None", "player-uuid", 3, 2);

        assertEquals("player-uuid", record.getUUID());
        assertEquals("None", record.getLastTranslationTime());
        assertEquals(3, record.getAttemptedTranslations());
        assertEquals(2, record.getSuccessfulTranslations());
        assertEquals("", record.getLocalizationCode());
        assertFalse(record.getHasBeenSaved());
    }

    @Test
    void mutatingPersistedFieldsMarksRecordUnsaved() {
        PlayerRecord record = new PlayerRecord("None", "player-uuid", 0, 0);

        record.setHasBeenSaved(true);
        record.setAttemptedTranslations(4);
        assertFalse(record.getHasBeenSaved());
        assertEquals(4, record.getAttemptedTranslations());

        record.setHasBeenSaved(true);
        record.setSuccessfulTranslations(3);
        assertFalse(record.getHasBeenSaved());
        assertEquals(3, record.getSuccessfulTranslations());

        record.setHasBeenSaved(true);
        record.setLocalizationCode("es");
        assertFalse(record.getHasBeenSaved());
        assertEquals("es", record.getLocalizationCode());
    }

    @Test
    void atomicIncrementMethodsMarkRecordUnsaved() {
        PlayerRecord record = new PlayerRecord("None", "player-uuid", 0, 0);

        record.setHasBeenSaved(true);
        assertEquals(1, record.incrementAttemptedTranslations());
        assertFalse(record.getHasBeenSaved());
        assertEquals(1, record.getAttemptedTranslations());

        record.setHasBeenSaved(true);
        assertEquals(1, record.incrementSuccessfulTranslations());
        assertFalse(record.getHasBeenSaved());
        assertEquals(1, record.getSuccessfulTranslations());
    }

    @Test
    void settingLastTranslationTimeReplacesPlaceholder() {
        PlayerRecord record = new PlayerRecord("None", "player-uuid", 0, 0);

        record.setHasBeenSaved(true);
        record.setLastTranslationTime();

        assertFalse(record.getHasBeenSaved());
        assertNotEquals("None", record.getLastTranslationTime());
        assertFalse(record.getLastTranslationTime().isEmpty());
    }
}
