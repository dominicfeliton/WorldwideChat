package com.dominicfeliton.worldwidechat.translators;

public class TranslationFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String reason;
    private final boolean notifyPlayer;

    public TranslationFailureException(String reason, String message, boolean notifyPlayer) {
        super(message);
        this.reason = normalizeReason(reason);
        this.notifyPlayer = notifyPlayer;
    }

    public String getReason() {
        return reason;
    }

    public boolean shouldNotifyPlayer() {
        return notifyPlayer;
    }

    public boolean isGuidelinesFailure() {
        return "Guidelines".equals(reason);
    }

    public static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "General";
        }

        String trimmed = reason.trim();
        if (trimmed.equalsIgnoreCase("none") || trimmed.equalsIgnoreCase("null")) {
            return "none";
        } else if (trimmed.equalsIgnoreCase("Detection")) {
            return "Detection";
        } else if (trimmed.equalsIgnoreCase("Identical")) {
            return "Identical";
        } else if (trimmed.equalsIgnoreCase("Guidelines")) {
            return "Guidelines";
        }
        return "General";
    }
}
