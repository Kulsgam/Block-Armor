package twopiradians.blockArmor.common.effect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.server.level.ServerLevel;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Persistent, player-owned set-effect blacklist. Armor stacks are never changed. */
public final class EffectBlacklistState extends SavedData {
    private static final Codec<Map<String, List<String>>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf());
    public static final SavedDataType<EffectBlacklistState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(BlockArmor.MODID, "player_effect_blacklist"),
            EffectBlacklistState::new, CODEC.xmap(EffectBlacklistState::new, EffectBlacklistState::serialize),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, Set<String>> disabled = new HashMap<>();

    private EffectBlacklistState() {}

    private EffectBlacklistState(Map<String, List<String>> encoded) {
        for (Map.Entry<String, List<String>> entry : encoded.entrySet()) {
            try {
                disabled.put(UUID.fromString(entry.getKey()), new HashSet<>(entry.getValue()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private Map<String, List<String>> serialize() {
        Map<String, List<String>> encoded = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : disabled.entrySet())
            if (!entry.getValue().isEmpty()) encoded.put(entry.getKey().toString(), List.copyOf(entry.getValue()));
        return encoded;
    }

    public static EffectBlacklistState get(ServerLevel level) {
        SavedDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public boolean isDisabled(UUID player, SetEffect effect) {
        return disabled.getOrDefault(player, Set.of()).contains(EffectBlacklist.id(effect));
    }

    public Set<String> get(UUID player) {
        return Set.copyOf(disabled.getOrDefault(player, Set.of()));
    }

    public void setDisabled(UUID player, String effectId, boolean value) {
        Set<String> effects = disabled.computeIfAbsent(player, ignored -> new HashSet<>());
        if (value) effects.add(effectId); else effects.remove(effectId);
        if (effects.isEmpty()) disabled.remove(player);
        setDirty();
    }
}
