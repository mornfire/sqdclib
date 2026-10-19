package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.Cutscenes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VideoFinishedPayload(ResourceLocation videoId, boolean success) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VideoFinishedPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "video_finished"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VideoFinishedPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, VideoFinishedPayload::videoId,
                    ByteBufCodecs.BOOL, VideoFinishedPayload::success,
                    VideoFinishedPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VideoFinishedPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Cutscenes.completeVideo(player, payload.videoId(), payload.success());
        }
    }
}
