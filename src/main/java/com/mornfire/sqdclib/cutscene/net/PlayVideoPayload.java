package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.client.video.ClientVideos;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayVideoPayload(
        ResourceLocation videoId,
        Optional<ResourceLocation> audio,
        boolean lockMovement) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayVideoPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "play_video"));

    private static final StreamCodec<ByteBuf, Optional<ResourceLocation>> OPTIONAL_ID =
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayVideoPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, PlayVideoPayload::videoId,
                    OPTIONAL_ID, PlayVideoPayload::audio,
                    ByteBufCodecs.BOOL, PlayVideoPayload::lockMovement,
                    PlayVideoPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayVideoPayload payload, IPayloadContext context) {
        ClientVideos.play(payload.videoId(), payload.audio(), payload.lockMovement());
    }
}
