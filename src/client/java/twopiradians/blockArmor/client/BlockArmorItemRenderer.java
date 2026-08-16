package twopiradians.blockArmor.client;

import com.mojang.math.Quadrant;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.CombinedArmorData;
import twopiradians.blockArmor.common.BlockArmor;

/** Builds a flat, atlas-backed icon from the actual block faces stored in each stack. */
final class BlockArmorItemRenderer implements ItemModel {
    private record Key(net.minecraft.resources.Identifier left, net.minecraft.resources.Identifier right,
            EquipmentSlot slot, boolean split) { }
    private static final Map<Key, List<BakedQuad>> CACHE = new HashMap<>();
    private static final Map<EquipmentSlot, boolean[][][]> MASKS = new EnumMap<>(EquipmentSlot.class);
    private static int updateCalls;

    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final ModelBaker baker;

    BlockArmorItemRenderer(ModelRenderProperties properties, Matrix4fc transformation, ModelBaker baker) {
        this.properties = properties;
        this.transformation = transformation;
        this.baker = baker;
    }

    static void register() { }
    static void clearCaches() { CACHE.clear(); MASKS.clear(); }
    static int updateCalls() { return updateCalls; }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
            ItemDisplayContext display, ClientLevel level, ItemOwner owner, int seed) {
        if (!(stack.getItem() instanceof BlockArmorItem armor)) return;
        updateCalls++;
        // 26.1 reuses item render state by this identity. A singleton identity
        // made the first rendered armor (often Crying Obsidian chestplate) get
        // reused for every generated item and slot.
        state.appendModelIdentityElement(stack.getItem());
        state.appendModelIdentityElement(stack.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY));
        boolean split = CombinedArmorData.isCombined(stack);
        BlockArmorTextures.Info left = split
                ? BlockArmorTextures.findSource(CombinedArmorData.source(stack, true), armor)
                : BlockArmorTextures.find(armor);
        BlockArmorTextures.Info right = split
                ? BlockArmorTextures.findSource(CombinedArmorData.source(stack, false), armor) : left;
        Key key = new Key(left.sprite().contents().name(), right.sprite().contents().name(), armor.getSlot(), split);
        List<BakedQuad> quads = CACHE.computeIfAbsent(key, ignored -> bake(left, right, armor.getSlot()));

        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        layer.prepareQuadList().addAll(quads);
        layer.tintLayers().add(colorArgb(left.color()));
        if (split) layer.tintLayers().add(colorArgb(right.color()));
        layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
        layer.setLocalTransform(transformation);
        layer.setParticleMaterial(new Material.Baked(left.sprite(), false));
        properties.applyToLayer(layer, display);
        if (BlockArmorItem.hasRealEnchantment(stack)
                || twopiradians.blockArmor.client.config.BlockArmorClientConfig.alwaysShowArmorGlint)
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        if (left.sprite().contents().isAnimated() || right.sprite().contents().isAnimated()) state.setAnimated();
    }

    private List<BakedQuad> bake(BlockArmorTextures.Info left, BlockArmorTextures.Info right, EquipmentSlot slot) {
        List<BakedQuad> out = new ArrayList<>();
        String type = type(slot);
        var atlas = (net.minecraft.client.renderer.texture.TextureAtlas) net.minecraft.client.Minecraft.getInstance()
                .getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
        BlockArmorTextures.Info base = new BlockArmorTextures.Info(atlas.getSprite(icon(type, "_base")), -1);
        BlockArmorTextures.Info cover = new BlockArmorTextures.Info(atlas.getSprite(icon(type, "_cover")), -1);
        boolean[][][] masks = MASKS.computeIfAbsent(slot, BlockArmorItemRenderer::loadMasks);

        // Match Forge's ItemLayerModel composition.  The base is the only
        // extruded layer; the block-texture templates and cover are two-sided
        // planes, offset by tiny amounts to avoid depth fighting.
        addPanel(out, base, 0, 16, 0, 16, -1, 7.5f, 8.5f, true);
        for (int layer = 0; layer < 2; layer++)
            addMasked(out, left, right, masks[layer], layer, 7.498f, 8.502f);
        addPanel(out, cover, 0, 16, 0, 16, -1, 7.496f, 8.503f, true);
        addEdges(out, base, base, masks[2], -1);
        return List.copyOf(out);
    }

    private void addMasked(List<BakedQuad> out, BlockArmorTextures.Info left,
            BlockArmorTextures.Info right, boolean[][] mask, int ignoredLayer,
            float northZ, float southZ) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) if (mask[y][x]) {
                BlockArmorTextures.Info info = x < 8 ? left : right;
                addPanel(out, info, x, x + 1, x, x + 1, x < 8 ? 0 : 1,
                        northZ, southZ, true,
                        15 - y, 16 - y, y, y + 1);
            }
        }
    }

    private void addPanel(List<BakedQuad> out, BlockArmorTextures.Info info,
            float minX, float maxX, float minU, float maxU, int tint,
            float northZ, float southZ, boolean back) {
        addPanel(out, info, minX, maxX, minU, maxU, tint,
                northZ, southZ, back, 0, 16, 0, 16);
    }

    private void addPanel(List<BakedQuad> out, BlockArmorTextures.Info info,
            float minX, float maxX, float minU, float maxU, int tint,
            float northZ, float southZ, boolean back,
            float minY, float maxY, float minV, float maxV) {
        Material.Baked material = new Material.Baked(info.sprite(), false);
        CuboidFace.UVs uv = new CuboidFace.UVs(minU, minV, maxU, maxV);
        CuboidFace face = new CuboidFace(null, tint, "#armor", uv, Quadrant.R0);
        Vector3f from = new Vector3f(minX, minY, northZ);
        Vector3f to = new Vector3f(maxX, maxY, southZ);
        out.add(FaceBakery.bakeQuad(baker, from, to, face, material, Direction.NORTH,
                BlockModelRotation.IDENTITY, null, true, 0));
        if (back) out.add(FaceBakery.bakeQuad(baker, from, to, face, material, Direction.SOUTH,
                    BlockModelRotation.IDENTITY, null, true, 0));
    }

    private void addEdges(List<BakedQuad> out, BlockArmorTextures.Info left, BlockArmorTextures.Info right,
            boolean[][] mask, int tint) {
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) if (mask[y][x]) {
            BlockArmorTextures.Info info = x < 8 ? left : right;
            if (x == 0 || !mask[y][x - 1]) addEdge(out, info, x, 15-y, x+1, 16-y, tint < 0 ? -1 : x < 8 ? 0 : 1, Direction.WEST);
            if (x == 15 || !mask[y][x + 1]) addEdge(out, info, x, 15-y, x+1, 16-y, tint < 0 ? -1 : x < 8 ? 0 : 1, Direction.EAST);
            if (y == 0 || !mask[y - 1][x]) addEdge(out, info, x, 15-y, x+1, 16-y, tint < 0 ? -1 : x < 8 ? 0 : 1, Direction.UP);
            if (y == 15 || !mask[y + 1][x]) addEdge(out, info, x, 15-y, x+1, 16-y, tint < 0 ? -1 : x < 8 ? 0 : 1, Direction.DOWN);
        }
    }

    private void addEdge(List<BakedQuad> out, BlockArmorTextures.Info info, float minX, float minY,
            float maxX, float maxY, int tint, Direction direction) {
        Material.Baked material = new Material.Baked(info.sprite(), false);
        CuboidFace face = new CuboidFace(null, tint, "#armor",
                new CuboidFace.UVs(minX, 16-maxY, maxX, 16-minY), Quadrant.R0);
        out.add(FaceBakery.bakeQuad(baker, new Vector3f(minX, minY, 7.5f),
                new Vector3f(maxX, maxY, 8.5f), face, material, direction,
                BlockModelRotation.IDENTITY, null, true, 0));
    }

    private static boolean[][][] loadMasks(EquipmentSlot slot) {
        boolean[][][] result = new boolean[4][16][16];
        String type = type(slot);
        for (int layer = 0; layer < 4; layer++) {
            String suffix = layer < 2 ? (layer + 1) + "_template" : layer == 2 ? "_base" : "_cover";
            var png = net.minecraft.resources.Identifier.fromNamespaceAndPath(BlockArmor.MODID,
                    "textures/items/icons/block_armor_" + type + suffix + ".png");
            try (var input = net.minecraft.client.Minecraft.getInstance().getResourceManager()
                    .getResourceOrThrow(png).open(); NativeImage image = NativeImage.read(input)) {
                for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
                    result[layer][y][x] = (image.getPixel(x, y) >>> 24) != 0;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load Block Armor icon mask " + png, e);
            }
        }
        return result;
    }

    private static net.minecraft.resources.Identifier icon(String type, String suffix) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(BlockArmor.MODID,
                "items/icons/block_armor_" + type + suffix);
    }

    private static String type(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> "chestplate";
        };
    }

    private static int colorArgb(int rgb) {
        return rgb < 0 ? -1 : 0xff000000 | rgb;
    }
}
