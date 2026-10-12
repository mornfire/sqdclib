package com.mornfire.sqdclib.dialogue;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.dialogue.net.DialogueClosePayload;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Dialogues {
    public static final int CLOSE_INDEX = -1;
    public static final int MAX_AUTO_ADVANCE = 64;

    public static final ResourceKey<Registry<DialogueDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "story_dialogue"));

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private record Session(ResourceLocation dialogue, String node) {}

    public static Registry<DialogueDefinition> registry(Player player) {
        return player.level().registryAccess().registryOrThrow(REGISTRY_KEY);
    }

    public static Optional<DialogueDefinition> definition(Player player, ResourceLocation dialogue) {
        return registry(player).getOptional(dialogue);
    }

    public static boolean isTalking(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    public static ResourceLocation currentDialogue(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        return session == null ? null : session.dialogue();
    }

    public static boolean start(ServerPlayer player, ResourceLocation dialogue) {
        Optional<DialogueDefinition> definition = definition(player, dialogue);
        if (definition.isEmpty()) {
            return false;
        }
        show(player, dialogue, definition.get(), definition.get().start());
        return true;
    }

    public static void select(ServerPlayer player, int index) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        if (index == CLOSE_INDEX) {
            stop(player);
            return;
        }
        Optional<DialogueDefinition> definition = definition(player, session.dialogue());
        if (definition.isEmpty()) {
            stop(player);
            return;
        }
        DialogueNode node = definition.get().nodes().get(session.node());
        if (node == null) {
            stop(player);
            return;
        }
        List<DialogueOption> available = availableOptions(player, node);
        if (index < 0 || index >= available.size()) {
            return;
        }
        DialogueOption option = available.get(index);
        StoryAction.runAll(player, option.actions());
        if (option.next().isPresent()) {
            show(player, session.dialogue(), definition.get(), option.next().get());
        } else {
            stop(player);
        }
    }

    public static void stop(ServerPlayer player) {
        if (SESSIONS.remove(player.getUUID()) != null) {
            PacketDistributor.sendToPlayer(player, new DialogueClosePayload());
        }
    }

    public static List<DialogueOption> availableOptions(Player player, DialogueNode node) {
        List<DialogueOption> available = new ArrayList<>();
        for (DialogueOption option : node.options()) {
            if (option.condition().isEmpty() || option.condition().get().test(player)) {
                available.add(option);
            }
        }
        return available;
    }

    private static void show(ServerPlayer player, ResourceLocation dialogue, DialogueDefinition definition,
            String startNode) {
        String current = startNode;
        for (int guard = 0; guard < MAX_AUTO_ADVANCE; guard++) {
            DialogueNode node = definition.nodes().get(current);
            if (node == null) {
                stop(player);
                return;
            }
            StoryAction.runAll(player, node.actions());
            List<DialogueOption> available = availableOptions(player, node);
            if (!available.isEmpty()) {
                SESSIONS.put(player.getUUID(), new Session(dialogue, current));
                PacketDistributor.sendToPlayer(player, new DialogueOpenPayload(
                        dialogue,
                        current,
                        node.speaker(),
                        node.text(),
                        available.stream().map(DialogueOption::text).toList()));
                return;
            }
            if (node.next().isEmpty()) {
                stop(player);
                return;
            }
            current = node.next().get();
        }
        stop(player);
    }

    private Dialogues() {}
}
