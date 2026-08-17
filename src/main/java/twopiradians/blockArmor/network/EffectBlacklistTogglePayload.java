package twopiradians.blockArmor.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.common.BlockArmor;

public record EffectBlacklistTogglePayload(String effectId, boolean disabled) implements CustomPacketPayload {
    public static final Type<EffectBlacklistTogglePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "effect_blacklist_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectBlacklistTogglePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, EffectBlacklistTogglePayload::effectId,
                    ByteBufCodecs.BOOL, EffectBlacklistTogglePayload::disabled, EffectBlacklistTogglePayload::new);
    @Override public Type<EffectBlacklistTogglePayload> type() { return TYPE; }
}
