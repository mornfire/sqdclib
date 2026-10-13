package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.client.ClientInputLock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT)
public final class DialogueClientEvents {
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (ClientDialogues.lockMovement()) {
            ClientInputLock.clear(event.getInput());
        }
    }

    private DialogueClientEvents() {}
}
