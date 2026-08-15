package twopiradians.blockArmor.common.loot;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.common.seteffect.SetEffectLucky;

/** Scopes Forge-style looting-level adjustment to active mob loot evaluation. */
public final class LuckyLootContext {
    private record Frame(LivingEntity killer, LivingEntity victim, boolean[] notified) {}
    private static final ThreadLocal<Deque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);
    private LuckyLootContext() {}

    public static void push(LootContext context) {
        Object killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        Object victim = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        FRAMES.get().push(new Frame(killer instanceof LivingEntity living ? living : null,
                victim instanceof LivingEntity living ? living : null, new boolean[1]));
    }

    public static void pop() {
        Deque<Frame> frames = FRAMES.get();
        if (!frames.isEmpty()) frames.pop();
        if (frames.isEmpty()) FRAMES.remove();
    }

    public static int adjust(int original, LivingEntity entity) {
        Frame frame = FRAMES.get().peek();
        if (frame == null || frame.killer != entity || frame.victim == null
                || !ArmorSet.hasSetEffect(entity, SetEffect.LUCKY)) return original;
        if (!frame.notified[0] && frame.victim.level instanceof ServerLevel level) {
            frame.notified[0] = true;
            SetEffectLucky.doParticlesAndSound(level, frame.victim.blockPosition(), entity, 4);
        }
        return original + 4;
    }
}
