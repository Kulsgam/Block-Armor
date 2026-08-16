package twopiradians.blockArmor.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.CombinedArmorData;
import net.minecraft.world.item.Rarity;

/** Restores Forge's stack-aware armor attributes and damage callback. */
@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void blockarmor$stackAttributes(EquipmentSlot slot,
            java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor) {
            for (var entry : armor.getAttributeModifiers(slot, self).entries())
                consumer.accept(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getKey()), entry.getValue());
            ci.cancel();
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void blockarmor$stackAttributes(EquipmentSlotGroup group,
            org.apache.commons.lang3.function.TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer,
            CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor) {
            // Tooltip generation asks for ANY, ARMOR and the concrete slot.  Emitting
            // for every group that contains our slot repeats the same attributes three
            // times.  Block Armor attributes belong only under the concrete slot.
            EquipmentSlot slot = armor.getSlot();
            if (group == EquipmentSlotGroup.bySlot(slot)) {
                for (var entry : armor.getAttributeModifiers(slot, self).entries()) {
                    consumer.accept(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getKey()),
                            entry.getValue(), ItemAttributeModifiers.Display.attributeModifiers());
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "setDamageValue", at = @At("HEAD"))
    private void blockarmor$beforeDamageChanged(int damage, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor)
            armor.beforeDamageChanged(self, self.getDamageValue(), damage, BlockArmorItem.currentDamageContext());
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void blockarmor$configuredMaxDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof BlockArmorItem armor)
            cir.setReturnValue(armor.getConfiguredMaxDamage(self));
    }

    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    private void blockarmor$effectRarity(CallbackInfoReturnable<Rarity> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!(self.getItem() instanceof BlockArmorItem)) return;
        if (self.isEnchanted()) cir.setReturnValue(Rarity.RARE);
        else if (!CombinedArmorData.effects(self).isEmpty()) cir.setReturnValue(Rarity.UNCOMMON);
        else cir.setReturnValue(Rarity.COMMON);
    }
}
