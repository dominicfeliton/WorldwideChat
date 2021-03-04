package com.expl0itz.worldwidechat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.configuration.WWCConfigurationHandler;
import com.expl0itz.worldwidechat.listeners.WWCChatListener;
import com.expl0itz.worldwidechat.misc.WWCActiveTranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WorldwideChat extends JavaPlugin
{
	/* Managers */
	private Set<BukkitTask> backgroundTasks = new HashSet<BukkitTask>();
	private WWCConfigurationHandler configurationManager;
	
	/* Vars */
	private double pluginVersion = 0.5;
	
	private String pluginPrefixString = "WWC";
	private String pluginLang = "en";
	
	private ArrayList<WWCActiveTranslator> activeTranslators = new ArrayList<WWCActiveTranslator>();
	
	/*Little bug about text components as of adventure 4.5.1:
	 * If you do not use a NamedTextColor as your first color (ex: hex), the output will
	 * be garbled with some annoying variables. We used the MC dark red to "get around"
	 * this. Even though it's more of a good alternative solution now, keep this in mind if
	 * this is still not patched + you start with a hex color.
	 * */
	private final TextComponent pluginPrefix = Component.text()
	 .content("[").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true)
	 .append(Component.text().content(pluginPrefixString).color(TextColor.color(0x5757c4)))
	 .append(Component.text().content("]").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
	 .build();
	
	/* Methods */
	@Override
    public void onEnable()
    {
        //Load config + vals
		configurationManager = new WWCConfigurationHandler(this, pluginLang);
		configurationManager.initConfigs();
		
		//EventHandlers
		getServer().getPluginManager().registerEvents(new WWCChatListener(this), this);
		
		//Check for Updates
		//TODO
		
		//We made it!
		getLogger().info("Enabled WorldwideChat version " + pluginVersion + ".");
    }
	
	@Override
	public void onDisable()
	{
		//Cleanly cancel/reset all background tasks (runnables, timers, vars, etc.)
		cancelBackgroundTasks();
		
		//Null static vars

		
		//All done.
		getLogger().info("Disabled WorldwideChat version " + pluginVersion + ".");
	}
	
	public void cancelBackgroundTasks()
	{
		//Clear all active translators
		activeTranslators.clear();
		
		for (BukkitTask task : backgroundTasks)
		{
			//ask active tasks if they are active; let them finish + cancel?
			//TODO
			//task.cancel();
		}
	}
	
	//Init all commands
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (command.getName().equalsIgnoreCase("wwc"))
		{
			//Me fucking with adventure for the first time, cool
			final TextComponent versionNotice = Component.text()
			  .append(pluginPrefix.asComponent())
			  .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcVersion")).color(NamedTextColor.RED))
			  .append(Component.text().content(pluginVersion + "").color(NamedTextColor.LIGHT_PURPLE))
			.build();
  
			sender.sendMessage((versionNotice));
		}
		else if (command.getName().equalsIgnoreCase("wwcg"))
		{
		    //TODO: Global Translation
			if (checkSenderIdentity(sender))
			{
			    WWCGlobal wwcg = new WWCGlobal(sender, command, label, args, this);
			    return wwcg.processCommand();
			}
		}
		else if (command.getName().equalsIgnoreCase("wwct"))
		{
			//Translate to a specific language for one player
			if (checkSenderIdentity(sender))
			{
				WWCTranslate wwct = new WWCTranslate(sender, command, label, args, this);
				return wwct.processCommand(false);
			}
		}
		return true;
	}
	
	/* Setters */
	public void addActiveTranslator(WWCActiveTranslator i)
	{
		activeTranslators.add(i);
	}
	
	public void removeActiveTranslator(WWCActiveTranslator i)
	{
		for (Iterator<WWCActiveTranslator> aList = activeTranslators.iterator(); aList.hasNext(); )
		{
			WWCActiveTranslator activeTranslators = aList.next();
			if (activeTranslators == i)
			{
				aList.remove();
			}
		}
	}
	
	/* Getters */
	public WWCActiveTranslator getActiveTranslator(String uuid)
	{
		if (activeTranslators.size() > 0) //just return false if there are no active translators, less code to run
		{
			for (WWCActiveTranslator eaTranslator : activeTranslators)
			{
				if (eaTranslator.getUUID().equals(uuid)) //if uuid matches up with one in ArrayList, we chillin'
				{
					return eaTranslator;
				}
			}
		}
		return null;
	}
	
	public ArrayList<WWCActiveTranslator> getActiveTranslators()
	{
		return activeTranslators;
	}
	
	public TextComponent getPluginPrefix()
	{
		return pluginPrefix;
	}
	
	public String getPluginLang()
	{
		return pluginLang;
	}
	
	public WWCConfigurationHandler getConfigManager()
	{
		return configurationManager;
	}
	
	/* Common Methods */
	public boolean checkSenderIdentity(CommandSender sender)
	{
		if (!(sender instanceof Player))
		{
			final TextComponent consoleNotice = Component.text()
					  .append(pluginPrefix.asComponent())
					  .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcNoConsole")).color(NamedTextColor.RED))
					  .build();
			sender.sendMessage(consoleNotice);
			return false;
		}
		return true;
	}
}
