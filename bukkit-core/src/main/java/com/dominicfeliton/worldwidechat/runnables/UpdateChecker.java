package com.dominicfeliton.worldwidechat.runnables;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class UpdateChecker implements Runnable {

	private boolean upToDate = false;
	private String latest = "";
	
	private WorldwideChat main = WorldwideChat.instance;

	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private Logger log = main.getLogger();
	
	@Override
	public void run() {
		Callable<Boolean> result = () -> {
			refs.debugMsg("Starting UpdateChecker!!!");
			try (InputStream in = new URL(
					"https://raw.githubusercontent.com/dominicfeliton/WorldwideChat/master/latestVersion.txt").openStream()) {
				latest = IOUtils.readLines(in, StandardCharsets.UTF_8).get(1);
			} catch (MalformedURLException e) {
				latest = main.getPluginVersion() + ""; // Just set latest to the current plugin version, since we can't find
														// a newer one
				log.warning(refs.getPlainMsg("wwcUpdaterConnectionFailed"));
			} catch (IOException e) {
				latest = main.getPluginVersion() + "";
				log.warning(refs.getPlainMsg("wwcUpdaterParserFailed"));
			}

			try {
				if (main.getPluginVersion().equals(latest)) {
					log.info(refs.getPlainMsg("wwcUpdaterUpToDate",
							"",
							"&d"));
				} else if (new ComparableVersion(main.getPluginVersion()).compareTo(new ComparableVersion(latest)) > 0) {
					log.warning(refs.getPlainMsg("wwcUpdaterFutureDate",
							"&d"+latest,
							"&e"));
				} else {
					log.warning(refs.getPlainMsg("wwcUpdaterOutOfDate", "&d"+latest, "&e"));
					log.warning("https://github.com/dominicfeliton/WorldwideChat/releases");
					main.setOutOfDate(true);
				}
			} catch (Exception e) {
				log.warning(refs.getPlainMsg("wwcUpdaterFailedGeneric"));
			}
			return true;
		};
		
		/* Start Callback Process */
		Future<Boolean> process = main.getCallbackExecutor().submit(result);
		try {
			/* Get update status */
			process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			refs.debugMsg("Update Checker Timeout!! Either we are reloading or we have lost connection. Abort.");
			if (e instanceof TimeoutException) {refs.sendTimeoutExceptionMsg(main.getServer().getConsoleSender());};
		}
	}

	public boolean upToDate() {
		return upToDate;
	}

	public String getLatestVersion() {
		return latest;
	}
}
