package com.expl0itz.worldwidechat.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.runnables.BookTranslation;

import co.aikar.taskchain.TaskChain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BookReadListener implements Listener{

    private WorldwideChat main = WorldwideChat.getInstance();
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookRead(PlayerInteractEvent event) {
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) != null 
                && main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getTranslatingBook() 
                && event.getHand() != null 
                && event.getItem() != null
                && Material.WRITTEN_BOOK == event.getItem().getType()
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            /* Get vars, start book translation */
            try {
                TaskChain<?> chain = WorldwideChat.newSharedChain("bookTranslate");
                chain
                    .async(() -> {
                        new BookTranslation(event).run(null);
                    })
                    .sync(() -> {
                        ItemStack newBook = chain.getTaskData("translatedBook");
                        if (newBook != null) {
                            event.getPlayer().openBook(newBook);
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
