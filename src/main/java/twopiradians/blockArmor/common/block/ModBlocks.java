package twopiradians.blockArmor.common.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.BlockArmor;

/** Fabric registry declarations for Block Armor's own blocks. */
public final class ModBlocks {
    public static final BlockMovingLightSource MOVING_LIGHT_SOURCE = new BlockMovingLightSource();

    private ModBlocks() {}

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "moving_light_source"), MOVING_LIGHT_SOURCE);
    }
}
