package twopiradians.blockArmor.common.input;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;

/** Logical key state shared by the client sender and the dedicated server. */
public final class SetEffectKeyState {
    private final Map<UUID, Boolean> keyDown = new ConcurrentHashMap<>();

    public boolean isKeyDown(Player player) {
        return player != null && keyDown.getOrDefault(player.getUUID(), false);
    }

    public void setKeyDown(Player player, boolean pressed) {
        if (player != null) keyDown.put(player.getUUID(), pressed);
    }

    public void clear(Player player) {
        if (player != null) keyDown.remove(player.getUUID());
    }
}
