package twopiradians.blockArmor.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Stack-local mechanics and visual sources for armor made in an anvil. */
public final class CombinedArmorData {
    public static final String KEY = "BlockArmorCombined";
    private static final String ATTRIBUTES = "Attributes";
    private static final String EFFECTS = "Effects";
    private static final String LEFT = "Left";
    private static final String RIGHT = "Right";
    private static final String MAX_DAMAGE = "MaxDamage";
    private static final String ENCHANTABILITY = "Enchantability";
    private CombinedArmorData() {}

    /** Transitional readers used by the 26.1 component migration. */
    public static boolean isDevSpawned(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr("devSpawned", false);
    }

    public static boolean hasInjectedEnchantments(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .contains(twopiradians.blockArmor.common.BlockArmor.MODID + " injectedEnchantments");
    }

    /**
     * Returns whether the stack has an enchantment that was actually present
     * before Block Armor injected set-effect enchantments. Injected enchantments
     * remain functional without forcing a visual glint.
     */
    public static boolean hasRealEnchantments(ItemStack stack) {
        var enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS,
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return false;
        CompoundTag originals = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .getCompound(twopiradians.blockArmor.common.BlockArmor.MODID + " injectedEnchantments")
                .orElse(null);
        if (originals == null) return true;
        for (var entry : enchantments.entrySet()) {
            String id = entry.getKey().unwrapKey().map(key -> key.identifier().toString()).orElse("");
            if (!originals.contains(id) || originals.getIntOr(id, 0) > 0) return true;
        }
        return false;
    }

