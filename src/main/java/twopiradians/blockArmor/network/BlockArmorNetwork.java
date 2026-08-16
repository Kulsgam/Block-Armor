package twopiradians.blockArmor.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.packet.SDevColorsPacket;
import twopiradians.blockArmor.packet.SConfigSyncPacket;
import twopiradians.blockArmor.packet.SSyncCooldownsPacket;

/** Central Fabric networking registration and small, explicit packet send surface. */
public final class BlockArmorNetwork {
    public static final Identifier ACTIVATE_SET_EFFECT = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "activate_set_effect");
    public static final Identifier DEV_COLORS = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "dev_colors");
    public static final Identifier CONFIG_SYNC = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "config_sync");
    public static final Identifier COOLDOWN_SYNC = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "cooldown_sync");

    public void registerReceivers() {
        PayloadTypeRegistry.serverboundPlay().register(ActivateSetEffectPayload.TYPE, ActivateSetEffectPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockArmorPayloads.DevColors.TYPE, BlockArmorPayloads.DevColors.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockArmorPayloads.ConfigSync.TYPE, BlockArmorPayloads.ConfigSync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockArmorPayloads.CooldownSync.TYPE, BlockArmorPayloads.CooldownSync.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActivateSetEffectPayload.TYPE,
                (payload, context) -> context.server().execute(() -> BlockArmor.key.setKeyDown(context.player(), payload.pressed())));
    }

    public void sendDevColors(ServerPlayer player) {
        ServerPlayNetworking.send(player, new BlockArmorPayloads.DevColors(bytes(SDevColorsPacket.encode())));
    }

    public void sendConfig(ServerPlayer player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, new BlockArmorPayloads.ConfigSync(bytes(SConfigSyncPacket.encode(buffer))));
    }

    public void sendCooldowns(ServerPlayer player) {
        ServerPlayNetworking.send(player, new BlockArmorPayloads.CooldownSync(bytes(SSyncCooldownsPacket.encode(player))));
    }

    private static byte[] bytes(FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

}
