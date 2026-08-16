package twopiradians.blockArmor.common.item;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Mutable configuration-backed material data.  Modern Minecraft's ArmorMaterial
 * is an immutable value used while constructing an item; Block Armor instead
 * calculates its effective values from the server configuration at runtime.
 */
public final class BlockArmorMaterial {
    private static final int[] DURABILITY_MULTIPLIERS = {13, 15, 16, 11};
    private final int durability;
    private final int[] defence;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    public BlockArmorMaterial(String ignoredName, int durability, int[] defence, int enchantability,
            SoundEvent equipSound, float toughness, float knockbackResistance,
            Supplier<Ingredient> repairIngredient) {
        this.durability = durability;
        this.defence = defence.clone();
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    public int getDurabilityForSlot(EquipmentSlot slot) {
        return DURABILITY_MULTIPLIERS[slot.getIndex()] * durability;
    }

    public int getDefenseForSlot(EquipmentSlot slot) { return defence[slot.getIndex()]; }
    public int getEnchantmentValue() { return enchantability; }
    public SoundEvent getEquipSound() { return equipSound; }
    public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    public float getToughness() { return toughness; }
    public float getKnockbackResistance() { return knockbackResistance; }
}
