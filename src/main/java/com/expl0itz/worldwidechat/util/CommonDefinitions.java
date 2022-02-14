package com.expl0itz.worldwidechat.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import com.amazonaws.services.translate.model.InvalidRequestException;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.translators.AmazonTranslation;
import com.expl0itz.worldwidechat.translators.GoogleTranslation;
import com.expl0itz.worldwidechat.translators.TestTranslation;
import com.expl0itz.worldwidechat.translators.TranslatorFailException;
import com.expl0itz.worldwidechat.translators.WatsonTranslation;
import com.google.cloud.translate.TranslateException;
import com.google.common.base.CharMatcher;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;

import fr.minuskube.inv.SmartInventory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class CommonDefinitions {

	/* Important vars */
	private static WorldwideChat main = WorldwideChat.instance;
	
	public static String[] supportedMCVersions = { "1.18", "1.17", "1.16", "1.15", "1.14", "1.13", "1.12", "1.11", "1.10", "1.9", "1.8" };

	public static String[] supportedPluginLangCodes = { "af", "sq", "am", "ar", "hy", "az", "bn", "bs", "bg", "ca",
			"zh", "zh-TW", "hr", "cs", "da", "fa-AF", "ga", "nl", "en", "et", "fa", "tl", "fi", "fr", "fr-CA", "ka", "de",
			"el", "gu", "ht", "ha", "he", "hi", "hu", "is", "id", "it", "ja", "kn", "kk", "ko", "lv", "lt", "mk", "mr", "ms",
			"ml", "mt", "mn", "no", "fa", "pa", "ps", "pl", "pt", "pt-PT", "ro", "ru", "sr", "si", "sk", "sl", "so", "es", "es-MX",
			"sw", "sv", "tl", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "cy" };

	/* Getters */
	/**
	  * Compares two strings to check if they are the same language under the current translator.
	  * @param first - A valid language name
	  * @param second - A valid language name
	  * @return Boolean - Whether languages are the same or not
	  */
	public static boolean isSameLang(String first, String second) {
		return getSupportedTranslatorLang(first).compareTo(getSupportedTranslatorLang(second)) == 0 ? true : false;
	}

	/**
	  * Checks if string is a supported language under the current translator.
	  * @param in - A valid language name
	  * @return SupportedLanguageObject - Will be completely empty if the language is invalid
	  */
	public static SupportedLanguageObject getSupportedTranslatorLang(String in) {
		for (SupportedLanguageObject eaLang : main.getSupportedTranslatorLanguages()) {
			if ((eaLang.getLangCode().equalsIgnoreCase(in) || eaLang.getLangName().equalsIgnoreCase(in))) {
				return eaLang;
			}
		}
		return new SupportedLanguageObject("", "", "", false, false);
	}

	/**
	  * Gets a list of properly formatted, supported language codes.
	  * @return String - Formatted language codes
	  */
	public static String getFormattedValidLangCodes() {
		String out = "\n";
		for (SupportedLanguageObject eaLang : main.getSupportedTranslatorLanguages()) {
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
	public static void closeAllInventories() {
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
	public static void sendDebugMessage(String inMessage) {
		if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
			main.getLogger().warning("DEBUG: " + inMessage);
		}
	}
	
	/**
	 * Gets a message (with no replacements) from the currently selected messages-XX.yml.
	 * @param messageName - The name of the message from messages-XX.yml.
	 * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	 */
	public static String getMessage(String messageName) {
		return getMessage(messageName, new String[0]);
	}
	
	/**
	  * Gets a message from the currently selected messages-XX.yml.
	  * @param messageName - The name of the message from messages-XX.yml.
	  * @param replacements - The list of replacement values that replace variables in the selected message. There is no sorting system; the list must be already sorted.
	  * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	  */
	public static String getMessage(String messageName, String[] replacements) {
		/* Get message from messages.yml */
		String convertedOriginalMessage = "";
		YamlConfiguration messagesConfig = main.getConfigManager().getMessagesConfig();
		if (messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)) != null) {
			convertedOriginalMessage = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)));
		} else {
			convertedOriginalMessage = messagesConfig.getString("Messages." + ChatColor.stripColor(messageName));
			if (convertedOriginalMessage == null) {
				main.getLogger().severe("Bad message! Please fix your messages-" + messagesConfig.getString("General.pluginLang") + ".yml.");
				return ChatColor.RED + "Bad message! Please fix your messages-" + messagesConfig.getString("General.pluginLang") + ".yml.";
			}
		}
		
		/* Replace any and all %i, %e, %o, etc. */
		/* This code will only go as far as replacements[] goes. */
		//TODO: Replace with string.format
		StringBuilder fixedMessage = new StringBuilder();
		int replacementsPos = 0;
		for (int c = 0; c < convertedOriginalMessage.toCharArray().length; c++) {
			if (replacements != null && replacements.length > 0 && convertedOriginalMessage.toCharArray()[c] == '%' && replacementsPos < replacements.length) {
				fixedMessage.append(replacements[replacementsPos]);
				replacementsPos++;
				c++;
			} else {
				fixedMessage.append(convertedOriginalMessage.toCharArray()[c]);
			}
		}
		
		/* Return fixedMessage */
		return fixedMessage.toString();
	}
	
	/**
	  * Sends the user a properly formatted message through our adventure instance.
	  * @param sender - The target sender. Can be any entity that can receive messages.
	  * @param originalMessage - The unformatted TextComponent that should be sent to sender.
	  * @return
	  */
	public static void sendMessage(CommandSender sender, TextComponent originalMessage) {
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
		} catch (IllegalStateException e) {
			// This only happens when the plugin/adventure is disabled.
			// If the plugin/adventure is disabled, no messages should be sent anyways so silently catch this.
		}
	}
	
	/**
	  * Translates text using the selected translator.
	  * @param inMessage - The original message to be translated.
	  * @param currPlayer - The player who wants this message to be translated.
	  * @return String - The translated message. If this is equal to inMessage, the translation failed.
	  */
	public static String translateText(String inMessage, Player currPlayer) {
		/* If translator settings are invalid, do not do this... */
		if (!(inMessage.length() > 0) || CommonDefinitions.serverIsStopping()) {
			return inMessage;
		}
		
		/* Main logic callback */
		Callable<String> result = () -> {
			try {
				/* Get result in user-defined delay plus extra amt. of time */
				/* Sanitize Inputs */
				// Warn user about color codes
				// EssentialsX chat and maybe others replace "&4Test" with " 4Test"
				// Therefore, we find the " #" regex or the "&" char, and warn the user about it
				if ((inMessage.contains("&") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals(""))
						&& !(main.getActiveTranslator(currPlayer.getUniqueId().toString())
								.getCCWarning())) // check if user has already been sent CC warning
				{
					final TextComponent watsonCCWarning = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("watsonColorCodeWarning"))
									.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
							.build();
					CommonDefinitions.sendMessage(currPlayer, watsonCCWarning);
					// Set got CC warning of current translator to true, so that they don't get
					// spammed by it if they keep using CCs
					main.getActiveTranslator(currPlayer.getUniqueId().toString())
							.setCCWarning(true);
					// we're still gonna translate it but it won't look pretty
				}

				/* Modify or create new player record */
				PlayerRecord currPlayerRecord = main
						.getPlayerRecord(currPlayer.getUniqueId().toString(), true);
				if (main.getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations() + 1);

				/* Initialize current ActiveTranslator, sanity checks */
				ActiveTranslator currActiveTranslator;
				if (main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
						&& (!main.getActiveTranslator(currPlayer.getUniqueId().toString()).getUUID().equals(""))) {
					currActiveTranslator = main.getActiveTranslator(currPlayer.getUniqueId().toString());
				} else if (!main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
						&& (!main.getActiveTranslator(currPlayer.getUniqueId().toString()).getUUID().equals(""))) {
					currActiveTranslator = main.getActiveTranslator(currPlayer.getUniqueId().toString());
				} else {
					currActiveTranslator = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
				}

				/* Char limit check */
				int limit = main.getConfigManager().getMainConfig().getInt("Translator.messageCharLimit");
				if (inMessage.length() > limit) {
					final TextComponent charLimit = Component.text()
							.append(Component.text().content(CommonDefinitions.getMessage("wwcCharLimit", new String[] {"" + limit}))
									.color(NamedTextColor.YELLOW))
							.build();
					CommonDefinitions.sendMessage(currPlayer, charLimit);
					return inMessage;
				}
				
				/* Check cache */
				if (main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
					// Check cache for inputs, since config says we should
					for (Map.Entry<CachedTranslation, Integer> currentTerm : main.getCache().entrySet()) {
						if (currentTerm.getKey().getInputLang().equalsIgnoreCase(currActiveTranslator.getInLangCode())
								&& (currentTerm.getKey().getOutputLang().equalsIgnoreCase(currActiveTranslator.getOutLangCode()))
								&& (currentTerm.getKey().getInputPhrase().equalsIgnoreCase(inMessage))) {
							main.getCache().put(currentTerm.getKey(), currentTerm.getValue()+1);
							// Update stats, return output
							currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
							currPlayerRecord.setLastTranslationTime();
							return StringEscapeUtils.unescapeJava(
									ChatColor.translateAlternateColorCodes('&', currentTerm.getKey().getOutputPhrase()));
						}
					}
				}

				/* Rate limit check */
				try {
					// Init vars
					boolean isExempt = false;
					boolean hasPermission = false;
					int personalRateLimit = 0;
					String permissionCheck = "";

					// Get permission from Bukkit API synchronously, since we do not want to risk
					// concurrency problems
					if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") && !CommonDefinitions.serverIsStopping()) {
						try {
							permissionCheck = Bukkit.getScheduler().callSyncMethod(main, () -> {
								return checkForRateLimitPermissions(currPlayer);
							}).get(3, TimeUnit.SECONDS);
						} catch (TimeoutException | InterruptedException e) {
							CommonDefinitions.sendDebugMessage("Timeout from rate limit permission check should never happen, unless the server is stopping or /reloading. "
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
					if (!isExempt && !hasPermission && !main
							.getActiveTranslator(currPlayer.getUniqueId().toString()).getUUID().equals("")) {
						personalRateLimit = main
							.getActiveTranslator(currPlayer.getUniqueId().toString()).getRateLimit();
					}

					// Personal Limits (Override Global)
					if (!isExempt && personalRateLimit > 0) {
						if (!checkForRateLimits(personalRateLimit, currActiveTranslator, currPlayer)) {
							return inMessage;
						}
					// Global Limits
					} else if (!isExempt && main.getConfigManager().getMainConfig().getInt("Translator.rateLimit") > 0) {
						if (!checkForRateLimits(main.getConfigManager().getMainConfig().getInt("Translator.rateLimit"), currActiveTranslator, currPlayer)) {
							return inMessage;
						}
					}
				} catch (Exception e2) {
					// Couldn't get user permissions: stop, drop, and roll.
					e2.printStackTrace();
					return inMessage;
				}

				/* Begin actual translation, set message to output */
				String out = "";
				try {
					CommonDefinitions.sendDebugMessage("Translating a message (in " + currActiveTranslator.getInLangCode() + ") from " + currActiveTranslator.getUUID() + " to " + currActiveTranslator.getOutLangCode() + ".");
					if (main.getTranslatorName().equals("Watson")) {
						WatsonTranslation watsonInstance = new WatsonTranslation(inMessage,
								currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), currPlayer);
						// Get username + pass from config
						out = watsonInstance.useTranslator();
					} else if (main.getTranslatorName().equals("Google Translate")) {
						GoogleTranslation googleTranslateInstance = new GoogleTranslation(inMessage,
								currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), currPlayer);
						out = googleTranslateInstance.useTranslator();
					} else if (main.getTranslatorName().equals("Amazon Translate")) {
						AmazonTranslation amazonTranslateInstance = new AmazonTranslation(inMessage,
								currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), currPlayer);
						out = amazonTranslateInstance.useTranslator();
					} else if (main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
						TestTranslation testTranslator = new TestTranslation(inMessage, currActiveTranslator.getInLangCode(),
								currActiveTranslator.getOutLangCode(), currPlayer);
						out = testTranslator.useTranslator();
					}
				} catch (TranslatorFailException e) {
					// Double getCause() because each translator is also wrapped in a callback try/catch, 
					// and this error is our custom exception
					Throwable realReason = e.getCause().getCause();
					if ((realReason != null) && (realReason instanceof InterruptedException | realReason instanceof NotFoundException || realReason instanceof TranslateException || realReason instanceof InvalidRequestException)) {
						CommonDefinitions.sendDebugMessage("Low confidence exception thrown, do not add this to the error count!");
					} else {
						// If this isn't just low confidence, this error should be added
						// to the error count.
						throw new TranslatorFailException(e);
					}
					return inMessage;
				}

				/* Update stats */
				if (main.getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
				currPlayerRecord.setLastTranslationTime();

				/* Add to cache */
				if (main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize") > 0
						&& !(currActiveTranslator.getInLangCode().equals("None"))) {
					CachedTranslation newTerm = new CachedTranslation(currActiveTranslator.getInLangCode(),
							currActiveTranslator.getOutLangCode(), inMessage, out);
					main.addCacheTerm(newTerm);
				}
				return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', out));
			} catch (Exception e) {
				/* Add 1 to error count */
				if (e.getCause() != null && e.getCause() instanceof InterruptedException) {
					// If we are getting stopped by onDisable, end this immediately.
					return inMessage;
				}
				main.setTranslatorErrorCount(main.getTranslatorErrorCount() + 1);
				final TextComponent playerError = Component.text()
						.append(Component.text().content(CommonDefinitions.getMessage("wwcTranslatorError"))
								.color(NamedTextColor.RED))
						.build();
				CommonDefinitions.sendMessage(currPlayer, playerError);
				main.getLogger()
						.severe(CommonDefinitions.getMessage("wwcTranslatorErrorConsole", new String[] {currPlayer.getName()}));
				CommonDefinitions.sendDebugMessage(ExceptionUtils.getStackTrace(e));

				/* Write to log file */
				File errorLog = new File(main.getDataFolder(), "errorLog.txt");
				try {
					FileWriter fw = new FileWriter(errorLog, true);
					LocalDate date = LocalDate.now();
					LocalTime time = LocalTime.now();
					String dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - "))
							+ time.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));

					fw.write("========== " + dateStr + " ==========");
					fw.write(System.getProperty("line.separator"));
					fw.write(ExceptionUtils.getStackTrace(e));
					fw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				CommonDefinitions.sendDebugMessage("Error Count: " + main.getTranslatorErrorCount());
				
				/* If error count is greater than threshold set in config.yml, reload on this thread (we are already async) */
				if (main.getTranslatorErrorCount() >= main.getConfigManager().getMainConfig().getInt("Translator.errorLimit")) {
					main.getLogger().severe(CommonDefinitions.getMessage("wwcTranslatorErrorThresholdReached"));
					main.getLogger().severe(CommonDefinitions.getMessage("wwcTranslatorErrorThresholdReachedCheckLogs"));
					main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate",
							false);
					main.getConfigManager().getMainConfig().set("Translator.useGoogleTranslate",
							false);
					main.getConfigManager().getMainConfig().set("Translator.useAmazonTranslate",
							false);
					main.getConfigManager().saveMainConfig(false);
					main.reload();
				}
			}
			return inMessage;
		};
		
		/* Start Callback Process */
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(result);
		String finalOut = inMessage;
		try {
			/* Get translation */
			finalOut = process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			CommonDefinitions.sendDebugMessage("Failed to receive a response from the translator, and it was not interrupted. Abort.");
			if (e instanceof TimeoutException) {sendTimeoutExceptionMessage(currPlayer);};
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
	public static boolean sendNoConsoleChatMessage(CommandSender sender) {
		final TextComponent noConsoleChat = Component.text() // Cannot translate console chat
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctCannotTranslateConsole", new String[0]))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(sender, noConsoleChat);
		return false;
	}
	
	/**
	 * Sends a message to console and a sender that a timeout exception has occured.
	 * @param sender - Who will receive the message besides console.
	 * @return Returns true, so that a command can return this method.
	 */
	public static boolean sendTimeoutExceptionMessage(CommandSender sender) {
		if (sender instanceof Player) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcTimeoutExceptionConsole", new String[] {sender.getName()}));
		}
		final TextComponent timeoutException = Component.text()
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwcTimeoutException", new String[0]))
						.color(NamedTextColor.YELLOW))
				.build();
		CommonDefinitions.sendMessage(sender, timeoutException);
		return true;
	}
	
	/**
	  * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
	  * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
	  * @return Boolean - Whether the server is reloading/stopping or not
	  */
	public static boolean serverIsStopping() {
		try {
			new BukkitRunnable() {
				@Override
				public void run() {}
			}.runTask(main);
		} catch (Exception e) {
			CommonDefinitions.sendDebugMessage("Server is stopping! Don't run a task/do any dumb shit.");
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
	public static Prompt genericConfigConversation(boolean preCheck, ConversationContext context, String successfulChangeMsg, String configValName[], Object[] configVal, SmartInventory prevInventory) {
		if (configValName.length != configVal.length) {
			return null;
		}
		if (preCheck) {
			for (int i = 0; i < configValName.length; i++) {
				main.getConfigManager().getMainConfig().set(configValName[i], configVal[i]);
			}
			main.addPlayerUsingConfigurationGUI(((Player)context.getForWhom()).getUniqueId());
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage(successfulChangeMsg))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage((Player)context.getForWhom(), successfulChange);
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
	public static Prompt genericConfigConversation(boolean preCheck, ConversationContext context, String successfulChangeMsg, String configValName, Object configVal, SmartInventory prevInventory) {
		return genericConfigConversation(preCheck, context, successfulChangeMsg, new String[] {configValName}, new Object[] {configVal}, prevInventory);
	}
	
	/**
	  * Ensures that an ActiveTranslator does not currently need to be rate limited.
	  * @param delay - Exact rate limit that is being looked for
	  * @param currActiveTranslator - The valid ActiveTranslator target
	  * @param sender - The sender of the original command
	  * @return Boolean - Returns false if the user should currently be rate limited, and true otherwise.
	  */
	protected static boolean checkForRateLimits(int delay, ActiveTranslator currActiveTranslator, CommandSender sender) {
		if (!(currActiveTranslator.getRateLimitPreviousTime().equals("None"))) {
			Instant previous = Instant.parse(currActiveTranslator.getRateLimitPreviousTime());
			Instant currTime = Instant.now();
			if (currTime.compareTo(previous.plus(delay, ChronoUnit.SECONDS)) < 0) {
				final TextComponent rateLimit = Component.text()
						.append(Component.text().content(CommonDefinitions.getMessage("wwcRateLimit", new String[] {"" + ChronoUnit.SECONDS.between(currTime,
								previous.plus(delay, ChronoUnit.SECONDS))}))
								.color(NamedTextColor.YELLOW))
						.build();
				CommonDefinitions.sendMessage(sender, rateLimit);
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
	protected static String checkForRateLimitPermissions(Player currPlayer) {
		Set<PermissionAttachmentInfo> perms = currPlayer.getEffectivePermissions();
		for (PermissionAttachmentInfo perm : perms) {
			if (perm.getPermission().startsWith("worldwidechat.ratelimit.")) {
				return perm.getPermission();
			}
		}
		return "";
	}
}