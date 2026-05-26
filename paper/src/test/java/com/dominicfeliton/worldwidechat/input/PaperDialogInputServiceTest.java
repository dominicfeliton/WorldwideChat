package com.dominicfeliton.worldwidechat.input;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PaperDialogInputServiceTest {

    @Test
    void cancelDoesNotSubmitLegacyZeroToPrompt() {
        AtomicBoolean mutatedByZero = new AtomicBoolean(false);
        InputRequest request = InputRequest.fromPrompt(new InputPrompt() {
            @Override
            public String getPromptText(InputContext context) {
                return "Number";
            }

            @Override
            public InputResult acceptInput(InputContext context, String input) {
                if ("0".equals(input)) {
                    mutatedByZero.set(true);
                }
                return InputResult.complete();
            }
        });

        new PaperDialogInputService().handleCancel(request, null);

        assertFalse(mutatedByZero.get());
    }
}
