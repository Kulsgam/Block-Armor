package twopiradians.blockArmor.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.ItemStack;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.loot.LuckyLootContext;

/** Adds Lucky's virtual Looting levels while an entity loot table is evaluated. */
@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    @ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private static Object blockarmor$liveEnchantability(Object registeredValue,
            net.minecraft.util.RandomSource random, ItemStack stack, int level,
            java.util.stream.Stream<Holder<Enchantment>> candidates) {
        if (stack.getItem() instanceof twopiradians.blockArmor.common.item.BlockArmorItem armor)
            return new Enchantable(Math.max(0, armor.getEnchantmentValue()));
        return registeredValue;
    }

    @Inject(method = "getEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void blockarmor$luckyLooting(Holder<Enchantment> enchantment, LivingEntity entity,
            CallbackInfoReturnable<Integer> cir) {
        if (enchantment.is(Enchantments.LOOTING))
            cir.setReturnValue(LuckyLootContext.adjust(cir.getReturnValue(), entity));
    }
}
