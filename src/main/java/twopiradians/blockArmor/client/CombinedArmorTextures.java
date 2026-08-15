package twopiradians.blockArmor.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import twopiradians.blockArmor.mixin.TextureAtlasSpriteAccessor;

/** Builds one real 16x16 texture so the Forge armor model is rendered only once. */
final class CombinedArmorTextures {
    private record Key(ResourceLocation left, int leftColor, ResourceLocation right, int rightColor) { }
    private static final Map<Key, ResourceLocation> CACHE = new HashMap<>();

    private CombinedArmorTextures() { }

    static ResourceLocation get(BlockArmorTextures.Info left, BlockArmorTextures.Info right) {
        Key key = new Key(left.sprite().getName(), left.color(), right.sprite().getName(), right.color());
        return CACHE.computeIfAbsent(key, ignored -> create(left, right));
    }

    private static ResourceLocation create(BlockArmorTextures.Info left, BlockArmorTextures.Info right) {
        NativeImage output = new NativeImage(16, 16, true);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                BlockArmorTextures.Info source = x < 8 ? left : right;
                TextureAtlasSprite sprite = source.sprite();
                NativeImage image = ((TextureAtlasSpriteAccessor) (Object) sprite).blockarmor$getMainImages()[0];
                // mainImage can contain an entire animation strip.  The sprite
                // dimensions are the dimensions of one frame, which is what
                // Forge uses to build the armor texture/model.
                int sourceX = Math.min(image.getWidth() - 1, x * sprite.getWidth() / 16);
                int sourceY = Math.min(image.getHeight() - 1, y * sprite.getHeight() / 16);
                output.setPixelRGBA(x, y, tint(image.getPixelRGBA(sourceX, sourceY), source.color()));
            }
        }
        DynamicTexture texture = new DynamicTexture(output);
        return Minecraft.getInstance().getTextureManager().register("blockarmor_combined", texture);
    }

    // NativeImage's packed color is ABGR; block/item tint is RGB.
    private static int tint(int abgr, int color) {
        if (color < 0) return abgr;
        int a = abgr >>> 24;
        int b = (abgr >>> 16 & 255) * (color & 255) / 255;
        int g = (abgr >>> 8 & 255) * (color >>> 8 & 255) / 255;
        int r = (abgr & 255) * (color >>> 16 & 255) / 255;
        return a << 24 | b << 16 | g << 8 | r;
    }

    static void clear() {
        CACHE.values().forEach(location -> Minecraft.getInstance().getTextureManager().release(location));
        CACHE.clear();
    }
}
