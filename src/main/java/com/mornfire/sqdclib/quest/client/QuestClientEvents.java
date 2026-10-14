package com.mornfire.sqdclib.quest.client;

import com.mornfire.sqdclib.SQDCLib;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT)
public final class QuestClientEvents {
    public static void open() {
        Minecraft.getInstance().setScreen(new QuestScreen());
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("story")
                .then(Commands.literal("quests").executes(context -> {
                    open();
                    return 1;
                })));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (QuestKeyMappings.OPEN_QUESTS.consumeClick()) {
            if (minecraft.screen == null && minecraft.player != null) {
                minecraft.setScreen(new QuestScreen());
            }
        }
    }

    private QuestClientEvents() {}
}
