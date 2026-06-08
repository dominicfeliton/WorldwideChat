package com.dominicfeliton.worldwidechat.input;

public abstract class StringInputPrompt implements InputPrompt {
    @Override
    public InputType getInputType() {
        return InputType.TEXT;
    }
}
