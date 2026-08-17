package twopiradians.blockArmor.client.key;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.network.ActivateSetEffectPayload;

/** Individual, unbound-by-default keybinds for set-effect abilities. */
public final class KeySetEffectAbilities {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(BlockArmor.MODID, "blockarmor"));
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private record Entry(SetEffect effect, KeyMapping key, boolean lastDown) {
        private Entry withLastDown(boolean down) { return new Entry(effect, key, down); }
    }

    private KeySetEffectAbilities() {}

    public static void register() {
        add(SetEffect.ABSORBENT, "absorbent");
        add(SetEffect.ARROW_DEFENCE, "arrow_defence");
        add(SetEffect.BONEMEALER, "bonemealer");
        add(SetEffect.CRAFTER, "crafter");
        add(SetEffect.ENDER, "ender");
        add(SetEffect.ENDER_HOARDER, "ender_hoarder");
        add(SetEffect.EXPLOSIVE, "explosive");
        add(SetEffect.HOARDER, "hoarder");
        add(SetEffect.PULLER, "puller");
        add(SetEffect.PUSHER, "pusher");
        add(SetEffect.SLEEPY, "sleepy");
        add(SetEffect.SNOWY, "snowy");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            UUID id = client.player.getUUID();
            for (int i = 0; i < ENTRIES.size(); i++) {
                Entry entry = ENTRIES.get(i);
                boolean down = entry.key.isDown();
                if (down != entry.lastDown) {
                    ENTRIES.set(i, entry.withLastDown(down));
                    ClientPlayNetworking.send(new ActivateSetEffectPayload(SetEffect.id(entry.effect), down));
                }
            }
        });
    }

    private static void add(SetEffect effect, String name) {
        ENTRIES.add(new Entry(effect, KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.blockarmor.activate_" + name, InputConstants.UNKNOWN.getValue(), CATEGORY)), false));
    }
}
