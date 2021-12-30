package com.expl0itz.worldwidechat.commands;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.PlayerRecord;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.inventory.wwcstatsgui.WWCStatsGUIMainMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class WWCStats extends BasicCommand {

	private WorldwideChat main = WorldwideChat.instance;
	
	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

	public WWCStats(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	public boolean processCommand() {
		/* Sanitize args */
		if (args.length > 1) {
			// Not enough/too many args
			final TextComponent invalidArgs = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctInvalidArgs"))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, invalidArgs);
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
		if (!CommonDefinitions.serverIsStopping()) {
			Callable<?> result = () -> {
				new BukkitRunnable() {
					@Override
					public void run() {
						/* Get OfflinePlayer, this will allow us to get stats even if target is offline */
						OfflinePlayer inPlayer = null;
						if (sender.getName().equals(inName)) {
							inPlayer = (Player)sender;
						} else {
							//TODO: Add callback timeout to this operation, so that it does not stall the main thread on reload or stop
							inPlayer = Bukkit.getOfflinePlayer(inName);
							if (!inPlayer.hasPlayedBefore()) {
								// Target player not found
								final TextComponent playerNotFound = Component.text()
										.append(Component
												.text().content(CommonDefinitions.getMessage("wwcPlayerNotFound", new String[] {args[0]}))
												.color(NamedTextColor.RED))
										.build();
								CommonDefinitions.sendMessage(sender, playerNotFound);
								this.cancel();
								return;
							}
						}
						
						/* Process stats of target */
						if (!main.getPlayerRecord(inPlayer.getUniqueId().toString(), false).getUUID().equals("")) {
							// Is on record; continue
							if (sender instanceof Player) {
								final String targetUUID = inPlayer.getUniqueId().toString();
								if (!CommonDefinitions.serverIsStopping()) {
									new BukkitRunnable() {
										@Override
										public void run() {
											WWCStatsGUIMainMenu.getStatsMainMenu(targetUUID, inName).open((Player)sender);
										}
									}.runTask(main);
								}
							} else {
								String isActiveTranslator = ChatColor.BOLD + "" + ChatColor.RED + "\u2717";
								PlayerRecord record = main
										.getPlayerRecord(inPlayer.getUniqueId().toString(), false);
								if (!main.getActiveTranslator(inPlayer.getUniqueId().toString()).getUUID().equals("")) {
									// Is currently an active translator
									isActiveTranslator = ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713";
								}
								final TextComponent stats = Component.text()
										.append(Component.text()
												.content(CommonDefinitions.getMessage("wwcsTitle", new String[] {inPlayer.getName()}))
												.color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
										.append(Component.text()
												.content("\n- " + CommonDefinitions.getMessage("wwcsIsActiveTranslator", new String[] {isActiveTranslator}))
												.color(NamedTextColor.AQUA))
										.append(Component.text()
												.content("\n- " + CommonDefinitions.getMessage("wwcsAttemptedTranslations", new String[] {record.getAttemptedTranslations() + ""}))
												.color(NamedTextColor.AQUA))
										.append(Component.text()
												.content("\n- " + CommonDefinitions.getMessage("wwcsSuccessfulTranslations", new String[] {record.getSuccessfulTranslations() + ""}))
												.color(NamedTextColor.AQUA))
										.append(Component.text()
												.content("\n- " + CommonDefinitions.getMessage("wwcsLastTranslationTime", new String[] {record.getLastTranslationTime()}))
												.color(NamedTextColor.AQUA))
										.build();
								CommonDefinitions.sendMessage(sender, stats);
							}
						} else {
							noRecordsMessage(inPlayer.getName());
						}
					}
				}.runTaskAsynchronously(main);
				return null;
			};
			
			/* Start Callback Process */
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<?> process = executor.submit(result);
			try {
				/* Get translation */
				 process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
			} catch (TimeoutException | ExecutionException | InterruptedException e) {
				CommonDefinitions.sendDebugMessage("/wwcs Timeout!! Either we are reloading or we have lost connection. Abort.");
				//CommonDefinitions.sendDebugMessage("Exact error: " + ExceptionUtils.getStackTrace(e.getCause()));
				//TODO: If this is a TimeoutException, print out a warning if the server is not stopping?
				process.cancel(true);
			} finally {
				executor.shutdownNow();
			}
		}
	}
	
	private boolean noRecordsMessage(String name) {
		final TextComponent playerNotFound = Component.text() // No records found
				.append(Component
						.text().content(CommonDefinitions.getMessage("wwcsNotATranslator", new String[] {name}))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(sender, playerNotFound);
		return true;
	}

}
