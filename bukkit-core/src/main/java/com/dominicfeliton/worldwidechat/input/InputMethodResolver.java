package com.dominicfeliton.worldwidechat.input;

import org.apache.maven.artifact.versioning.ComparableVersion;

public final class InputMethodResolver {
    private static final ComparableVersion PAPER_DIALOG_MIN_VERSION = new ComparableVersion("1.21.7");

    private InputMethodResolver() {
    }

    public static InputMethod resolve(String configuredMethod, String platform, ComparableVersion mcVersion,
                                      boolean paperDialogsAvailable, boolean conversationsAvailable) {
        InputMethod requested = InputMethod.fromConfig(configuredMethod);
        if (requested == InputMethod.NONE) {
            return InputMethod.NONE;
        }

        InputMethod automatic = auto(platform, mcVersion, paperDialogsAvailable, conversationsAvailable);
        if (requested == InputMethod.AUTO) {
            return automatic;
        }
        if (isAvailable(requested, platform, mcVersion, paperDialogsAvailable, conversationsAvailable)) {
            return requested;
        }
        return automatic;
    }

    public static InputMethod auto(String platform, ComparableVersion mcVersion, boolean paperDialogsAvailable,
                                   boolean conversationsAvailable) {
        if (paperDialogEligible(platform, mcVersion, paperDialogsAvailable)) {
            return InputMethod.PAPER_DIALOG;
        }
        return conversationsAvailable ? InputMethod.CONVERSATION : InputMethod.NONE;
    }

    public static boolean isAvailable(InputMethod method, String platform, ComparableVersion mcVersion,
                                      boolean paperDialogsAvailable, boolean conversationsAvailable) {
        return switch (method) {
            case PAPER_DIALOG -> paperDialogEligible(platform, mcVersion, paperDialogsAvailable);
            case CONVERSATION -> conversationsAvailable;
            case AUTO, NONE -> true;
        };
    }

    private static boolean paperDialogEligible(String platform, ComparableVersion mcVersion, boolean paperDialogsAvailable) {
        if (!paperDialogsAvailable || mcVersion == null) {
            return false;
        }
        boolean paperFamily = "Paper".equalsIgnoreCase(platform) || "Folia".equalsIgnoreCase(platform);
        return paperFamily && mcVersion.compareTo(PAPER_DIALOG_MIN_VERSION) >= 0;
    }
}
