package com.mornfire.sqdclib.storystate;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.storystate.net.WorldStatePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SQDCLib.MOD_ID)
public final class StoryStateEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendWorldState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendWorldState(player);
        }
    }

    private static void sendWorldState(ServerPlayer player) {
        WorldState state = WorldState.get(player.serverLevel());
        PacketDistributor.sendToPlayer(player, new WorldStatePayload(state.snapshot()));
    }

    private StoryStateEvents() {}
}
