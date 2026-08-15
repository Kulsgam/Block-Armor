package twopiradians.blockArmor.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import twopiradians.blockArmor.client.key.KeyActivateSetEffect;
import twopiradians.blockArmor.network.BlockArmorNetwork;
import twopiradians.blockArmor.packet.SDevColorsPacket;
import twopiradians.blockArmor.packet.SConfigSyncPacket;
import twopiradians.blockArmor.packet.SSyncCooldownsPacket;
import twopiradians.blockArmor.common.item.ArmorSet;

public final class BlockArmorFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        twopiradians.blockArmor.common.ClientTooltipState.install(
                net.minecraft.client.gui.screens.Screen::hasShiftDown,
                () -> net.minecraft.client.Minecraft.getInstance().player);
        KeyActivateSetEffect.register();
        BlockArmorModelProvider.register();
        BlockArmorItemRenderer.register();
        BlockArmorRenderer.register();
        BlockArmorResourceReload.register();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            BlockArmorTextures.validateAll();
            if (client.level != null) ArmorSet.tickClient(client.level.players());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) twopiradians.blockArmor.common.seteffect.SetEffectSlimey.finishBounce(client.player);
        });
        ClientPlayNetworking.registerGlobalReceiver(BlockArmorNetwork.DEV_COLORS,
                (client, handler, buffer, responseSender) -> {
                    SDevColorsPacket.decode(buffer);
                    client.execute(() -> { });
                });
        ClientPlayNetworking.registerGlobalReceiver(BlockArmorNetwork.CONFIG_SYNC,
                (client, handler, buffer, responseSender) -> {
                    FriendlyByteBufCopy.apply(client, buffer);
                });
        ClientPlayNetworking.registerGlobalReceiver(BlockArmorNetwork.COOLDOWN_SYNC,
                (client, handler, buffer, responseSender) -> {
                    byte[] bytes = new byte[buffer.readableBytes()];
                    buffer.readBytes(bytes);
                    client.execute(() -> {
                        if (client.player != null) SSyncCooldownsPacket.decode(client.player,
                                new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(bytes)));
                    });
                });
        // Server sync is temporary client state. Restore this installation's local
        // configuration after leaving a dedicated server.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ConfigReload.restore(client));
        ClientProxy.setup();
    }

    private static final class ConfigReload {
        static void restore(net.minecraft.client.Minecraft client) {
            if (client.getSingleplayerServer() == null)
                client.execute(twopiradians.blockArmor.common.config.Config::reload);
        }
    }

    private static final class FriendlyByteBufCopy {
        static void apply(net.minecraft.client.Minecraft client, net.minecraft.network.FriendlyByteBuf source) {
            byte[] bytes = new byte[source.readableBytes()];
            source.readBytes(bytes);
            client.execute(() -> SConfigSyncPacket.decode(new net.minecraft.network.FriendlyByteBuf(
                    io.netty.buffer.Unpooled.wrappedBuffer(bytes))));
        }
    }
}
