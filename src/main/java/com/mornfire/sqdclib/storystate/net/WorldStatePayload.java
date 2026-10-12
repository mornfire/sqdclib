package com.mornfire.sqdclib.storystate.net;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.storystate.WorldState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorldStatePayload(CompoundTag data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorldStatePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "world_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorldStatePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, WorldStatePayload::data, WorldStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WorldStatePayload payload, IPayloadContext context) {
        WorldState.applyClientSnapshot(payload.data());
    }
}
