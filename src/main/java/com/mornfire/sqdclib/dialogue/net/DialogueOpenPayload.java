package com.mornfire.sqdclib.dialogue.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.dialogue.DialogueMode;
import com.mornfire.sqdclib.dialogue.DialoguePlacement;
import com.mornfire.sqdclib.dialogue.InputSpec;
import com.mornfire.sqdclib.dialogue.client.ClientDialogues;
import io.netty.buffer.ByteBuf;
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
        Optional<Component> speakerName,
        Optional<Component> identity,
        Optional<ResourceLocation> portrait,
        Component text,
        List<Component> options,
        boolean lockMovement,
        DialogueMode mode,
        int textSpeed,
        DialoguePlacement placement,
        int speakerColor,
        Optional<InputSpec> input) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DialogueOpenPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "dialogue_open"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Component>> OPTION_CODEC =
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<InputSpec>> INPUT_CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, InputSpec>optional(InputSpec.STREAM_CODEC);
    private static final StreamCodec<ByteBuf, Optional<ResourceLocation>> PORTRAIT_CODEC =
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueOpenPayload> STREAM_CODEC =
            StreamCodec.of(DialogueOpenPayload::write, DialogueOpenPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, DialogueOpenPayload payload) {
        ResourceLocation.STREAM_CODEC.encode(buf, payload.dialogue);
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.node);
        ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, payload.speakerName);
        ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, payload.identity);
        PORTRAIT_CODEC.encode(buf, payload.portrait);
        ComponentSerialization.STREAM_CODEC.encode(buf, payload.text);
        OPTION_CODEC.encode(buf, payload.options);
        ByteBufCodecs.BOOL.encode(buf, payload.lockMovement);
        ByteBufCodecs.VAR_INT.encode(buf, payload.mode.ordinal());
        ByteBufCodecs.VAR_INT.encode(buf, payload.textSpeed);
        ByteBufCodecs.VAR_INT.encode(buf, payload.placement.ordinal());
        ByteBufCodecs.INT.encode(buf, payload.speakerColor);
        INPUT_CODEC.encode(buf, payload.input);
    }

    private static DialogueOpenPayload read(RegistryFriendlyByteBuf buf) {
        ResourceLocation dialogue = ResourceLocation.STREAM_CODEC.decode(buf);
        String node = ByteBufCodecs.STRING_UTF8.decode(buf);
        Optional<Component> speakerName = ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf);
        Optional<Component> identity = ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf);
        Optional<ResourceLocation> portrait = PORTRAIT_CODEC.decode(buf);
        Component text = ComponentSerialization.STREAM_CODEC.decode(buf);
        List<Component> options = OPTION_CODEC.decode(buf);
        boolean lockMovement = ByteBufCodecs.BOOL.decode(buf);
        DialogueMode mode = DialogueMode.values()[ByteBufCodecs.VAR_INT.decode(buf)];
        int textSpeed = ByteBufCodecs.VAR_INT.decode(buf);
        DialoguePlacement placement = DialoguePlacement.values()[ByteBufCodecs.VAR_INT.decode(buf)];
        int speakerColor = ByteBufCodecs.INT.decode(buf);
        Optional<InputSpec> input = INPUT_CODEC.decode(buf);
        return new DialogueOpenPayload(dialogue, node, speakerName, identity, portrait, text, options, lockMovement,
                mode, textSpeed, placement, speakerColor, input);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DialogueOpenPayload payload, IPayloadContext context) {
        ClientDialogues.open(payload);
    }
}
