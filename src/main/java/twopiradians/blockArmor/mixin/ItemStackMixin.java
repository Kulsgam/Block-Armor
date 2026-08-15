package twopiradians.blockArmor.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.item.BlockArmorItem;

/** Restores Forge's stack-aware armor attributes and damage callback. */
@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void blockarmor$stackAttributes(EquipmentSlot slot,
            CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor)
            cir.setReturnValue(armor.getAttributeModifiers(slot, self));
    }

    @Inject(method = "setDamageValue", at = @At("HEAD"))
    private void blockarmor$beforeDamageChanged(int damage, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor)
            armor.beforeDamageChanged(self, self.getDamageValue(), damage);
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void blockarmor$configuredMaxDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor)
            cir.setReturnValue(armor.getConfiguredMaxDamage(self));
    }
}
