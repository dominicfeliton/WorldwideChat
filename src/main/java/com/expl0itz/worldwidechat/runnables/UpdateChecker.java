package com.expl0itz.worldwidechat.runnables;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.md_5.bungee.api.ChatColor;


public class UpdateChecker implements Runnable{

    private boolean upToDate = false;
    private String latest = "";
    private BukkitTask updaterTask;
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

        try {
            upToDate = (main.getPluginVersion() == Double.parseDouble(latest));
            if (upToDate) {
                main.getLogger().info(ChatColor.LIGHT_PURPLE + main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterUpToDate"));
            } else {
                main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterOutOfDate").replace("%i", "" + Double.parseDouble(latest)));
                main.getLogger().warning("https://github.com/3xpl0itz/WorldwideChat/releases");
                main.setOutOfDate(true);
            }
        } catch (Exception e) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUpdaterFailedGeneric"));           
        }
        //Schedule the next update check here
        //DEBUG: main.getLogger().info(main.getBackgroundTasks().size() + "");
        main.removeBackgroundTask("updateChecker"); //mem leaks bad; remove all previous tasks of WWCUpdateChecker
        updaterTask = Bukkit.getScheduler().runTaskLaterAsynchronously(main, new UpdateChecker(), main.getUpdateCheckerDelay()*20);
        main.addBackgroundTask("updateChecker", updaterTask);
    }

    public boolean upToDate() {
     return upToDate;
    }
    
    public String getLatestVersion() {
     return latest;
    }
}
