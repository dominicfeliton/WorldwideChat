package com.expl0itz.worldwidechat.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.runnables.SignTranslation;

import co.aikar.taskchain.TaskChain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SignReadListener implements Listener {
    
    private WorldwideChat main = WorldwideChat.getInstance();
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingSign() 
                && event.getClickedBlock() != null) {
            if ((event.getClickedBlock().getType().name().contains("SIGN") && event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                try {
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
