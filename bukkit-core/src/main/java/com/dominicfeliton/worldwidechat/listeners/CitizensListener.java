package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.event.SpeechEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class CitizensListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();
    protected WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpeech(SpeechEvent event) {
        String originalMessage = event.getMessage();
        Set<Player> targets = new HashSet<>();

        // First pass: identify players who need translation
        for (Talkable target : event.getContext()) {
            if (target == null || !(target.getEntity() instanceof Player)) {
                refs.debugMsg("Not a player");
                continue;
            }

            Player player = (Player) target.getEntity();
            refs.debugMsg("Player " + player.getName() + " entities: " +
                    (main.isActiveTranslator(player) ? main.getActiveTranslator(player).getTranslatingEntity() : "not translator"));

            if (main.isActiveTranslator(player) && main.getActiveTranslator(player).getTranslatingEntity()) {
                refs.debugMsg("Adding player to translation list: " + player.getName());
                targets.add(player);
            }
        }

        // If we have players to translate for, cancel the original event
        if (!targets.isEmpty()) {
            final String msgWithName = originalMessage.replace("<npc>", event.getContext().getTalker().getName());
            event.setCancelled(true);

            // Send custom messages to players who need translation
            for (Player player : targets) {
                GenericRunnable chat = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        String translated = refs.translateText(msgWithName, player);

                        refs.sendMsg(player, refs.deserial(formatMessage(translated, msgWithName)), false);
                    }
                };
                wwcHelper.runAsync(chat, WorldwideChatHelper.SchedulerType.ASYNC);
            }

            // Send original message to players who don't need translation
            for (Talkable target : event.getContext()) {
                if (target != null && target.getEntity() instanceof Player) {
                    Player player = (Player) target.getEntity();
                    if (!targets.contains(player)) {
                        refs.sendMsg(player, refs.deserial(msgWithName), false);
                    }
                }
            }
        }
    }

    private String formatMessage(String translation, String original) {
        Component outMsg = refs.deserial(translation);

        if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
            refs.debugMsg("Add hover!");
            outMsg = outMsg.hoverEvent(HoverEvent.showText(refs.deserial(original)));
        }

        return refs.serial(outMsg);
    }
}