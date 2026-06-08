package com.dominicfeliton.worldwidechat.input;

import org.jetbrains.annotations.NotNull;

public abstract class NumericInputPrompt implements InputPrompt {
    @Override
    public InputType getInputType() {
        return InputType.NUMBER;
    }

    @Override
    public boolean isMultiline() {
        return false;
    }

    @Override
    public InputResult acceptInput(@NotNull InputContext context, String input) {
        try {
            return acceptValidatedInput(context, Integer.parseInt(input.trim()));
        } catch (Exception e) {
            return InputResult.repeat();
        }
    }

    protected abstract InputResult acceptValidatedInput(@NotNull InputContext context, Number input);
}
