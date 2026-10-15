package com.mornfire.sqdclib.cutscene.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.QteCue;
import com.mornfire.sqdclib.cutscene.client.ClientQte;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QteStartPayload(
        int id,
        QteCue.Kind kind,
        String key,
        int duration,
        Optional<Component> prompt) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<QteStartPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "qte_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QteStartPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, QteStartPayload::id,
            ByteBufCodecs.VAR_INT.map(kind -> QteCue.Kind.values()[kind], QteCue.Kind::ordinal),
            QteStartPayload::kind,
            ByteBufCodecs.STRING_UTF8, QteStartPayload::key,
            ByteBufCodecs.VAR_INT, QteStartPayload::duration,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, QteStartPayload::prompt,
            QteStartPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QteStartPayload payload, IPayloadContext context) {
        ClientQte.start(payload);
    }
}
