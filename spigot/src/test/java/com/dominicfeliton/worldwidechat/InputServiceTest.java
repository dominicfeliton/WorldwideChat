package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.input.InputContext;
import com.dominicfeliton.worldwidechat.input.InputPrompt;
import com.dominicfeliton.worldwidechat.input.InputRequest;
import com.dominicfeliton.worldwidechat.input.InputResult;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class InputServiceTest extends WWCIntegrationTest {

    @Test
    void configuredNoneDoesNotStartSpigotConversation() {
        plugin().getConfigManager().getMainConfig().set("General.inputMethod", "none");
        PlayerMock player = WWCTestSupport.addOpPlayer("NoInput");
        drainPlayerMessages(player);
        AtomicBoolean acceptedInput = new AtomicBoolean(false);

        assertEquals("none", plugin().getInputService().getActiveBackendName());

        plugin().getInputService().open(player, InputRequest.fromPrompt(new InputPrompt() {
            @Override
            public String getPromptText(InputContext context) {
                return "Enter input";
            }

            @Override
            public InputResult acceptInput(InputContext context, String input) {
                acceptedInput.set(true);
                return InputResult.complete();
            }
        }));

        assertFalse(player.isConversing());
        player.acceptConversationInput("should not be accepted");
        assertFalse(acceptedInput.get());
        assertTrue(drainPlayerMessages(player).stream()
                        .anyMatch(message -> message.contains("Bukkit Conversations are currently broken")),
                "Expected player to receive the configured unavailable input message.");
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
