package twopiradians.blockArmor.client.key;

import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import io.netty.buffer.Unpooled;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.network.BlockArmorNetwork;

/** Client key state mirrored to the server only when it changes. */
public final class KeyActivateSetEffect {
    public static KeyMapping ACTIVATE_SET_EFFECT;
    public static void register() {
        ACTIVATE_SET_EFFECT = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.blockarmor.activate_set_effect", 82, BlockArmor.MODNAME));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            UUID id = client.player.getUUID();
            boolean pressed = ACTIVATE_SET_EFFECT.isDown();
            if (pressed != BlockArmor.key.isKeyDown(client.player)) {
                BlockArmor.key.setKeyDown(client.player, pressed);
                FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                packet.writeBoolean(pressed);
                ClientPlayNetworking.send(BlockArmorNetwork.ACTIVATE_SET_EFFECT, packet);
            }
        });
    }
}
