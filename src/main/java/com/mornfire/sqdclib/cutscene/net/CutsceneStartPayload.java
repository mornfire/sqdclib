package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.CutsceneDefinition;
import com.mornfire.sqdclib.cutscene.client.ClientCutscenes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutsceneStartPayload(CutsceneDefinition definition) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CutsceneStartPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "cutscene_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStartPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CutsceneDefinition.CODEC)
                    .map(CutsceneStartPayload::new, CutsceneStartPayload::definition);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CutsceneStartPayload payload, IPayloadContext context) {
        ClientCutscenes.start(payload.definition());
    }
}
