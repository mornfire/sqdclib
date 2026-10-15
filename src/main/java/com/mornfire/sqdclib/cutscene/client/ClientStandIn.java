package com.mornfire.sqdclib.cutscene.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mornfire.sqdclib.cutscene.StandInCue;
import com.mornfire.sqdclib.cutscene.Timeline;
import com.mornfire.sqdclib.cutscene.net.StandInPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ClientStandIn {
    private record Active(UUID owner, double x, double y, double z, float baseYaw,
            List<StandInCue.StandInKey> keys, float time) {}

    @Nullable
    private static PlayerModel<AbstractClientPlayer> model;
    @Nullable
    private static Active active;

    public static boolean isActive() {
        return active != null;
    }

    public static void update(StandInPayload payload) {
        if (!payload.active()) {
            active = null;
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            active = null;
            return;
        }
        active = new Active(payload.owner(), player.getX(), player.getY(), player.getZ(),
                payload.baseYaw(), new ArrayList<>(payload.keys()), 0.0F);
    }

    public static void tick() {
        Active current = active;
        if (current == null) {
            return;
        }
        active = new Active(current.owner(), current.x(), current.y(), current.z(), current.baseYaw(),
                current.keys(), current.time() + 1.0F);
    }

    public static void clear() {
        active = null;
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffers, Camera camera, float partialTick) {
        Active current = active;
        LocalPlayer player = Minecraft.getInstance().player;
        if (current == null || player == null) {
            return;
        }

        Vec3 offset = Vec3.ZERO;
        float yawOffset = 0.0F;
        StandInCue.Motion motion = StandInCue.Motion.IDLE;
        if (!current.keys().isEmpty()) {
            int[] times = new int[current.keys().size()];
            for (int i = 0; i < times.length; i++) {
                times[i] = current.keys().get(i).time();
            }
            Timeline.Segment segment = Timeline.locate(times, current.time() + partialTick);
            if (segment != null) {
                StandInCue.StandInKey from = current.keys().get(segment.from());
                StandInCue.StandInKey to = current.keys().get(segment.to());
                offset = from.offset().lerp(to.offset(), segment.progress());
                yawOffset = Mth.lerp(segment.progress(), from.yawOffset(), to.yawOffset());
                motion = segment.progress() < 0.5F ? from.motion() : to.motion();
            }
        }

        double x = current.x() + offset.x;
        double y = current.y() + offset.y;
        double z = current.z() + offset.z;
        float yaw = current.baseYaw() + yawOffset;
        float age = current.time() + partialTick;

        PlayerModel<AbstractClientPlayer> playerModel = model();
        playerModel.setupAnim(player, 0.0F, 0.0F, age, 0.0F, 0.0F);
        applyMotion(playerModel, motion, age);

        Vec3 cameraPosition = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        playerModel.renderToBuffer(poseStack,
                buffers.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
    }

    private static void applyMotion(PlayerModel<AbstractClientPlayer> playerModel, StandInCue.Motion motion,
            float age) {
        float swing = Mth.sin(age * 0.3F) * 0.6F;
        switch (motion) {
            case WALK -> {
                playerModel.rightArm.xRot = -swing;
                playerModel.leftArm.xRot = swing;
                playerModel.rightLeg.xRot = swing;
                playerModel.leftLeg.xRot = -swing;
            }
            case WAVE -> {
                playerModel.rightArm.xRot = (float) -Math.PI + 0.2F;
                playerModel.rightArm.zRot = Mth.sin(age * 0.5F) * 0.4F;
            }
            case POINT -> playerModel.rightArm.xRot = (float) -Math.PI / 2.0F;
            case LOOK_UP -> playerModel.head.xRot = -0.6F;
            case BOW -> playerModel.body.xRot = 0.5F;
            case IDLE -> {
            }
        }
    }

    private static PlayerModel<AbstractClientPlayer> model() {
        if (model == null) {
            model = new PlayerModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        }
        return model;
    }

    private ClientStandIn() {}
}
