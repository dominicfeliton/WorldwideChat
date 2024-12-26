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
import org.bukkit.event.Listener;

public class CitizensListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();
    protected WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    @EventHandler
    public void onSpeech(SpeechEvent event) {
        // This event is on the main thread

        // Who is the target/bystander? If not a Player, we can't translate for them.
        Talkable target = event.getContext().getTalker();
        if (target == null || !(target.getEntity() instanceof Player)) {
            refs.debugMsg("Not a player");
            return;
        }
        Player player = (Player) target.getEntity();

        // We are a translator and translating entities
        if (!main.isActiveTranslator(player) || !main.getActiveTranslator(player).getTranslatingEntity()) {
            refs.debugMsg("Player does not have citizens translation enabled");
            return;
        }

        // Cancel event + gather info
        refs.debugMsg("Attempting to translate citizen");
        event.setCancelled(true);
        String npcName = event.getContext().getTalker().getName();
        String originalMessage = event.getMessage();

        GenericRunnable chat = new GenericRunnable() {
            @Override
            protected void execute() {
                // Translate for this specific player
                String translated = refs.translateText(originalMessage, player);

                refs.sendMsg(player, formatMessage(npcName, player, translated, originalMessage));
            }
        };
        wwcHelper.runAsync(chat, WorldwideChatHelper.SchedulerType.ASYNC);
    }

    private Component formatMessage(String npcName, Player targetPlayer, String translation, String original) {
        // Vault Support (if it exists)
        Component outMsg = refs.getVaultMessage(targetPlayer, refs.deserial(translation), refs.deserial(npcName));

        // Add hover text w/original message
        if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
            refs.debugMsg("Add hover!");
            outMsg = outMsg
                    .hoverEvent(HoverEvent.showText(refs.getVaultHoverMessage(null, refs.deserial(original), refs.deserial(npcName), targetPlayer)));
        }

        return outMsg;
    }

}
