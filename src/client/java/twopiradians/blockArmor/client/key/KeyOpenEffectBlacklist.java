package twopiradians.blockArmor.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.network.EffectBlacklistRequestPayload;

/** Opens the player-specific set-effect blacklist. Unbound by default. */
public final class KeyOpenEffectBlacklist {
    private KeyOpenEffectBlacklist() {}
    public static void register() {
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.blockarmor.open_effect_blacklist", InputConstants.UNKNOWN.getValue(),
                KeySetEffectAbilities.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && key.consumeClick()) {
                ClientPlayNetworking.send(new EffectBlacklistRequestPayload());
                client.setScreen(new twopiradians.blockArmor.client.gui.EffectBlacklistScreen(null));
            }
        });
    }
}
