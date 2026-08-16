package twopiradians.blockArmor.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.common.BlockArmor;

/** Typed wrappers for the three existing binary sync formats. */
public final class BlockArmorPayloads {
    private BlockArmorPayloads() { }

    public record DevColors(byte[] bytes) implements CustomPacketPayload {
        public static final Type<DevColors> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "dev_colors"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DevColors> CODEC = ByteBufCodecs.BYTE_ARRAY.map(DevColors::new, DevColors::bytes).cast();
        @Override public Type<DevColors> type() { return TYPE; }
    }
    public record ConfigSync(byte[] bytes) implements CustomPacketPayload {
        public static final Type<ConfigSync> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "config_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSync> CODEC = ByteBufCodecs.BYTE_ARRAY.map(ConfigSync::new, ConfigSync::bytes).cast();
        @Override public Type<ConfigSync> type() { return TYPE; }
    }
    public record CooldownSync(byte[] bytes) implements CustomPacketPayload {
        public static final Type<CooldownSync> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "cooldown_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CooldownSync> CODEC = ByteBufCodecs.BYTE_ARRAY.map(CooldownSync::new, CooldownSync::bytes).cast();
        @Override public Type<CooldownSync> type() { return TYPE; }
    }
}
