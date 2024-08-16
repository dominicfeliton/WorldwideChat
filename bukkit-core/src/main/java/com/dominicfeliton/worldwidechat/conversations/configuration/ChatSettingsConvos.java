package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import fr.minuskube.inv.SmartInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class ChatSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;

	private static WWCInventoryManager invMan = new WWCInventoryManager();

	public static class ChannelIcon extends StringPrompt {
		private CommonRefs refs = main.getServerFactory().getCommonRefs();

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.serial(refs.getFancyMsg("wwcConfigConversationChannelIconInput",
					new String[] {"&r" + main.getConfigManager().getMainConfig().getString("Chat.separateChatChannel.icon")},
					"&b",
					currPlayer));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player currPlayer = ((Player) context.getForWhom());
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationChannelIconSuccess",
					"Chat.separateChatChannel.icon", input, MenuGui.CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.smartInv);
		}
	}

	public static class ChannelFormat extends StringPrompt {
		private CommonRefs refs = main.getServerFactory().getCommonRefs();
		private String vars = "{prefix}, {username}, {suffix}";

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.serial(refs.getFancyMsg("wwcConfigConversationChannelFormatInput",
					new String[] {"&r" + main.getConfigManager().getMainConfig().getString("Chat.separateChatChannel.format"),
							vars},
					"&b",
					currPlayer));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player currPlayer = ((Player) context.getForWhom());

			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationChatFormatSuccess",
					"Chat.separateChatChannel.format", input, MenuGui.CONFIG_GUI_TAGS.CHAT_CHANNEL_SET.smartInv);
		}
	}

	public static class ModifyChatPriority extends StringPrompt {
		private CommonRefs refs = main.getServerFactory().getCommonRefs();

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.serial(refs.getFancyMsg("wwcConfigConversationChatPriorityInput",
					new String[] {"&6" + main.getConfigManager().getMainConfig().getString("Chat.chatListenerPriority"),
					"&6"+Arrays.toString(EventPriority.values())},
					"&b",
					currPlayer));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player currPlayer = ((Player) context.getForWhom());
			boolean valid = false;
			for (EventPriority eaPriority : EventPriority.values()) {
				if (eaPriority.name().equalsIgnoreCase(input) || input.equals("0")) {
					valid = true;
				}
			}

			if (valid) {
				return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationChatPrioritySuccess",
						"Chat.chatListenerPriority", input, MenuGui.CONFIG_GUI_TAGS.CHAT_SET.smartInv);
			}
			refs.sendFancyMsg("wwcConfigConversationChatPriorityBadInput",
					new String[] {"&6"+Arrays.toString(EventPriority.values())}, "&c", currPlayer);
			return this;
		}
	}

	public static class AddBlacklistTerm extends StringPrompt {
		private SmartInventory previousInventory;
		private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

		public AddBlacklistTerm(SmartInventory previousInventory) {
			this.previousInventory = previousInventory;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationAddBlacklist", currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			// TODO: Make sure async call is safe on this and MessagesOverride...
			CommonRefs refs = main.getServerFactory().getCommonRefs();

			BukkitRunnable run = new BukkitRunnable() {
				@Override
				public void run() {
					if (!input.equals("0")) {
						YamlConfiguration config = main.getConfigManager().getBlacklistConfig();
						Player currPlayer = ((Player) context.getForWhom());
						Set<String> bannedWords = main.getBlacklistTerms();
						bannedWords.add(input); // Add currentTerm to the list
						config.set("bannedWords", new ArrayList<String>(bannedWords)); // Save the updated list back to the config

						main.addPlayerUsingConfigurationGUI(currPlayer.getUniqueId());
						final TextComponent successfulChange = Component.text()
								.content(refs.getMsg("wwcConfigConversationBlacklistAddSuccess", currPlayer))
								.color(NamedTextColor.GREEN)
								.build();
						refs.sendMsg((Player)context.getForWhom(), successfulChange);

						main.getConfigManager().saveCustomConfig(config, main.getConfigManager().getBlacklistFile(), false);
						BukkitRunnable open = new BukkitRunnable() {
							@Override
							public void run() {
								previousInventory.open((Player)context.getForWhom());
							}
						};
						wwcHelper.runSync(open, ENTITY, new Object[] {(Player) context.getForWhom()});
					}
				}
			};
			wwcHelper.runAsync(run, ASYNC, null);
			return END_OF_CONVERSATION;
		}
	}

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

				main.getConfigManager().saveMessagesConfig(inLang, true);
			}
			previousInventory.open((Player)context.getForWhom());
			return END_OF_CONVERSATION;
		}
	}
	
}
