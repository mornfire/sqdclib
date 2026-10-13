package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.Cutscenes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutsceneSkipPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CutsceneSkipPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "cutscene_skip"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneSkipPayload> STREAM_CODEC =
            StreamCodec.unit(new CutsceneSkipPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CutsceneSkipPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Cutscenes.skip(player);
        }
    }
}
