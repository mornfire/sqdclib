package com.mornfire.sqdclib.cutscene.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class VideoFinishedEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation videoId;
    private final boolean success;

    public VideoFinishedEvent(ServerPlayer player, ResourceLocation videoId, boolean success) {
        this.player = player;
        this.videoId = videoId;
        this.success = success;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getVideoId() {
        return videoId;
    }

    public boolean isSuccess() {
        return success;
    }
}
