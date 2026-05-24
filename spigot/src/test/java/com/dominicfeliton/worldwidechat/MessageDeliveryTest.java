package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.SpigotComponentMessenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MessageDeliveryTest extends WWCIntegrationTest {

    @Test
    void spigotMessengerSchedulesAsyncMessageBeforeDelivery() {
        PlayerMock player = WWCTestSupport.addOpPlayer("AsyncMessageTarget");
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin(), () -> {
            try {
                SpigotComponentMessenger.INSTANCE.sendMessage(player, Component.text("Async hello"));
            } catch (Throwable thrown) {
                failure.set(thrown);
            }
        });

        WWCTestSupport.drainScheduler();

        assertNull(failure.get());
        assertTrue(drainPlayerMessages(player).stream().anyMatch(message -> message.contains("Async hello")));
    }

    @Test
    void spigotJsonConversionPreservesHoverAndClickEvents() {
        Component message = Component.text("release")
                .clickEvent(ClickEvent.openUrl("https://github.com/dominicfeliton/WorldwideChat/releases"))
                .hoverEvent(HoverEvent.showText(Component.text("Open releases")));

        String json = ComponentSerializer.toString(invokeBaseComponents("toBaseComponents", message));

        assertTrue(json.contains("open_url"));
        assertTrue(json.contains("https://github.com/dominicfeliton/WorldwideChat/releases"));
        assertTrue(json.contains("show_text"));
        assertTrue(json.contains("Open releases"));
    }

    @Test
    void legacyFallbackConversionPreservesColorFormatting() {
        Component message = Component.text("Fallback", NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);

        String json = ComponentSerializer.toString(invokeBaseComponents("toLegacyBaseComponents", message));

        assertTrue(json.contains("\"color\":\"red\""));
        assertTrue(json.contains("\"bold\":true"));
        assertTrue(json.contains("Fallback"));
    }

    private BaseComponent[] invokeBaseComponents(String methodName, Component message) {
        try {
            Method method = SpigotComponentMessenger.class.getDeclaredMethod(methodName, Component.class);
            method.setAccessible(true);
            return (BaseComponent[]) method.invoke(SpigotComponentMessenger.INSTANCE, message);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new AssertionError(cause);
        }
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
}
