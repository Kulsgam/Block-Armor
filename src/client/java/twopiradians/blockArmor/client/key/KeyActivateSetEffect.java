package twopiradians.blockArmor.client.key;

import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.network.ActivateSetEffectPayload;

/** Client key state mirrored to the server only when it changes. */
public final class KeyActivateSetEffect {
    public static KeyMapping ACTIVATE_SET_EFFECT;
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(BlockArmor.MODID, "blockarmor"));
    public static void register() {
        ACTIVATE_SET_EFFECT = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.blockarmor.activate_set_effect", 82,
                        CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            UUID id = client.player.getUUID();
            boolean pressed = ACTIVATE_SET_EFFECT.isDown();
            if (pressed != BlockArmor.key.isKeyDown(client.player)) {
                BlockArmor.key.setKeyDown(client.player, pressed);
                ClientPlayNetworking.send(new ActivateSetEffectPayload(pressed));
            }
        });
    }
}
