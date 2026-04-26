package com.dominicfeliton.worldwidechat.input;

public class InputRequest {
    private final InputPrompt prompt;

    private InputRequest(InputPrompt prompt) {
        this.prompt = prompt;
    }

    public static InputRequest fromPrompt(InputPrompt prompt) {
        return new InputRequest(prompt);
    }

    public InputPrompt getPrompt() {
        return prompt;
    }

    public String getPromptText(InputContext context) {
        return prompt.getPromptText(context);
    }

    public InputResult acceptInput(InputContext context, String input) {
        return prompt.acceptInput(context, input);
    }

    public InputResult cancelInput(InputContext context) {
        return prompt.cancelInput(context);
    }

    public InputType getInputType() {
        return prompt.getInputType();
    }

    public int getMaxLength() {
        return prompt.getMaxLength();
    }

    public boolean isMultiline() {
        return prompt.isMultiline();
    }

    public int getTimeoutSeconds() {
        return prompt.getTimeoutSeconds();
    }

    public String getUnavailableMessageKey() {
        return prompt.getUnavailableMessageKey();
    }
}
