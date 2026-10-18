package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.client.ClientCutscenes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutsceneSeekPayload(int tick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CutsceneSeekPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "cutscene_seek"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneSeekPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CutsceneSeekPayload::tick, CutsceneSeekPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CutsceneSeekPayload payload, IPayloadContext context) {
        ClientCutscenes.seekTo(payload.tick());
    }
}
