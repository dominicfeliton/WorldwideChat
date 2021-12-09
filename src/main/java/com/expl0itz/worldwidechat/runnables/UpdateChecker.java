package com.expl0itz.worldwidechat.runnables;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class UpdateChecker implements Runnable {

	private boolean upToDate = false;
	private String latest = "";
	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public void run() {
		CommonDefinitions.sendDebugMessage("Starting UpdateChecker!!!");
		try (InputStream in = new URL(
				"https://raw.githubusercontent.com/3xpl0itz/WorldwideChat/master/latestVersion.txt").openStream()) {
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
				main.getLogger().warning("https://github.com/3xpl0itz/WorldwideChat/releases");
				main.setOutOfDate(true);
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcUpdaterFailedGeneric"));
		}
	}

	public boolean upToDate() {
		return upToDate;
	}

	public String getLatestVersion() {
		return latest;
	}
}
