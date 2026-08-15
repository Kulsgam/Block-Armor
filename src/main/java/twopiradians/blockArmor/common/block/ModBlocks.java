package twopiradians.blockArmor.common.block;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.BlockArmor;

/** Fabric registry declarations for Block Armor's own blocks. */
public final class ModBlocks {
    public static final BlockMovingLightSource MOVING_LIGHT_SOURCE = new BlockMovingLightSource();

    private ModBlocks() {}

    public static void register() {
        Registry.register(Registry.BLOCK, new ResourceLocation(BlockArmor.MODID, "moving_light_source"), MOVING_LIGHT_SOURCE);
    }
}
