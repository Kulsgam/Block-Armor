package twopiradians.blockArmor.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.TextureOverrideInfo;
import twopiradians.blockArmor.mixin.ItemStackRenderStateAccessor;

/** Resolves the block face used by an armor item in both inventory and entity rendering. */
final class BlockArmorTextures {
    record Info(TextureAtlasSprite sprite, int color) { }
    private record Lookup(Info info, boolean found) { }

    private static final Map<BlockArmorItem, Lookup> CACHE = new HashMap<>();
    private static final Set<ArmorSet> VALIDATED = new HashSet<>();
    private static final Set<ArmorSet> MISSING_TEXTURES = new HashSet<>();
    private static final Set<ArmorSet> DISABLED_BY_TEXTURES = new HashSet<>();
    private static boolean needsValidation = true;

    private BlockArmorTextures() { }

    /**
     * The original armor mesh deliberately uses coordinates outside a single
     * 0..1 sprite.  Feeding it an atlas sprite remaps those coordinates into
     * neighbouring atlas entries, so worn armor must bind the source image
     * itself just like the working 1.18 Fabric renderer did.
     */
    static Identifier sourceTexture(Info info) {
        Identifier sprite = info.sprite().contents().name();
        return Identifier.fromNamespaceAndPath(sprite.getNamespace(),
                "textures/" + sprite.getPath() + ".png");
    }

    static Info find(BlockArmorItem armor) {
        validate(armor.set);
        return CACHE.computeIfAbsent(armor, BlockArmorTextures::lookup).info();
    }

    static Info findSource(CompoundTag source, BlockArmorItem fallback) {
        try {
            var stackTag = source.get("Stack");
            ItemStack stack = stackTag == null ? ItemStack.EMPTY
                    : ItemStack.OPTIONAL_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, stackTag)
                            .result().orElse(ItemStack.EMPTY);
            if (stack.isEmpty()) {
                String id = source.getStringOr("Item", "");
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                        net.minecraft.resources.Identifier.tryParse(id));
                stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
            }
            if (stack.getItem() instanceof BlockArmorItem armor) return find(armor);
            if (!stack.isEmpty()) {
                var state = new net.minecraft.client.renderer.item.ItemStackRenderState();
                Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack,
                        net.minecraft.world.item.ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
                var material = state.pickParticleMaterial(RandomSource.create(0));
                if (material != null && !isMissing(material.sprite())) return new Info(material.sprite(), -1);
            }
        } catch (RuntimeException ignored) { }
        return find(fallback);
    }

    static void clearCaches() {
        for (ArmorSet set : MISSING_TEXTURES) set.missingTextures = false;
        for (ArmorSet set : DISABLED_BY_TEXTURES) set.enable();
        CACHE.clear();
        VALIDATED.clear();
        MISSING_TEXTURES.clear();
        DISABLED_BY_TEXTURES.clear();
        needsValidation = true;
    }

    static void validateAll() {
        if (!needsValidation || Minecraft.getInstance().getModelManager() == null) return;
        // Client ticks start while the initial reload is still baking.  The
        // manager deliberately throws until its block-model set is installed.
        try {
            Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        } catch (NullPointerException notReady) {
            return;
        }
        needsValidation = false;
        for (ArmorSet set : ArmorSet.allSets) validate(set);
    }

    private static void validate(ArmorSet set) {
        if (!VALIDATED.add(set)) return;
        boolean missing = false;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            BlockArmorItem armor = set.getArmorForSlot(slot);
            if (armor != null && !CACHE.computeIfAbsent(armor, BlockArmorTextures::lookup).found()) missing = true;
        }
        if (missing) {
            boolean wasEnabled = set.isEnabled();
            set.missingTextures = true;
            set.disable();
            MISSING_TEXTURES.add(set);
            if (wasEnabled) DISABLED_BY_TEXTURES.add(set);
        }
    }

    private static Lookup lookup(BlockArmorItem armor) {
        TextureOverrideInfo override = ArmorSet.TEXTURE_OVERRIDES.get(armor.set.block);
        if (override != null && override.overrides.containsKey(armor.getSlot())) {
            TextureOverrideInfo.Info info = override.overrides.get(armor.getSlot());
            TextureAtlasSprite sprite = blockAtlas().getSprite(info.shortLoc);
            if (!isMissing(sprite)) return new Lookup(new Info(sprite, info.color), true);
        }

        BlockState state = armor.set.block.defaultBlockState();
        if (armor.set.block == Blocks.REDSTONE_LAMP) state = state.setValue(RedstoneLampBlock.LIT, true);
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(0), parts);
        Direction wanted = switch (armor.getSlot()) {
            case HEAD -> Direction.UP;
            case CHEST -> Direction.NORTH;
            case LEGS -> Direction.SOUTH;
            case FEET -> Direction.DOWN;
            default -> Direction.NORTH;
        };
        // Forge gathered both the unculled quads and every cull-face bucket,
        // then selected by the direction recorded on the quad itself.  Looking
        // only in getQuads(wanted) loses the faces of models which deliberately
        // put their geometry in the unculled bucket (and made every armor slot
        // fall back to the particle sprite).
        for (BlockStateModelPart part : parts) {
            List<BakedQuad> quads = new ArrayList<>(part.getQuads(null));
            for (Direction direction : Direction.values()) quads.addAll(part.getQuads(direction));
            for (BakedQuad quad : quads) {
                if (quad.direction() != wanted) continue;
                TextureAtlasSprite sprite = quad.materialInfo().sprite();
                if (sprite.contents().name().getPath().contains("overlay")) continue;
                int tint = quad.materialInfo().tintIndex();
                int color = itemTint(armor, state, tint);
                return new Lookup(new Info(sprite, color), !isMissing(sprite));
            }
        }
        TextureAtlasSprite particle = model.particleMaterial().sprite();
        return new Lookup(new Info(particle, -1), !isMissing(particle));
    }

    private static TextureAtlas blockAtlas() {
        return (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
    }

	/** Matches Forge's ItemColors lookup rather than applying a world-biome tint. */
	private static int itemTint(BlockArmorItem armor, BlockState state, int tintIndex) {
		if (tintIndex < 0) return -1;
		try {
			var renderState = new net.minecraft.client.renderer.item.ItemStackRenderState();
			Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState,
					new ItemStack(armor.set.item), net.minecraft.world.item.ItemDisplayContext.GUI,
					Minecraft.getInstance().level, null, 0);
			ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) renderState;
			for (int layer = 0; layer < accessor.blockarmor$getActiveLayerCount(); layer++) {
				var colors = accessor.blockarmor$getLayers()[layer].tintLayers();
				if (tintIndex < colors.size()) return colors.getInt(tintIndex);
			}
		} catch (RuntimeException ignored) { }
		var fallback = Minecraft.getInstance().getBlockColors().getTintSource(state, tintIndex);
		return fallback == null ? -1 : fallback.color(state);
	}

    private static boolean isMissing(TextureAtlasSprite sprite) {
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }
}
