package com.dominicfeliton.worldwidechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class ChatPacketListener {
    private final WorldwideChat main = WorldwideChat.instance;
    private final CommonRefs refs = main.getServerFactory().getCommonRefs();
    private final WorldwideChatHelper helper = main.getServerFactory().getWWCHelper();

    public ChatPacketListener() {
        registerChatListener();
    }

    private void registerChatListener() {
        main.getProtocolManager().addPacketListener(new PacketAdapter(main,
                PacketType.Play.Server.SYSTEM_CHAT  // 1.19+ system messages only
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled()) return;

                refs.debugMsg("Not cancelled! (chat packets)");
                Player player = event.getPlayer();
                if (player == null) return;
                if (!main.isActiveTranslator(player)) return;
                if (!main.getActiveTranslator(player).getTranslatingChatIncoming()) return;

                PacketContainer packet = event.getPacket();
                String message = extractMessage(packet);

                if (message == null) return;

                // Prevent the packet from being sent immediately
                event.setCancelled(true);

                // Process the message asynchronously
                GenericRunnable translate = new GenericRunnable() {
                    @Override
                    protected void execute() {
                        String translated = translateMessage(message, player);

                        // Update the packet on the main thread
                        GenericRunnable update = new GenericRunnable() {
                            @Override
                            protected void execute() {
                                try {
                                    updatePacket(packet, translated);
                                    // Resend the modified packet
                                    main.getProtocolManager().sendServerPacket(player, packet, false);
                                } catch (Exception e) {
                                    main.getLogger().warning("Error sending translated packet: " + e.getMessage());
                                    // If translation fails, send the original packet
                                    main.getProtocolManager().sendServerPacket(player, event.getPacket(), false);
                                }
                            }
                        };
                        helper.runSync(update, ENTITY, new Object[] {player});
                    }
                };
                helper.runAsync(translate, ASYNC);
            }
        });
    }

    private String extractMessage(PacketContainer packet) {
        try {
            if (packet.getStrings().size() > 0) {
                // Some 1.19+ versions send raw JSON string
                return packet.getStrings().read(0);
            } else {
                // Others use chat components
                WrappedChatComponent component = packet.getChatComponents().read(0);
                return component.getJson();
            }
        } catch (Exception e) {
            main.getLogger().warning("Error extracting message from packet: " + e.getMessage());
            return null;
        }
    }

    private String translateMessage(String jsonMessage, Player player) {
        try {
            // Parse the JSON message
            BaseComponent[] components = ComponentSerializer.parse(jsonMessage);

            // Here you would implement your translation logic
            BaseComponent[] translatedComponents = translateComponents(components, player);

            // Convert back to JSON
            return ComponentSerializer.toString(translatedComponents);
        } catch (Exception e) {
            main.getLogger().warning("Error translating message: " + e.getMessage());
            return jsonMessage; // Return original if translation fails
        }
    }

    private BaseComponent[] translateComponents(BaseComponent[] components, Player player) {
        String[] translatedComponents = new String[components.length];

        // Convert each component to JSON
        for (int i = 0; i < components.length; i++) {
            translatedComponents[i] = ComponentSerializer.toString(new BaseComponent[]{components[i]});
        }

        // Translate the JSON strings
        refs.translateText(translatedComponents, player, true);

        // Create a new list to hold all parsed components
        List<BaseComponent> finalComponents = new ArrayList<>();

        // Parse each translated JSON string and add all resulting components to our list
        for (String translatedJson : translatedComponents) {
            BaseComponent[] parsed = ComponentSerializer.parse(translatedJson);
            Collections.addAll(finalComponents, parsed);
        }

        // Convert list back to array
        return finalComponents.toArray(new BaseComponent[0]);
    }

    private void updatePacket(PacketContainer packet, String translatedJson) {
        try {
            if (packet.getStrings().size() > 0) {
                packet.getStrings().write(0, translatedJson);
            } else {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(translatedJson));
            }
        } catch (Exception e) {
            main.getLogger().warning("Error updating packet with translated message: " + e.getMessage());
        }
    }
}