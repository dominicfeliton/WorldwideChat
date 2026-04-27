package com.dominicfeliton.worldwidechat.input;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputMethodResolverTest {
    @Test
    void autoUsesPaperDialogsOnPaper1217Plus() {
        assertEquals(InputMethod.PAPER_DIALOG, InputMethodResolver.resolve(
                "auto", "Paper", new ComparableVersion("1.21.7"), true, true));
    }

    @Test
    void autoUsesPaperDialogsOnPaper261() {
        assertEquals(InputMethod.PAPER_DIALOG, InputMethodResolver.resolve(
                "auto", "Paper", new ComparableVersion("26.1"), true, true));
    }

    @Test
    void autoUsesConversationsOnOlderPaper() {
        assertEquals(InputMethod.CONVERSATION, InputMethodResolver.resolve(
                "auto", "Paper", new ComparableVersion("1.21.6"), true, true));
    }

    @Test
    void autoUsesConversationsOnSpigot() {
        assertEquals(InputMethod.CONVERSATION, InputMethodResolver.resolve(
                "auto", "Spigot", new ComparableVersion("1.21.11"), false, true));
    }

    @Test
    void forcedConversationOnPaperStaysConversation() {
        assertEquals(InputMethod.CONVERSATION, InputMethodResolver.resolve(
                "conversation", "Paper", new ComparableVersion("26.1"), true, true));
    }

    @Test
    void forcedUnavailableBackendFallsBackToAuto() {
        assertEquals(InputMethod.CONVERSATION, InputMethodResolver.resolve(
                "paper-dialog", "Spigot", new ComparableVersion("1.21.11"), false, true));
    }

    @Test
    void forcedPaperDialogUnavailableFallsBackToAutoBackend() {
        assertEquals(InputMethod.CONVERSATION, InputMethodResolver.resolve(
                "paper-dialog", "Paper", new ComparableVersion("26.1"), false, true));
    }

    @Test
    void invalidConfigNormalizesToAuto() {
        assertEquals(InputMethod.PAPER_DIALOG, InputMethodResolver.resolve(
                "not-real", "Paper", new ComparableVersion("26.1"), true, true));
    }

    @Test
    void blankConfigNormalizesToAuto() {
        assertEquals(InputMethod.CONVERSATION, InputMethodResolver.resolve(
                " ", "Spigot", new ComparableVersion("1.21.11"), false, true));
    }

    @Test
    void foliaWithoutDialogsHasNoInputBackend() {
        assertEquals(InputMethod.NONE, InputMethodResolver.resolve(
                "auto", "Folia", new ComparableVersion("1.21.6"), false, false));
    }
}
