package twopiradians.blockArmor.common.menu;

import java.util.List;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.CombinedArmorData;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/**
 * Dedicated menu for the armor effect tuner. It exposes one armor input slot
 * and the player's inventory below it.
 */
public final class ArmorEffectTunerMenu extends AbstractContainerMenu {
    public static final int TUNER_SLOTS = 1;
    private final SimpleContainer tuner;

    private static SimpleContainer createTuner() {
        return new SimpleContainer(TUNER_SLOTS) {
        @Override public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot == 0 && stack.getItem() instanceof BlockArmorItem;
        }
        };
    }

    public ArmorEffectTunerMenu(int id, Inventory inventory) {
        this(id, inventory, createTuner());
    }

    private ArmorEffectTunerMenu(int id, Inventory inventory, SimpleContainer tuner) {
        super(ModMenuTypes.ARMOR_EFFECT_TUNER, id);
        this.tuner = tuner;
        tuner.startOpen(inventory.player);
        addSlot(new Slot(tuner, 0, 8, 24));
        addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack stack = tuner.getItem(0);
        if (!(stack.getItem() instanceof BlockArmorItem)) return false;
        List<SetEffect> effects = CombinedArmorData.effects(stack);
        if (id < 0 || id >= effects.size()) return false;
        SetEffect effect = effects.get(id);
        CombinedArmorData.setEffectEnabled(stack, effect, !CombinedArmorData.isEffectEnabled(stack, effect));
        tuner.setChanged();
        SetEffect.onEquipmentChange(player);
        if (!player.level().isClientSide()) SetEffect.reconcileEnchantments(stack, player.level(), player);
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < TUNER_SLOTS) {
            if (!moveItemStackTo(source, TUNER_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!(source.getItem() instanceof BlockArmorItem)
                    || !moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, source);
        return original;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        tuner.stopOpen(player);
        clearContainer(player, tuner);
    }
}
