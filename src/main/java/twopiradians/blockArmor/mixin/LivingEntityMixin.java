package twopiradians.blockArmor.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.seteffect.SetEffectRespawn;
import twopiradians.blockArmor.common.seteffect.SetEffectSoft_Fall;
import twopiradians.blockArmor.common.seteffect.SetEffectLightweight;
import twopiradians.blockArmor.common.seteffect.SetEffectSlimey;
import twopiradians.blockArmor.common.seteffect.SetEffectFiery;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.common.item.ArmorSet;

/** Hooks that Fabric 1.18 does not expose as lifecycle callbacks. */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void blockarmor$reconcileEquipmentAttributes(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) SetEffect.onEquipmentChange(entity);
    }
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void blockarmor$preventFallDamage(float distance, float multiplier, DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof net.minecraft.world.entity.player.Player player
                && SetEffectSlimey.preventsFallDamage(player, distance)) {
            cir.setReturnValue(false);
            return;
        }
        if (SetEffectSoft_Fall.preventsFallDamage((LivingEntity) (Object) this, distance)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float blockarmor$reduceLightweightDistance(float distance) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return ArmorSet.hasSetEffect(entity, SetEffect.LIGHTWEIGHT) && !entity.isShiftKeyDown()
                ? distance / 10.0F : distance;
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float blockarmor$reduceLightweightDamage(float multiplier) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ArmorSet.hasSetEffect(entity, SetEffect.SLIMEY) && entity.isShiftKeyDown()) multiplier *= .1F;
        if (!ArmorSet.hasSetEffect(entity, SetEffect.LIGHTWEIGHT)) return multiplier;
        return multiplier * (entity.isShiftKeyDown() ? 0.1F : 0.01F);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void blockarmor$applyFieryAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof LivingEntity attacker)
            SetEffectFiery.onAttack(attacker, (LivingEntity) (Object) this);
    }
}
