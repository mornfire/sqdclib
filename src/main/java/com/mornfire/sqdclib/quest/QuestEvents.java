package com.mornfire.sqdclib.quest;

import com.mornfire.sqdclib.SQDCLib;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SQDCLib.MOD_ID)
public final class QuestEvents {
    private static final int REFRESH_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % REFRESH_INTERVAL == 0) {
            Quests.refresh(player);
        }
    }

    private QuestEvents() {}
}
