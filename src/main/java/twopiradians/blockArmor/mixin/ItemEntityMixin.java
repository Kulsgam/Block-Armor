package twopiradians.blockArmor.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twopiradians.blockArmor.common.item.BlockArmorItem;

/** Fabric replacement for Forge's Item#onEntityItemUpdate. */
@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void blockarmor$tickDroppedArmor(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.getItem().getItem() instanceof BlockArmorItem armor && armor.tickDropped(self.getItem(), self))
            ci.cancel();
    }
}
