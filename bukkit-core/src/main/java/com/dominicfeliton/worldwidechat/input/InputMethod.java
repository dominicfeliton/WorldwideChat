package com.dominicfeliton.worldwidechat.input;

import java.util.Locale;

public enum InputMethod {
    AUTO("auto"),
    PAPER_DIALOG("paper-dialog"),
    CONVERSATION("conversation"),
    NONE("none");

    private final String configValue;

    InputMethod(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static InputMethod fromConfig(String input) {
        if (input == null || input.isBlank()) {
            return AUTO;
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        for (InputMethod method : values()) {
            if (method.configValue.equals(normalized)) {
                return method;
            }
        }
        return AUTO;
    }
}
