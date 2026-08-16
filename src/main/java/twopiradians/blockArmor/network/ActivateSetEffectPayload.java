package twopiradians.blockArmor.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.common.BlockArmor;

/** Typed 26.1 play payload for the client set-effect key state. */
public record ActivateSetEffectPayload(boolean pressed) implements CustomPacketPayload {
    public static final Type<ActivateSetEffectPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BlockArmor.MODID, "activate_set_effect"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ActivateSetEffectPayload> CODEC =
            ByteBufCodecs.BOOL.map(ActivateSetEffectPayload::new, ActivateSetEffectPayload::pressed).cast();
    @Override public Type<ActivateSetEffectPayload> type() { return TYPE; }
}
