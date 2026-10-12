package com.mornfire.sqdclib.dialogue.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.dialogue.client.ClientDialogues;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueOpenPayload(
        ResourceLocation dialogue,
        String node,
        Optional<Component> speaker,
        Component text,
        List<Component> options) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DialogueOpenPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "dialogue_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueOpenPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, DialogueOpenPayload::dialogue,
            ByteBufCodecs.STRING_UTF8, DialogueOpenPayload::node,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, DialogueOpenPayload::speaker,
            ComponentSerialization.STREAM_CODEC, DialogueOpenPayload::text,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), DialogueOpenPayload::options,
            DialogueOpenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DialogueOpenPayload payload, IPayloadContext context) {
        ClientDialogues.open(payload);
    }
}
