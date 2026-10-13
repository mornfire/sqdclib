package com.mornfire.sqdclib.cutscene.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class CutsceneFinishedEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation cutscene;

    public CutsceneFinishedEvent(ServerPlayer player, ResourceLocation cutscene) {
        this.player = player;
        this.cutscene = cutscene;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getCutscene() {
        return cutscene;
    }
}
