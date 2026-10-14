package com.mornfire.sqdclib.quest.client;

import com.mornfire.sqdclib.SQDCLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class QuestKeyMappings {
    public static final KeyMapping OPEN_QUESTS = new KeyMapping(
            "key.sqdclib.quests",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            "key.categories.sqdclib");

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_QUESTS);
    }

    private QuestKeyMappings() {}
}
