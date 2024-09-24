package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class ConfigListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;

    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryCloseEvent(InventoryCloseEvent e) {
        /* Send the user a message if they quit a config GUI without saving properly */
        Player currPlayer = (Player) e.getPlayer();
        GenericRunnable out = new GenericRunnable() {
            @Override
            protected void execute() {
                try {
                    if (main.isPlayerUsingGUI(currPlayer)
                            && currPlayer.getOpenInventory().getType() != InventoryType.CHEST
                            && !(currPlayer).isConversing()
                            && !(main.getPlayerDataUsingGUI(currPlayer).length > 0)) {
                        refs.sendMsg("wwcConfigGUIChangesNotSaved", "", "&e", currPlayer);
                        main.removePlayerUsingConfigurationGUI(currPlayer);
                        refs.playSound(CommonRefs.SoundType.PENDING_RELOAD, currPlayer);
                    }
                } catch (IncompatibleClassChangeError | Exception ex) {
                    // usually on lower MC versions, just disregard
                    refs.debugMsg(ex.getMessage());
                    refs.debugMsg("No config gui notif, exception above encountered...");
                }
            }
        };
        wwcHelper.runSync(true, 10, out, ENTITY, new Object[]{currPlayer});
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookOpenForConfigEvent(PlayerEditBookEvent e) {
        Player p = e.getPlayer();
        if (!main.isPlayerUsingGUI(p)) {
            refs.debugMsg("Player is not using GUI!");
            return;
        }

        Object[] data = main.getPlayerDataUsingGUI(p);
        if (data != null && data.length >= 4
                && data[0] instanceof YamlConfiguration
                && data[1] instanceof String
                && data[2] instanceof SmartInventory
                && data[3] instanceof GenericRunnable) {
            YamlConfiguration config = (YamlConfiguration) data[0];
            String key = (String) data[1];
            SmartInventory inv = (SmartInventory) data[2];
            GenericRunnable timeout = (GenericRunnable) data[3];

            if (e.getPreviousBookMeta().getPages().equals(e.getNewBookMeta().getPages())) {
                refs.debugMsg("Book UNCHANGED! Do not push changes...");
                cleanupConfigBook(e, inv, timeout, false);
                return;
            }

            StringBuilder out = new StringBuilder();
            for (String eaPage : e.getNewBookMeta().getPages()) {
                out.append(eaPage);
            }

            config.set(key, out.toString());
            refs.debugMsg("Wrote " + out.length() + " chars to " + key + "! Cancelling event, our output only.");
            refs.sendMsg("wwcLargeConfigInputSuccess", "" + out.length(), "&a", p);

            cleanupConfigBook(e, inv, timeout, true);
        } else {
            refs.debugMsg("Invalid/no data in GUI map, not opening book and removing!");
            main.removePlayerUsingConfigurationGUI(p);
        }
    }

    private void cleanupConfigBook(PlayerEditBookEvent e, SmartInventory inv, GenericRunnable timeout, boolean changed) {
        // always use old meta because we cancel the event
        Player p = e.getPlayer();
        ItemMeta meta = e.getPreviousBookMeta();
        e.setCancelled(true);

        refs.debugMsg("Schedule to delete book!");

        // cancel delayed task then manually remove items
        timeout.cancel();
        GenericRunnable remove = new GenericRunnable() {
            @Override
            protected void execute() {
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null && item.hasItemMeta() && item.getItemMeta().equals(meta)) {
                        p.getInventory().removeItem(item);
                        break;
                    }
                }
            }
        };
        wwcHelper.runSync(true, 10, remove, ENTITY, new Object[]{p});

        refs.debugMsg("Now opening previous GUI!");
        if (changed) {
            main.addPlayerUsingConfigurationGUI(p); // add player back as normal, queues up reminder message
        } else {
            main.removePlayerUsingConfigurationGUI(p);
        }
        inv.open(p);
    }
}
