package com.dominicfeliton.worldwidechat.input;

public interface InputPrompt {
    int DEFAULT_TIMEOUT_SECONDS = 600;
    int DEFAULT_TEXT_MAX_LENGTH = 4096;
    int DEFAULT_NUMBER_MAX_LENGTH = 32;

    String getPromptText(InputContext context);

    InputResult acceptInput(InputContext context, String input);

    default InputResult cancelInput(InputContext context) {
        return acceptInput(context, "0");
    }

    default InputType getInputType() {
        return InputType.TEXT;
    }

    default int getMaxLength() {
        return getInputType() == InputType.NUMBER ? DEFAULT_NUMBER_MAX_LENGTH : DEFAULT_TEXT_MAX_LENGTH;
    }

    default boolean isMultiline() {
        return getInputType() == InputType.TEXT;
    }

    default int getTimeoutSeconds() {
        return DEFAULT_TIMEOUT_SECONDS;
    }

    default String getUnavailableMessageKey() {
        return "wwcNoConvoFolia";
    }
}
