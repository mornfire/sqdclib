package com.mornfire.sqdclib.cutscene;

import com.mojang.brigadier.StringReader;
import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.event.CutsceneFinishedEvent;
import com.mornfire.sqdclib.cutscene.net.CutsceneStartPayload;
import com.mornfire.sqdclib.cutscene.net.CutsceneStopPayload;
import com.mornfire.sqdclib.cutscene.net.QteStartPayload;
import com.mornfire.sqdclib.cutscene.net.StandInPayload;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class Cutscenes {
    public static final ResourceKey<Registry<CutsceneDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "story_cutscene"));

    private static final double NEAREST_RADIUS = 16.0D;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private record PendingQte(int id, QteCue cue, int deadline) {}

    private static final class Session {
        private final ResourceLocation cutscene;
        private final CutsceneDefinition definition;
        private final Set<Integer> fired = new HashSet<>();
        private final List<Runnable> finishers = new ArrayList<>();
        private int tick;
        private int qteCounter;
        private boolean standIn;
        @Nullable
        private PendingQte pending;

        private Session(ResourceLocation cutscene, CutsceneDefinition definition, @Nullable Runnable onFinish) {
            this.cutscene = cutscene;
            this.definition = definition;
            if (onFinish != null) {
                this.finishers.add(onFinish);
            }
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

    public static boolean isSkippable(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        return session != null && session.definition.skippable();
    }

    public static ResourceLocation currentCutscene(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        return session == null ? null : session.cutscene;
    }

    public static boolean play(ServerPlayer player, ResourceLocation cutscene) {
        return play(player, cutscene, null);
    }

    public static boolean play(ServerPlayer player, ResourceLocation cutscene, @Nullable Runnable onFinish) {
        Optional<CutsceneDefinition> definition = definition(player, cutscene);
        if (definition.isEmpty()) {
            return false;
        }
        Session previous = SESSIONS.get(player.getUUID());
        if (previous != null) {
            PacketDistributor.sendToPlayer(player, new CutsceneStopPayload());
        }
        SESSIONS.put(player.getUUID(), new Session(cutscene, definition.get(), onFinish));
        PacketDistributor.sendToPlayer(player, new CutsceneStartPayload(definition.get()));
        return true;
    }

    public static int playFor(Collection<ServerPlayer> players, ResourceLocation cutscene) {
        int started = 0;
        for (ServerPlayer player : players) {
            if (play(player, cutscene)) {
                started++;
            }
        }
        return started;
    }

    public static int playNear(ServerPlayer anchor, double radius, ResourceLocation cutscene) {
        double limit = radius * radius;
        List<ServerPlayer> nearby = new ArrayList<>();
        for (ServerPlayer candidate : anchor.serverLevel().getServer().getPlayerList().getPlayers()) {
            if (candidate == anchor
                    || candidate.level() == anchor.level() && candidate.distanceToSqr(anchor) <= limit) {
                nearby.add(candidate);
            }
        }
        return playFor(nearby, cutscene);
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
                applyEvent(player, session, event, false);
            }
        }
        PendingQte pending = session.pending;
        if (pending != null && session.tick >= pending.deadline()) {
            resolveQte(player, pending.id(), false);
        }
        if (session.tick >= session.definition.duration()) {
            finish(player, session);
        }
    }

    public static boolean skip(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !session.definition.skippable()) {
            return false;
        }
        List<CutsceneEvent> events = session.definition.events();
        for (int i = 0; i < events.size(); i++) {
            if (session.fired.add(i)) {
                applyEvent(player, session, events.get(i), true);
            }
        }
        finish(player, session);
        return true;
    }

    public static void resolveQte(ServerPlayer player, int id, boolean success) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        PendingQte pending = session.pending;
        if (pending == null || pending.id() != id) {
            return;
        }
        session.pending = null;
        QteCue cue = pending.cue();
        StoryAction.runAll(player, success ? cue.onSuccess() : cue.onFail());
    }

    public static void stop(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null) {
            finish(player, session);
        }
    }

    private static void applyEvent(ServerPlayer player, Session session, CutsceneEvent event, boolean skipping) {
        event.animation().ifPresent(cue -> runAnimation(player, cue));
        event.standIn().ifPresent(cue -> {
            session.standIn = cue.hide();
            PacketDistributor.sendToPlayer(player, new StandInPayload(
                    cue.hide(), player.getUUID(), player.getYRot(), cue.keys()));
        });
        event.qte().ifPresent(cue -> {
            if (skipping) {
                StoryAction.runAll(player, cue.onSuccess());
            } else {
                startQte(player, session, cue);
            }
        });
        StoryAction.runAll(player, event.actions());
    }

    private static void startQte(ServerPlayer player, Session session, QteCue cue) {
        session.qteCounter++;
        int id = session.qteCounter;
        session.pending = new PendingQte(id, cue, session.tick + Math.max(1, cue.duration()));
        PacketDistributor.sendToPlayer(player, new QteStartPayload(
                id, cue.kind(), cue.key(), Math.max(1, cue.duration()), cue.prompt()));
    }

    private static void runAnimation(ServerPlayer player, AnimationCue cue) {
        if (!AnimationBridge.available()) {
            return;
        }
        Entity target = resolveTarget(player, cue.entity());
        if (target != null) {
            AnimationBridge.trigger(target, cue);
        }
    }

    @Nullable
    private static Entity resolveTarget(ServerPlayer player, @Nullable String selector) {
        String raw = selector == null ? "" : selector.trim();
        if (raw.isEmpty() || raw.equals(AnimationCue.SELF) || raw.equals(AnimationCue.PLAYER)) {
            return player;
        }
        if (raw.equals(AnimationCue.NEAREST)) {
            List<Entity> found = player.level().getEntities(player,
                    new AABB(player.blockPosition()).inflate(NEAREST_RADIUS),
                    entity -> !(entity instanceof Player));
            return found.isEmpty() ? null : found.get(0);
        }
        try {
            EntitySelectorParser parser = new EntitySelectorParser(new StringReader(raw), true);
            EntitySelector parsed = parser.parse();
            return parsed.findSingleEntity(player.createCommandSourceStack());
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static void finish(ServerPlayer player, Session session) {
        if (SESSIONS.remove(player.getUUID()) == null) {
            return;
        }
        if (session.standIn) {
            PacketDistributor.sendToPlayer(player, new StandInPayload(
                    false, player.getUUID(), player.getYRot(), List.of()));
        }
        session.pending = null;
        PacketDistributor.sendToPlayer(player, new CutsceneStopPayload());
        NeoForge.EVENT_BUS.post(new CutsceneFinishedEvent(player, session.cutscene));
        for (Runnable finisher : session.finishers) {
            finisher.run();
        }
    }

    private Cutscenes() {}
}
