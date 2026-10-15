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

public record QteResultPayload(int id, boolean success) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QteResultPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "qte_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QteResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, QteResultPayload::id,
            ByteBufCodecs.BOOL, QteResultPayload::success,
            QteResultPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QteResultPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Cutscenes.resolveQte(player, payload.id(), payload.success());
        }
    }
}
