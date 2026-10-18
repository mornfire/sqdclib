package com.mornfire.sqdclib.cutscene.event;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.neoforged.bus.api.Event;

public class CutsceneFinishedEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation cutscene;
    private final Reason reason;

    public CutsceneFinishedEvent(ServerPlayer player, ResourceLocation cutscene, Reason reason) {
        this.player = player;
        this.cutscene = cutscene;
        this.reason = reason;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getCutscene() {
        return cutscene;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason implements StringRepresentable {
        FINISHED,
        SKIPPED,
        STOPPED;

        public static final com.mojang.serialization.Codec<Reason> CODEC =
                StringRepresentable.fromEnum(Reason::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
