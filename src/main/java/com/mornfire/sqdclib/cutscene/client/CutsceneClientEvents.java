package com.mornfire.sqdclib.cutscene.client;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.client.ClientInputLock;
import com.mornfire.sqdclib.cutscene.net.CutsceneSkipPayload;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT)
public final class CutsceneClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientCutscenes.clientTick();
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (ClientCutscenes.lockMovement()) {
            ClientInputLock.clear(event.getInput());
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (!ClientCutscenes.isPlaying()) {
            return;
        }
        ClientCutscenes.CameraSample sample = ClientCutscenes.camera((float) event.getPartialTick());
        if (sample != null) {
            event.setFOV(sample.fov());
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof PauseScreen && ClientCutscenes.isSkippable()) {
            event.setCanceled(true);
            PacketDistributor.sendToServer(new CutsceneSkipPayload());
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "cutscene_overlay"),
                CutsceneOverlay::render);
    }

    private CutsceneClientEvents() {}
}
