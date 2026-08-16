package twopiradians.blockArmor.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twopiradians.blockArmor.client.HumanoidRenderStateExtension;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN"))
    private void blockarmor$captureEntityUuid(LivingEntity entity, LivingEntityRenderState state,
            float partialTick, CallbackInfo ci) {
        ((HumanoidRenderStateExtension) state).blockarmor$setEntityUuid(entity.getUUID());
    }
}
