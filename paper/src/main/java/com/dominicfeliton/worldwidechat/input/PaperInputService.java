package com.dominicfeliton.worldwidechat.input;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;

public class PaperInputService implements InputService {
    private final WorldwideChat main = WorldwideChat.instance;
    private final CommonRefs refs = main.getServerFactory().getCommonRefs();
    private InputService conversationInputService;
    private InputService paperDialogInputService;
    private boolean warnedUnavailable = false;

    @Override
    public String getActiveBackendName() {
        return resolveMethod().getConfigValue();
    }

    @Override
    public void open(Player player, InputRequest request) {
        InputMethod resolved = resolveMethod();
        if (resolved == InputMethod.PAPER_DIALOG) {
            getPaperDialogInputService().open(player, request);
            return;
        }
        if (resolved == InputMethod.CONVERSATION && conversationsAvailable()) {
            getConversationInputService().open(player, request);
            return;
        }
        refs.sendMsg(request.getUnavailableMessageKey(), "", "&c", player);
    }

    protected InputMethod resolveMethod() {
        String configured = getConfiguredMethod();
        InputMethod resolved = InputMethodResolver.resolve(
                configured,
                main.getCurrPlatform(),
                main.getCurrMCVersion(),
                paperDialogsAvailable(),
                conversationsAvailable()
        );
        InputMethod requested = InputMethod.fromConfig(configured);
        if (requested != InputMethod.AUTO && requested != resolved && !warnedUnavailable) {
            main.getLogger().warning("Input method '" + configured + "' is unavailable on " + main.getCurrPlatform()
                    + " " + main.getCurrMCVersion() + "; falling back to " + resolved.getConfigValue() + ".");
            warnedUnavailable = true;
        }
        return resolved;
    }

    private String getConfiguredMethod() {
        if (main.getConfigManager() == null || main.getConfigManager().getMainConfig() == null) {
            return "auto";
        }
        return main.getConfigManager().getMainConfig().getString("General.inputMethod", "auto");
    }

    protected boolean conversationsAvailable() {
        if (main.getCurrPlatform().equalsIgnoreCase("Folia")) {
            return false;
        }
        try {
            Class.forName("org.bukkit.conversations.ConversationFactory");
            Class.forName("com.dominicfeliton.worldwidechat.input.SpigotConversationInputService");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private InputService getConversationInputService() {
        if (conversationInputService == null) {
            try {
                conversationInputService = (InputService) Class
                        .forName("com.dominicfeliton.worldwidechat.input.SpigotConversationInputService")
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (ReflectiveOperationException | LinkageError e) {
                throw new IllegalStateException("Conversation input service is unavailable.", e);
            }
        }
        return conversationInputService;
    }

    protected boolean paperDialogsAvailable() {
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            Class.forName("io.papermc.paper.registry.data.dialog.input.DialogInput");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private InputService getPaperDialogInputService() {
        if (paperDialogInputService == null) {
            try {
                paperDialogInputService = (InputService) Class
                        .forName("com.dominicfeliton.worldwidechat.input.PaperDialogInputService")
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (ReflectiveOperationException | LinkageError e) {
                throw new IllegalStateException("Paper Dialog input service is unavailable.", e);
            }
        }
        return paperDialogInputService;
    }
}
