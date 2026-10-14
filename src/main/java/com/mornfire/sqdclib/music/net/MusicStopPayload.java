package com.mornfire.sqdclib.music.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.music.client.ClientMusic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MusicStopPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicStopPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "music_stop"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MusicStopPayload> STREAM_CODEC =
            StreamCodec.unit(new MusicStopPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MusicStopPayload payload, IPayloadContext context) {
        ClientMusic.stop();
    }
}
