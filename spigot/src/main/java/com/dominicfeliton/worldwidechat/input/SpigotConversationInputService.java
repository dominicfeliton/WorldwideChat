package com.dominicfeliton.worldwidechat.input;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotConversationInputService implements InputService {
    protected final WorldwideChat main = WorldwideChat.instance;
    protected final CommonRefs refs = main.getServerFactory().getCommonRefs();

    @Override
    public String getActiveBackendName() {
        return resolveMethod().getConfigValue();
    }

    @Override
    public void open(Player player, InputRequest request) {
        String configured = getConfiguredMethod();
        InputMethod resolved = resolveMethod();
        refs.debugMsg("Opening input with backend " + resolved.getConfigValue()
                + " (configured: " + configured + ").");
        if (resolved == InputMethod.NONE) {
            refs.sendMsg(request.getUnavailableMessageKey(), "", "&c", player);
            return;
        }
        if (resolved != InputMethod.CONVERSATION) {
            main.getLogger().warning("Input method '" + configured + "' is unavailable on " + main.getCurrPlatform()
                    + "; falling back to conversations.");
        }
        Prompt prompt = request.getInputType() == InputType.NUMBER
                ? new NumericAdapter(request)
                : new StringAdapter(request);
        new ConversationFactory(main)
                .withModality(true)
                .withTimeout(request.getTimeoutSeconds())
                .withFirstPrompt(prompt)
                .buildConversation(player)
                .begin();
    }

    protected String getConfiguredMethod() {
        if (main.getConfigManager() == null || main.getConfigManager().getMainConfig() == null) {
            return "auto";
        }
        return main.getConfigManager().getMainConfig().getString("General.inputMethod", "auto");
    }

    protected InputMethod resolveMethod() {
        return InputMethodResolver.resolve(
                getConfiguredMethod(),
                main.getCurrPlatform(),
                main.getCurrMCVersion(),
                false,
                true
        );
    }

    protected Prompt applyResult(InputResult result, Prompt repeatPrompt) {
        if (result == null || result.getAction() == InputResult.Action.COMPLETE || result.getAction() == InputResult.Action.ASYNC_COMPLETE) {
            return Prompt.END_OF_CONVERSATION;
        }
        return repeatPrompt;
    }

    private class StringAdapter extends StringPrompt {
        private final InputRequest request;

        private StringAdapter(InputRequest request) {
            this.request = request;
        }

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            return request.getPromptText(new InputContext((Player) context.getForWhom()));
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return applyResult(request.acceptInput(new InputContext((Player) context.getForWhom()), input), this);
        }
    }

    private class NumericAdapter extends NumericPrompt {
        private final InputRequest request;

        private NumericAdapter(InputRequest request) {
            this.request = request;
        }

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            return request.getPromptText(new InputContext((Player) context.getForWhom()));
        }

        @Override
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
            return applyResult(request.acceptInput(new InputContext((Player) context.getForWhom()), input.toString()), this);
        }
    }
}
