package com.mornfire.sqdclib.dialogue.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.dialogue.Dialogues;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueSubmitPayload(String value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DialogueSubmitPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "dialogue_submit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueSubmitPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, DialogueSubmitPayload::value,
                    DialogueSubmitPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DialogueSubmitPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Dialogues.submit(player, payload.value());
        }
    }
}
