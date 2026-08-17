package twopiradians.blockArmor.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.common.BlockArmor;

public record EffectBlacklistRequestPayload() implements CustomPacketPayload {
    public static final Type<EffectBlacklistRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "effect_blacklist_request"));
    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, EffectBlacklistRequestPayload> CODEC =
            net.minecraft.network.codec.StreamCodec.unit(new EffectBlacklistRequestPayload());
    @Override public Type<EffectBlacklistRequestPayload> type() { return TYPE; }
}
