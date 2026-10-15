package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.client.ClientInputLock;
import com.mornfire.sqdclib.dialogue.Dialogues;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT)
public final class DialogueClientEvents {
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (ClientDialogues.lockMovement()) {
            ClientInputLock.clear(event.getInput());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientDialogues.tick();
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "dialogue_overlay"),
                DialogueOverlay::render);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        DialoguePanel panel = ClientDialogues.panel();
        if (panel == null) {
            return;
        }
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
            ClientDialogues.select(Dialogues.CLOSE_INDEX);
            ClientDialogues.close();
            return;
        }
        int index = event.getKey() - GLFW.GLFW_KEY_1;
        if (index >= 0 && index < panel.options().size()) {
            ClientDialogues.select(index);
        }
    }

    private DialogueClientEvents() {}
}
