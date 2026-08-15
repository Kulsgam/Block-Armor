package twopiradians.blockArmor.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.item.CombinedArmorData;

/** Adds the intentionally free Block Armor hybrid operation without changing normal anvil recipes. */
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin {
    @Shadow private String itemName;
    @Shadow private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void blockarmor$createCombinedResult(CallbackInfo ci) {
        ItemCombinerMenuAccessor menu = (ItemCombinerMenuAccessor) (Object) this;
        ItemStack first = menu.blockarmor$getInputSlots().getItem(0);
        ItemStack second = menu.blockarmor$getInputSlots().getItem(1);
        if (!CombinedArmorData.canCombine(first, second)) return;
        menu.blockarmor$getResultSlots().setItem(0, CombinedArmorData.combine(first, second, itemName));
        repairItemCountCost = 0;
        cost.set(0);
        ((AnvilMenu) (Object) this).broadcastChanges();
        ci.cancel();
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void blockarmor$allowFreePickup(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        ItemCombinerMenuAccessor menu = (ItemCombinerMenuAccessor) (Object) this;
        if (CombinedArmorData.canCombine(menu.blockarmor$getInputSlots().getItem(0), menu.blockarmor$getInputSlots().getItem(1))
                && CombinedArmorData.isCombined(menu.blockarmor$getResultSlots().getItem(0))) cir.setReturnValue(true);
    }
}
