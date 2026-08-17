package twopiradians.blockArmor.common.input;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;

/** Logical key state shared by the client sender and the dedicated server. */
public final class SetEffectKeyState {
    private final Map<UUID, Map<String, Boolean>> keyDown = new ConcurrentHashMap<>();

    public boolean isKeyDown(Player player) {
        return player != null && keyDown.getOrDefault(player.getUUID(), Map.of())
                .getOrDefault("", false);
    }

    public boolean isKeyDown(Player player, twopiradians.blockArmor.common.seteffect.SetEffect effect) {
        return player != null && keyDown.getOrDefault(player.getUUID(), Map.of())
                .getOrDefault(twopiradians.blockArmor.common.seteffect.SetEffect.id(effect), false);
    }

    public void setKeyDown(Player player, boolean pressed) {
        setKeyDown(player, "", pressed);
    }

    public void setKeyDown(Player player, String effectId, boolean pressed) {
        if (player != null)
            keyDown.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>()).put(effectId, pressed);
    }

    public void clear(Player player) {
        if (player != null) keyDown.remove(player.getUUID());
    }
}
