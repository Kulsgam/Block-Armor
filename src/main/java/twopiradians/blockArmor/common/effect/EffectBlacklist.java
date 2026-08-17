package twopiradians.blockArmor.common.effect;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Shared lookup used by effect calculation on both logical sides. */
public final class EffectBlacklist {
    private static final Map<UUID, Set<String>> CLIENT = new ConcurrentHashMap<>();
    private EffectBlacklist() {}

    public static boolean isDisabled(LivingEntity entity, SetEffect effect) {
        if (entity == null || effect == null) return false;
        if (entity.level().isClientSide())
            return CLIENT.getOrDefault(entity.getUUID(), Set.of()).contains(id(effect));
        if (entity instanceof ServerPlayer player)
            return EffectBlacklistState.get((net.minecraft.server.level.ServerLevel) player.level()).isDisabled(player.getUUID(), effect);
        return false;
    }

    public static void setClient(UUID player, Set<String> effects) {
        if (effects.isEmpty()) CLIENT.remove(player);
        else CLIENT.put(player, Set.copyOf(effects));
    }

    public static Set<String> client(UUID player) {
        return Set.copyOf(CLIENT.getOrDefault(player, Set.of()));
    }

    public static void clearClient(UUID player) { CLIENT.remove(player); }

    public static String id(SetEffect effect) {
        return effect.getClass().getName();
    }
}
