package twopiradians.blockArmor.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.loot.LuckyLootContext;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    @Inject(method = "getMobLooting", at = @At("RETURN"), cancellable = true)
    private static void blockarmor$luckyLooting(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(LuckyLootContext.adjust(cir.getReturnValue(), entity));
    }
}
