package twopiradians.blockArmor.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Compatibility helpers expressed in terms of the post-material BlockState API. */
public final class BlockUtils {
    private BlockUtils() { }

    public static BlockBehaviour.Properties getProperties(Block block) {
        return BlockBehaviour.Properties.ofFullCopy(block);
    }

    public static float getHardness(Block block) {
        try { return block.defaultBlockState().getDestroySpeed(null, BlockPos.ZERO); }
        catch (RuntimeException ignored) { return .5F; }
    }

    public static float getBlastResistance(Block block) { return block.getExplosionResistance(); }
    public static boolean getIsSolid(Block block) { return block.defaultBlockState().isSolid(); }
    public static boolean getRequiresTool(Block block) { return block.defaultBlockState().requiresCorrectToolForDrops(); }

    public static int getLightLevel(Block block) {
        int level = block.defaultBlockState().getLightEmission();
        if (level <= 0 && SetEffect.registryNameContains(block, new String[] {"lamp", "glass"})) level = 15;
        return level;
    }
}
