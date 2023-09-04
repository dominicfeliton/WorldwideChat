package com.badskater0729.worldwidechat.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.zone.ZoneRulesException;

import com.amazonaws.util.StringUtils;
import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.translators.AmazonTranslation;
import com.badskater0729.worldwidechat.translators.DeepLTranslation;
import com.badskater0729.worldwidechat.translators.GoogleTranslation;
import com.badskater0729.worldwidechat.translators.LibreTranslation;
import com.badskater0729.worldwidechat.translators.TestTranslation;
import com.badskater0729.worldwidechat.translators.WatsonTranslation;
import com.google.common.base.CharMatcher;

import fr.minuskube.inv.SmartInventory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class CommonRefs {

	/* Important vars */
	private static WorldwideChat main = WorldwideChat.instance;
	
	public static String[] supportedMCVersions = { "1.20", "1.19", "1.18", "1.17", "1.16", "1.15", "1.14", "1.13" };

	public static String[] supportedPluginLangCodes = { "af", "sq", "am", "ar", "hy", "az", "bn", "bs", "bg", "ca",
			"zh", "zh-TW", "hr", "cs", "da", "fa-AF", "ga", "nl", "en", "et", "fa", "tl", "fi", "fr", "fr-CA", "ka", "de",
			"el", "gu", "ht", "ha", "he", "hi", "hu", "is", "id", "it", "ja", "kn", "kk", "ko", "lv", "lt", "mk", "mr", "ms",
			"ml", "mt", "mn", "no", "fa", "pa", "ps", "pl", "pt", "pt-PT", "ro", "ru", "sr", "si", "sk", "sl", "so", "es", "es-MX",
			"sw", "sv", "tl", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "cy" };
	
	/* Getters */
	public static void runAsync(BukkitRunnable in) {
		runAsync(true, in);
	}
	
	public static void runAsync(boolean serverMustBeRunning, BukkitRunnable in) {
		runAsync(serverMustBeRunning, 0, in);
	}
	
	public static void runAsync(boolean serverMustBeRunning, int delay, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!CommonRefs.serverIsStopping()) {
				in.runTaskLaterAsynchronously(main, delay);
			}
		} else {
			in.runTaskLaterAsynchronously(main, delay);
		}
	}
	
	public static void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!CommonRefs.serverIsStopping()) {
				in.runTaskTimerAsynchronously(main, delay, repeatTime);
			}
		} else {
			in.runTaskTimerAsynchronously(main, delay, repeatTime);
		}
	}
	
	public static void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, BukkitRunnable in) {
		runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in);
	}
	
	public static void runSync(BukkitRunnable in) {
		runSync(true, in);
	}
	
	public static void runSync(boolean serverMustBeRunning, BukkitRunnable in) {
		runSync(serverMustBeRunning, 0, in);
	}
	
	public static void runSync(boolean serverMustBeRunning, int delay, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!CommonRefs.serverIsStopping()) {
				in.runTaskLater(main, delay);
			}
		} else {
			in.runTaskLater(main, delay);
		}
	}
	
	public static void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!CommonRefs.serverIsStopping()) {
				in.runTaskTimer(main, delay, repeatTime);
			}
		} else {
			in.runTaskTimer(main, delay, repeatTime);
		}
	}
	
	public static void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, BukkitRunnable in) {
		runSyncRepeating(serverMustBeRunning, 0, repeatTime, in);
	}
	
	/**
	  * Compares two strings to check if they are the same language under the current translator.
	  * @param first - A valid language name
	  * @param second - A valid language name
	  * @param langType - 'out' or 'in' are two valid inputs for this;
	  * 'out' will check if in is a valid output lang, 'in' will check the input lang list
	  * 'all' will check both lists
	  * @return Boolean - Whether languages are the same or not
	  */
	public static boolean isSameTranslatorLang(String first, String second, String langType) {
		return isSupportedTranslatorLang(first, langType) && isSupportedTranslatorLang(second, langType) 
				&& getSupportedTranslatorLang(first, langType).getLangCode().equals(getSupportedTranslatorLang(second, langType).getLangCode());
	}

	/**
	  * Gets a supported language under the current translator.
	  * @param langName - A valid language name
	  * @param langType - 'out' or 'in' are two valid inputs for this;
	  * 'out' will check if in is a valid output lang, 'in' will check the input lang list
	  * 'all' will check both lists
	  * @return SupportedLanguageObject - Will be completely empty if the language is invalid
	  */
	public static SupportedLang getSupportedTranslatorLang(String langName, String langType) {
		/* Setup vars */
		List<SupportedLang> langList = new ArrayList<SupportedLang>();
		SupportedLang invalidLang = new SupportedLang("","","");
		
		/* Check langType */
		if (langType.equalsIgnoreCase("in")) {
			langList.addAll(main.getSupportedInputLangs());
		} else if (langType.equalsIgnoreCase("out")) {
			langList.addAll(main.getSupportedOutputLangs());
		} else if (langType.equalsIgnoreCase("all")) { 
			langList.addAll(main.getSupportedInputLangs());
			langList.addAll(main.getSupportedOutputLangs());
		} else {
			debugMsg("Invalid langType for getSupportedTranslatorLang()! langType: " + langType + " ...returning invalid, not checking language. Fix this!!!");
		    return invalidLang;
		}
		
		/* Check selected list for lang */
		for (SupportedLang eaLang : langList) {
			if ((eaLang.getLangCode().equalsIgnoreCase(langName) || eaLang.getLangName().equalsIgnoreCase(langName))) {
				return eaLang;
			}
		}
		
		/* Return invalid if nothing is found */
		return invalidLang;
	}
	
	/**
	 * Checks if a language is supported under the current translator.
	 * @param in - A valid language name
     * @param langType - 'out' or 'in' are two valid inputs for this;
	 * 'out' will check if in is a valid output lang, 'in' will check the input lang list
	 * 'all' will check both lists
	 * @return true if supported, false otherwise
	 */
	public static boolean isSupportedTranslatorLang(String in, String langType) {
		return !getSupportedTranslatorLang(in, langType).getLangCode().equals("");
	}

	/**
	  * Gets a list of properly formatted, supported language codes.
	  * @param langType - 'out' or 'in' are two valid inputs for this;
	  * 'out' will check if in is a valid output lang, 'in' will check the input lang list
	  * @return String - Formatted language codes
	  * 
	  */
	public static String getFormattedValidLangCodes(String langType) {
		/* Setup vars */
		List<SupportedLang> langList;
		String out = "\n";
		
		/* Check langType */
		if (langType.equalsIgnoreCase("in")) {
			langList = main.getSupportedInputLangs();
		} else if (langType.equalsIgnoreCase("out")) {
			langList = main.getSupportedOutputLangs();
		} else {
			debugMsg("Invalid langType for getFormattedValidLangCodes()! langType: " + langType + " ...returning invalid, not checking language. Fix this!!!");
		    return out;
		}
		
		for (SupportedLang eaLang : langList) {
			out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), ";
		}
		if (out.indexOf(",") != -1) {
			out = out.substring(0, out.lastIndexOf(","));
		}
		return out;
	}

	/**
	  * Closes all inventories registered by WorldwideChat.
	  */
	public static void closeAllInvs() {
		// Close all active GUIs
		main.getPlayersUsingGUI().clear();
		for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
			try {
				SmartInventory currInventory = main.getInventoryManager().getInventory(eaPlayer).get();
				if (currInventory instanceof SmartInventory
						&& currInventory.getManager()
								.equals(main.getInventoryManager())) {
					eaPlayer.closeInventory();
				}
			} catch (NoSuchElementException e) {
				continue;
			}
		}
	}

	/**
	  * Sends a debug message to console. Will only work when debug mode is set to true in the Console.
	  * @param inMessage - The debug message that will be sent to the Console.
	  */
	public static void debugMsg(String inMessage) {
		if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
			main.getLogger().warning("DEBUG: " + inMessage);
		}
	}
	
	/**
	 * Gets a message (with no replacements) from the currently selected messages-XX.yml.
	 * @param messageName - The name of the message from messages-XX.yml.
	 * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	 */
	public static String getMsg(String messageName) {
		return getMsg(messageName, new String[0]);
	}

	/**
	 * Gets a message from the currently selected messages-XX.yml.
	 * @param messageName - The name of the message from messages-XX.yml.
	 * @param replacement - The replacement value for the selected message.
	 * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	 */
	public static String getMsg(String messageName, String replacement) { return getMsg(messageName, new String[] {replacement}); }

	/**
	  * Gets a message from the currently selected messages-XX.yml.
	  * @param messageName - The name of the message from messages-XX.yml.
	  * @param replacements - The list of replacement values that replace variables in the selected message. There is no sorting system; the list must be already sorted.
	  * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	  */
	public static String getMsg(String messageName, String[] replacements) {
		/* Get message from messages.yml */
		String convertedOriginalMessage = "";
		YamlConfiguration messagesConfig = main.getConfigManager().getMsgsConfig();
		if (messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)) != null) {
			convertedOriginalMessage = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)));
		} else {
			convertedOriginalMessage = messagesConfig.getString("Messages." + ChatColor.stripColor(messageName));
			if (convertedOriginalMessage == null) {
				main.getLogger().severe("Bad message! Please fix your messages-XX.yml.");
				return ChatColor.RED + "Bad message! Please fix your messages-XX.yml.";
			}
		}
		
		/* Replace any and all %i, %e, %o, etc. */
		/* This code will only go as far as replacements[] goes. */
		for (int i = 0; i < replacements.length; i++) {
			convertedOriginalMessage = convertedOriginalMessage.replaceFirst("%[ioeu]", replacements[i]);
		}
		
 		/* Return fixedMessage */
		return convertedOriginalMessage;
	}
	
	/**
	  * Sends the user a properly formatted message through our adventure instance.
	  * @param sender - The target sender. Can be any entity that can receive messages.
	  * @param originalMessage - The unformatted TextComponent that should be sent to sender.
	  * @return
	  */
	public static void sendMsg(CommandSender sender, TextComponent originalMessage) {
		try {
			Audience adventureSender = main.adventure().sender(sender);
			final TextComponent outMessage = Component.text().append(main.getPluginPrefix().asComponent())
					.append(Component.text().content(" "))
					.append(originalMessage.asComponent())
					.build();
			if (sender instanceof ConsoleCommandSender) {
				main.getServer().getConsoleSender().sendMessage((ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(outMessage))));
			} else {
				adventureSender.sendMessage(outMessage);
			}
		} catch (IllegalStateException e) {}
	}
	
	/**
	  * Translates text using the selected translator.
	  * @param inMessage - The original message to be translated.
	  * @param currPlayer - The player who wants this message to be translated.
	  * @return String - The translated message. If this is equal to inMessage, the translation failed.
	  */
	public static String translateText(String inMessage, Player currPlayer) {
		/* If translator settings are invalid, do not do this... */
		if (!(inMessage.length() > 0) || CommonRefs.serverIsStopping()) {
			return inMessage;
		}
		
		/* Main logic callback */
		YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
		Callable<String> result = () -> {
			/* Detect color codes in message */
			detectColorCodes(inMessage, currPlayer);

			/* Modify or create new player record */
			PlayerRecord currPlayerRecord = main
					.getPlayerRecord(currPlayer.getUniqueId().toString(), true);
			if (main.getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations() + 1);

			/* Initialize current vars + ActiveTranslator, sanity checks */
			ActiveTranslator currActiveTranslator;
			if (!main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
					&& (main.isActiveTranslator(currPlayer))) {
				currActiveTranslator = main.getActiveTranslator(currPlayer.getUniqueId().toString());
			} else if (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
					&& (main.isActiveTranslator(currPlayer))) {
				currActiveTranslator = main.getActiveTranslator(currPlayer.getUniqueId().toString());
			} else {
				currActiveTranslator = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
			}

			/* Char limit check */
			int limit = mainConfig.getInt("Translator.messageCharLimit");
			if (inMessage.length() > limit) {
				final TextComponent charLimit = Component.text()
								.content(CommonRefs.getMsg("wwcCharLimit", new String[] {"" + limit}))
								.color(NamedTextColor.YELLOW)
						.build();
				CommonRefs.sendMsg(currPlayer, charLimit);
				return inMessage;
			}
			
			/* Check cache */
			CachedTranslation testTranslation = new CachedTranslation(currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), inMessage);
			String testCache = main.getCacheTerm(testTranslation);
			
			if (testCache != null) {
				currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
				currPlayerRecord.setLastTranslationTime();
				return StringEscapeUtils.unescapeJava(
						ChatColor.translateAlternateColorCodes('&', testCache));
			}

			// Init vars
			boolean isExempt = false;
			boolean hasPermission = false;
			int personalRateLimit = 0;
			String permissionCheck = "";

			// Get permission from Bukkit API synchronously, since we do not want to risk
			// concurrency problems
			if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") && !CommonRefs.serverIsStopping()) {
				try {
					permissionCheck = Bukkit.getScheduler().callSyncMethod(main, () -> {
						return checkForRateLimitPermissions(currPlayer);
					}).get(3, TimeUnit.SECONDS);
				} catch (TimeoutException | InterruptedException e) {
					CommonRefs.debugMsg("Timeout from rate limit permission check should never happen, unless the server is stopping or /reloading. "
							+ "If it isn't, and we can't fetch a user permission in less than ~2.5 seconds, we have a problem.");
					return inMessage;
				}
			} else if (main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
				// It is extremely unlikely that we run into concurrency issues with MockBukkit.
				// Until it supports callSyncMethod(), this will do.
				permissionCheck = checkForRateLimitPermissions(currPlayer);
			}

			// If exempt, set exempt to true; else, get the delay from the end of the
			// permission string
			if (permissionCheck.equalsIgnoreCase("worldwidechat.ratelimit.exempt")) {
				isExempt = true;
			} else {
				String delayStr = CharMatcher.inRange('0', '9').retainFrom(permissionCheck);
				if (!delayStr.isEmpty()) {
					personalRateLimit = Integer.parseInt(delayStr);
					hasPermission = true;
				}
			}

			// Get user's personal rate limit, if permission is not set and they are an
			// active translator.
			if (!isExempt && !hasPermission && main.isActiveTranslator(currPlayer)) {
				personalRateLimit = main
					.getActiveTranslator(currPlayer.getUniqueId().toString()).getRateLimit();
			}

			// Personal Limits (Override Global)
			if (!isExempt && personalRateLimit > 0) {
				if (!checkForRateLimits(personalRateLimit, currActiveTranslator, currPlayer)) {
					return inMessage;
				}
			// Global Limits
			} else if (!isExempt && mainConfig.getInt("Translator.rateLimit") > 0) {
				if (!checkForRateLimits(mainConfig.getInt("Translator.rateLimit"), currActiveTranslator, currPlayer)) {
					return inMessage;
				}
			}

			/* Begin actual translation, set message to output */
			String out = inMessage;
			CommonRefs.debugMsg("Translating a message (in " + currActiveTranslator.getInLangCode() + ") from " + currActiveTranslator.getUUID() + " to " + currActiveTranslator.getOutLangCode() + ".");
			switch (main.getTranslatorName()) {
			case "Watson":
				WatsonTranslation watsonInstance = new WatsonTranslation(inMessage,
						currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode());
				// Get username + pass from config
				out = watsonInstance.useTranslator();
				break;
			case "Google Translate":
				GoogleTranslation googleTranslateInstance = new GoogleTranslation(inMessage,
						currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode());
				out = googleTranslateInstance.useTranslator();
				break;
			case "Amazon Translate":
				AmazonTranslation amazonTranslateInstance = new AmazonTranslation(inMessage,
						currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode());
				out = amazonTranslateInstance.useTranslator();
				break;
			case "Libre Translate":
				LibreTranslation libreTranslateInstance = new LibreTranslation(inMessage,
						currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode());
				out = libreTranslateInstance.useTranslator();
				break;
			case "DeepL Translate":
				DeepLTranslation deeplTranslateInstance = new DeepLTranslation(inMessage,
						currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode());
			    out = deeplTranslateInstance.useTranslator();
			    break;
			case "JUnit/MockBukkit Testing Translator":
				TestTranslation testTranslator = new TestTranslation(inMessage, currActiveTranslator.getInLangCode(),
						currActiveTranslator.getOutLangCode());
				out = testTranslator.useTranslator();
				break;
			default:
				// Get here if we are adding a new translation service
				CommonRefs.debugMsg("No valid translator currently in use, according to translateText(). Returning original message to " + currPlayer.getName() + "...");
				return inMessage;
			}

			/* Update stats */
			currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
			currPlayerRecord.setLastTranslationTime();

			/* Add to cache */
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.addCacheTerm(testTranslation, out);
			}
			return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', out));
		};
		
		/* Start Callback Process */
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(result);
		String finalOut = inMessage;
		try {
			/* Get translation */
			finalOut = process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			/* Sanitize error before proceeding to write it to errorLog */
			if (e instanceof InterruptedException || main.getTranslatorName().equals("Starting")) {
				// If we are getting stopped by onDisable, end this immediately.
				CommonRefs.debugMsg("Interrupted translateText(), or server state is changing...");
				return inMessage;
			} else if (e instanceof ExecutionException && (e.getCause() != null) && isErrorToIgnore(e.getCause())) {
				// If the translator has low confidence
				CommonRefs.debugMsg("Low confidence from current translator!");
				return inMessage;
			} else if (e instanceof TimeoutException) {
				// If we get a timeoutexception
				sendTimeoutExceptionMsg(currPlayer);
				return inMessage;
			}
			
			/* Add 1 to error count */
			main.setTranslatorErrorCount(main.getTranslatorErrorCount() + 1);
			final TextComponent playerError = Component.text()
							.content(CommonRefs.getMsg("wwcTranslatorError"))
							.color(NamedTextColor.RED)
					.build();
			CommonRefs.sendMsg(currPlayer, playerError);
			main.getLogger()
					.severe(CommonRefs.getMsg("wwcTranslatorErrorConsole", new String[] {currPlayer.getName()}));
			CommonRefs.debugMsg(ExceptionUtils.getStackTrace(e));

			/* Write to log file */
			File errorLog = new File(main.getDataFolder(), "errorLog.txt");
			try {
				FileWriter fw = new FileWriter(errorLog, true);
				String dateStr = "";
				try {
				    debugMsg(ZoneId.systemDefault().toString());
				    LocalDate date = LocalDate.now();
				    LocalTime time = LocalTime.now();
				    dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - "))
						+ time.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
				} catch (ZoneRulesException noTZ) {
					// This error occurs when we can't fetch the local TZ for whatever reason.
					// If this error happens on all machines then it may be something I have to further investigate.
					// For now, only seems to happen on my macOS device.
					dateStr = "(Unable to fetch local TZ, so here's an ugly date) - " + Instant.now();
				}

				fw.write("========== " + dateStr + " ==========");
				fw.write(System.getProperty("line.separator"));
				fw.write(ExceptionUtils.getStackTrace(e));
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			CommonRefs.debugMsg("Error Count: " + main.getTranslatorErrorCount());
			
			/* If error count is greater than threshold set in config.yml, reload on this thread (we are already async) */
			if (main.getTranslatorErrorCount() >= mainConfig.getInt("Translator.errorLimit")) {
				main.getLogger().severe(CommonRefs.getMsg("wwcTranslatorErrorThresholdReached"));
				main.getLogger().severe(CommonRefs.getMsg("wwcTranslatorErrorThresholdReachedCheckLogs"));
				runSync(new BukkitRunnable() {
					@Override
					public void run() {
						main.reload();
					}
				});
			}
			process.cancel(true);
		} finally {
			executor.shutdownNow();
		}
		
		/* Return final result */
		return finalOut;
	}
	
	/**
	 * Sends a message that console cannot have a translation session for itself.
	 * @param sender - Who will receive the message
	 * @return Returns false, so that a command can return this method.
	 */
	public static boolean sendNoConsoleChatMsg(CommandSender sender) {
		final TextComponent noConsoleChat = Component.text() // Cannot translate console chat
						.content(CommonRefs.getMsg("wwctCannotTranslateConsole", new String[0]))
						.color(NamedTextColor.RED)
				.build();
		CommonRefs.sendMsg(sender, noConsoleChat);
		return false;
	}
	
	/**
	 * Sends a message to console and a sender that a timeout exception has occured.
	 * @param sender - Who will receive the message besides console.
	 * @return Returns true, so that a command can return this method.
	 */
	public static boolean sendTimeoutExceptionMsg(CommandSender sender) {
		if (sender instanceof Player) {
			main.getLogger().warning(CommonRefs.getMsg("wwcTimeoutExceptionConsole", new String[] {sender.getName()}));
		}
		final TextComponent timeoutException = Component.text()
						.content(CommonRefs.getMsg("wwcTimeoutException"))
						.color(NamedTextColor.YELLOW)
				.build();
		CommonRefs.sendMsg(sender, timeoutException);
		return true;
	}
	
	/**
	  * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
	  * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
	  * @return Boolean - Whether the server is reloading/stopping or not
	  */
	public static boolean serverIsStopping() {
		if (!main.isEnabled()) return true;
		try {
			new BukkitRunnable() {
				@Override
				public void run() {}
			}.runTask(main);
		} catch (Exception e) {
			CommonRefs.debugMsg("Server is stopping! Don't run a task/do any dumb shit.");
			return true;
		}
		return false;
	}
	
	/** 
	  * Returns the generic conversation for modifying values in our config.yml using the GUI.
	  * @param preCheck - The boolean that needs to be true for the change to proceed
	  * @param context - The conversation context obj
	  * @param successfulChangeMsg - Names of the message sent on successful change 
	  * @param configValName - The names of the config value to be updated
	  * @param configVal - The new value
	  * @param prevInventory - The previous inventory to open up after the conversation is over
	  * @return Prompt.END_OF_CONVERSATION - This will ultimately be returned to end the conversation. If the length of configValName != the length of configVal, then null is returned.
	  */
	public static Prompt genericConfigConvo(boolean preCheck, ConversationContext context, String successfulChangeMsg, String configValName[], Object[] configVal, SmartInventory prevInventory) {
		if (configValName.length != configVal.length) {
			return null;
		}
		if (preCheck) {
			for (int i = 0; i < configValName.length; i++) {
				main.getConfigManager().getMainConfig().set(configValName[i], configVal[i]);
			}
			main.addPlayerUsingConfigurationGUI(((Player)context.getForWhom()).getUniqueId());
			final TextComponent successfulChange = Component.text()
							.content(CommonRefs.getMsg(successfulChangeMsg))
							.color(NamedTextColor.GREEN)
					.build();
			CommonRefs.sendMsg((Player)context.getForWhom(), successfulChange);
		}
		/* Re-open previous GUI */
		prevInventory.open((Player)context.getForWhom());
		return Prompt.END_OF_CONVERSATION;
	}
	
	/** 
	  * Returns the generic conversation for modifying values in our config.yml using the GUI.
	  * @param preCheck - The boolean that needs to be true for the change to proceed
	  * @param context - The conversation context obj
	  * @param successfulChangeMsg - Name of the message sent on successful change 
	  * @param configValName - The name of the config value to be updated
	  * @param configVal - The new value
	  * @param prevInventory - The previous inventory to open up after the conversation is over
	  * @return Prompt.END_OF_CONVERSATION - This will ultimately be returned to end the conversation.
	  */
	public static Prompt genericConfigConvo(boolean preCheck, ConversationContext context, String successfulChangeMsg, String configValName, Object configVal, SmartInventory prevInventory) {
		return genericConfigConvo(preCheck, context, successfulChangeMsg, new String[] {configValName}, new Object[] {configVal}, prevInventory);
	}
	
	/**
	  * Checks if a provided exception is a no confidence one/one to be ignored from our target translator.
	  * @param throwable - The exception to be checked
	  * @return Boolean - If exception is no confidence, true; false otherwise
	  */
	private static boolean isErrorToIgnore(Throwable throwable) {
		//String[] lowConfidenceDict = {"confidence", "same as target", "detect the source language"};
		// same as target == Watson
		// detect the source language == Watson
		ArrayList<String> lowConfidenceDict = (ArrayList<String>) main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore");
		String exceptionMessage = StringUtils.lowerCase(throwable.getMessage());

		// This is a special character. If the user puts this character, then we ignore all errors.
		if (lowConfidenceDict.contains("*")) return true;

		// Check if the exception message contains any of the strings in our low confidence dictionary
		for (String eaStr : lowConfidenceDict) {
			if (exceptionMessage.contains(eaStr)) {
				debugMsg("Ignoring error thrown by translator: " + exceptionMessage);
				return true;
			}
		}
		return false;
	}
	
	/**
	  * Detects presence of color codes in a given string
	  * @param inMessage - Message to be checked
	  * @param currPlayer - Player that sent the message
	  */
	private static void detectColorCodes(String inMessage, Player currPlayer) {
		if ((inMessage.contains("&") && !main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"))
				&& !(main.getActiveTranslator(currPlayer.getUniqueId().toString())
						.getCCWarning())) // check if user has already been sent CC warning
		{
			final TextComponent watsonCCWarning = Component.text()
							.content(CommonRefs.getMsg("watsonColorCodeWarning"))
							.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true)
					.build();
			CommonRefs.sendMsg(currPlayer, watsonCCWarning);
			// Set got CC warning of current translator to true, so that they don't get
			// spammed by it if they keep using CCs
			main.getActiveTranslator(currPlayer.getUniqueId().toString())
					.setCCWarning(true);
			// we're still gonna translate it but it won't look pretty
		}
	}
	
	/**
	  * Ensures that an ActiveTranslator does not currently need to be rate limited.
	  * @param delay - Exact rate limit that is being looked for
	  * @param currActiveTranslator - The valid ActiveTranslator target
	  * @param sender - The sender of the original command
	  * @return Boolean - Returns false if the user should currently be rate limited, and true otherwise.
	  */
	private static boolean checkForRateLimits(int delay, ActiveTranslator currActiveTranslator, CommandSender sender) {
		if (!(currActiveTranslator.getRateLimitPreviousTime().equals("None"))) {
			Instant previous = Instant.parse(currActiveTranslator.getRateLimitPreviousTime());
			Instant currTime = Instant.now();
			if (currTime.compareTo(previous.plus(delay, ChronoUnit.SECONDS)) < 0) {
				final TextComponent rateLimit = Component.text()
						.content(CommonRefs.getMsg("wwcRateLimit", new String[] {"" + ChronoUnit.SECONDS.between(currTime,
								previous.plus(delay, ChronoUnit.SECONDS))}))
								.color(NamedTextColor.YELLOW)
						.build();
				CommonRefs.sendMsg(sender, rateLimit);
				return false;
			} else {
				currActiveTranslator.setRateLimitPreviousTime(Instant.now());
			}
		} else {
			currActiveTranslator.setRateLimitPreviousTime(Instant.now());
		}
		return true;
	}
	
	/**
	  * Ensures that a player does not have a rate limit permission.
	  * @param currPlayer - Player that is being checked.
	  * @return String - Returns an empty string if no permission was found, or the permission name if it is
	  */
	private static String checkForRateLimitPermissions(Player currPlayer) {
		Set<PermissionAttachmentInfo> perms = currPlayer.getEffectivePermissions();
		for (PermissionAttachmentInfo perm : perms) {
			if (perm.getPermission().startsWith("worldwidechat.ratelimit.")) {
				return perm.getPermission();
			}
		}
		return "";
	}
}