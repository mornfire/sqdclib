package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.StandInCue;
import com.mornfire.sqdclib.cutscene.client.ClientStandIn;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StandInPayload(
        boolean active,
        UUID owner,
        float baseYaw,
        List<StandInCue.StandInKey> keys) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StandInPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "stand_in"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<StandInCue.StandInKey>> KEY_CODEC =
            StandInCue.StandInKey.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, StandInPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, StandInPayload::active,
            UUIDUtil.STREAM_CODEC, StandInPayload::owner,
            ByteBufCodecs.FLOAT, StandInPayload::baseYaw,
            KEY_CODEC, StandInPayload::keys,
            StandInPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StandInPayload payload, IPayloadContext context) {
        ClientStandIn.update(payload);
    }
}
