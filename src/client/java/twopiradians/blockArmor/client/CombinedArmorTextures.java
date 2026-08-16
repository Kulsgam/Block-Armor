package twopiradians.blockArmor.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import twopiradians.blockArmor.mixin.SpriteContentsAccessor;

/** Composes the two source sprites of combined armor into one renderable texture. */
final class CombinedArmorTextures {
    private record Key(Identifier left, int leftColor, Identifier right, int rightColor) { }
    private static final Map<Key, Identifier> CACHE = new HashMap<>();

    private CombinedArmorTextures() { }

    static Identifier get(BlockArmorTextures.Info left, BlockArmorTextures.Info right) {
        Key key = new Key(left.sprite().contents().name(), left.color(),
                right.sprite().contents().name(), right.color());
        return CACHE.computeIfAbsent(key, ignored -> create(left, right, key.hashCode()));
    }

    private static Identifier create(BlockArmorTextures.Info left, BlockArmorTextures.Info right, int hash) {
        NativeImage output = new NativeImage(16, 16, true);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                BlockArmorTextures.Info source = x < 8 ? left : right;
                TextureAtlasSprite sprite = source.sprite();
                NativeImage image = ((SpriteContentsAccessor) (Object) sprite.contents()).blockarmor$getOriginalImage();
                int sourceX = Math.min(image.getWidth() - 1, x * sprite.contents().width() / 16);
                int sourceY = Math.min(image.getHeight() - 1, y * sprite.contents().height() / 16);
                output.setPixel(x, y, tint(image.getPixel(sourceX, sourceY), source.color()));
            }
        }
        Identifier id = Identifier.fromNamespaceAndPath("blockarmor", "generated/combined_" + Integer.toUnsignedString(hash));
        DynamicTexture texture = new DynamicTexture(() -> id.toString(), output);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        return id;
    }

    private static int tint(int argb, int color) {
        if (color < 0) return argb;
        int a = argb >>> 24;
        int r = (argb >>> 16 & 255) * (color >>> 16 & 255) / 255;
        int g = (argb >>> 8 & 255) * (color >>> 8 & 255) / 255;
        int b = (argb & 255) * (color & 255) / 255;
        return a << 24 | r << 16 | g << 8 | b;
    }

    static void clear() {
        CACHE.values().forEach(Minecraft.getInstance().getTextureManager()::release);
        CACHE.clear();
    }
}
