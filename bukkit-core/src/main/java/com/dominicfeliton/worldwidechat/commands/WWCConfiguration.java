package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WWCConfiguration extends BasicCommand {

    private WorldwideChat main = WorldwideChat.instance;

    public WWCConfiguration(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    @Override
    public boolean processCommand() {
        MenuGui menuGui = new MenuGui((Player) sender, main.getTranslatorName());
        menuGui.genAllConfigUIs();
        MenuGui.CONFIG_GUI_TAGS.GEN_SET.smartInv.open((Player) sender);
        return true;
    }

}
