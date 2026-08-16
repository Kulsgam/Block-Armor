package twopiradians.blockArmor.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import twopiradians.blockArmor.client.key.KeyActivateSetEffect;
import twopiradians.blockArmor.network.BlockArmorPayloads;
import twopiradians.blockArmor.packet.SDevColorsPacket;
import twopiradians.blockArmor.packet.SConfigSyncPacket;
import twopiradians.blockArmor.packet.SSyncCooldownsPacket;
import twopiradians.blockArmor.common.item.ArmorSet;

public final class BlockArmorFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        twopiradians.blockArmor.client.config.BlockArmorClientConfig.load();
        twopiradians.blockArmor.common.ClientTooltipState.install(
                () -> com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), 340)
                        || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), 344),
                () -> net.minecraft.client.Minecraft.getInstance().player);
        KeyActivateSetEffect.register();
        BlockArmorModelProvider.register();
        BlockArmorItemRenderer.register();
        BlockArmorRenderer.register();
        BlockArmorResourceReload.register();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            BlockArmorTextures.validateAll();
            BlockArmorClientDiagnostics.validateOnce(client);
            BlockArmorClientDiagnostics.tickEquippedRenderProbe(client);
            if (client.level != null) ArmorSet.tickClient(client.level.players());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) twopiradians.blockArmor.common.seteffect.SetEffectSlimey.finishBounce(client.player);
        });
        ClientPlayNetworking.registerGlobalReceiver(BlockArmorPayloads.DevColors.TYPE,
                (payload, context) -> {
                    SDevColorsPacket.decode(new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.bytes())));
                    context.client().execute(() -> { });
                });
        ClientPlayNetworking.registerGlobalReceiver(BlockArmorPayloads.ConfigSync.TYPE,
                (payload, context) -> {
                    FriendlyByteBufCopy.apply(context.client(), new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.bytes())));
                });
        ClientPlayNetworking.registerGlobalReceiver(BlockArmorPayloads.CooldownSync.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> {
                        if (context.client().player != null) SSyncCooldownsPacket.decode(context.client().player,
                                new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.bytes())));
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
            client.execute(() -> {
                try {
                    // An integrated server uses these same static ArmorSet objects
                    // and already owns the authoritative configuration. Applying
                    // its echo on the client thread races the server tick and used
                    // to cause ConcurrentModificationException in set effects.
                    if (client.getSingleplayerServer() != null) return;
                    SConfigSyncPacket.decode(new net.minecraft.network.FriendlyByteBuf(
                            io.netty.buffer.Unpooled.wrappedBuffer(bytes)));
                } catch (RuntimeException exception) {
                    twopiradians.blockArmor.common.BlockArmor.LOGGER.warn(
                            "Rejected invalid server configuration snapshot", exception);
                }
            });
        }
    }
}
