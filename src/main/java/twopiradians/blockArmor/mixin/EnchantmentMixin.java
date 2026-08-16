package twopiradians.blockArmor.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.item.BlockArmorItem;

/** Makes runtime-generated Block Armor participate in vanilla armor enchantments. */
@Mixin(Enchantment.class)
abstract class EnchantmentMixin {
    @Inject(method = {"canEnchant", "isSupportedItem", "isPrimaryItem"}, at = @At("HEAD"), cancellable = true)
    private void blockarmor$acceptSlotCompatibleArmorEnchantments(ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof BlockArmorItem armor
                && ((Enchantment) (Object) this).matchingSlot(armor.getSlot())) {
            cir.setReturnValue(true);
        }
    }
}
