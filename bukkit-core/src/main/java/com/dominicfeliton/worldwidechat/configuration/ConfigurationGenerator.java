package com.dominicfeliton.worldwidechat.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigurationGenerator {

    private WorldwideChat main = WorldwideChat.instance;

    private ConfigurationHandler handler;

    public ConfigurationGenerator(ConfigurationHandler in) {
        handler = in;
    }

    public YamlConfiguration setupConfig(File file) {
        /* Generate config file, if it does not exist */
        String fileName = file.getName();
        if (!file.exists()) {
            main.saveResource(fileName, false);
        }

        /* Load template config */
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(main.getResource(fileName), StandardCharsets.UTF_8));

        /* Add default options, if they do not exist */
        config.setDefaults(templateConfig);

        /* Copy and save defaults */
        config.options().copyDefaults(true);

        return config;
    }

}
