package com.expl0itz.worldwidechat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.WWCActiveTranslator;
import com.expl0itz.worldwidechat.watson.WWCWatson;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class WWCChatListener implements Listener {
	
	private WorldwideChat main;
	
	public WWCChatListener(WorldwideChat mainInstance)
	{
		main = mainInstance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if (main.isActiveTranslator(event.getPlayer().getUniqueId().toString()) instanceof WWCActiveTranslator)
		{
			WWCActiveTranslator currPlayer = main.isActiveTranslator(event.getPlayer().getUniqueId().toString());
		    //if watson, if google translate, if bing, etc (TODO)
			//if global (TODO)
			if (currPlayer.getTranslator().equals("Watson"))
			{
			   WWCWatson watsonInstance = new WWCWatson(main);
			   event.setMessage(watsonInstance.translateWithAPIKey(event.getMessage(), currPlayer.getInLangCode(), currPlayer.getOutLangCode(), "B8z5o9LApcXCItcC90Yh8w2M7__eVwrnRx-zYHVFRGft", "https://api.us-south.language-translator.watson.cloud.ibm.com"));
			}
		}
	} 
	
}
