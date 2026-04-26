package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationTest extends WWCIntegrationTest {

    @Test
    void startsWithJUnitTranslatorAndSupportedLanguages() {
        assertEquals("JUnit/MockBukkit Testing Translator", plugin().getTranslatorName());

        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        assertTrue(refs.isSupportedLang("en", CommonRefs.LangType.INPUT));
        assertTrue(refs.isSupportedLang("es", CommonRefs.LangType.OUTPUT));
        assertTrue(refs.isSupportedLang("fr", CommonRefs.LangType.OUTPUT));
    }

    @Test
    void translatesKnownSourceTargetPhrase() {
        PlayerMock player = WWCTestSupport.addOpPlayer("Translator");
        player.performCommand("wwct en es");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("Hello, how are you?", player);

        assertEquals("Hola, como estas?", translated);
    }

    @Test
    void translatesKnownAutoTargetPhrase() {
        PlayerMock player = WWCTestSupport.addOpPlayer("AutoTranslator");
        player.performCommand("wwct es");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("How many diamonds do you have?", player);

        assertEquals("Cuantos diamantes tienes?", translated);
    }

    @Test
    void translationWithoutSessionReturnsOriginalText() {
        PlayerMock player = WWCTestSupport.addOpPlayer("NoSession");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("This phrase should not change", player);

        assertEquals("This phrase should not change", translated);
    }

    @Test
    void cachesSuccessfulTranslationsAndReusesResult() {
        PlayerMock player = WWCTestSupport.addOpPlayer("CacheUser");
        player.performCommand("wwct en es");

        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        String first = refs.translateText("Hello, how are you?", player);
        String second = refs.translateText("Hello, how are you?", player);

        assertEquals(first, second);
        assertTrue(plugin().getEstimatedCacheSize() > 0);
        assertEquals("Hola, como estas?",
                plugin().getCacheTerm(new CachedTranslation("en", "es", "Hello, how are you?")));
    }

    @Test
    void cacheEvictsEntriesOverConfiguredLimit() {
        plugin().getConfigManager().getMainConfig().set("Translator.translatorCacheSize", 2);
        plugin().setCacheProperties(2);

        CachedTranslation first = new CachedTranslation("en", "es", "first");
        CachedTranslation second = new CachedTranslation("en", "es", "second");
        CachedTranslation third = new CachedTranslation("en", "es", "third");

        plugin().addCacheTerm(first, "uno");
        plugin().addCacheTerm(second, "dos");
        plugin().addCacheTerm(third, "tres");

        WWCTestSupport.waitForCondition(() -> plugin().getEstimatedCacheSize() <= 2);

        List<CachedTranslation> keys = List.of(first, second, third);
        long retainedEntries = keys.stream()
                .filter(plugin()::hasCacheTerm)
                .count();

        assertEquals(2, plugin().getEstimatedCacheSize());
        assertEquals(2, retainedEntries);
    }
}
