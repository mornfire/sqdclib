package com.mornfire.sqdclib.cutscene.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class CutsceneTickEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation cutscene;
    private final int tick;
    private final int duration;

    public CutsceneTickEvent(ServerPlayer player, ResourceLocation cutscene, int tick, int duration) {
        this.player = player;
        this.cutscene = cutscene;
        this.tick = tick;
        this.duration = duration;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getCutscene() {
        return cutscene;
    }

    public int getTick() {
        return tick;
    }

    public int getDuration() {
        return duration;
    }
}
