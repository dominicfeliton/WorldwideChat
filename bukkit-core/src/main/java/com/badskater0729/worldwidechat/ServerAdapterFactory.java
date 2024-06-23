package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.util.CommonRefs;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServerAdapterFactory {

    private WorldwideChat main = WorldwideChat.instance;

    private String currPlatform = getServerInfo().getKey();

    /* Init server */
    public LinkedHashMap<String, String> getSupportedServerTypes() {
        // ALWAYS KEEP NEWEST FORKS FIRST
        // EX: Bukkit -> Spigot -> Paper
        LinkedHashMap<String, String> serverTypes = new LinkedHashMap<>();
        serverTypes.put("Bukkit", "org.bukkit.Bukkit");
        serverTypes.put("Spigot", "org.spigotmc.SpigotConfig");
        serverTypes.put("Paper", "com.destroystokyo.paper.PaperWorldConfig");
        serverTypes.put("Folia", "io.papermc.paper.plugin.configuration.PluginMeta");

        //serverTypes.put("BungeeCord", "net.md_5.bungee.api.ProxyServer");
        //serverTypes.put("Velocity", "com.velocitypowered.proxy.Velocity");

        return serverTypes;
    }

    /**
     * Check server type/version
     */
    public Pair<String, String> getServerInfo() {
        String serverPlatform = "Unknown";
        String serverVersion = "";

        /* Find specific server */
        for (Map.Entry<String, String> entry : getSupportedServerTypes().entrySet()) {
            try {
                Class.forName(entry.getValue());

                // If we are Folia, check if we have isFoliaSupported in PluginMeta class
                if (entry.getKey().equals("Folia")) Class.forName("io.papermc.paper.plugin.configuration.PluginMeta").getMethod("isFoliaSupported");

                // We found class but continue loop, may be a fork (Bukkit -> Spigot -> Paper)
                serverPlatform = entry.getKey();
            } catch (Exception e) {}
        }

        /* Version check */
        switch (serverPlatform) {
            case "Bukkit":
            case "Spigot":
            case "Paper":
            case "Folia":
                // TODO: Use reflection here
                serverVersion = Bukkit.getServer().getVersion();
                break;
            default:
                serverVersion = "Bukkit"; // Default to Bukkit if N/A
                break;
        }

        /* Additional checks */
        if (serverPlatform.equals("Paper") && (serverVersion.contains("1.13") || serverVersion.contains("1.14") || serverVersion.contains("1.15"))) {
            // These versions are so old that they lack much of what we take for granted in later versions of Paper.
            // Paper on these versions is unsupported. Use the spigot version of the plugin instead.
            // sendmsg(?) paper too old, default to spig
            serverPlatform = "Spigot";
        }

        if (serverPlatform.equals("Paper")) {
            try {
                Class.forName("com.badskater0729.worldwidechat.PaperWorldwideChatHelper");
            } catch (ClassNotFoundException e) {
                // On paper but using the spigot JAR
                // sendmsg(?) using spigot on paper, you are missing out on features...
                serverPlatform = "Spigot";
            }
        }

        return Pair.of(serverPlatform, serverVersion);
    }

    public CommonRefs getCommonRefs() {
        HashMap<String, String> commonRefsDefs = new HashMap<String, String>();
        commonRefsDefs.put("Spigot","com.badskater0729.worldwidechat.util.CommonRefs");
        commonRefsDefs.put("Bukkit","com.badskater0729.worldwidechat.util.CommonRefs");
        commonRefsDefs.put("Paper","com.badskater0729.worldwidechat.util.PaperCommonRefs");
        commonRefsDefs.put("Folia","com.badskater0729.worldwidechat.util.FoliaCommonRefs");

        return (CommonRefs) getInstance(commonRefsDefs);
    }

    public WorldwideChatHelper getWWCHelper() {
        HashMap<String, String> wwcHelperDefs = new HashMap<String, String>();
        wwcHelperDefs.put("Spigot","com.badskater0729.worldwidechat.SpigotWorldwideChatHelper");
        wwcHelperDefs.put("Bukkit","com.badskater0729.worldwidechat.SpigotWorldwideChatHelper");
        wwcHelperDefs.put("Paper","com.badskater0729.worldwidechat.PaperWorldwideChatHelper");
        wwcHelperDefs.put("Folia","com.badskater0729.worldwidechat.FoliaWorldwideChatHelper");

        return (WorldwideChatHelper) getInstance(wwcHelperDefs);
    }

    private Object getInstance(HashMap<String, String> platformAndClass) {
        return getInstance(platformAndClass, null);
    }

    private Object getInstance(HashMap<String, String> platformAndClass, Class<?>...parameterTypes) {
        try {
            for (Map.Entry<String, String> entry : platformAndClass.entrySet()) {
                // Return a class with parameters in constructor
                if (parameterTypes != null && entry.getKey().equals(currPlatform)) {
                    Class clazz = Class.forName(entry.getValue());
                    Constructor<?> constClazz = clazz.getConstructor(parameterTypes);
                    return constClazz.newInstance();
                // Return a class with no parameters in constructor (parameterTypes == null)
                } else if (entry.getKey().equals(currPlatform)) {
                    Class clazz = Class.forName(entry.getValue());
                    Constructor<?> constClazz = clazz.getConstructor();
                    return constClazz.newInstance();
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
        InvocationTargetException e) {
            e.printStackTrace();
        }

        // Shouldn't ever get here, 99% likely to be FATAL
        main.getLogger().severe("Failed to initialize definition from ServerAdapterFactory!");
        main.getLogger().severe("Please contact the developer if your server platform is supported!");
        return null;
    }

}
