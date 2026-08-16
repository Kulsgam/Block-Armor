package twopiradians.blockArmor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.CombinedArmorData;
import twopiradians.blockArmor.common.item.ModItems;
import twopiradians.blockArmor.client.model.ModelBAArmor;
import twopiradians.blockArmor.common.command.CommandDev;
import twopiradians.blockArmor.client.config.BlockArmorClientConfig;

/** Supplies per-block textures to Fabric's 26.1 equipped-armor renderer. */
final class BlockArmorRenderer implements ArmorRenderer {
    private static final BlockArmorRenderer INSTANCE = new BlockArmorRenderer();
    private final java.util.EnumMap<EquipmentSlot, ModelBAArmor> models = new java.util.EnumMap<>(EquipmentSlot.class);
    private static int renderCalls;

    private BlockArmorRenderer() { }

    static void register() {
        ArmorRenderer.register(INSTANCE, ModItems.allArmors.toArray(net.minecraft.world.level.ItemLike[]::new));
        twopiradians.blockArmor.common.BlockArmor.LOGGER.info(
                "Registered Block Armor equipped renderer for {} generated items", ModItems.allArmors.size());
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector nodes, ItemStack stack,
            HumanoidRenderState state, EquipmentSlot slot, int light,
            HumanoidModel<HumanoidRenderState> model) {
        renderCalls++;
        BlockArmorItem armor = (BlockArmorItem) stack.getItem();
        boolean combined = CombinedArmorData.isCombined(stack);
        BlockArmorTextures.Info left = combined
                ? BlockArmorTextures.findSource(CombinedArmorData.source(stack, true), armor)
                : BlockArmorTextures.find(armor);
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
        int color = applyDevColor(combined ? -1 : left.color(), state);
        ModelBAArmor custom = models.computeIfAbsent(slot, ModelBAArmor::new);
		matrices.pushPose();
		if (state.isBaby) {
			// Preserve the working Fabric port's child-model transform for every
			// slot.  The old Forge renderer's split head/body transform does not
			// match Fabric's copied humanoid pose.
			matrices.scale(.5F, .5F, .5F);
			matrices.translate(0D, 1.5D, 0D);
		}
        if (combined) {
            BlockArmorTextures.Info right = BlockArmorTextures.findSource(CombinedArmorData.source(stack, false), armor);
            ArmorRenderer.submitTransformCopyingModel(model, state, custom, state, false, nodes, matrices,
                    RenderTypes.armorTranslucent(CombinedArmorTextures.get(left, right)), light, overlay,
                    colorArgb(color), null, 0, null);
        } else {
            ArmorRenderer.submitTransformCopyingModel(model, state, custom, state, false, nodes, matrices,
                    RenderTypes.armorTranslucent(BlockArmorTextures.sourceTexture(left)),
                    light, overlay, colorArgb(color), null, 0, null);
        }
        // Match EquipmentLayerRenderer: hasFoil() is the vanilla/mod-overridable
        // decision point and armorEntityGlint() supplies the native animated pass.
        if (stack.hasFoil() || BlockArmorClientConfig.alwaysShowArmorGlint) {
            ArmorRenderer.submitTransformCopyingModel(model, state, custom, state, false, nodes.order(1), matrices,
                    RenderTypes.armorEntityGlint(), light, overlay, colorArgb(color), null, 0, null);
        }
		matrices.popPose();
    }

    @Override
    public boolean shouldRenderDefaultHeadItem(net.minecraft.world.entity.LivingEntity entity, ItemStack stack) {
        return false;
    }

    private static int colorArgb(int rgb) {
        return rgb < 0 ? -1 : 0xff000000 | rgb;
    }

	private static int applyDevColor(int rgb, HumanoidRenderState state) {
		Float[] dev = CommandDev.devColors.get(((HumanoidRenderStateExtension) state).blockarmor$getEntityUuid());
		if (dev == null) return rgb;
		float red = rgb < 0 ? 1F : (rgb >> 16 & 255) / 255F;
		float green = rgb < 0 ? 1F : (rgb >> 8 & 255) / 255F;
		float blue = rgb < 0 ? 1F : (rgb & 255) / 255F;
		if (dev[0] == 0F && dev[1] == 0F && dev[2] == 0F) {
			int rainbow = java.awt.Color.HSBtoRGB(((int) state.ageInTicks) / 30F, 1F, 1F);
			red = (rainbow >> 16 & 255) / 255F;
			green = (rainbow >> 8 & 255) / 255F;
			blue = (rainbow & 255) / 255F;
		} else {
			double pulse = (Math.cos(((int) state.ageInTicks) / 5F) + 1D) / 3D + .01D;
			red += pulse * dev[0]; green += pulse * dev[1]; blue += pulse * dev[2];
		}
		return Math.min(255, Math.round(red * 255F)) << 16
				| Math.min(255, Math.round(green * 255F)) << 8
				| Math.min(255, Math.round(blue * 255F));
	}

    static void clearCaches() {
        INSTANCE.models.clear();
        CombinedArmorTextures.clear();
    }

    static int renderCalls() { return renderCalls; }
}
