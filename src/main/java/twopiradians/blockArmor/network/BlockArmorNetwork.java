package twopiradians.blockArmor.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.packet.SDevColorsPacket;
import twopiradians.blockArmor.packet.SConfigSyncPacket;
import twopiradians.blockArmor.packet.SSyncCooldownsPacket;

/** Central Fabric networking registration and small, explicit packet send surface. */
public final class BlockArmorNetwork {
    public static final ResourceLocation ACTIVATE_SET_EFFECT = new ResourceLocation(BlockArmor.MODID, "activate_set_effect");
    public static final ResourceLocation DEV_COLORS = new ResourceLocation(BlockArmor.MODID, "dev_colors");
    public static final ResourceLocation CONFIG_SYNC = new ResourceLocation(BlockArmor.MODID, "config_sync");
    public static final ResourceLocation COOLDOWN_SYNC = new ResourceLocation(BlockArmor.MODID, "cooldown_sync");

    public void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ACTIVATE_SET_EFFECT,
                (server, player, handler, buffer, responseSender) -> {
                    boolean pressed = buffer.readBoolean();
                    server.execute(() -> BlockArmor.key.setKeyDown(player, pressed));
                });
    }

    public void sendDevColors(ServerPlayer player) {
        ServerPlayNetworking.send(player, DEV_COLORS, SDevColorsPacket.encode());
    }

    public void sendConfig(ServerPlayer player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, CONFIG_SYNC, SConfigSyncPacket.encode(buffer));
    }

    public void sendCooldowns(ServerPlayer player) {
        ServerPlayNetworking.send(player, COOLDOWN_SYNC, SSyncCooldownsPacket.encode(player));
    }
}
