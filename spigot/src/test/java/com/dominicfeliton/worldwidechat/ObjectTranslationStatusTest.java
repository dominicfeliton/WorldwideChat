package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.TranslationProgressIndicator;
import com.dominicfeliton.worldwidechat.listeners.TranslateInGameListener;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.CowMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static org.junit.jupiter.api.Assertions.*;

class ObjectTranslationStatusTest extends WWCIntegrationTest {

    @Test
    void entityTranslationListenerRoutesStatusToActionBar() {
        PlayerMock player = WWCTestSupport.addOpPlayer("EntityStatusActionBar");
        player.performCommand("wwct en es");
        player.performCommand("wwcte");
        drainPlayerMessages(player);

        CowMock cow = new CowMock(WWCTestSupport.server(), UUID.randomUUID());
        cow.setCustomName("Hello, how are you?");
        Object previousHelper = installRecordingActionBarHelper();

        try {
            new TranslateInGameListener().onInGameEntityTranslateRequest(
                    new PlayerInteractEntityEvent(player, cow, EquipmentSlot.HAND));
            WWCTestSupport.drainScheduler();

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Translating target entity")));
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Done!")));
            assertTrue(actionBars.stream()
                    .noneMatch(message -> message.contains("Translated Entity Name")
                            || message.contains("Hola, como estas?")));

            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Translated Entity Name")
                            && message.contains("Hola, como estas?")));
            assertTrue(messages.stream()
                    .noneMatch(message -> message.contains("Translating target entity")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void entityTranslationDispatchesBlockingWorkOnAsyncScheduler() {
        PlayerMock player = WWCTestSupport.addOpPlayer("EntityStatusAsync");
        player.performCommand("wwct en es");
        player.performCommand("wwcte");
        drainPlayerMessages(player);

        CowMock cow = new CowMock(WWCTestSupport.server(), UUID.randomUUID());
        cow.setCustomName("Hello, how are you?");
        RecordingSchedulerHelper helper = new RecordingSchedulerHelper();
        Object previousHelper = installRecordingActionBarHelper(helper);
        TranslateInGameListener listener = new TranslateInGameListener();
        installListenerHelper(listener, helper);

        try {
            listener.onInGameEntityTranslateRequest(
                    new PlayerInteractEntityEvent(player, cow, EquipmentSlot.HAND));

            assertEquals(List.of(ASYNC), helper.asyncSchedulerTypes);
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void objectStatusUsesActionBarWhenEnabled() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StatusActionBar");
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        Object previousHelper = installRecordingActionBarHelper();

        try {
            var status = refs.beginStatusMsg("wwcSignTranslateStart", "", "&d&l", player);

            assertTrue(drainActionBars(player).stream()
                    .anyMatch(message -> message.contains("Translating target sign")));
            refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
            WWCTestSupport.server().getScheduler().performTicks(1);
            assertTrue(drainActionBars(player).stream()
                    .anyMatch(message -> message.contains("Sign translated successfully")));
            assertTrue(drainPlayerMessages(player).isEmpty());
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void objectStatusFailureShowsErrorAfterImmediateStart() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StatusErrorActionBar");
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        Object previousHelper = installRecordingActionBarHelper();

        try {
            var status = refs.beginStatusMsg("wwcSignTranslateStart", "", "&d&l", player);
            assertTrue(drainActionBars(player).stream()
                    .anyMatch(message -> message.contains("Translating target sign")));

            refs.failStatusMsg(status, player);
            WWCTestSupport.server().getScheduler().performTicks(1);
            assertTrue(drainActionBars(player).stream()
                    .anyMatch(message -> message.contains("Error")));
            assertTrue(drainPlayerMessages(player).isEmpty());
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void bookTitleDetailStaysInChatWhileLifecycleUsesActionBar() {
        PlayerMock player = WWCTestSupport.addOpPlayer("BookTitleChat");
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        Object previousHelper = installRecordingActionBarHelper();

        try {
            var status = refs.beginStatusMsg("wwcBookTranslateStart", "", "&d&l", player);
            refs.sendMsg("wwcBookTranslateTitleSuccess", "&a&oHola, como estas?", "&2&l", player);
            refs.finishStatusMsg(status, "wwcBookDone", "", "&a&o", player);
            WWCTestSupport.server().getScheduler().performTicks(1);

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Translating target book")));
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Book translated successfully")));
            assertTrue(actionBars.stream()
                    .noneMatch(message -> message.contains("Translated Book Title")
                            || message.contains("Hola, como estas?")));

            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Translated Book Title")
                            && message.contains("Hola, como estas?")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void objectStatusFallsBackToChatWhenActionBarDisabled() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StatusChat");
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        Object previousHelper = installRecordingActionBarHelper();

        plugin().setSendActionBar(false);
        plugin().getConfigManager().getMainConfig().set("Chat.sendActionBar", false);

        try {
            var status = refs.beginStatusMsg("wwcSignTranslateStart", "", "&d&l", player);
            refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
            WWCTestSupport.drainScheduler();

            assertTrue(drainActionBars(player).isEmpty());
            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Translating target sign")));
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Sign translated successfully")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void signTooLongFallbackSendsChatAndReportsSuccess() {
        PlayerMock player = signTranslator("SignTooLongFallback");
        Block signBlock = signBlock(player, "batch-sign-too-long");
        Object previousHelper = installRecordingActionBarHelper();

        try {
            fireSignTranslation(player, signBlock);
            WWCTestSupport.drainScheduler();

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Translating target sign")));
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Sign translated successfully")));
            assertTrue(actionBars.stream()
                    .noneMatch(message -> message.contains("Error")));

            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Target sign was either deleted or the translation was too long")
                            && message.contains("translated-batch-sign-too-long")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void signDeletedFallbackSendsChatAndReportsSuccess() {
        PlayerMock player = signTranslator("SignDeletedFallback");
        plugin().addCacheTerm(new CachedTranslation("en", "es", "ok"), "bien");
        Block signBlock = signBlock(player, "ok");
        Object previousHelper = installRecordingActionBarHelper();

        try {
            new TranslateInGameListener().onInGameObjTranslateRequest(signClick(player, signBlock));
            signBlock.setType(Material.AIR);
            WWCTestSupport.drainScheduler();

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Translating target sign")));
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Sign translated successfully")));
            assertTrue(actionBars.stream()
                    .noneMatch(message -> message.contains("Error")));

            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Target sign was either deleted or the translation was too long")
                            && message.contains("bien")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void signSendLengthRejectionSendsChatAndReportsSuccess() {
        PlayerMock player = signRejectingTranslator("SignSendLengthRejected");
        plugin().addCacheTerm(new CachedTranslation("en", "es", "ok"), "bien");
        Block signBlock = signBlock(player, "ok");
        Object previousHelper = installRecordingActionBarHelper();

        try {
            fireSignTranslation(player, signBlock);
            WWCTestSupport.drainScheduler();

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Translating target sign")));
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Sign translated successfully")));
            assertTrue(actionBars.stream()
                    .noneMatch(message -> message.contains("Error")));

            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream()
                    .anyMatch(message -> message.contains("Target sign was either deleted or the translation was too long")
                            && message.contains("bien")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void signIdenticalTranslationStillShowsErrorAndNotTranslatedMessage() {
        PlayerMock player = signTranslator("SignIdenticalFailure");
        Block signBlock = signBlock(player, "same");
        Object previousHelper = installRecordingActionBarHelper();

        try {
            fireSignTranslation(player, signBlock);
            WWCTestSupport.drainScheduler();

            assertTrue(drainActionBars(player).stream()
                    .anyMatch(message -> message.contains("Error")));
            assertTrue(drainPlayerMessages(player).stream()
                    .anyMatch(message -> message.contains("Target sign was not successfully translated")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    @Test
    void signSendSuccessShowsTranslatedSuccessfullyStatus() {
        PlayerMock player = signTranslator("SignSendSuccess");
        plugin().addCacheTerm(new CachedTranslation("en", "es", "ok"), "bien");
        Block signBlock = signBlock(player, "ok");
        Object previousHelper = installRecordingActionBarHelper();

        try {
            fireSignTranslation(player, signBlock);
            WWCTestSupport.drainScheduler();

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream()
                    .anyMatch(message -> message.contains("Sign translated successfully")));
            assertTrue(actionBars.stream()
                    .noneMatch(message -> message.contains("Error")));
        } finally {
            restoreActionBarEnabled();
            restoreWWCHelper(previousHelper);
        }
    }

    private void restoreActionBarEnabled() {
        plugin().setSendActionBar(true);
        plugin().getConfigManager().getMainConfig().set("Chat.sendActionBar", true);
    }

    private PlayerMock signTranslator(String name) {
        PlayerMock player = WWCTestSupport.addOpPlayer(name);
        enableSignTranslation(player);
        return player;
    }

    private PlayerMock signRejectingTranslator(String name) {
        SignRejectingPlayerMock player = new SignRejectingPlayerMock(WWCTestSupport.server(), name);
        WWCTestSupport.server().addPlayer(player);
        player.setOp(true);
        enableSignTranslation(player);
        return player;
    }

    private void enableSignTranslation(PlayerMock player) {
        player.performCommand("wwct en es");
        player.performCommand("wwcts");
        drainPlayerMessages(player);
    }

    private Block signBlock(PlayerMock player, String firstLine) {
        Block block = player.getWorld().getBlockAt(0, 64, 0);
        block.setType(Material.OAK_SIGN);
        Sign sign = (Sign) block.getState();
        sign.setLine(0, firstLine);
        sign.update();
        return block;
    }

    private void fireSignTranslation(PlayerMock player, Block signBlock) {
        new TranslateInGameListener().onInGameObjTranslateRequest(signClick(player, signBlock));
    }

    private PlayerInteractEvent signClick(PlayerMock player, Block signBlock) {
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, signBlock, BlockFace.SELF, EquipmentSlot.HAND);
    }

    private Object installRecordingActionBarHelper() {
        return installRecordingActionBarHelper(new RecordingActionBarHelper());
    }

    private Object installRecordingActionBarHelper(RecordingActionBarHelper helper) {
        try {
            Field helperField = CommonRefs.class.getDeclaredField("wwcHelper");
            helperField.setAccessible(true);
            Object previousHelper = helperField.get(null);
            helperField.set(null, helper);

            Field indicatorField = WorldwideChat.class.getDeclaredField("translationProgressIndicator");
            indicatorField.setAccessible(true);
            Object previousIndicator = indicatorField.get(plugin());
            indicatorField.set(plugin(), new TranslationProgressIndicator(plugin(), plugin().getServerFactory().getCommonRefs(), helper));

            return new Object[]{previousHelper, previousIndicator};
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void installListenerHelper(TranslateInGameListener listener, WorldwideChatHelper helper) {
        try {
            Field helperField = TranslateInGameListener.class.getDeclaredField("wwcHelper");
            helperField.setAccessible(true);
            helperField.set(listener, helper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void restoreWWCHelper(Object previousState) {
        try {
            Object[] state = (Object[]) previousState;
            Field helperField = CommonRefs.class.getDeclaredField("wwcHelper");
            helperField.setAccessible(true);
            helperField.set(null, state[0]);

            Field indicatorField = WorldwideChat.class.getDeclaredField("translationProgressIndicator");
            indicatorField.setAccessible(true);
            indicatorField.set(plugin(), state[1]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> drainActionBars(PlayerMock player) {
        List<String> actionBars = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Component actionBar = player.nextActionBar();
            if (actionBar == null) {
                return actionBars;
            }
            actionBars.add(PlainTextComponentSerializer.plainText().serialize(actionBar));
        }
        fail("Player still had queued action bars after draining 50 entries.");
        return actionBars;
    }

    private List<String> drainPlayerMessages(PlayerMock player) {
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

    private static class RecordingActionBarHelper extends SpigotWorldwideChatHelper {
        @Override
        public void sendActionBar(Component message, CommandSender sender) {
            if (sender instanceof Player player) {
                player.sendActionBar(message);
            }
        }
    }

    private static final class RecordingSchedulerHelper extends RecordingActionBarHelper {
        private final List<SchedulerType> asyncSchedulerTypes = new ArrayList<>();

        @Override
        public void runAsync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
            asyncSchedulerTypes.add(schedulerType);
            in.run();
        }
    }

    private static final class SignRejectingPlayerMock extends PlayerMock {
        private SignRejectingPlayerMock(ServerMock server, String name) {
            super(server, name);
        }

        @Override
        public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {
            throw new IllegalArgumentException("Line 1 is too long to fit on the sign");
        }
    }
}
