package com.dominicfeliton.worldwidechat.input;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.util.List;

public class PaperDialogInputService implements InputService {
    @Override
    public String getActiveBackendName() {
        return "paper-dialog";
    }

    @Override
    public void open(Player player, InputRequest request) {
        InputContext context = new InputContext(player);
        String promptText = request.getPromptText(context);
        TextDialogInput.Builder inputBuilder = DialogInput.text("wwc_input", Component.text("Input"))
                .width(300)
                .maxLength(Math.max(1, request.getMaxLength()))
                .initial("");
        if (request.isMultiline()) {
            inputBuilder.multiline(TextDialogInput.MultilineOptions.create(8, 120));
        }

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("WorldwideChat"))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(Component.text(promptText), 350)))
                        .inputs(List.of(inputBuilder.build()))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.create(Component.text("Confirm"), Component.text("Apply this input."), 100,
                                DialogAction.customClick((view, audience) -> handleInput(request, view.getText("wwc_input"), audience),
                                        ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build())),
                        ActionButton.create(Component.text("Cancel"), Component.text("Go back."), 100,
                                DialogAction.customClick((view, audience) -> handleCancel(request, audience),
                                        ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()))
                )));
        player.showDialog(dialog);
    }

    private void handleInput(InputRequest request, String input, Audience audience) {
        if (!(audience instanceof Player player)) {
            return;
        }
        InputResult result = request.acceptInput(new InputContext(player), input == null ? "" : input);
        if (result != null && result.getAction() == InputResult.Action.REPEAT) {
            open(player, request);
        }
    }

    private void handleCancel(InputRequest request, Audience audience) {
        if (audience instanceof Player player) {
            request.cancelInput(new InputContext(player));
        }
    }
}
