package com.expl0itz.worldwidechat.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
import com.expl0itz.worldwidechat.runnables.SignTranslation;

import co.aikar.taskchain.TaskChain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SignReadListener implements Listener {
    
    private WorldwideChat main = WorldwideChat.getInstance();
    
    private Material[] signList = {
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
            Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
            Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN,
            Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
            Material.OAK_SIGN, Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
            Material.WARPED_SIGN, Material.WARPED_WALL_SIGN
    };
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingSign() 
                && event.getClickedBlock() != null) {
            for (Material eaMaterial : signList) {
                if (event.getClickedBlock().getType() == eaMaterial && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    try {
                        /* Modify or create new player record*/
                        PlayerRecord currPlayerRecord = main.getPlayerRecord(event.getPlayer().getUniqueId().toString());
                        currPlayerRecord.setAttemptedTranslations(currPlayerRecord.getAttemptedTranslations()+1);
                        currPlayerRecord.writeToConfig();
                        
                        /* Start sign translation */
                        Sign currentSign = (Sign) event.getClickedBlock().getState();
                        TaskChain<?> chain = WorldwideChat.newSharedChain("signTranslate");
                        chain
                            .async(() -> {
                                new SignTranslation(event).run(currentSign);
                            })
                            .sync(() -> {
                                String[] translatedSign = chain.getTaskData("translatedSign");
                                if (translatedSign != null) {
                                    event.getPlayer().sendSignChange(currentSign.getLocation(), translatedSign);
                                }
                            })
                            .sync(TaskChain::abort)
                            .execute();
                    } catch (Exception e) {
                        final TextComponent translationFailMsg = Component.text()
                                .append(main.getPluginPrefix().asComponent())
                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslationFail").replace("%i", main.getTranslatorName())).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                                .build();
                            main.adventure().sender(event.getPlayer()).sendMessage(translationFailMsg);
                    }
                }
            }
        }
    }
}
