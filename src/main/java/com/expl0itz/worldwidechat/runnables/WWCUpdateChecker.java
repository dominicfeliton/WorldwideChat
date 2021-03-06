package com.expl0itz.worldwidechat.runnables;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.bukkit.scheduler.BukkitTask;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.md_5.bungee.api.ChatColor;


public class WWCUpdateChecker implements Runnable{

    private boolean upToDate = false;
    private String latest = "";
    private WorldwideChat main = WorldwideChat.getInstance();
    
    @Override
    public void run() {
        InputStream in = null;
        try {
            in = new URL("https://raw.githubusercontent.com/3xpl0itz/WorldwideChat/master/latestVersion.txt").openStream();
        } catch (MalformedURLException e) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterConnectionFailed"));
        } catch (IOException e) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterConnectionFailed"));
        }
        
        try {
            latest = IOUtils.readLines(in).get(0);
        } catch (IOException e) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterParserFailed"));
        } finally {
            IOUtils.closeQuietly(in);
        }

        upToDate = (main.getPluginVersion() == Double.parseDouble(latest));
        if (upToDate) {
            main.getLogger().info(ChatColor.LIGHT_PURPLE + main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterUpToDate"));
        } else {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterOutOfDate"));
        }
    }

    public boolean upToDate() {
     return upToDate;
    }
    
    public String getLatestVersion() {
     return latest;
    }
}
