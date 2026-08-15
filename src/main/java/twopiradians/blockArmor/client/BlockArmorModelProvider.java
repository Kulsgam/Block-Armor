package twopiradians.blockArmor.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.BlockArmorItem;

/** Routes every generated armor item to Fabric's dynamic icon renderer. */
final class BlockArmorModelProvider {
    private BlockArmorModelProvider() { }

    static void register() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(loader -> (modelId, context) -> {
            if (!"inventory".equals(modelId.getVariant()) || !BlockArmor.MODID.equals(modelId.getNamespace())) return null;
            var item = net.minecraft.core.Registry.ITEM.get(new ResourceLocation(modelId.getNamespace(), modelId.getPath()));
            return item instanceof BlockArmorItem ? DynamicUnbaked.INSTANCE : null;
        });
    }

    private enum DynamicUnbaked implements UnbakedModel {
        INSTANCE;
        public Collection<ResourceLocation> getDependencies() { return Collections.emptyList(); }
        public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> models,
                Set<com.mojang.datafixers.util.Pair<String, String>> errors) { return Collections.emptyList(); }
        public BakedModel bake(ModelBakery bakery, Function<Material, TextureAtlasSprite> sprites,
                ModelState state, ResourceLocation location) { return DynamicBaked.INSTANCE; }
    }

    private enum DynamicBaked implements BakedModel {
        INSTANCE;
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, java.util.Random random) { return List.of(); }
        public boolean useAmbientOcclusion() { return false; }
        public boolean isGui3d() { return false; }
        public boolean usesBlockLight() { return false; }
        public boolean isCustomRenderer() { return true; }
        public TextureAtlasSprite getParticleIcon() {
            return net.minecraft.client.Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .getSprite(MissingTextureAtlasSprite.getLocation());
        }
        public ItemTransforms getTransforms() {
            BakedModel vanillaArmor = net.minecraft.client.Minecraft.getInstance().getModelManager().getModel(
                    new ModelResourceLocation("minecraft:iron_chestplate#inventory"));
            return vanillaArmor == this ? ItemTransforms.NO_TRANSFORMS : vanillaArmor.getTransforms();
        }
        public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
    }
}
