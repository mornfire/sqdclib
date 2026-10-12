package com.mornfire.sqdclib.quest.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public abstract class QuestEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation quest;

    protected QuestEvent(ServerPlayer player, ResourceLocation quest) {
        this.player = player;
        this.quest = quest;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getQuest() {
        return quest;
    }
}
