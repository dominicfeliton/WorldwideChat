package com.badskater0729.worldwidechat.commands;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.badskater0729.worldwidechat.util.ActiveTranslator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.wwcstatsgui.WWCStatsGuiMainMenu;
import com.badskater0729.worldwidechat.util.PlayerRecord;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.runAsync;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendTimeoutExceptionMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.runSync;

public class WWCStats extends BasicCommand {

	private WorldwideChat main = WorldwideChat.instance;
	
	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

	public WWCStats(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	@Override
	public boolean processCommand() {
		/* Sanitize args */
		if (args.length > 1) {
			// Not enough/too many args
			final TextComponent invalidArgs = Component.text()
							.content(getMsg("wwctInvalidArgs"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg(sender, invalidArgs);
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
						final TextComponent playerNotFound = Component
										.text().content(getMsg("wwcPlayerNotFound", new String[] {args[0]}))
										.color(NamedTextColor.RED)
								.build();
						/* Don't run API against invalid long names */
						if (inName.length() > 16 || inName.length() < 3) {
							sendMsg(sender, playerNotFound);
							return null;
						}
						inPlayer = Bukkit.getPlayer(inName);
						if (inPlayer == null) {
							inPlayer = Bukkit.getOfflinePlayer(inName);
						}
						/* getOfflinePlayer always returns a player, so we must check if this player has played on this server */
						if (!inPlayer.hasPlayedBefore()) {
							// Target player not found
							sendMsg(sender, playerNotFound);
							return null;
						}
					}
					
					/* Process stats of target */
					// TODO::: Show stats of ActiveTranslator as well
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
								WWCStatsGuiMainMenu.getStatsMainMenu(targetUUID, inName).open((Player)sender);
							}
						};
						runSync(out);
					} else {
						PlayerRecord record = main
								.getPlayerRecord(inPlayer.getUniqueId().toString(), false);
						TextComponent stats = Component.text()
										.content(getMsg("wwcsTitle", new String[] {inPlayer.getName()}))
										.color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true)
								.append(Component.text()
										.content("\n- " + getMsg("wwcsAttemptedTranslations", new String[] {record.getAttemptedTranslations() + ""}))
										.color(NamedTextColor.AQUA))
								.append(Component.text()
										.content("\n- " + getMsg("wwcsSuccessfulTranslations", new String[] {record.getSuccessfulTranslations() + ""}))
										.color(NamedTextColor.AQUA))
								.append(Component.text()
										.content("\n- " + getMsg("wwcsLastTranslationTime", new String[] {record.getLastTranslationTime()}))
										.color(NamedTextColor.AQUA))
								.build();
						// Add current translator stats if user is ActiveTranslator
						TextComponent isActiveTranslator = Component.text()
										.content("\n- " + getMsg("wwcsIsActiveTranslator", new String[] {ChatColor.BOLD + "" + ChatColor.RED + "\u2717"}))
										.color(NamedTextColor.AQUA)
								.build();
						if (main.isActiveTranslator(inPlayer.getUniqueId())) {
							// Is currently an active translator
							// Therefore, append active translator stats
							ActiveTranslator currTrans = main.getActiveTranslator(inPlayer.getUniqueId());
							isActiveTranslator = Component.text()
											.content("\n- " + getMsg("wwcsIsActiveTranslator", new String[] {ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713"}))
											.color(NamedTextColor.AQUA)
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransUUID", new String[] {currTrans.getUUID()}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransRateLimit", new String[] {currTrans.getRateLimit() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransInLang", new String[] {currTrans.getInLangCode()}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransOutLang", new String[] {currTrans.getOutLangCode()}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransOutgoing", new String[] {currTrans.getTranslatingChatOutgoing() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransIncoming", new String[] {currTrans.getTranslatingChatIncoming() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransBook", new String[] {currTrans.getTranslatingBook() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransSign", new String[] {currTrans.getTranslatingSign() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransItem", new String[] {currTrans.getTranslatingItem() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.append(Component.text()
											.content("\n  - " + getMsg("wwcsActiveTransEntity", new String[] {currTrans.getTranslatingEntity() + ""}))
											.color(NamedTextColor.LIGHT_PURPLE))
									.build();
							// If debug, append extra vars
							if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
								TextComponent debugInfo = Component.text()
												.content("\n  - " + getMsg("wwcsActiveTransColorWarning", new String[] {currTrans.getCCWarning() + ""}))
												.color(NamedTextColor.LIGHT_PURPLE)
										.append(Component.text()
												.content("\n  - " + getMsg("wwcsActiveTransSaved", new String[] {currTrans.getHasBeenSaved() + ""}))
												.color(NamedTextColor.LIGHT_PURPLE))
										.append(Component.text()
												.content("\n  - " + getMsg("wwcsActiveTransPrevRate", new String[] {currTrans.getRateLimitPreviousTime()}))
												.color(NamedTextColor.LIGHT_PURPLE))
										.build();
								isActiveTranslator = isActiveTranslator.append(debugInfo);
							}
						}
						stats = stats.append(isActiveTranslator);
						sendMsg(sender, stats);
					}
					return null;
				};
				
				/* Start Callback Process */
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Future<?> process = executor.submit(result);
				try {
					/* Get translation */
					 process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
				} catch (TimeoutException | ExecutionException | InterruptedException e) {
					if (e instanceof TimeoutException) {sendTimeoutExceptionMsg(sender);}
					process.cancel(true);
					this.cancel();
				} finally {
					executor.shutdownNow();
				}
			}
		};
		runAsync(translatorMessage);
	}
	
	private boolean noRecordsMessage(String name) {
		final TextComponent playerNotFound = Component.text() // No records found
						.content(getMsg("wwcsNotATranslator", new String[] {name}))
						.color(NamedTextColor.RED)
				.build();
		sendMsg(sender, playerNotFound);
		return true;
	}

}
