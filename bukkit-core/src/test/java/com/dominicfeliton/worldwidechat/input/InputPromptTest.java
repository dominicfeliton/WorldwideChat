package com.dominicfeliton.worldwidechat.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputPromptTest {

    @Test
    void numericPromptRepeatsOnInvalidInput() {
        NumericInputPrompt prompt = new NumericInputPrompt() {
            @Override
            public String getPromptText(InputContext context) {
                return "";
            }

            @Override
            protected InputResult acceptValidatedInput(InputContext context, Number input) {
                return InputResult.complete();
            }
        };

        assertEquals(InputResult.Action.REPEAT, prompt.acceptInput(new InputContext(null), "not-a-number").getAction());
    }

    @Test
    void numericPromptPassesParsedInputToValidator() {
        NumericInputPrompt prompt = new NumericInputPrompt() {
            @Override
            public String getPromptText(InputContext context) {
                return "";
            }

            @Override
            protected InputResult acceptValidatedInput(InputContext context, Number input) {
                return input.intValue() == 42 ? InputResult.complete() : InputResult.repeat();
            }
        };

        assertEquals(InputResult.Action.COMPLETE, prompt.acceptInput(new InputContext(null), "42").getAction());
    }

    @Test
    void defaultCancelMapsToZeroInput() {
        StringInputPrompt prompt = new StringInputPrompt() {
            @Override
            public String getPromptText(InputContext context) {
                return "";
            }

            @Override
            public InputResult acceptInput(InputContext context, String input) {
                return "0".equals(input) ? InputResult.complete() : InputResult.repeat();
            }
        };

        assertEquals(InputResult.Action.COMPLETE, prompt.cancelInput(new InputContext(null)).getAction());
    }
}
