package com.expl0itz.worldwidechat.runnables;

import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;

import co.aikar.taskchain.TaskChainTasks.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SignTranslation implements Task<Sign, Sign>{

    private PlayerInteractEvent event;
    private WorldwideChat main = WorldwideChat.getInstance();
    
    public SignTranslation(PlayerInteractEvent e) {
        event = e;
    }

    @Override
    public Sign run(Sign input) {
        /* Get valid sign */
        Sign currentSign = (Sign) input;
        
        /* Init vars */
        String[] signText = currentSign.getLines();
        String[] changedSignText = new String[signText.length];
        boolean textLimit = false;
        boolean oneOrMoreLinesFailed = false;
        
        /* Send message */
        final TextComponent signStart = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslateStart")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
            .build();
        main.adventure().sender(event.getPlayer()).sendMessage(signStart);
        
        /* Translate each line of sign */
        for (int i = 0; i < changedSignText.length; i++) {
            String eaLine = CommonDefinitions.translateText(signText[i], event.getPlayer());
            /* Save translated line */
            if (eaLine.length() > 15) {
                textLimit = true;
            }
            if (eaLine.equals(signText[i]) && !signText[i].equals("")) {
            	oneOrMoreLinesFailed = true;
            }
            changedSignText[i] = eaLine;
        }
            /* Change sign for this user only, if translationNotTooLong and sign still exists*/
            if (!textLimit && !oneOrMoreLinesFailed && currentSign.getLocation() != null) {
                /* Set completed message */
                getCurrentChain().setTaskData("translatedSign", changedSignText);
                final TextComponent signDone = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignDone")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(signDone);
            } 
            else if (oneOrMoreLinesFailed && currentSign.getLocation() != null) {
            	/* Even though one or more lines failed, set completed message */
                getCurrentChain().setTaskData("translatedSign", changedSignText);
                final TextComponent signDone = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignTranslationFail")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(signDone);
            } else if (textLimit) {
                /* Format sign for chat, if translation exceeds 15 chars or sign was already deleted */
                String out = "\n";
                for (String eaLine : changedSignText) {
                    if (eaLine.length() > 1)
                        out += eaLine + "\n";
                }
                
                /* If we are here, sign is too long or deleted msg */
                final TextComponent translationNoticeMsg = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcSignDeletedOrTooLong")).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, true))
                    .append(Component.text().content("\n" + "---------------").color(NamedTextColor.GOLD)
                    .append(Component.text().content(out).color(NamedTextColor.WHITE))
                    .append(Component.text().content("---------------").color(NamedTextColor.GOLD)))
                    .build();
                main.adventure().sender(event.getPlayer()).sendMessage(translationNoticeMsg);
                getCurrentChain().setTaskData("translatedSign", null);
            }
            return null;
    }

}
