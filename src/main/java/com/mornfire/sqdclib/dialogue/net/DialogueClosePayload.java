package com.mornfire.sqdclib.dialogue.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.dialogue.client.ClientDialogues;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueClosePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DialogueClosePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "dialogue_close"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueClosePayload> STREAM_CODEC =
            StreamCodec.unit(new DialogueClosePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DialogueClosePayload payload, IPayloadContext context) {
        ClientDialogues.close();
    }
}
