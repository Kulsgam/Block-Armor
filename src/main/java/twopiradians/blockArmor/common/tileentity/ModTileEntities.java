package twopiradians.blockArmor.common.tileentity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.block.ModBlocks;

public final class ModTileEntities {
    private ModTileEntities() {}

    public static void register() {
		TileEntityMovingLightSource.type = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BlockArmor.MODID, "light_tile_entity"),
                FabricBlockEntityTypeBuilder.create((pos, state) -> new TileEntityMovingLightSource(0, pos, state), ModBlocks.MOVING_LIGHT_SOURCE).build());
    }
}
