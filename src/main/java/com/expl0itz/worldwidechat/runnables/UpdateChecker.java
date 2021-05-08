package com.expl0itz.worldwidechat.runnables;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import com.expl0itz.worldwidechat.WorldwideChat;

import net.md_5.bungee.api.ChatColor;

public class UpdateChecker implements Runnable{

    private boolean upToDate = false;
    private String latest = "";
    private WorldwideChat main = WorldwideChat.getInstance();
    
    @Override
    public void run() {
        try (InputStream in = new URL("https://raw.githubusercontent.com/3xpl0itz/WorldwideChat/master/latestVersion.txt").openStream()) {
            latest = IOUtils.readLines(in, StandardCharsets.UTF_8).get(0);
        } catch (MalformedURLException e) {
            latest = main.getPluginVersion() + ""; //Just set latest to the current plugin version, since we can't find a newer one
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterConnectionFailed"));
        } catch (IOException e) {
            latest = main.getPluginVersion() + "";
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterParserFailed"));
        }

        try {
            if (main.getPluginVersion() == Double.parseDouble(latest)) {
                main.getLogger().info(ChatColor.LIGHT_PURPLE + main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterUpToDate"));
            } else if (main.getPluginVersion() > Double.parseDouble(latest)) {
            	 main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterFutureDate").replace("%i", "" + Double.parseDouble(latest)));
            } else {
                main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterOutOfDate").replace("%i", "" + Double.parseDouble(latest)));
                main.getLogger().warning("https://github.com/3xpl0itz/WorldwideChat/releases");
                main.setOutOfDate(true);
            }
        } catch (Exception e) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterFailedGeneric"));           
        }
    }

    public boolean upToDate() {
     return upToDate;
    }
    
    public String getLatestVersion() {
     return latest;
    }
}