    public static boolean isCombined(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(KEY).isPresent();
    }
    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(KEY).orElseGet(CompoundTag::new);
    }

    public static boolean canCombine(ItemStack first, ItemStack second) {
        return first.getItem() instanceof BlockArmorItem left
                && second.get(DataComponents.EQUIPPABLE) instanceof Equippable equippable
                && left.getSlot() == equippable.slot();
    }

    public static ItemStack combine(ItemStack first, ItemStack second, String requestedName) {
        BlockArmorItem armor = (BlockArmorItem) first.getItem();
        ItemStack result = first.copy();
        result.setCount(1);
        CompoundTag out = new CompoundTag();
        out.putInt("Version", 1);
        out.putInt(MAX_DAMAGE, Math.max(first.getMaxDamage(), second.getMaxDamage()));
        out.putInt(ENCHANTABILITY, Math.max(enchantability(first), enchantability(second)));
        writeAttributes(out, mergeAttributes(first, second, armor.getSlot()));
        writeEffects(out, mergeEffects(effects(first), effects(second)));
        out.put(LEFT, source(first, true));
        out.put(RIGHT, source(second, false));
        CustomData.update(DataComponents.CUSTOM_DATA, result, root -> {
            root.remove("wearingFullSet");
            root.put(KEY, out);
        });
        mergeEnchantments(result, second);
        int firstRepairCost = first.getOrDefault(DataComponents.REPAIR_COST, 0);
        int secondRepairCost = second.getOrDefault(DataComponents.REPAIR_COST, 0);
        result.set(DataComponents.REPAIR_COST,
                net.minecraft.world.inventory.AnvilMenu.calculateIncreasedRepairCost(
                        Math.max(firstRepairCost, secondRepairCost)));
        int repaired = repairedDamage(first, second, out.getIntOr(MAX_DAMAGE, result.getMaxDamage()));
        result.setDamageValue(repaired);
        if (requestedName != null) {
			if (requestedName.isBlank()) result.remove(DataComponents.CUSTOM_NAME);
			else result.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(requestedName));
        }
        return result;
    }

    private static int enchantability(ItemStack stack) {
		return isCombined(stack) ? data(stack).getIntOr(ENCHANTABILITY, 0) : 0;
    }
    public static int maxDamage(ItemStack stack, int fallback) {
        return isCombined(stack) ? Math.max(0, data(stack).getIntOr(MAX_DAMAGE, fallback)) : fallback;
    }
    public static int enchantability(ItemStack stack, int fallback) {
        return isCombined(stack) ? Math.max(0, data(stack).getIntOr(ENCHANTABILITY, fallback)) : fallback;
    }

    public static List<SetEffect> effects(ItemStack stack) {
        if (isCombined(stack)) {
            ArrayList<SetEffect> result = new ArrayList<>();
            ListTag stored = data(stack).getListOrEmpty(EFFECTS);
            for (int index = 0; index < stored.size(); index++) {
                SetEffect effect = SetEffect.getEffectFromString(stored.getStringOr(index, ""));
                if (effect != null) result.add(effect);
            }
            return result;
        }
        return stack.getItem() instanceof BlockArmorItem armor ? armor.set.setEffects : List.of();
    }
    public static boolean hasEffect(ItemStack stack, SetEffect effect) {
        return effects(stack).stream().anyMatch(candidate -> candidate.equals(effect));
    }

    public static Multimap<Attribute, AttributeModifier> attributes(ItemStack stack, EquipmentSlot slot,
            Multimap<Attribute, AttributeModifier> fallback) {
        if (!isCombined(stack)) return fallback;
        Multimap<Attribute, AttributeModifier> result = HashMultimap.create();
        ListTag serialized = data(stack).getListOrEmpty(ATTRIBUTES);
        for (int index = 0; index < serialized.size(); index++) {
            CompoundTag tag = serialized.getCompoundOrEmpty(index);
            Identifier id = Identifier.tryParse(tag.getStringOr("Attribute", ""));
            if (id == null) continue;
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getValue(id);
            if (attribute == null) continue;
            try {
				AttributeModifier.Operation[] operations = AttributeModifier.Operation.values();
				int operationIndex = tag.getIntOr("Operation", 0);
				if (operationIndex < 0 || operationIndex >= operations.length) continue;
				AttributeModifier.Operation operation = operations[operationIndex];
                double amount = tag.getDoubleOr("Amount", 0.0D);
                if (!Double.isFinite(amount)) continue;
                Identifier modifierId = Identifier.fromNamespaceAndPath("blockarmor", "combined/" + slot.getName() + "/" + id.getPath().replace('/', '_'));
                result.put(attribute, new AttributeModifier(modifierId, amount, operation));
            } catch (RuntimeException ignored) { }
        }
        return result;
    }

    /** The source used for a side; nested combinations intentionally crop to the corresponding existing side. */
    public static CompoundTag source(ItemStack stack, boolean left) {
        if (isCombined(stack)) return data(stack).getCompoundOrEmpty(left ? LEFT : RIGHT).copy();
        CompoundTag source = new CompoundTag();
        source.putString("Item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        ItemStack copy = stack.copy();
        copy.setCount(1);
        ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, copy).result().ifPresent(encoded -> source.put("Stack", encoded));
        return source;
    }

    public static CompoundTag source(ItemStack stack, boolean left, CompoundTag fallback) {
        return isCombined(stack) ? source(stack, left) : fallback;
    }

    private static Multimap<Attribute, AttributeModifier> mergeAttributes(ItemStack first, ItemStack second, EquipmentSlot slot) {
        Map<Key, Double> one = totals(first, slot);
        Map<Key, Double> two = totals(second, slot);
        Map<Key, Double> merged = new LinkedHashMap<>(one);
        for (Map.Entry<Key, Double> entry : two.entrySet()) merged.merge(entry.getKey(), entry.getValue(), Math::max);
        Multimap<Attribute, AttributeModifier> out = HashMultimap.create();
        for (Map.Entry<Key, Double> entry : merged.entrySet()) {
            Identifier modifierId = Identifier.fromNamespaceAndPath("blockarmor", "combined/" + slot.getName() + "/" + entry.getKey().id.replace(':', '_').replace('/', '_'));
            out.put(entry.getKey().attribute, new AttributeModifier(modifierId, entry.getValue(), entry.getKey().operation));
        }
        return out;
    }
    private static Map<Key, Double> totals(ItemStack stack, EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> map;
        if (isCombined(stack)) map = attributes(stack, slot, HashMultimap.create());
        else if (stack.getItem() instanceof BlockArmorItem armor) map = armor.baseAttributes(slot);
        else {
            map = HashMultimap.create();
            stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
                    .forEach(slot, (attribute, modifier) -> map.put(attribute.value(), modifier));
        }
        Map<Key, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> entry : map.entries()) {
            AttributeModifier modifier = entry.getValue();
            Key key = new Key(entry.getKey(), BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey()).toString(), modifier.operation());
            result.merge(key, modifier.amount(), Double::sum);
        }
        return result;
    }
    private static void writeAttributes(CompoundTag out, Multimap<Attribute, AttributeModifier> attributes) {
        ListTag list = new ListTag();
        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entries()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Attribute", BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey()).toString());
			tag.putInt("Operation", entry.getValue().operation().ordinal());
            tag.putDouble("Amount", entry.getValue().amount());
            list.add(tag);
        }
        out.put(ATTRIBUTES, list);
    }
    private static void writeEffects(CompoundTag out, List<SetEffect> effects) {
        ListTag list = new ListTag();
        for (SetEffect effect : effects) list.add(net.minecraft.nbt.StringTag.valueOf(effect.writeToString()));
        out.put(EFFECTS, list);
    }
    private static List<SetEffect> mergeEffects(List<SetEffect> first, List<SetEffect> second) {
        LinkedHashMap<Class<?>, SetEffect> merged = new LinkedHashMap<>();
        for (SetEffect effect : first) merged.put(effect.getClass(), effect);
        for (SetEffect effect : second) {
            SetEffect existing = merged.get(effect.getClass());
            if (existing == null || effect.mergeStrength() > existing.mergeStrength()) merged.put(effect.getClass(), effect);
        }
        return new ArrayList<>(merged.values());
    }
    private static int repairedDamage(ItemStack first, ItemStack second, int max) {
        if (max <= 0) return first.getDamageValue();
        int remaining = Math.max(0, first.getMaxDamage() - first.getDamageValue())
                + Math.max(0, second.getMaxDamage() - second.getDamageValue()) + max * 12 / 100;
        return Math.max(0, max - Math.min(max, remaining));
    }

    /** Preserve the working 1.18.1 anvil merge semantics on the component API. */
    private static void mergeEnchantments(ItemStack output, ItemStack second) {
        var merged = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(output));
        var incoming = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(second);
        for (var entry : incoming.entrySet()) {
            var enchantment = entry.getKey();
            int oldLevel = merged.getLevel(enchantment);
            boolean compatible = net.minecraft.world.item.enchantment.EnchantmentHelper
                    .isEnchantmentCompatible(merged.keySet(), enchantment);
            if (!compatible && oldLevel == 0) continue;
            int incomingLevel = entry.getIntValue();
            int level = oldLevel == incomingLevel
                    ? Math.min(incomingLevel + 1, enchantment.value().getMaxLevel())
                    : Math.max(oldLevel, incomingLevel);
            merged.set(enchantment, level);
        }
        net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(output, merged.toImmutable());
    }
    private record Key(Attribute attribute, String id, AttributeModifier.Operation operation) { }
}
