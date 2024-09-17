package com.dominicfeliton.worldwidechat.runnables;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class UpdateChecker {

    // Indicates whether the plugin is up to date
    private boolean upToDate = false;

    // Stores the latest version retrieved from the remote source
    private String latestVersion = "";

    // Reference to the main plugin instance
    private final WorldwideChat main = WorldwideChat.instance;

    // Common references utility
    private final CommonRefs refs = main.getServerFactory().getCommonRefs();

    // Logger for logging messages
    private final Logger log = main.getLogger();

    // URL to fetch the latest version information
    private static final String VERSION_URL = "https://raw.githubusercontent.com/dominicfeliton/WorldwideChat/master/latestVersion.txt";

    /**
     * Constructor initializes the UpdateChecker and starts the update check.
     */
    public UpdateChecker() {
        checkForUpdates();
    }

    private void checkForUpdates() {
        refs.debugMsg("Starting UpdateChecker!!!");

        //latestVersion = fetchLatestVersion();
        latestVersion = "1.20.9";
        compareVersions();
    }

    private String fetchLatestVersion() {
        try (InputStream in = new URL(VERSION_URL).openStream()) {
            List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);

            if (lines.size() > 1) {
                return lines.get(1).trim();
            } else if (!lines.isEmpty()) {
                return lines.get(0).trim();
            } else {
                return main.getPluginVersion();
            }

        } catch (IOException e) {
            return main.getPluginVersion();
        }
    }

    private void compareVersions() {
        String currentVersion = main.getPluginVersion();

        try {
            ComparableVersion current = new ComparableVersion(currentVersion);
            ComparableVersion latest = new ComparableVersion(latestVersion);
            refs.debugMsg("latest: "+ latestVersion);

            int comparison = current.compareTo(latest);

            if (comparison == 0) {
                upToDate = true;
                log.info(refs.getPlainMsg("wwcUpdaterUpToDate", "", "&d"));
            } else if (comparison > 0) {
                log.warning(refs.getPlainMsg("wwcUpdaterFutureDate", "", "&e"));
            } else {
                log.warning(refs.getPlainMsg("wwcUpdaterOutOfDate", "&d" + latestVersion, "&e"));
                log.warning("https://github.com/dominicfeliton/WorldwideChat/releases/tag/v" + latestVersion);
                main.setOutOfDate(true);
            }

        } catch (Exception e) {
            log.warning(refs.getPlainMsg("wwcUpdaterFailedGeneric"));
        }
    }
}