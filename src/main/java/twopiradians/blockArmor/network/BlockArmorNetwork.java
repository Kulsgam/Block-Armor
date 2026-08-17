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
import twopiradians.blockArmor.common.effect.EffectBlacklistState;

/** Central Fabric networking registration and small, explicit packet send surface. */
public final class BlockArmorNetwork {
    public static final Identifier ACTIVATE_SET_EFFECT = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "activate_set_effect");
    public static final Identifier DEV_COLORS = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "dev_colors");
    public static final Identifier CONFIG_SYNC = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "config_sync");
    public static final Identifier COOLDOWN_SYNC = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "cooldown_sync");

    public void registerReceivers() {
        PayloadTypeRegistry.serverboundPlay().register(ActivateSetEffectPayload.TYPE, ActivateSetEffectPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EffectBlacklistRequestPayload.TYPE, EffectBlacklistRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EffectBlacklistTogglePayload.TYPE, EffectBlacklistTogglePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockArmorPayloads.DevColors.TYPE, BlockArmorPayloads.DevColors.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockArmorPayloads.ConfigSync.TYPE, BlockArmorPayloads.ConfigSync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockArmorPayloads.CooldownSync.TYPE, BlockArmorPayloads.CooldownSync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EffectBlacklistSyncPayload.TYPE, EffectBlacklistSyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActivateSetEffectPayload.TYPE,
                (payload, context) -> context.server().execute(() -> {
                    if (twopiradians.blockArmor.common.seteffect.SetEffect.isKnownId(payload.effectId()))
                        BlockArmor.key.setKeyDown(context.player(), payload.effectId(), payload.pressed());
                }));
        ServerPlayNetworking.registerGlobalReceiver(EffectBlacklistRequestPayload.TYPE,
                (payload, context) -> context.server().execute(() -> sendEffectBlacklist(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(EffectBlacklistTogglePayload.TYPE,
                (payload, context) -> context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    if (!twopiradians.blockArmor.common.seteffect.SetEffect.isKnownId(payload.effectId())) return;
                    EffectBlacklistState state = EffectBlacklistState.get((net.minecraft.server.level.ServerLevel) player.level());
                    state.setDisabled(player.getUUID(), payload.effectId(), payload.disabled());
                    twopiradians.blockArmor.common.item.ArmorSet.refreshPlayerSetEffects(player);
                    sendEffectBlacklist(player);
                }));
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

    public void sendEffectBlacklist(ServerPlayer player) {
        ServerPlayNetworking.send(player, new EffectBlacklistSyncPayload(
                EffectBlacklistState.get((net.minecraft.server.level.ServerLevel) player.level()).get(player.getUUID()).stream().sorted().toList()));
    }

    private static byte[] bytes(FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

}
