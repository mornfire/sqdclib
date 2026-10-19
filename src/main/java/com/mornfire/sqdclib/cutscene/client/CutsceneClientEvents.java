package com.mornfire.sqdclib.cutscene.client;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.client.ClientInputLock;
import com.mornfire.sqdclib.cutscene.net.CutsceneSkipPayload;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT)
public final class CutsceneClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientCutscenes.clientTick();
        ClientStandIn.tick();
        ClientQte.tick();
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (ClientCutscenes.lockMovement()) {
            ClientInputLock.clear(event.getInput());
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            ClientQte.onKey(event.getKey());
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
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientStandIn.render(event.getPoseStack(), minecraft.renderBuffers().bufferSource(),
                event.getCamera(), minecraft.getTimer().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        // 过场期间玩家自己也不该出现在画面里（第一人称身体/第三人称模型都会穿帮）
        if (player != null && (ClientStandIn.isActive() || ClientCutscenes.isPlaying())
                && event.getEntity() == player) {
            event.setCanceled(true);
        }
    }

    /** 过场期间隐藏第一人称的手和手持物。 */
    @SubscribeEvent
    public static void onRenderHand(net.neoforged.neoforge.client.event.RenderHandEvent event) {
        if (ClientCutscenes.isPlaying()) {
            event.setCanceled(true);
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
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "qte_overlay"),
                ClientQte::render);
    }

    private CutsceneClientEvents() {}
}
