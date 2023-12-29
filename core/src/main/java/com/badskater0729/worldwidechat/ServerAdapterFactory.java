package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.util.CommonRefs;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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

                // We found class but continue loop, may be a fork (Bukkit -> Spigot -> Paper)
                serverPlatform = entry.getKey();
            } catch (ClassNotFoundException e) {}
        }

        /* Version check */
        switch (serverPlatform) {
            case "Bukkit":
            case "Spigot":
            case "Paper":
                // TODO: Use reflection for this method
                serverVersion = Bukkit.getServer().getVersion();
                break;
            case "Folia":
                // TODO
                serverVersion = "";
                break;
            default:
                serverVersion = "Bukkit"; // Default to Bukkit if N/A
                break;
        }

        return Pair.of(serverPlatform, serverVersion);
    }

    public CommonRefs getCommonRefs() {
        HashMap<String, String> commonRefsDefs = new HashMap<String, String>();
        commonRefsDefs.put("Spigot","com.badskater0729.worldwidechat.util.CommonRefs");
        commonRefsDefs.put("Bukkit","com.badskater0729.worldwidechat.util.CommonRefs");
        commonRefsDefs.put("Paper","com.badskater0729.worldwidechat.util.PaperCommonRefs");

        return (CommonRefs) getInstance(commonRefsDefs);
    }

    public WorldwideChatHelper getWWCHelper() {
        HashMap<String, String> wwcHelperDefs = new HashMap<String, String>();
        wwcHelperDefs.put("Spigot","com.badskater0729.worldwidechat.WorldwideChatHelper");
        wwcHelperDefs.put("Bukkit","com.badskater0729.worldwidechat.WorldwideChatHelper");
        wwcHelperDefs.put("Paper","com.badskater0729.worldwidechat.PaperWorldwideChatHelper");

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
                } else {
                    // Return a class with no parameters in constructor
                    if (entry.getKey().equals(currPlatform)) {
                        main.getLogger().info("Going with: " + entry.getKey() + " and " + entry.getValue());
                        Class clazz = Class.forName(entry.getValue());
                        Constructor<?> constClazz = clazz.getConstructor();
                        return constClazz.newInstance();
                    }
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