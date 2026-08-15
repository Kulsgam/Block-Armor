package twopiradians.blockArmor.common;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.item.BlockArmorItem;

/** Fabric replacement for Forge's ArmorItem.onArmorTick hook. */
public final class ArmorLifecycle {
    private ArmorLifecycle() {}

    public static void tickPlayer(Player player) {
        for (EquipmentSlot slot : ArmorSet.SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockArmorItem armor)
                armor.tickEquipped(stack, player.level, player);
        }
    }
}
