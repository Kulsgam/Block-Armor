package twopiradians.blockArmor.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.ModItems;
import twopiradians.blockArmor.common.item.ArmorSet;

/** Composes the original Forge base/template/cover inventory icon at render time. */
final class BlockArmorItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final BlockArmorItemRenderer INSTANCE = new BlockArmorItemRenderer();
    private static final Map<EquipmentSlot, boolean[][][]> MASKS = new EnumMap<>(EquipmentSlot.class);

    static void register() {
        ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((atlas, registry) -> {
            for (String type : new String[] {"helmet", "chestplate", "leggings", "boots"}) {
                registry.register(icon(type, "_base"));
                registry.register(icon(type, "_cover"));
            }
            ArmorSet.TEXTURE_OVERRIDES.values().forEach(override ->
                    override.overrides.values().forEach(info -> registry.register(info.shortLoc)));
        });
        for (Item item : ModItems.allArmors) BuiltinItemRendererRegistry.INSTANCE.register(item, INSTANCE);
    }

    @Override
    public void render(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack pose,
            MultiBufferSource consumers, int light, int overlay) {
        BlockArmorItem armor = (BlockArmorItem)stack.getItem();
        String type = type(armor.getSlot());
        var atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite base = atlas.getSprite(icon(type, "_base"));
        TextureAtlasSprite cover = atlas.getSprite(icon(type, "_cover"));
        BlockArmorTextures.Info info = BlockArmorTextures.find(armor);
        boolean[][][] masks = MASKS.computeIfAbsent(armor.getSlot(), BlockArmorItemRenderer::loadMasks);
        VertexConsumer vertex = ItemRenderer.getFoilBufferDirect(consumers,
                RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), true, stack.hasFoil());
        pose.pushPose();
        quad(pose, vertex, base, 0, 0, 16, 16, .4685f, 0xffffffff, light, overlay);
        int color = info.color() < 0 ? 0xffffffff : 0xff000000 | info.color();
        for (int layer=0; layer<2; layer++) {
            boolean[][] mask = masks[layer];
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) if (mask[y][x])
                quad(pose, vertex, info.sprite(), x, 15 - y, x + 1, 16 - y, .4687f, color, light, overlay);
        }
        quad(pose, vertex, cover, 0, 0, 16, 16, .4690f, 0xffffffff, light, overlay);
        backQuad(pose, vertex, base, 0, 0, 16, 16, .5315f, 0xffffffff, light, overlay);
        backQuad(pose, vertex, cover, 0, 0, 16, 16, .5310f, 0xffffffff, light, overlay);
        addEdges(pose, vertex, info.sprite(), masks[0], color, light, overlay);
        addEdges(pose, vertex, info.sprite(), masks[1], color, light, overlay);
        addEdges(pose, vertex, base, masks[2], 0xffffffff, light, overlay);
        addEdges(pose, vertex, cover, masks[3], 0xffffffff, light, overlay);
        pose.popPose();
    }

    static void clearCaches() { MASKS.clear(); }

    private static void backQuad(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite,
            float x1, float y1, float x2, float y2, float z, int color, int light, int overlay) {
        float r=((color>>16)&255)/255f,g=((color>>8)&255)/255f,b=(color&255)/255f,a=((color>>>24)&255)/255f;
        PoseStack.Pose p=pose.last();
        vertex(out,p,x1/16f,y1/16f,z,r,g,b,a,sprite.getU(x1),sprite.getV(16-y1),light,overlay,0,0,-1);
        vertex(out,p,x1/16f,y2/16f,z,r,g,b,a,sprite.getU(x1),sprite.getV(16-y2),light,overlay,0,0,-1);
        vertex(out,p,x2/16f,y2/16f,z,r,g,b,a,sprite.getU(x2),sprite.getV(16-y2),light,overlay,0,0,-1);
        vertex(out,p,x2/16f,y1/16f,z,r,g,b,a,sprite.getU(x2),sprite.getV(16-y1),light,overlay,0,0,-1);
    }

    private static void addEdges(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite,
            boolean[][] mask, int color, int light, int overlay) {
        for (int y=0;y<16;y++) for (int x=0;x<16;x++) if (mask[y][x]) {
            float x1=x/16f,x2=(x+1)/16f,y1=(15-y)/16f,y2=(16-y)/16f,z1=.4687f,z2=.5313f;
            if (x==0 || !mask[y][x-1]) edge(pose,out,sprite,x1,y1,z1,x1,y2,z2,color,light,overlay,-1,0,0);
            if (x==15 || !mask[y][x+1]) edge(pose,out,sprite,x2,y2,z1,x2,y1,z2,color,light,overlay,1,0,0);
            if (y==0 || !mask[y-1][x]) edge(pose,out,sprite,x2,y2,z1,x1,y2,z2,color,light,overlay,0,1,0);
            if (y==15 || !mask[y+1][x]) edge(pose,out,sprite,x1,y1,z1,x2,y1,z2,color,light,overlay,0,-1,0);
        }
    }

    private static void edge(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite,
            float x1,float y1,float z1,float x2,float y2,float z2,int color,int light,int overlay,float nx,float ny,float nz) {
        float r=((color>>16)&255)/255f,g=((color>>8)&255)/255f,b=(color&255)/255f,a=((color>>>24)&255)/255f;
        PoseStack.Pose p=pose.last(); float u=sprite.getU((x1+x2)*8),v=sprite.getV(16-(y1+y2)*8);
        vertex(out,p,x1,y1,z1,r,g,b,a,u,v,light,overlay,nx,ny,nz); vertex(out,p,x2,y2,z1,r,g,b,a,u,v,light,overlay,nx,ny,nz);
        vertex(out,p,x2,y2,z2,r,g,b,a,u,v,light,overlay,nx,ny,nz); vertex(out,p,x1,y1,z2,r,g,b,a,u,v,light,overlay,nx,ny,nz);
    }

    private static void quad(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite,
            float x1, float y1, float x2, float y2, float z, int color, int light, int overlay) {
        float r = ((color >> 16) & 255) / 255f, g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f, a = ((color >>> 24) & 255) / 255f;
        PoseStack.Pose p = pose.last();
        vertex(out,p,x1/16f,y1/16f,z,r,g,b,a,sprite.getU(x1),sprite.getV(16-y1),light,overlay);
        vertex(out,p,x2/16f,y1/16f,z,r,g,b,a,sprite.getU(x2),sprite.getV(16-y1),light,overlay);
        vertex(out,p,x2/16f,y2/16f,z,r,g,b,a,sprite.getU(x2),sprite.getV(16-y2),light,overlay);
        vertex(out,p,x1/16f,y2/16f,z,r,g,b,a,sprite.getU(x1),sprite.getV(16-y2),light,overlay);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, float x, float y, float z,
            float r, float g, float b, float a, float u, float v, int light, int overlay) {
        vertex(out, pose, x,y,z,r,g,b,a,u,v,light,overlay,0,0,1);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, float x, float y, float z,
            float r, float g, float b, float a, float u, float v, int light, int overlay,
            float nx, float ny, float nz) {
        out.vertex(pose.pose(),x,y,z).color(r,g,b,a).uv(u,v).overlayCoords(overlay).uv2(light)
                .normal(pose.normal(),nx,ny,nz).endVertex();
    }

    private static boolean[][][] loadMasks(EquipmentSlot slot) {
        boolean[][][] result = new boolean[4][16][16];
        String type = type(slot);
        for (int layer = 0; layer < 4; layer++) {
            String suffix = layer < 2 ? (layer + 1) + "_template" : layer == 2 ? "_base" : "_cover";
            ResourceLocation png = new ResourceLocation(BlockArmor.MODID,
                    "textures/items/icons/block_armor_" + type + suffix + ".png");
            try (var input = Minecraft.getInstance().getResourceManager().getResource(png).getInputStream();
                    NativeImage image = NativeImage.read(input)) {
                for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
                    result[layer][y][x] = ((image.getPixelRGBA(x, y) >>> 24) & 255) != 0;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load Block Armor icon mask " + png, e);
            }
        }
        return result;
    }

    private static ResourceLocation icon(String type, String suffix) {
        return new ResourceLocation(BlockArmor.MODID, "items/icons/block_armor_" + type + suffix);
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
}
