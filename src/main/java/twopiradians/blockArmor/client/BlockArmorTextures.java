package twopiradians.blockArmor.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.TextureOverrideInfo;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.mixin.MinecraftItemColorsAccessor;

final class BlockArmorTextures {
    record Info(TextureAtlasSprite sprite, int color) { }
    private record Lookup(Info info, boolean found) { }
    private static final Map<BlockArmorItem, Lookup> CACHE = new HashMap<>();
    private static final Set<ArmorSet> VALIDATED = new HashSet<>();
    private static final Set<ArmorSet> DISABLED_BY_TEXTURES = new HashSet<>();
    private static boolean needsValidation = true;

    private BlockArmorTextures() { }

    static Info find(BlockArmorItem armor) {
        validate(armor.set);
        return CACHE.computeIfAbsent(armor, BlockArmorTextures::lookup).info();
    }

    /** Resolves a stored visual source. Non-Block Armor sources use their baked inventory sprite. */
    static Info findSource(CompoundTag source, BlockArmorItem fallback) {
        try {
            ItemStack stack = ItemStack.of(source.getCompound("Stack"));
            if (stack.isEmpty()) return find(fallback);
            if (stack.getItem() instanceof BlockArmorItem armor) return find(armor);
            var model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
            TextureAtlasSprite sprite = model.getParticleIcon();
            int color = ((MinecraftItemColorsAccessor) (Object) Minecraft.getInstance()).blockarmor$getItemColors().getColor(stack, 0);
            return new Info(sprite, color < 0 ? -1 : color);
        } catch (RuntimeException ignored) { return find(fallback); }
    }

    static void clearCaches() {
        for (ArmorSet set : DISABLED_BY_TEXTURES) {
            set.missingTextures = false;
            set.enable();
        }
        CACHE.clear();
        VALIDATED.clear();
        DISABLED_BY_TEXTURES.clear();
        needsValidation = true;
    }

    static void validateAll() {
        if (!needsValidation) return;
        // Client ticks begin while the initial resource reload is still baking models.
        // Defer validation instead of treating that temporary null model as a missing texture.
        if (Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState()) == null) return;
        needsValidation = false;
        for (ArmorSet set : ArmorSet.allSets) validate(set);
    }

    private static void validate(ArmorSet set) {
        if (!VALIDATED.add(set)) return;
        boolean missing = false;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            BlockArmorItem armor = set.getArmorForSlot(slot);
            if (armor != null && !CACHE.computeIfAbsent(armor, BlockArmorTextures::lookup).found()) missing = true;
        }
        if (missing) {
            set.missingTextures = true;
            set.disable();
            DISABLED_BY_TEXTURES.add(set);
        }
    }

    private static Lookup lookup(BlockArmorItem armor) {
        TextureOverrideInfo override = armor.set.TEXTURE_OVERRIDES.get(armor.set.block);
        if (override != null && override.overrides.containsKey(armor.getSlot())) {
            TextureOverrideInfo.Info info = override.overrides.get(armor.getSlot());
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                    .getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(info.shortLoc);
            if (!sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) return new Lookup(new Info(sprite, info.color), true);
        }

        BlockState state = armor.set.block.defaultBlockState();
        if (armor.set.block == Blocks.REDSTONE_LAMP) state = state.setValue(RedstoneLampBlock.LIT, true);
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        if (model == null) {
            TextureAtlasSprite missing = Minecraft.getInstance().getModelManager()
                    .getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(MissingTextureAtlasSprite.getLocation());
            return new Lookup(new Info(missing, -1), false);
        }
        List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, null, new Random(0)));
        for (Direction direction : Direction.values()) quads.addAll(model.getQuads(state, direction, new Random(0)));
        Direction wanted = switch (armor.getSlot()) {
            case HEAD -> Direction.UP;
            case CHEST -> Direction.NORTH;
            case LEGS -> Direction.SOUTH;
            case FEET -> Direction.DOWN;
            default -> Direction.NORTH;
        };
        for (BakedQuad quad : quads) {
            if (quad.getDirection() != wanted || quad.getSprite().getName().getPath().contains("overlay")) continue;
            int color = quad.isTinted()
                    ? Minecraft.getInstance().getBlockColors().getColor(state, null, null, quad.getTintIndex()) : -1;
            boolean found = !quad.getSprite().getName().equals(MissingTextureAtlasSprite.getLocation());
            return new Lookup(new Info(quad.getSprite(), color), found);
        }
        TextureAtlasSprite particle = model.getParticleIcon();
        return new Lookup(new Info(particle, -1), false);
    }
}
