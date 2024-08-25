package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.inventory.wwcstatsgui.WWCStatsGuiMainMenu;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.*;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;

public class WWCStats extends BasicCommand {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

	public WWCStats(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	@Override
	public boolean processCommand() {
		/* Sanitize args */
		if (args.length > 1) {
			// Not enough/too many args
			refs.sendMsg("wwctInvalidArgs", "", "&c", sender);
		}

		/* Get Sender Stats */
		if (args.length == 0) {
			if (isConsoleSender) {
				return noRecordsMessage("Console");
			}
			translatorMessage(sender.getName());
			return true;
		}

		/* Get Target Stats */
		if (args.length == 1) {
			translatorMessage(args[0]);
			return true;
		}
		return false;
	}

	private void translatorMessage(String inName) {
		BukkitRunnable translatorMessage = new BukkitRunnable() {
			@Override
			public void run() {
				Callable<?> result = () -> {
					/* Get OfflinePlayer, this will allow us to get stats even if target is offline */
					OfflinePlayer inPlayer;
					if (sender.getName().equals(inName)) {
						inPlayer = (Player)sender;
					} else {
						/* Don't run API against invalid long names */
						if (inName.length() > 16 || inName.length() < 3) {
							refs.sendMsg("wwcPlayerNotFound", "&6" + args[0], "&c", sender);
							return null;
						}
						inPlayer = Bukkit.getPlayer(inName);
						if (inPlayer == null) {
							inPlayer = Bukkit.getOfflinePlayer(inName);
						}
						/* getOfflinePlayer always returns a player, so we must check if this player has played on this server */
						if (!inPlayer.hasPlayedBefore()) {
							// Target player not found
							refs.sendMsg("wwcPlayerNotFound", "&6" + args[0], "&c", sender);
							return null;
						}
					}

					/* Process stats of target */
					// Check if PlayerRecord is valid
					if (!main.isPlayerRecord(inPlayer.getUniqueId())) {
						noRecordsMessage(inPlayer.getName());
						return null;
					}
					// Is on record; continue
					if (sender instanceof Player) {
						final String targetUUID = inPlayer.getUniqueId().toString();
						BukkitRunnable out = new BukkitRunnable() {
							@Override
							public void run() {
								new WWCStatsGuiMainMenu(targetUUID, (Player)sender).getStatsMainMenu().open((Player)sender);
							}
						};

						wwcHelper.runSync(out, ENTITY, new Object[] {(Player)sender});
					} else {
						PlayerRecord record = main
								.getPlayerRecord(inPlayer.getUniqueId().toString(), false);
						TextComponent stats = Component.text()
								.content(refs.getPlainMsg("wwcsTitle", inPlayer.getName(), sender))
								.color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true)
								.append(Component.text()
										.content("\n- " + refs.getPlainMsg("wwcsAttemptedTranslations", record.getAttemptedTranslations() + "", sender))
										.color(NamedTextColor.AQUA))
								.append(Component.text()
										.content("\n- " + refs.getPlainMsg("wwcsSuccessfulTranslations", record.getSuccessfulTranslations() + "", sender))
										.color(NamedTextColor.AQUA))
								.append(Component.text()
										.content("\n- " + refs.getPlainMsg("wwcsLocalization", record.getLocalizationCode().isEmpty() ? refs.checkOrX(false) : record.getLocalizationCode(), sender))
										.color(NamedTextColor.AQUA))
								.append(Component.text()
										.content("\n- " + refs.getPlainMsg("wwcsLastTranslationTime", record.getLastTranslationTime(), sender))
										.color(NamedTextColor.AQUA))
								.build();
						// Add current translator stats if user is ActiveTranslator
						TextComponent isActiveTranslator = Component.text()
								.content("\n- " + refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(false), sender))
								.color(NamedTextColor.AQUA)
								.build();
						if (main.isActiveTranslator(inPlayer.getUniqueId())) {
							// Is currently an active translator
							// Therefore, append active translator stats
							ActiveTranslator currTrans = main.getActiveTranslator(inPlayer.getUniqueId());
							isActiveTranslator = Component.text()
									.content("\n- " + refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(true), sender))
									.color(NamedTextColor.AQUA)
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransUUID", ChatColor.GOLD + currTrans.getUUID(), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransRateLimit", ChatColor.GOLD + "" + currTrans.getRateLimit(), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransInLang", ChatColor.GOLD + currTrans.getInLangCode(), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransOutLang", ChatColor.GOLD + currTrans.getOutLangCode(), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransOutgoing", refs.checkOrX(currTrans.getTranslatingChatOutgoing()), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransIncoming", refs.checkOrX(currTrans.getTranslatingChatIncoming()), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransBook", refs.checkOrX(currTrans.getTranslatingBook()), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransSign", refs.checkOrX(currTrans.getTranslatingSign()), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransItem", refs.checkOrX(currTrans.getTranslatingItem()), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + refs.getPlainMsg("wwcsActiveTransEntity", refs.checkOrX(currTrans.getTranslatingEntity()), sender))
											.color(NamedTextColor.LIGHT_PURPLE))
									.build();
							// If debug, append extra vars
							if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
								TextComponent debugInfo = Component.text()
										.content("\n  - " + refs.getPlainMsg("wwcsActiveTransColorWarning", refs.checkOrX(currTrans.getCCWarning()), sender))
										.color(NamedTextColor.LIGHT_PURPLE)
										.append(Component.text()
												.content("\n  - " + refs.getPlainMsg("wwcsActiveTransSignWarning", refs.checkOrX(currTrans.getSignWarning()), sender))
												.color(NamedTextColor.LIGHT_PURPLE))
										.append(Component.text()
												.content("\n  - " + refs.getPlainMsg("wwcsActiveTransSaved", refs.checkOrX(currTrans.getHasBeenSaved()), sender))
												.color(NamedTextColor.LIGHT_PURPLE))
										.append(Component.text()
												.content("\n  - " + refs.getPlainMsg("wwcsActiveTransPrevRate", ChatColor.GOLD + currTrans.getRateLimitPreviousTime(), sender))
												.color(NamedTextColor.LIGHT_PURPLE))
										.build();
								isActiveTranslator = isActiveTranslator.append(debugInfo);
							}
						}
						stats = stats.append(isActiveTranslator);
						refs.sendMsg(sender, stats);
					}
					return null;
				};

				/* Start Callback Process */
				Future<?> process = main.getCallbackExecutor().submit(result);
				try {
					/* Get translation */
					process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
				} catch (TimeoutException | ExecutionException | InterruptedException e) {
					if (e instanceof TimeoutException) {refs.sendTimeoutExceptionMsg(sender);}
				}
			}
		};
		wwcHelper.runAsync(translatorMessage, GLOBAL, null);
	}

	private boolean noRecordsMessage(String name) {
		refs.sendMsg("wwcsNotATranslator", "&6" + name, "&c", sender);
		return true;
	}
}