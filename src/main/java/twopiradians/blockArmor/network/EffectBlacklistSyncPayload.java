package twopiradians.blockArmor.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.common.BlockArmor;

public record EffectBlacklistSyncPayload(List<String> disabled) implements CustomPacketPayload {
    public static final Type<EffectBlacklistSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "effect_blacklist_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectBlacklistSyncPayload> CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, String>list().apply(ByteBufCodecs.STRING_UTF8.cast())
                    .map(EffectBlacklistSyncPayload::new, EffectBlacklistSyncPayload::disabled).cast();
    @Override public Type<EffectBlacklistSyncPayload> type() { return TYPE; }
}
