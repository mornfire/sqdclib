package com.mornfire.sqdclib.music.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.music.client.ClientMusic;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MusicStartPayload(Holder<SoundEvent> sound, float volume) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicStartPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "music_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MusicStartPayload> STREAM_CODEC = StreamCodec.composite(
            SoundEvent.STREAM_CODEC, MusicStartPayload::sound,
            ByteBufCodecs.FLOAT, MusicStartPayload::volume,
            MusicStartPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MusicStartPayload payload, IPayloadContext context) {
        ClientMusic.play(payload.sound(), payload.volume());
    }
}
