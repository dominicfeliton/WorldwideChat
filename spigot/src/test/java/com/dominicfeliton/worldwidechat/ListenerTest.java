package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.SpigotChatListener;
import com.dominicfeliton.worldwidechat.listeners.SpigotPlayerLocaleListener;
import com.dominicfeliton.worldwidechat.listeners.SpigotSignListener;
import com.dominicfeliton.worldwidechat.listeners.TranslateInGameListener;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ListenerTest extends WWCIntegrationTest {

    @Test
    void outgoingChatTranslationMutatesAsyncChatMessage() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("Speaker");
        PlayerMock recipient = WWCTestSupport.addOpPlayer("Listener");
        sender.performCommand("wwct en es");

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                "Hello, how are you?", new HashSet<>(Set.of(recipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
    }

    @Test
    void incomingChatTranslationRemovesTranslatedRecipientFromOriginalEvent() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("Speaker");
        PlayerMock translatedRecipient = WWCTestSupport.addOpPlayer("TranslatedRecipient");
        PlayerMock untouchedRecipient = WWCTestSupport.addOpPlayer("UntouchedRecipient");
        sender.performCommand("wwct en fr");
        sender.performCommand("wwctco");
        translatedRecipient.performCommand("wwct en es");
        translatedRecipient.performCommand("wwctci");

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                "Hello, how are you?", new HashSet<>(Set.of(translatedRecipient, untouchedRecipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertFalse(event.getRecipients().contains(translatedRecipient));
        assertTrue(event.getRecipients().contains(untouchedRecipient));
    }

    @Test
    void multiPlayerChatTranslationRoutesOutgoingAndIncomingByTranslatorConfig() {
        PlayerMock speaker = WWCTestSupport.addOpPlayer("Speaker");
        PlayerMock sameConfigListener = WWCTestSupport.addOpPlayer("SameConfigListener");
        PlayerMock englishListener = WWCTestSupport.addOpPlayer("EnglishListener");
        PlayerMock frenchListener = WWCTestSupport.addOpPlayer("FrenchListener");
        PlayerMock autoFrenchListener = WWCTestSupport.addOpPlayer("AutoFrenchListener");
        PlayerMock untranslatedListener = WWCTestSupport.addOpPlayer("UntranslatedListener");
        PlayerMock incomingDisabledListener = WWCTestSupport.addOpPlayer("IncomingDisabledListener");

        speaker.performCommand("wwct en es");
        sameConfigListener.performCommand("wwct en es");
        sameConfigListener.performCommand("wwctci");
        englishListener.performCommand("wwct es en");
        englishListener.performCommand("wwctci");
        frenchListener.performCommand("wwct es fr");
        frenchListener.performCommand("wwctci");
        autoFrenchListener.performCommand("wwct fr");
        autoFrenchListener.performCommand("wwctci");
        incomingDisabledListener.performCommand("wwct es en");

        WWCTestSupport.addCacheTerm("es", "en", "Hola, como estas?", "English cached translation");
        WWCTestSupport.addCacheTerm("es", "fr", "Hola, como estas?", "French cached translation");
        WWCTestSupport.addCacheTerm("None", "fr", "Hola, como estas?", "Auto French cached translation");

        drainPlayerMessages(speaker);
        drainPlayerMessages(sameConfigListener);
        drainPlayerMessages(englishListener);
        drainPlayerMessages(frenchListener);
        drainPlayerMessages(autoFrenchListener);
        drainPlayerMessages(untranslatedListener);
        drainPlayerMessages(incomingDisabledListener);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, speaker,
                "Hello, how are you?", new HashSet<>(Set.of(
                sameConfigListener, englishListener, frenchListener, autoFrenchListener,
                untranslatedListener, incomingDisabledListener)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
        assertTrue(event.getRecipients().contains(sameConfigListener));
        assertTrue(event.getRecipients().contains(untranslatedListener));
        assertTrue(event.getRecipients().contains(incomingDisabledListener));
        assertFalse(event.getRecipients().contains(englishListener));
        assertFalse(event.getRecipients().contains(frenchListener));
        assertFalse(event.getRecipients().contains(autoFrenchListener));
        assertTrue(drainPlayerMessages(sameConfigListener).isEmpty());
        assertTrue(drainPlayerMessages(untranslatedListener).isEmpty());
        assertTrue(drainPlayerMessages(incomingDisabledListener).isEmpty());
        assertTrue(drainPlayerMessages(englishListener).stream()
                .anyMatch(message -> message.contains("English cached translation")));
        assertTrue(drainPlayerMessages(frenchListener).stream()
                .anyMatch(message -> message.contains("French cached translation")));
        assertTrue(drainPlayerMessages(autoFrenchListener).stream()
                .anyMatch(message -> message.contains("Auto French cached translation")));
    }

    @Test
    void bidirectionalTranslatorsRespectOutgoingAndIncomingPairDifferences() {
        PlayerMock outgoingOnlySpeaker = WWCTestSupport.addOpPlayer("OutgoingOnlySpeaker");
        PlayerMock samePairBidirectional = WWCTestSupport.addOpPlayer("SamePairBidirectional");
        PlayerMock differentPairBidirectional = WWCTestSupport.addOpPlayer("DifferentPairBidirectional");

        outgoingOnlySpeaker.performCommand("wwct en es");
        samePairBidirectional.performCommand("wwct en es");
        samePairBidirectional.performCommand("wwctci");
        differentPairBidirectional.performCommand("wwct es fr");
        differentPairBidirectional.performCommand("wwctci");

        ActiveTranslator outgoingOnly = plugin().getActiveTranslator(outgoingOnlySpeaker);
        ActiveTranslator samePair = plugin().getActiveTranslator(samePairBidirectional);
        ActiveTranslator differentPair = plugin().getActiveTranslator(differentPairBidirectional);
        assertTrue(outgoingOnly.getTranslatingChatOutgoing());
        assertFalse(outgoingOnly.getTranslatingChatIncoming());
        assertTrue(samePair.getTranslatingChatOutgoing());
        assertTrue(samePair.getTranslatingChatIncoming());
        assertEquals("en", samePair.getInLangCode());
        assertEquals("es", samePair.getOutLangCode());
        assertTrue(differentPair.getTranslatingChatOutgoing());
        assertTrue(differentPair.getTranslatingChatIncoming());
        assertEquals("es", differentPair.getInLangCode());
        assertEquals("fr", differentPair.getOutLangCode());

        WWCTestSupport.addCacheTerm("es", "fr", "Hola, como estas?", "Different pair incoming translation");
        drainPlayerMessages(outgoingOnlySpeaker);
        drainPlayerMessages(samePairBidirectional);
        drainPlayerMessages(differentPairBidirectional);

        AsyncPlayerChatEvent outgoingOnlyEvent = new AsyncPlayerChatEvent(true, outgoingOnlySpeaker,
                "Hello, how are you?", new HashSet<>(Set.of(samePairBidirectional, differentPairBidirectional)));

        new SpigotChatListener().onPlayerChat(outgoingOnlyEvent);

        assertEquals("Hola, como estas?", outgoingOnlyEvent.getMessage());
        assertFalse(outgoingOnlyEvent.isCancelled());
        assertTrue(outgoingOnlyEvent.getRecipients().contains(samePairBidirectional));
        assertFalse(outgoingOnlyEvent.getRecipients().contains(differentPairBidirectional));
        assertTrue(drainPlayerMessages(samePairBidirectional).isEmpty());
        assertTrue(drainPlayerMessages(differentPairBidirectional).stream()
                .anyMatch(message -> message.contains("Different pair incoming translation")));

        AsyncPlayerChatEvent bidirectionalSpeakerEvent = new AsyncPlayerChatEvent(true, samePairBidirectional,
                "Hello, how are you?", new HashSet<>(Set.of(outgoingOnlySpeaker, differentPairBidirectional)));

        new SpigotChatListener().onPlayerChat(bidirectionalSpeakerEvent);

        assertEquals("Hola, como estas?", bidirectionalSpeakerEvent.getMessage());
        assertFalse(bidirectionalSpeakerEvent.isCancelled());
        assertTrue(bidirectionalSpeakerEvent.getRecipients().contains(outgoingOnlySpeaker));
        assertFalse(bidirectionalSpeakerEvent.getRecipients().contains(differentPairBidirectional));
        assertTrue(drainPlayerMessages(outgoingOnlySpeaker).isEmpty());
        assertTrue(drainPlayerMessages(differentPairBidirectional).stream()
                .anyMatch(message -> message.contains("Different pair incoming translation")));
    }

    @Test
    void globalOutgoingChatTranslationMutatesMessageForNonTranslatorSpeaker() {
        PlayerMock admin = WWCTestSupport.addOpPlayer("GlobalAdmin");
        PlayerMock speaker = WWCTestSupport.addOpPlayer("GlobalSpeaker");
        PlayerMock recipient = WWCTestSupport.addOpPlayer("GlobalRecipient");
        admin.performCommand("wwcg en es");
        drainPlayerMessages(admin);
        drainPlayerMessages(speaker);
        drainPlayerMessages(recipient);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, speaker,
                "Hello, how are you?", new HashSet<>(Set.of(recipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
        assertTrue(event.getRecipients().contains(recipient));
        assertTrue(drainPlayerMessages(recipient).isEmpty());
    }

    @Test
    void globalOutgoingChatFeedsIncomingRecipientTranslations() {
        PlayerMock admin = WWCTestSupport.addOpPlayer("GlobalAdmin");
        PlayerMock speaker = WWCTestSupport.addOpPlayer("GlobalSpeaker");
        PlayerMock frenchListener = WWCTestSupport.addOpPlayer("GlobalFrenchListener");
        PlayerMock untranslatedListener = WWCTestSupport.addOpPlayer("GlobalUntranslatedListener");
        admin.performCommand("wwcg en es");
        frenchListener.performCommand("wwct es fr");
        frenchListener.performCommand("wwctci");
        WWCTestSupport.addCacheTerm("es", "fr", "Hola, como estas?", "Global French cached translation");
        drainPlayerMessages(admin);
        drainPlayerMessages(speaker);
        drainPlayerMessages(frenchListener);
        drainPlayerMessages(untranslatedListener);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, speaker,
                "Hello, how are you?", new HashSet<>(Set.of(frenchListener, untranslatedListener)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
        assertFalse(event.getRecipients().contains(frenchListener));
        assertTrue(event.getRecipients().contains(untranslatedListener));
        assertTrue(drainPlayerMessages(frenchListener).stream()
                .anyMatch(message -> message.contains("Global French cached translation")));
        assertTrue(drainPlayerMessages(untranslatedListener).isEmpty());
    }

    @Test
    void incomingGuidelinesAIBlockRunsOnceAndLeavesOriginalRecipients() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureChatGPTGuidelinesChecks(stub);
            PlayerMock sender = WWCTestSupport.addOpPlayer("GuidelinesSpeaker");
            PlayerMock firstRecipient = WWCTestSupport.addOpPlayer("GuidelinesListenerOne");
            PlayerMock secondRecipient = WWCTestSupport.addOpPlayer("GuidelinesListenerTwo");
            sender.performCommand("wwct en fr");
            sender.performCommand("wwctco");
            firstRecipient.performCommand("wwct en es");
            firstRecipient.performCommand("wwctci");
            secondRecipient.performCommand("wwct en es");
            secondRecipient.performCommand("wwctci");
            drainPlayerMessages(sender);

            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                    "Hello, how are you?", new HashSet<>(Set.of(firstRecipient, secondRecipient)));

            new SpigotChatListener().onPlayerChat(event);

            assertEquals("Hello, how are you?", event.getMessage());
            assertFalse(event.isCancelled());
            assertTrue(event.getRecipients().contains(firstRecipient));
            assertTrue(event.getRecipients().contains(secondRecipient));
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
            assertEquals(1, drainPlayerMessages(sender).stream()
                    .filter(message -> message.contains("translation guidelines"))
                    .count());
        }
    }

    @Test
    void incomingCachedTranslationsSkipGuidelinesAIBlock() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureChatGPTGuidelinesChecks(stub);
            PlayerMock sender = WWCTestSupport.addOpPlayer("CachedGuidelinesSpeaker");
            PlayerMock firstRecipient = WWCTestSupport.addOpPlayer("CachedGuidelinesListenerOne");
            PlayerMock secondRecipient = WWCTestSupport.addOpPlayer("CachedGuidelinesListenerTwo");
            sender.performCommand("wwct en fr");
            sender.performCommand("wwctco");
            firstRecipient.performCommand("wwct en es");
            firstRecipient.performCommand("wwctci");
            secondRecipient.performCommand("wwct en es");
            secondRecipient.performCommand("wwctci");
            plugin().addCacheTerm(new CachedTranslation("en", "es", "Hello, how are you?"), "Cached translation");

            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                    "Hello, how are you?", new HashSet<>(Set.of(firstRecipient, secondRecipient)));

            new SpigotChatListener().onPlayerChat(event);

            assertEquals(0, stub.requestCount());
            assertFalse(event.getRecipients().contains(firstRecipient));
            assertFalse(event.getRecipients().contains(secondRecipient));
        }
    }

    @Test
    void forcedSeparateChannelCancelsTranslatedOriginalChatEvent() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("ForcedSpeaker");
        PlayerMock recipient = WWCTestSupport.addOpPlayer("ForcedListener");
        plugin().setForceSeparateChatChannel(true);
        sender.performCommand("wwct en es");

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                "Hello, how are you?", new HashSet<>(Set.of(recipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertTrue(event.isCancelled());
        assertEquals("Hello, how are you?", event.getMessage());
    }

    @Test
    void signEditIsCancelledAndWarningRecordedWhenSignTranslationEnabled() {
        PlayerMock player = WWCTestSupport.addOpPlayer("SignEditor");
        player.performCommand("wwct en es");
        player.performCommand("wwcts");
        ActiveTranslator translator = plugin().getActiveTranslator(player);

        PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, null, Side.FRONT,
                PlayerSignOpenEvent.Cause.INTERACT);

        new SpigotSignListener().onSignEdit(event);

        assertTrue(event.isCancelled());
        assertTrue(translator.getSignWarning());
    }

    @Test
    void bookObjectTranslationBatchesTitleAndPagesThroughOneRateLimitWindow() {
        PlayerMock player = WWCTestSupport.addOpPlayer("BookBatch");
        player.performCommand("wwct en es");
        player.performCommand("wwctb");
        player.performCommand("wwctrl 5");
        drainPlayerMessages(player);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("JUnit");
        meta.setTitle("batch-book-title");
        meta.setPages(List.of("batch-book-page-0", "batch-book-page-1"));
        book.setItemMeta(meta);

        new TranslateInGameListener().onInGameObjTranslateRequest(interactWith(player, book));
        WWCTestSupport.drainScheduler();

        assertEquals("translated-batch-book-title", plugin().getCacheTerm(new CachedTranslation("en", "es", "batch-book-title")));
        assertEquals("translated-batch-book-page-0", plugin().getCacheTerm(new CachedTranslation("en", "es", "batch-book-page-0")));
        assertEquals("translated-batch-book-page-1", plugin().getCacheTerm(new CachedTranslation("en", "es", "batch-book-page-1")));
    }

    @Test
    void itemObjectTranslationHandlesTitleOnly() {
        PlayerMock player = itemTranslator("ItemTitleOnly");
        ItemStack item = namedItem("batch-item-title");

        fireItemTranslation(player, item);

        ItemMeta translatedMeta = translatedTempItem(player).getItemMeta();
        assertTrue(translatedMeta.hasDisplayName());
        assertEquals("translated-batch-item-title", translatedMeta.getDisplayName());
        assertFalse(translatedMeta.hasLore());
    }

    @Test
    void itemObjectTranslationHandlesLoreOnly() {
        PlayerMock player = itemTranslator("ItemLoreOnly");
        ItemStack item = loreItem(List.of("batch-lore-only-0", "batch-lore-only-1"));

        fireItemTranslation(player, item);

        ItemMeta translatedMeta = translatedTempItem(player).getItemMeta();
        assertFalse(translatedMeta.hasDisplayName());
        assertEquals(List.of("translated-batch-lore-only-0", "translated-batch-lore-only-1"), translatedMeta.getLore());
    }

    @Test
    void itemObjectTranslationHandlesTitleAndLoreThroughOneRateLimitWindow() {
        PlayerMock player = itemTranslator("ItemTitleLore");
        ItemStack item = namedItem("batch-item-title");
        ItemMeta meta = item.getItemMeta();
        meta.setLore(List.of("batch-item-lore-0", "batch-item-lore-1"));
        item.setItemMeta(meta);

        fireItemTranslation(player, item);

        ItemMeta translatedMeta = translatedTempItem(player).getItemMeta();
        assertEquals("translated-batch-item-title", translatedMeta.getDisplayName());
        assertEquals(List.of("translated-batch-item-lore-0", "translated-batch-item-lore-1"), translatedMeta.getLore());
    }

    @Test
    void itemObjectTranslationRejectsStockItemWithNoLore() {
        PlayerMock player = itemTranslator("ItemStock");

        fireItemTranslation(player, new ItemStack(Material.DIAMOND));

        assertEquals(InventoryType.CRAFTING, player.getOpenInventory().getType());
        assertTrue(drainPlayerMessages(player).stream()
                .anyMatch(message -> message.contains("Unable to translate default item names")));
    }

    @Test
    void localeListenerStoresSupportedLocaleCode() {
        PlayerMock player = WWCTestSupport.addOpPlayer("LocaleUser");

        new SpigotPlayerLocaleListener().checkAndSetLocale((Player) player, "es");
        WWCTestSupport.drainScheduler();

        assertEquals("es", plugin().getPlayerRecord(player, false).getLocalizationCode());
    }

    private void configureChatGPTGuidelinesChecks(OpenAIStub stub) {
        plugin().getConfigManager().getMainConfig().set("Translator.testModeTranslator", false);
        plugin().getConfigManager().getMainConfig().set("Translator.useChatGPT", true);
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTAPIKey", "chatgpt-test-key");
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTURL", stub.url());
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTModel", "main-model");
        plugin().getConfigManager().getMainConfig().set("Translator.useOpenAICompatible", false);
        plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", true);
        plugin().getConfigManager().getMainConfig().set("Translator.guidelinesAIModel", "guidelines-model");
        plugin().setAISystemPrompt("Translate according to the schema.");
        plugin().setGuidelinesAIPrompt("Return whether this exact message is translatable.");
        plugin().setTranslatorName("ChatGPT");
        WWCTestSupport.useDirectPermissionChecks();
    }

    private static List<String> drainPlayerMessages(PlayerMock player) {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            try {
                String message = player.nextMessage();
                if (message == null) {
                    return messages;
                }
                messages.add(message);
            } catch (AssertionError noMoreMessages) {
                return messages;
            }
        }
        fail("Player still had queued messages after draining 50 entries.");
        return messages;
    }

    private PlayerMock itemTranslator(String name) {
        PlayerMock player = WWCTestSupport.addOpPlayer(name);
        player.performCommand("wwct en es");
        player.performCommand("wwcti");
        player.performCommand("wwctrl 5");
        drainPlayerMessages(player);
        return player;
    }

    private void fireItemTranslation(PlayerMock player, ItemStack item) {
        player.getInventory().setItemInMainHand(item);
        new TranslateInGameListener().onInGameObjTranslateRequest(interactWith(player, item));
        WWCTestSupport.drainScheduler();
    }

    private PlayerInteractEvent interactWith(PlayerMock player, ItemStack item) {
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SELF, EquipmentSlot.HAND);
    }

    private ItemStack namedItem(String displayName) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack loreItem(List<String> lore) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack translatedTempItem(PlayerMock player) {
        ItemStack displayedItem = player.getOpenInventory().getTopInventory().getItem(22);
        assertNotNull(displayedItem);
        return displayedItem;
    }
}
