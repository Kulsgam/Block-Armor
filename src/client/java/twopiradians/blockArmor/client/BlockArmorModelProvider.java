package twopiradians.blockArmor.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.mixin.CuboidItemModelWrapperAccessor;

/** Replaces the shared fallback model with the runtime per-stack Block Armor model. */
final class BlockArmorModelProvider {
    private BlockArmorModelProvider() { }

    static void register() {
        ModelLoadingPlugin.register(context -> context.modifyItemModelAfterBake().register((original, bake) -> {
            // ITEM_MODEL points every generated stack at this shared id. The
            // bake callback therefore receives blockarmor:block_armor, not the
            // generated item's registry id; checking the item registry here
            // rejected every stack and left the iron-chestplate placeholder.
            boolean sharedModel = bake.itemId().equals(Identifier.fromNamespaceAndPath(
                    twopiradians.blockArmor.common.BlockArmor.MODID, "block_armor"));
            boolean generatedItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(bake.itemId())
                    instanceof twopiradians.blockArmor.common.item.BlockArmorItem;
            if ((!sharedModel && !generatedItem) || !(original instanceof CuboidItemModelWrapper cuboid)) return original;
            return new BlockArmorItemRenderer(((CuboidItemModelWrapperAccessor) cuboid).blockarmor$getProperties(),
                    bake.transformation(), bake.bakingContext().blockModelBaker());
        }));
    }
}
