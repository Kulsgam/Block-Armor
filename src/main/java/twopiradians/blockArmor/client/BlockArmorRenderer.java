package twopiradians.blockArmor.client;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.client.model.ModelBAArmor;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.ModItems;
import twopiradians.blockArmor.common.item.CombinedArmorData;

/** Fabric renderer using the same plane layout and block-face selection as Forge. */
final class BlockArmorRenderer implements ArmorRenderer {
    private static final BlockArmorRenderer INSTANCE = new BlockArmorRenderer();
    private final Map<String, ModelBAArmor> models = new HashMap<>();

    static void register() {
        ArmorRenderer.register(INSTANCE, ModItems.allArmors.toArray(new Item[0]));
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.PoseStack matrices,
            net.minecraft.client.renderer.MultiBufferSource consumers, ItemStack stack,
            LivingEntity entity, EquipmentSlot slot, int light,
            HumanoidModel<LivingEntity> contextModel) {
        BlockArmorItem armor = (BlockArmorItem)stack.getItem();
        BlockArmorTextures.Info left = CombinedArmorData.isCombined(stack)
                ? BlockArmorTextures.findSource(CombinedArmorData.source(stack, true), armor) : BlockArmorTextures.find(armor);
        boolean combined = CombinedArmorData.isCombined(stack);
        BlockArmorTextures.Info right = combined
                ? BlockArmorTextures.findSource(CombinedArmorData.source(stack, false), armor) : left;
        ResourceLocation texture = combined ? CombinedArmorTextures.get(left, right)
                : ModelBAArmor.textureLocation(left.sprite().getName());
        int color = combined ? -1 : left.color();
        renderModel(matrices, consumers, stack, entity, slot, light, contextModel, texture, color);
    }

    private void renderModel(com.mojang.blaze3d.vertex.PoseStack matrices, net.minecraft.client.renderer.MultiBufferSource consumers,
            ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<LivingEntity> context,
            ResourceLocation texture, int color) {
        String key = slot.getName() + ":" + texture + ":" + color;
        ModelBAArmor model = models.computeIfAbsent(key, ignored -> new ModelBAArmor(slot, texture, color));
        model.copyPose(context); model.setEntity(entity);
        ArmorRenderer.renderPart(matrices, consumers, light, renderStack(stack), model, TextureAtlas.LOCATION_BLOCKS);
    }

    static void clearCaches() {
        INSTANCE.models.clear();
        CombinedArmorTextures.clear();
    }

    private static ItemStack renderStack(ItemStack original) {
        if (twopiradians.blockArmor.common.item.BlockArmorItem.hasRealEnchantment(original)) return original;
        ItemStack copy = original.copy();
        if (copy.hasTag()) copy.getTag().remove("Enchantments");
        return copy;
    }
}
