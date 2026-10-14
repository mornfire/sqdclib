package com.mornfire.sqdclib.music;

import com.mornfire.sqdclib.SQDCLib;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = SQDCLib.MOD_ID)
public final class MusicEvents {
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Music.forget(player);
        }
    }

    private MusicEvents() {}
}
