package com.badskater0729.worldwidechat.runnables;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import com.badskater0729.worldwidechat.WorldwideChat;

import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendTimeoutExceptionMsg;;

public class UpdateChecker implements Runnable {

	private boolean upToDate = false;
	private String latest = "";
	
	private WorldwideChat main = WorldwideChat.instance; 
	private Logger log = main.getLogger();
	
	@Override
	public void run() {
		Callable<Boolean> result = () -> {
			debugMsg("Starting UpdateChecker!!!");
			try (InputStream in = new URL(
					"https://raw.githubusercontent.com/BadSkater0729/WorldwideChat/master/latestVersion.txt").openStream()) {
				latest = IOUtils.readLines(in, StandardCharsets.UTF_8).get(1);
			} catch (MalformedURLException e) {
				latest = main.getPluginVersion() + ""; // Just set latest to the current plugin version, since we can't find
														// a newer one
				log.warning(getMsg("wwcUpdaterConnectionFailed"));
			} catch (IOException e) {
				latest = main.getPluginVersion() + "";
				log.warning(getMsg("wwcUpdaterParserFailed"));
			}

			try {
				if (main.getPluginVersion().equals(latest)) {
					log.info(ChatColor.LIGHT_PURPLE
							+ getMsg("wwcUpdaterUpToDate"));
				} else if (new ComparableVersion(main.getPluginVersion()).compareTo(new ComparableVersion(latest)) > 0) {
					log.warning(getMsg("wwcUpdaterFutureDate", latest));
				} else {
					log.warning(getMsg("wwcUpdaterOutOfDate", latest));
					log.warning("https://github.com/BadSkater0729/WorldwideChat/releases");
					main.setOutOfDate(true);
				}
			} catch (Exception e) {
				log.warning(getMsg("wwcUpdaterFailedGeneric"));
			}
			return true;
		};
		
		/* Start Callback Process */
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Boolean> process = executor.submit(result);
		try {
			/* Get update status */
			process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			debugMsg("Update Checker Timeout!! Either we are reloading or we have lost connection. Abort.");
			if (e instanceof TimeoutException) {sendTimeoutExceptionMsg(WorldwideChat.instance.getServer().getConsoleSender());};
			process.cancel(true);
		} finally {
			executor.shutdownNow();
		}
	}

	public boolean upToDate() {
		return upToDate;
	}

	public String getLatestVersion() {
		return latest;
	}
}
