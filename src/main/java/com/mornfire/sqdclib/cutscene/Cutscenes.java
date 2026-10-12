package com.mornfire.sqdclib.cutscene;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.net.CutsceneStartPayload;
import com.mornfire.sqdclib.cutscene.net.CutsceneStopPayload;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Cutscenes {
    public static final ResourceKey<Registry<CutsceneDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "story_cutscene"));

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private static final class Session {
        private final ResourceLocation cutscene;
        private final CutsceneDefinition definition;
        private final Set<Integer> fired = new HashSet<>();
        private int tick;

        private Session(ResourceLocation cutscene, CutsceneDefinition definition) {
            this.cutscene = cutscene;
            this.definition = definition;
        }
    }

    public static Registry<CutsceneDefinition> registry(Player player) {
        return player.level().registryAccess().registryOrThrow(REGISTRY_KEY);
    }

    public static Optional<CutsceneDefinition> definition(Player player, ResourceLocation cutscene) {
        return registry(player).getOptional(cutscene);
    }

    public static boolean isPlaying(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    public static ResourceLocation currentCutscene(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        return session == null ? null : session.cutscene;
    }

    public static boolean play(ServerPlayer player, ResourceLocation cutscene) {
        Optional<CutsceneDefinition> definition = definition(player, cutscene);
        if (definition.isEmpty()) {
            return false;
        }
        SESSIONS.put(player.getUUID(), new Session(cutscene, definition.get()));
        PacketDistributor.sendToPlayer(player, new CutsceneStartPayload(definition.get()));
        return true;
    }

    public static void tick(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        session.tick++;
        List<CutsceneEvent> events = session.definition.events();
        for (int i = 0; i < events.size(); i++) {
            CutsceneEvent event = events.get(i);
            if (event.time() == session.tick && session.fired.add(i)) {
                StoryAction.runAll(player, event.actions());
            }
        }
        if (session.tick >= session.definition.duration()) {
            stop(player);
        }
    }

    public static void stop(ServerPlayer player) {
        if (SESSIONS.remove(player.getUUID()) != null) {
            PacketDistributor.sendToPlayer(player, new CutsceneStopPayload());
        }
    }

    private Cutscenes() {}
}
