package com.expl0itz.worldwidechat.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.IllegalPluginAccessException;
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
import com.expl0itz.worldwidechat.translators.WatsonTranslation;
import com.google.cloud.translate.TranslateException;
import com.google.common.base.CharMatcher;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.sun.tools.javac.launcher.Main;

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
	public static String[] supportedMCVersions = { "1.17", "1.16", "1.15", "1.14", "1.13", "1.12", "1.11", "1.10", "1.9", "1.8" };

	public static String[] supportedPluginLangCodes = { "af", "sq", "am", "ar", "hy", "az", "bn", "bs", "bg", "ca",
			"zh", "zh-TW", "hr", "cs", "da", "fa-AF", "nl", "en", "et", "fa", "tl", "fi", "fr", "fr-CA", "ka", "de",
			"el", "gu", "ht", "ha", "he", "hi", "hu", "is", "id", "it", "ja", "kn", "kk", "ko", "lv", "lt", "mk", "ms",
			"ml", "mt", "mn", "no", "fa", "ps", "pl", "pt", "ro", "ru", "sr", "si", "sk", "sl", "so", "es", "es-MX",
			"sw", "sv", "tl", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "cy" };

	/* Getters */
	public static boolean isInteger(String str) {
		/* This gets whether something is an integer, FAST. 
		 * It's messy, and should only be used if you value performance over readability.
		 * Suck it, matches()
		 * */
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
	
	public static boolean isSameLang(String first, String second) {
		for (SupportedLanguageObject eaLang : WorldwideChat.getInstance().getSupportedTranslatorLanguages()) {
			if ((eaLang.getLangName().equals(getSupportedTranslatorLang(first).getLangName())
					&& eaLang.getLangName().equals(getSupportedTranslatorLang(second).getLangName()))) {
				return true;
			}
		}
		return false;
	}

	public static SupportedLanguageObject getSupportedTranslatorLang(String in) {
		for (SupportedLanguageObject eaLang : WorldwideChat.getInstance().getSupportedTranslatorLanguages()) {
			if ((eaLang.getLangCode().equalsIgnoreCase(in) || eaLang.getLangName().equalsIgnoreCase(in))) {
				return eaLang;
			}
		}
		return new SupportedLanguageObject("", "", "", false, false);
	}

	public static String getFormattedValidLangCodes() {
		String out = "\n";
		for (SupportedLanguageObject eaLang : WorldwideChat.getInstance().getSupportedTranslatorLanguages()) {
			out += "(" + eaLang.getLangCode() + " - " + eaLang.getLangName() + "), ";
		}
		if (out.indexOf(",") != -1) {
			out = out.substring(0, out.lastIndexOf(","));
		}
		return out;
	}

	public static void closeAllInventories() {
		// Close all active GUIs
		WorldwideChat.getInstance().getPlayersUsingGUI().clear();
		for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
			try {
				if (WorldwideChat.getInstance().getInventoryManager().getInventory(eaPlayer)
						.get() instanceof SmartInventory
						&& WorldwideChat.getInstance().getInventoryManager().getInventory(eaPlayer).get().getManager()
								.equals(WorldwideChat.getInstance().getInventoryManager())) {
					eaPlayer.closeInventory();
				}
			} catch (NoSuchElementException e) {
				continue;
			}
		}
	}

	public static void sendDebugMessage(String inMessage) {
		if (WorldwideChat.getInstance().getDebugMode()) {
			WorldwideChat.getInstance().getLogger().warning("DEBUG: " + inMessage);
		}
	}
	
	public static String getMessage(String messageName) {
		return getMessage(messageName, new String[0]);
	}
	
	public static String getMessage(String messageName, String[] replacements) {
		/* Get message from messages.yml */
		String convertedOriginalMessage = "";
		if (WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Overrides." + ChatColor.stripColor(messageName)) != null) {
			convertedOriginalMessage = WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Overrides." + ChatColor.stripColor(messageName));
		} else {
			convertedOriginalMessage = WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages." + ChatColor.stripColor(messageName));
			if (convertedOriginalMessage == null) {
				WorldwideChat.getInstance().getLogger().severe("Bad message! Please fix your messages-" + WorldwideChat.getInstance().getPluginLang() + ".yml.");
				return ChatColor.RED + "Bad message! Please fix your messages-" + WorldwideChat.getInstance().getPluginLang() + ".yml.";
			}
		}
		
		/* Replace any and all %i, %e, %o, etc. */
		/* This code will only go as far as replacements[] goes. */
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
	
	public static void sendMessage(CommandSender sender, TextComponent originalMessage) {
		try {
			Audience adventureSender = WorldwideChat.getInstance().adventure().sender(sender);
			final TextComponent outMessage = Component.text().append(WorldwideChat.getInstance().getPluginPrefix().asComponent())
					.append(Component.text().content(" "))
					.append(originalMessage.asComponent())
					.build();
			if (sender instanceof ConsoleCommandSender) {
				WorldwideChat.getInstance().getServer().getConsoleSender().sendMessage((ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(outMessage))));
			} else {
				adventureSender.sendMessage(outMessage);
			}
		} catch (IllegalStateException e) {
			// This only happens when the plugin/adventure is disabled.
			// If the plugin/adventure is disabled, no messages should be sent anyways so silently catch this.
		}
	}
	
	public static String translateText(String inMessage, Player currPlayer) {
		try {
			/* If translator settings are invalid, do not do this... */
			if (WorldwideChat.getInstance().getTranslatorName().equals("Invalid") || !(inMessage.length() > 0)) {
				return inMessage;
			}

			/* Sanitize Inputs */
			// Warn user about color codes
			// EssentialsX chat and maybe others replace "&4Test" with " 4Test"
			// Therefore, we find the " #" regex or the "&" char, and warn the user about it
			if ((inMessage.contains("&") && WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals(""))
					&& !(WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString())
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
				WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString())
						.setCCWarning(true);
				// we're still gonna translate it but it won't look pretty
			}

			/* Modify or create new player record */
			PlayerRecord currPlayerRecord = WorldwideChat.getInstance()
					.getPlayerRecord(currPlayer.getUniqueId().toString(), true);
			if (WorldwideChat.getInstance().getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations() + 1);

			/* Initialize current ActiveTranslator, sanity checks */
			ActiveTranslator currActiveTranslator;
			if (WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
					&& (!WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString()).getUUID().equals(""))) {
				currActiveTranslator = WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString());
			} else if (!WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
					&& (!WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString()).getUUID().equals(""))) {
				currActiveTranslator = WorldwideChat.getInstance().getActiveTranslator(currPlayer.getUniqueId().toString());
			} else {
				currActiveTranslator = WorldwideChat.getInstance().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
			}

			/* Char limit check */
			int limit = WorldwideChat.getInstance().getMessageCharLimit();
			if (inMessage.length() > limit) {
				final TextComponent charLimit = Component.text()
						.append(Component.text().content(CommonDefinitions.getMessage("wwcCharLimit", new String[] {"" + limit}))
								.color(NamedTextColor.YELLOW))
						.build();
				CommonDefinitions.sendMessage(currPlayer, charLimit);
				return inMessage;
			}
			
			/* Check cache */
			if (WorldwideChat.getInstance().getTranslatorCacheLimit() > 0) {
				// Check cache for inputs, since config says we should
				List<CachedTranslation> currCache = WorldwideChat.getInstance().getCache();
				synchronized (currCache) {
					for (CachedTranslation currentTerm : currCache) {
						if (currentTerm.getInputLang().equalsIgnoreCase(currActiveTranslator.getInLangCode())
								&& (currentTerm.getOutputLang().equalsIgnoreCase(currActiveTranslator.getOutLangCode()))
								&& (currentTerm.getInputPhrase().equalsIgnoreCase(inMessage))) {
							currentTerm.setNumberOfTimes(currentTerm.getNumberOfTimes() + 1);
							// Update stats, return output
							if (WorldwideChat.getInstance().getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
							currPlayerRecord.setLastTranslationTime();
							return StringEscapeUtils.unescapeJava(
									ChatColor.translateAlternateColorCodes('&', currentTerm.getOutputPhrase())); // done
																													// :)
						}
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
				if (!WorldwideChat.getInstance().getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
					permissionCheck = Bukkit.getScheduler().callSyncMethod(WorldwideChat.getInstance(), () -> {
						return checkForRateLimitPermissions(currPlayer);
					}).get();
				} else {
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
				if (!isExempt && !hasPermission && !WorldwideChat.getInstance()
						.getActiveTranslator(currPlayer.getUniqueId().toString()).getUUID().equals("")) {
					personalRateLimit = WorldwideChat.getInstance()
						.getActiveTranslator(currPlayer.getUniqueId().toString()).getRateLimit();
				}

				// Personal Limits (Override Global)
				if (!isExempt && personalRateLimit > 0) {
					if (!checkForRateLimits(personalRateLimit, currActiveTranslator, currPlayer)) {
						return inMessage;
					}
				// Global Limits
				} else if (!isExempt && WorldwideChat.getInstance().getRateLimit() > 0) {
					if (!checkForRateLimits(WorldwideChat.getInstance().getRateLimit(), currActiveTranslator, currPlayer)) {
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
			CommonDefinitions.sendDebugMessage("Translating a message (in " + currActiveTranslator.getInLangCode() + ") from " + currActiveTranslator.getUUID() + " to " + currActiveTranslator.getOutLangCode() + ".");
			if (WorldwideChat.getInstance().getTranslatorName().equals("Watson")) {
				try {
					WatsonTranslation watsonInstance = new WatsonTranslation(inMessage,
							currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), currPlayer);
					// Get username + pass from config
					out = watsonInstance.useTranslator();
				} catch (NotFoundException lowConfidenceInAnswer) {
					/*
					 * This exception happens if the Watson translator is auto-detecting the input
					 * language. By definition, the translator is unsure if the source language
					 * detected is accurate due to confidence levels being below a certain
					 * threshold. Usually, either already translated input is given or occasionally
					 * a phrase is not fully translatable. This is where we catch that and send the
					 * player a message telling them that their message was unable to be parsed by
					 * the translator. You should be able to turn this off in the config.
					 */
					final TextComponent lowConfidence = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("watsonNotFoundExceptionNotification"))
									.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
							.build();
					CommonDefinitions.sendMessage(currPlayer, lowConfidence);
					return inMessage;
				}
			} else if (WorldwideChat.getInstance().getTranslatorName().equals("Google Translate")) {
				try {
					GoogleTranslation googleTranslateInstance = new GoogleTranslation(inMessage,
							currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), currPlayer);
					out = googleTranslateInstance.useTranslator();
				} catch (TranslateException e) {
					/*
					 * This exception happens for the same reason that Watson does: low confidence.
					 * Usually when a player tries to get around our same language translation
					 * block. Examples of when this triggers: .wwct en and typing in English.
					 */
					final TextComponent lowConfidence = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("watsonNotFoundExceptionNotification"))
									.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
							.build();
					CommonDefinitions.sendMessage(currPlayer, lowConfidence);
					return inMessage;
				}
			} else if (WorldwideChat.getInstance().getTranslatorName().equals("Amazon Translate")) {
				try {
					AmazonTranslation amazonTranslateInstance = new AmazonTranslation(inMessage,
							currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), currPlayer);
					out = amazonTranslateInstance.useTranslator();
				} catch (InvalidRequestException e) {
					/* Low confidence exception, Amazon Translate Edition */
					final TextComponent lowConfidence = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("watsonNotFoundExceptionNotification"))
									.color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
							.build();
					CommonDefinitions.sendMessage(currPlayer, lowConfidence);
					return inMessage;
				}
			} else if (WorldwideChat.getInstance().getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
				TestTranslation testTranslator = new TestTranslation(inMessage, currActiveTranslator.getInLangCode(),
						currActiveTranslator.getOutLangCode(), currPlayer);
				out = testTranslator.useTranslator();
			}

			/* Update stats */
			if (WorldwideChat.getInstance().getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setSuccessfulTranslations(currPlayerRecord.getSuccessfulTranslations() + 1);
			currPlayerRecord.setLastTranslationTime();

			/* Add to cache */
			if (WorldwideChat.getInstance().getTranslatorCacheLimit() > 0
					&& !(currActiveTranslator.getInLangCode().equals("None"))) {
				CachedTranslation newTerm = new CachedTranslation(currActiveTranslator.getInLangCode(),
						currActiveTranslator.getOutLangCode(), inMessage, out);
				WorldwideChat.getInstance().addCacheTerm(newTerm);
			}
			return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', out));
		} catch (Exception e) {
			try {
				/* Add 1 to error count */
				WorldwideChat.getInstance().setErrorCount(WorldwideChat.getInstance().getErrorCount() + 1);
				final TextComponent playerError = Component.text()
						.append(Component.text().content(CommonDefinitions.getMessage("wwcTranslatorError"))
								.color(NamedTextColor.RED))
						.build();
				CommonDefinitions.sendMessage(currPlayer, playerError);
				WorldwideChat.getInstance().getLogger()
						.severe(CommonDefinitions.getMessage("wwcTranslatorErrorConsole", new String[] {currPlayer.getName()}));
				CommonDefinitions.sendDebugMessage(ExceptionUtils.getStackTrace(e));

				/* Write to log file */
				File errorLog = new File(WorldwideChat.getInstance().getDataFolder(), "errorLog.txt");
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

				/* If error count is greater than threshold set in config.yml, reload on this thread (we are already async) */
				if (WorldwideChat.getInstance().getErrorCount() >= WorldwideChat.getInstance().getErrorLimit()) {
					WorldwideChat.getInstance().getLogger().severe(CommonDefinitions.getMessage("wwcTranslatorErrorThresholdReached"));
					WorldwideChat.getInstance().getLogger().severe(CommonDefinitions.getMessage("wwcTranslatorErrorThresholdReachedCheckLogs"));
					WorldwideChat.getInstance().getConfigManager().getMainConfig().set("Translator.useWatsonTranslate",
							false);
					WorldwideChat.getInstance().getConfigManager().getMainConfig().set("Translator.useGoogleTranslate",
							false);
					WorldwideChat.getInstance().getConfigManager().getMainConfig().set("Translator.useAmazonTranslate",
							false);
					WorldwideChat.getInstance().getConfigManager().saveMainConfig(false);
					WorldwideChat.getInstance().reload(null);
				}
			} catch (Exception e1) {
				// If we got here, the plugin is most likely disabling.
			}
		}
		return inMessage;
	}

	public static boolean getNoConsoleChatMessage(CommandSender sender) {
		final TextComponent noConsoleChat = Component.text() // Cannot translate console chat
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctCannotTranslateConsole", new String[0]))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(sender, noConsoleChat);
		return false;
	}
	
	public static boolean serverIsStopping() {
		try {
			new BukkitRunnable() {
				@Override
				public void run() {
					CommonDefinitions.sendDebugMessage("Server is not stopping!");
				}
			}.runTask(WorldwideChat.getInstance());
		} catch (IllegalPluginAccessException e) {
			CommonDefinitions.sendDebugMessage("Server is stopping! Don't run a task/do any dumb shit.");
			return true;
		}
		return false;
	}
	
	private static boolean checkForRateLimits(int delay, ActiveTranslator currActiveTranslator, CommandSender sender) {
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