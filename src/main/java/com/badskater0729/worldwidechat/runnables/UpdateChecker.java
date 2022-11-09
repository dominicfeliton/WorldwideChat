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

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class UpdateChecker implements Runnable {

	private boolean upToDate = false;
	private String latest = "";
	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public void run() {
		Callable<Boolean> result = () -> {
			CommonDefinitions.sendDebugMessage("Starting UpdateChecker!!!");
			try (InputStream in = new URL(
					"https://raw.githubusercontent.com/BadSkater0729/WorldwideChat/master/latestVersion.txt").openStream()) {
				latest = IOUtils.readLines(in, StandardCharsets.UTF_8).get(1);
			} catch (MalformedURLException e) {
				latest = main.getPluginVersion() + ""; // Just set latest to the current plugin version, since we can't find
														// a newer one
				main.getLogger().warning(CommonDefinitions.getMessage("wwcUpdaterConnectionFailed"));
			} catch (IOException e) {
				latest = main.getPluginVersion() + "";
				main.getLogger().warning(CommonDefinitions.getMessage("wwcUpdaterParserFailed"));
			}

			try {
				if (main.getPluginVersion().equals(latest)) {
					main.getLogger().info(ChatColor.LIGHT_PURPLE
							+ CommonDefinitions.getMessage("wwcUpdaterUpToDate"));
				} else if (new ComparableVersion(main.getPluginVersion()).compareTo(new ComparableVersion(latest)) > 0) {
					main.getLogger().warning(CommonDefinitions.getMessage("wwcUpdaterFutureDate", new String[] {latest}));
				} else {
					main.getLogger().warning(CommonDefinitions.getMessage("wwcUpdaterOutOfDate", new String[] {latest}));
					main.getLogger().warning("https://github.com/BadSkater0729/WorldwideChat/releases");
					main.setOutOfDate(true);
				}
			} catch (Exception e) {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcUpdaterFailedGeneric"));
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
			CommonDefinitions.sendDebugMessage("Update Checker Timeout!! Either we are reloading or we have lost connection. Abort.");
			if (e instanceof TimeoutException) {CommonDefinitions.sendTimeoutExceptionMessage(WorldwideChat.instance.getServer().getConsoleSender());};
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
