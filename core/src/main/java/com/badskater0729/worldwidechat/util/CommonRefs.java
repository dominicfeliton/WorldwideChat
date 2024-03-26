package com.badskater0729.worldwidechat.util;

import java.io.*;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import com.badskater0729.worldwidechat.translators.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
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

	public static List<Pair<String, String>> translatorPairs = new ArrayList<>(Arrays.asList(
			Pair.of("Translator.testModeTranslator", "JUnit/MockBukkit Testing Translator"),
			Pair.of("Translator.useGoogleTranslate", "Google Translate"),
			Pair.of("Translator.useAmazonTranslate", "Amazon Translate"),
			Pair.of("Translator.useLibreTranslate", "Libre Translate"),
			Pair.of("Translator.useDeepLTranslate", "DeepL Translate"),
			Pair.of("Translator.useWatsonTranslate", "Watson"),
			Pair.of("Translator.useAzureTranslate", "Azure Translate"),
			Pair.of("Translator.useSystranTranslate", "Systran Translate")
	));

	public void runAsync(BukkitRunnable in) {
		runAsync(true, in);
	}
	
	public void runAsync(boolean serverMustBeRunning, BukkitRunnable in) {
		runAsync(serverMustBeRunning, 0, in);
	}
	
	public void runAsync(boolean serverMustBeRunning, int delay, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!serverIsStopping()) {
				in.runTaskLaterAsynchronously(main, delay);
			}
		} else {
			in.runTaskLaterAsynchronously(main, delay);
		}
	}
	
	public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!serverIsStopping()) {
				in.runTaskTimerAsynchronously(main, delay, repeatTime);
			}
		} else {
			in.runTaskTimerAsynchronously(main, delay, repeatTime);
		}
	}
	
	public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, BukkitRunnable in) {
		runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in);
	}
	
	public void runSync(BukkitRunnable in) {
		runSync(true, in);
	}
	
	public void runSync(boolean serverMustBeRunning, BukkitRunnable in) {
		runSync(serverMustBeRunning, 0, in);
	}
	
	public void runSync(boolean serverMustBeRunning, int delay, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!serverIsStopping()) {
				in.runTaskLater(main, delay);
			}
		} else {
			in.runTaskLater(main, delay);
		}
	}
	
	public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, BukkitRunnable in) {
		if (serverMustBeRunning) {
			if (!serverIsStopping()) {
				in.runTaskTimer(main, delay, repeatTime);
			}
		} else {
			in.runTaskTimer(main, delay, repeatTime);
		}
	}
	
	public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, BukkitRunnable in) {
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
	public boolean isSameTranslatorLang(String first, String second, String langType) {
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
	public SupportedLang getSupportedTranslatorLang(String langName, String langType) {
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
	public boolean isSupportedTranslatorLang(String in, String langType) {
		return !getSupportedTranslatorLang(in, langType).getLangCode().isEmpty();
	}

	/**
	  * Gets a list of properly formatted, supported language codes.
	  * @param langType - 'out' or 'in' are two valid inputs for this;
	  * 'out' will check if in is a valid output lang, 'in' will check the input lang list
	  * @return String - Formatted language codes
	  * 
	  */
	public String getFormattedValidLangCodes(String langType) {
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
	 * Fixes a given list of SupportedLangs to include native names/language names
	 * @param in - List of SupportedLang objs
	 * @param nativesOnly - Whether we should add regular lang names as well as native langs
	 * @return - The fixed list of supportedLang objs
	 */
	public List<SupportedLang> fixLangNames(List<SupportedLang> in, boolean nativesOnly) {
		// Adjust the file path as necessary
		String isoJsonFilePath = "ISO_639-2.min.json";
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, ISOLanguage> languageMap;

		try {
			languageMap = objectMapper.readValue(main.getResource(isoJsonFilePath), new TypeReference<Map<String, ISOLanguage>>(){});
			for (int i = 0; i < in.size(); i++) {
				SupportedLang currLang = in.get(i);
				String currCode = currLang.getLangCode();
				ISOLanguage jsonLang = languageMap.get(currCode);

				if (jsonLang != null) {
					if (nativesOnly) {
						in.set(i, new SupportedLang(currLang.getLangCode(), currLang.getLangName(), jsonLang.getNativeName()));
					} else {
						in.set(i, new SupportedLang(currLang.getLangCode(), jsonLang.getIntName(), jsonLang.getNativeName()));
					}
				} else {
					debugMsg("Could not find " + currCode + " in JSON!");
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			main.getLogger().warning(getMsg("wwcISOJSONFail"));
		}

		return in;
	}

	/**
	  * Closes all inventories registered by WorldwideChat.
	  */
	public void closeAllInvs() {
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
	public void debugMsg(String inMessage) {
		if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
			main.getLogger().warning("DEBUG: " + inMessage);
		}
	}
	
	/**
	 * Gets a message (with no replacements) from the currently selected messages-XX.yml.
	 * @param messageName - The name of the message from messages-XX.yml.
	 * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	 */
	public String getMsg(String messageName) {
		return getMsg(messageName, new String[0]);
	}

	/**
	 * Gets a message from the currently selected messages-XX.yml.
	 * @param messageName - The name of the message from messages-XX.yml.
	 * @param replacement - The replacement value for the selected message.
	 * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	 */
	public String getMsg(String messageName, String replacement) { return getMsg(messageName, new String[] {replacement}); }

	/**
	  * Gets a message from the currently selected messages-XX.yml.
	  * @param messageName - The name of the message from messages-XX.yml.
	  * @param replacements - The list of replacement values that replace variables in the selected message. There is no sorting system; the list must be already sorted.
	  * @return String - The formatted message from messages-XX.yml. A warning will be returned instead if messageName is missing from messages-XX.yml.
	  */
	public String getMsg(String messageName, String[] replacements) {
		/* Get message from messages.yml */
		String convertedOriginalMessage = "";
		YamlConfiguration messagesConfig = main.getConfigManager().getMsgsConfig();
		if (messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)) != null) {
			convertedOriginalMessage = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)));
		} else {
			if (messagesConfig.getString("Messages." + ChatColor.stripColor(messageName)) == null) {
				main.getLogger().severe("Bad message! Please fix your messages-XX.yml.");
				return ChatColor.RED + "Bad message! Please fix your messages-XX.yml.";
			}
			convertedOriginalMessage = messagesConfig.getString("Messages." + ChatColor.stripColor(messageName));
		}

		// Translate color codes in the original message
		convertedOriginalMessage = ChatColor.translateAlternateColorCodes('&', convertedOriginalMessage);

		// Return fixedMessage with replaced vars
		return MessageFormat.format(convertedOriginalMessage, (Object[]) replacements);
	}

	public TextComponent getFancyMsg(String messageName, String[] replacements, String resetCode) {
		for (int i = 0; i < replacements.length; i++) {
			// Translate color codes in replacements
			replacements[i] = ChatColor.translateAlternateColorCodes('&', replacements[i] + resetCode);
		}

		/* Get message from messages.yml */
		String convertedOriginalMessage = resetCode;
		YamlConfiguration messagesConfig = main.getConfigManager().getMsgsConfig();
		if (messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)) != null) {
			convertedOriginalMessage += ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("Overrides." + ChatColor.stripColor(messageName)));
		} else {
			if (messagesConfig.getString("Messages." + ChatColor.stripColor(messageName)) == null) {
				main.getLogger().severe("Bad message! Please fix your messages-XX.yml.");
				return Component.text().content(ChatColor.RED + "Bad message! Please fix your messages-XX.yml.").build();
			}
			convertedOriginalMessage += messagesConfig.getString("Messages." + ChatColor.stripColor(messageName));
		}

		// Translate color codes in the original message
		convertedOriginalMessage = ChatColor.translateAlternateColorCodes('&', convertedOriginalMessage);

		// Return fixedMessage with replaced vars
		return Component.text().content(MessageFormat.format(convertedOriginalMessage, (Object[]) replacements)).build();
	}

	public void sendFancyMsg(String messageName, CommandSender sender) {
		sendMsg(sender, getFancyMsg(messageName, new String[]{}, "&r&d"));
	}

	public void sendFancyMsg(String messageName, String[] replacements, CommandSender sender) {
		// Default WWC color is usually LIGHT_PURPLE (&d)
		sendMsg(sender, getFancyMsg(messageName, replacements, "&r&d"));
	}

	public void sendFancyMsg(String messageName, String replacement, CommandSender sender) {
		sendMsg(sender, getFancyMsg(messageName, new String[] {replacement}, "&r&d"));
	}

	public void sendFancyMsg(String messageName, String replacement, String resetCode, CommandSender sender) {
		sendMsg(sender, getFancyMsg(messageName, new String[] {replacement}, "&r"+resetCode));
	}

	public void sendFancyMsg(String messageName, String[] replacements, String resetCode, CommandSender sender) {
		sendMsg(sender, getFancyMsg(messageName, replacements, "&r"+resetCode));
	}
	
	/**
	  * Sends the user a properly formatted message through our adventure instance.
	  * @param sender - The target sender. Can be any entity that can receive messages.
	  * @param originalMessage - The unformatted TextComponent that should be sent to sender.
	  * @return
	  */
	public void sendMsg(CommandSender sender, TextComponent originalMessage) {
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
	 * Shorthand for component to str
	 * @param comp - TextComponent
	 * @return string version
	 */
	public String serial(Component comp) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(comp);
	}

	/**
	 * Shorthand for str to component
	 * @param str
	 * @return textcomponent version
	 */
	public Component deserial(String str) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
	}

	/**
	  * Translates text using the selected translator.
	  * @param inMessage - The original message to be translated.
	  * @param currPlayer - The player who wants this message to be translated.
	  * @return String - The translated message. If this is equal to inMessage, the translation failed.
	  */
	public String translateText(String inMessage, Player currPlayer) {
		/* If translator settings are invalid, do not do this... */
		if (!(inMessage.length() > 0) || serverIsStopping()) {
			return inMessage;
		}
		
		/* Main logic callback */
		YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
		Callable<String> result = () -> {
			/* Detect color codes in message */
			detectColorCodes(inMessage, currPlayer);

			/* Modify or create new player record */
			PlayerRecord currPlayerRecord = main
					.getPlayerRecord(currPlayer, true);
			if (main.getServer().getPluginManager().getPlugin("DeluxeChat") == null) currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations() + 1);

			/* Initialize current vars + ActiveTranslator, sanity checks */
			ActiveTranslator currActiveTranslator;
			if (!main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
					&& (main.isActiveTranslator(currPlayer))) {
				currActiveTranslator = main.getActiveTranslator(currPlayer);
			} else if (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
					&& (main.isActiveTranslator(currPlayer))) {
				currActiveTranslator = main.getActiveTranslator(currPlayer);
			} else {
				currActiveTranslator = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
			}

			/* Char limit check */
			int limit = mainConfig.getInt("Translator.messageCharLimit");
			if (inMessage.length() > limit) {
				final TextComponent charLimit = Component.text()
								.content(getMsg("wwcCharLimit", "" + limit))
								.color(NamedTextColor.YELLOW)
						.build();
				sendMsg(currPlayer, charLimit);
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
			if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator") && !serverIsStopping()) {
				try {
					permissionCheck = Bukkit.getScheduler().callSyncMethod(main, () -> {
						return checkForRateLimitPermissions(currPlayer);
					}).get(3, TimeUnit.SECONDS);
				} catch (TimeoutException | InterruptedException e) {
					debugMsg("Timeout from rate limit permission check should never happen, unless the server is stopping or /reloading. "
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
					.getActiveTranslator(currPlayer).getRateLimit();
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
			debugMsg("Translating a message (in " + currActiveTranslator.getInLangCode() + ") from " + currActiveTranslator.getUUID() + " to " + currActiveTranslator.getOutLangCode() + ".");
			out = getTranslatorResult(main.getTranslatorName(), inMessage, currActiveTranslator.getInLangCode(), currActiveTranslator.getOutLangCode(), false);

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
		Future<String> process = main.getCallbackExecutor().submit(result);
		String finalOut = inMessage;
		try {
			/* Get translation */
			finalOut = process.get(WorldwideChat.translatorFatalAbortSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			/* Sanitize error before proceeding to write it to errorLog */
			if (e instanceof InterruptedException || main.getTranslatorName().equals("Starting")) {
				// If we are getting stopped by onDisable, end this immediately.
				debugMsg("Interrupted translateText(), or server state is changing...");
				return inMessage;
			} else if (e instanceof ExecutionException && (e.getCause() != null) && isErrorToIgnore(e.getCause())) {
				// If the translator has low confidence
				debugMsg("Low confidence from current translator!");
				return inMessage;
			} else if (e instanceof TimeoutException) {
				// If we get a timeoutexception
				sendTimeoutExceptionMsg(currPlayer);
				return inMessage;
			}
			
			/* Add 1 to error count */
			main.setTranslatorErrorCount(main.getTranslatorErrorCount() + 1);
			final TextComponent playerError = Component.text()
							.content(getMsg("wwcTranslatorError"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg(currPlayer, playerError);
			main.getLogger()
					.severe(getMsg("wwcTranslatorErrorConsole", currPlayer.getName()));
			debugMsg(ExceptionUtils.getStackTrace(e));

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
			debugMsg("Error Count: " + main.getTranslatorErrorCount());
			
			/* If error count is greater than threshold set in config.yml, reload on this thread (we are already async) */
			if (main.getTranslatorErrorCount() >= mainConfig.getInt("Translator.errorLimit")) {
				main.getLogger().severe(getMsg("wwcTranslatorErrorThresholdReached"));
				main.getLogger().severe(getMsg("wwcTranslatorErrorThresholdReachedCheckLogs"));
				runSync(new BukkitRunnable() {
					@Override
					public void run() {
						main.reload();
					}
				});
			}
		}
		
		/* Return final result */
		return finalOut;
	}

	public String getTranslatorResult(String translatorName, boolean isInitializing) throws ExecutionException, InterruptedException, TimeoutException {
		return getTranslatorResult(translatorName, "", "", "", true);
	}

	public String getTranslatorResult(String translatorName, String inMessage, String inLangCode, String outLangCode, boolean isInitializing) throws ExecutionException, InterruptedException, TimeoutException {
		String out = inMessage;
		YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();

		switch (translatorName) {
			case "Watson":
				WatsonTranslation watsonInstance;
				if (isInitializing) {
					watsonInstance = new WatsonTranslation(mainConfig.getString("Translator.watsonAPIKey"),
							mainConfig.getString("Translator.watsonURL"), true, main.getCallbackExecutor());
				} else {
					watsonInstance = new WatsonTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = watsonInstance.useTranslator();
				break;
			case "Google Translate":
				GoogleTranslation googleTranslateInstance;
				if (isInitializing) {
					googleTranslateInstance = new GoogleTranslation(
							mainConfig.getString("Translator.googleTranslateAPIKey"), true, main.getCallbackExecutor());
				} else {
					googleTranslateInstance = new GoogleTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = googleTranslateInstance.useTranslator();
				break;
			case "Amazon Translate":
				AmazonTranslation amazonTranslateInstance;
				if (isInitializing) {
					amazonTranslateInstance = new AmazonTranslation(mainConfig.getString("Translator.amazonAccessKey"),
							mainConfig.getString("Translator.amazonSecretKey"),
							mainConfig.getString("Translator.amazonRegion"), true, main.getCallbackExecutor());
				} else {
					amazonTranslateInstance = new AmazonTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = amazonTranslateInstance.useTranslator();
				break;
			case "Libre Translate":
				LibreTranslation libreTranslateInstance;
				if (isInitializing) {
					libreTranslateInstance = new LibreTranslation(mainConfig.getString("Translator.libreAPIKey"),
							mainConfig.getString("Translator.libreURL"), true, main.getCallbackExecutor());
				} else {
					libreTranslateInstance = new LibreTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = libreTranslateInstance.useTranslator();
				break;
			case "DeepL Translate":
				DeepLTranslation deeplTranslateInstance;
				if (isInitializing) {
					deeplTranslateInstance = new DeepLTranslation(mainConfig.getString("Translator.deepLAPIKey"), true, main.getCallbackExecutor());
				} else {
					deeplTranslateInstance = new DeepLTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = deeplTranslateInstance.useTranslator();
				break;
			case "Azure Translate":
				AzureTranslation azureTranslateInstance;
				if (isInitializing) {
					azureTranslateInstance = new AzureTranslation(mainConfig.getString("Translator.azureAPIKey"),
							mainConfig.getString("Translator.azureRegion"),
							true, main.getCallbackExecutor());
				} else {
					azureTranslateInstance = new AzureTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = azureTranslateInstance.useTranslator();
				break;
			case "Systran Translate":
				SystranTranslation systranTranslateInstance;
				if (isInitializing) {
					systranTranslateInstance = new SystranTranslation(mainConfig.getString("Translator.systranAPIKey"),
							true, main.getCallbackExecutor());
				} else {
					systranTranslateInstance = new SystranTranslation(inMessage,
							inLangCode, outLangCode, main.getCallbackExecutor());
				}
				out = systranTranslateInstance.useTranslator();
				break;
			case "JUnit/MockBukkit Testing Translator":
				TestTranslation testTranslator;
				if (isInitializing) {
					testTranslator = new TestTranslation(
							"TXkgYm95ZnJpZW5kICgyMk0pIHJlZnVzZXMgdG8gZHJpbmsgd2F0ZXIgdW5sZXNzIEkgKDI0RikgZHllIGl0IGJsdWUgYW5kIGNhbGwgaXQgZ2FtZXIganVpY2Uu", true, main.getCallbackExecutor());
				} else {
					testTranslator = new TestTranslation(inMessage, inLangCode,
							outLangCode, main.getCallbackExecutor());
				}
				out = testTranslator.useTranslator();
				break;
			default:
				// Get here if we are adding a new translation service
				debugMsg("No valid translator currently in use, according to translateText(). Returning original message...");
				return inMessage;
		}
		return out;
	}
	
	/**
	 * Sends a message that console cannot have a translation session for itself.
	 * @param sender - Who will receive the message
	 * @return Returns false, so that a command can return this method.
	 */
	public boolean sendNoConsoleChatMsg(CommandSender sender) {
		final TextComponent noConsoleChat = Component.text() // Cannot translate console chat
						.content(getMsg("wwctCannotTranslateConsole", new String[0]))
						.color(NamedTextColor.RED)
				.build();
		sendMsg(sender, noConsoleChat);
		return false;
	}
	
	/**
	 * Sends a message to console and a sender that a timeout exception has occured.
	 * @param sender - Who will receive the message besides console.
	 * @return Returns true, so that a command can return this method.
	 */
	public boolean sendTimeoutExceptionMsg(CommandSender sender) {
		if (sender instanceof Player) {
			main.getLogger().warning(getMsg("wwcTimeoutExceptionConsole", sender.getName()));
		}
		final TextComponent timeoutException = Component.text()
						.content(getMsg("wwcTimeoutException"))
						.color(NamedTextColor.YELLOW)
				.build();
		sendMsg(sender, timeoutException);
		return true;
	}
	
	/**
	  * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
	  * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
	  * @return Boolean - Whether the server is reloading/stopping or not
	  */
	public boolean serverIsStopping() {
		if (!main.isEnabled()) return true;
		try {
			new BukkitRunnable() {
				@Override
				public void run() {}
			}.runTask(main);
		} catch (Exception e) {
			debugMsg("Server is stopping! Don't run a task/do any dumb shit.");
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
	public Prompt genericConfigConvo(boolean preCheck, ConversationContext context, String successfulChangeMsg, String[] configValName, Object[] configVal, SmartInventory prevInventory) {
		if (configValName.length != configVal.length) {
			return null;
		}
		if (preCheck) {
			for (int i = 0; i < configValName.length; i++) {
				main.getConfigManager().getMainConfig().set(configValName[i], configVal[i]);
			}
			main.addPlayerUsingConfigurationGUI(((Player)context.getForWhom()).getUniqueId());
			final TextComponent successfulChange = Component.text()
							.content(getMsg(successfulChangeMsg))
							.color(NamedTextColor.GREEN)
					.build();
			sendMsg((Player)context.getForWhom(), successfulChange);
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
	public Prompt genericConfigConvo(boolean preCheck, ConversationContext context, String successfulChangeMsg, String configValName, Object configVal, SmartInventory prevInventory) {
		return genericConfigConvo(preCheck, context, successfulChangeMsg, new String[] {configValName}, new Object[] {configVal}, prevInventory);
	}
	
	/**
	  * Checks if a provided exception is a no confidence one/one to be ignored from our target translator.
	  * @param throwable - The exception to be checked
	  * @return Boolean - If exception is no confidence, true; false otherwise
	  */
	private boolean isErrorToIgnore(Throwable throwable) {
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
	private void detectColorCodes(String inMessage, Player currPlayer) {
		if ((inMessage.contains("&") && !main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"))
				&& !(main.getActiveTranslator(currPlayer)
						.getCCWarning())) // check if user has already been sent CC warning
		{
			sendFancyMsg("watsonColorCodeWarning", "", "&d&o", currPlayer);
			main.getActiveTranslator(currPlayer)
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
	private boolean checkForRateLimits(int delay, ActiveTranslator currActiveTranslator, CommandSender sender) {
		if (!(currActiveTranslator.getRateLimitPreviousTime().equals("None"))) {
			Instant previous = Instant.parse(currActiveTranslator.getRateLimitPreviousTime());
			Instant currTime = Instant.now();
			if (currTime.compareTo(previous.plus(delay, ChronoUnit.SECONDS)) < 0) {
				final TextComponent rateLimit = Component.text()
						.content(getMsg("wwcRateLimit", "" + ChronoUnit.SECONDS.between(currTime,
								previous.plus(delay, ChronoUnit.SECONDS))))
								.color(NamedTextColor.YELLOW)
						.build();
				sendMsg(sender, rateLimit);
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
	private String checkForRateLimitPermissions(Player currPlayer) {
		Set<PermissionAttachmentInfo> perms = currPlayer.getEffectivePermissions();
		for (PermissionAttachmentInfo perm : perms) {
			if (perm.getPermission().startsWith("worldwidechat.ratelimit.")) {
				return perm.getPermission();
			}
		}
		return "";
	}

	public void badPermsMessage(String correctPerm, CommandSender sender) {
		sendFancyMsg("wwcBadPerms", "&6" + correctPerm, "&c", sender);
	}

	static class ISOLanguage {
		@JsonProperty("int")
		private List<String> intNames;
		@JsonProperty("native")
		private List<String> nativeNames;

		// Getters and Setters
		public List<String> getIntNames() { return intNames; }
		public void setIntNames(List<String> intNames) { this.intNames = intNames; }
		public List<String> getNativeNames() { return nativeNames; }
		public void setNativeNames(List<String> nativeNames) { this.nativeNames = nativeNames; }
		public String getIntName() { return intNames.get(0); }
		public String getNativeName() { return nativeNames.get(0); }
	}
}