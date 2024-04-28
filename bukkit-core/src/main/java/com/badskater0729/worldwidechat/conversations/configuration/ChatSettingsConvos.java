package com.badskater0729.worldwidechat.conversations.configuration;

import com.badskater0729.worldwidechat.util.PlayerRecord;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;

import fr.minuskube.inv.SmartInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class ChatSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ModifyOverrideText extends StringPrompt {
		private SmartInventory previousInventory;
		
		private String currentOverrideName;

		private String inLang;
		
		public ModifyOverrideText(SmartInventory previousInventory, String currentOverrideName, String inLang) {
			this.previousInventory = previousInventory;
			this.currentOverrideName = currentOverrideName;
			this.inLang = inLang;
		}
		
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationOverrideTextChange", currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();

			if (!input.equals("0")) {
				Player currPlayer = ((Player) context.getForWhom());
				YamlConfiguration msgConfig = main.getConfigManager().getCustomMessagesConfig(inLang);

				msgConfig.set("Overrides." + currentOverrideName, input);
				main.addPlayerUsingConfigurationGUI(currPlayer.getUniqueId());
				final TextComponent successfulChange = Component.text()
								.content(refs.getMsg("wwcConfigConversationOverrideTextChangeSuccess", currPlayer))
								.color(NamedTextColor.GREEN)
						.build();
				refs.sendMsg((Player)context.getForWhom(), successfulChange);
				// TODO: Should we make a save button eventually?
				main.getConfigManager().saveMessagesConfig(inLang, true);
			}
			previousInventory.open((Player)context.getForWhom());
			return END_OF_CONVERSATION;
		}
	}
	
}
