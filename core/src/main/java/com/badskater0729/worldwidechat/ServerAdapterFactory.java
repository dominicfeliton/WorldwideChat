package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ServerAdapterFactory {

    private WorldwideChat main = WorldwideChat.instance;

    private CommonRefs refs = new CommonRefs();

    private ServerAdapter currAdapter = main.getAdapter();

    private String currPlatform = refs.getServerInfo().getKey();

    //TODO: Use reflectionutils?

    public CommonRefs getCommonRefs() {
        try {
            switch (currPlatform) {
                case "Spigot", "Bukkit":
                    main.getLogger().info("sad");
                    Class clazz = Class.forName("com.badskater0729.worldwidechat.util.CommonRefs");
                    Constructor<?> ctor = clazz.getConstructor(); // Assuming the constructor takes a String for eaVer
                    return (CommonRefs) ctor.newInstance(); // Cast to your ServerAdapter interface or appropriate type
                default:
                    main.getLogger().info("We are using paper adapter supposedly????");
                    Class paperClazz = Class.forName("com.badskater0729.worldwidechat.util.PaperCommonRefs");
                    Constructor<?> paperCtor = paperClazz.getConstructor(); // Assuming the constructor takes a String for eaVer
                    return (CommonRefs) paperCtor.newInstance(); // Cast to your ServerAdapter interface or appropriate type
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
            main.getLogger().severe("Failed to initialize CommonRefs from ServerAdapterFactory!");
            main.getLogger().severe("Please contact the developer if your server platform is supported!");
            return null;
        }
    }

}
