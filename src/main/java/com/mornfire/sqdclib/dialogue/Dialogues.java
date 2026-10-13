package com.mornfire.sqdclib.dialogue;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.dialogue.net.DialogueClosePayload;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.storystate.StoryProgress;
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
        advance(player, dialogue, definition.get(), definition.get().start(), 0);
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
            present(player, session.dialogue(), definition.get(), session.node(), node, available);
            return;
        }
        DialogueOption option = available.get(index);
        StoryAction.runSequentially(player, option.actions(), () -> {
            if (option.next().isPresent()) {
                advance(player, session.dialogue(), definition.get(), option.next().get(), 0);
            } else {
                stop(player);
            }
        });
    }

    public static void stop(ServerPlayer player) {
        if (SESSIONS.remove(player.getUUID()) != null) {
            StoryProgress.of(player).clearDialogue();
            PacketDistributor.sendToPlayer(player, new DialogueClosePayload());
        }
    }

    public static void suspend(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
    }

    public static boolean resume(ServerPlayer player) {
        StoryProgress progress = StoryProgress.of(player);
        Optional<ResourceLocation> dialogue = progress.getDialogue();
        if (dialogue.isEmpty()) {
            return false;
        }
        Optional<DialogueDefinition> definition = definition(player, dialogue.get());
        if (definition.isEmpty()) {
            progress.clearDialogue();
            return false;
        }
        String nodeId = progress.getDialogueNode();
        DialogueNode node = definition.get().nodes().get(nodeId);
        if (node == null) {
            progress.clearDialogue();
            return false;
        }
        List<DialogueOption> available = availableOptions(player, node);
        if (available.isEmpty()) {
            progress.clearDialogue();
            return false;
        }
        present(player, dialogue.get(), definition.get(), nodeId, node, available);
        return true;
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

    private static void advance(ServerPlayer player, ResourceLocation dialogue, DialogueDefinition definition,
            String nodeId, int guard) {
        if (guard >= MAX_AUTO_ADVANCE) {
            stop(player);
            return;
        }
        DialogueNode node = definition.nodes().get(nodeId);
        if (node == null) {
            stop(player);
            return;
        }
        StoryAction.runSequentially(player, node.actions(), () -> {
            List<DialogueOption> available = availableOptions(player, node);
            if (!available.isEmpty()) {
                present(player, dialogue, definition, nodeId, node, available);
                return;
            }
            if (node.waitsForClick()) {
                present(player, dialogue, definition, nodeId, node, List.of(node.continueOption()));
                return;
            }
            if (node.next().isPresent()) {
                advance(player, dialogue, definition, node.next().get(), guard + 1);
                return;
            }
            stop(player);
        });
    }

    private static void present(ServerPlayer player, ResourceLocation dialogue, DialogueDefinition definition,
            String nodeId, DialogueNode node, List<DialogueOption> available) {
        SESSIONS.put(player.getUUID(), new Session(dialogue, nodeId));
        StoryProgress.of(player).setDialogue(dialogue, nodeId);
        PacketDistributor.sendToPlayer(player, new DialogueOpenPayload(
                dialogue,
                nodeId,
                node.speaker(),
                node.text(),
                available.stream().map(DialogueOption::text).toList(),
                definition.lockMovement()));
    }

    private Dialogues() {}
}
