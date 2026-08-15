package twopiradians.blockArmor.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Named Fabric accessor replacing Forge reflection for custom Hoarder slots. */
@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
    @Accessor("lastSlots")
    NonNullList<ItemStack> blockarmor$getLastSlots();

    @Accessor("remoteSlots")
    NonNullList<ItemStack> blockarmor$getRemoteSlots();
}
