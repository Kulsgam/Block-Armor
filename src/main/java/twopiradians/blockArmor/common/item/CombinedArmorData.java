package twopiradians.blockArmor.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

    public static boolean isCombined(ItemStack stack) { return stack.hasTag() && stack.getTag().contains(KEY, Tag.TAG_COMPOUND); }
    private static CompoundTag data(ItemStack stack) { return stack.getTag().getCompound(KEY); }

    public static boolean canCombine(ItemStack first, ItemStack second) {
        return first.getItem() instanceof BlockArmorItem left && second.getItem() instanceof ArmorItem right
                && left.getSlot() == right.getSlot();
    }

    public static ItemStack combine(ItemStack first, ItemStack second, String requestedName) {
        BlockArmorItem armor = (BlockArmorItem) first.getItem();
        ItemStack result = first.copy();
        result.setCount(1);
        CompoundTag root = result.getOrCreateTag();
        root.remove("wearingFullSet");
        CompoundTag out = new CompoundTag();
        out.putInt("Version", 1);
        out.putInt(MAX_DAMAGE, Math.max(first.getMaxDamage(), second.getMaxDamage()));
        out.putInt(ENCHANTABILITY, Math.max(enchantability(first), enchantability(second)));
        writeAttributes(out, mergeAttributes(first, second, armor.getSlot()));
        writeEffects(out, mergeEffects(effects(first), effects(second)));
        out.put(LEFT, source(first, true));
        out.put(RIGHT, source(second, false));
        root.put(KEY, out);
        mergeEnchantments(result, second);
        result.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(Math.max(first.getBaseRepairCost(), second.getBaseRepairCost())));
        int repaired = repairedDamage(first, second, out.getInt(MAX_DAMAGE));
        result.setDamageValue(repaired);
        if (requestedName != null) {
            if (requestedName.isBlank()) result.resetHoverName();
            else result.setHoverName(new net.minecraft.network.chat.TextComponent(requestedName));
        }
        return result;
    }

    private static int enchantability(ItemStack stack) {
        return isCombined(stack) ? data(stack).getInt(ENCHANTABILITY) : stack.getItem().getEnchantmentValue();
    }
    public static int maxDamage(ItemStack stack, int fallback) {
        return isCombined(stack) ? Math.max(0, data(stack).getInt(MAX_DAMAGE)) : fallback;
    }
    public static int enchantability(ItemStack stack, int fallback) {
        return isCombined(stack) ? Math.max(0, data(stack).getInt(ENCHANTABILITY)) : fallback;
    }

    public static List<SetEffect> effects(ItemStack stack) {
        if (isCombined(stack)) {
            ArrayList<SetEffect> result = new ArrayList<>();
            for (Tag tag : data(stack).getList(EFFECTS, Tag.TAG_STRING)) {
                SetEffect effect = SetEffect.getEffectFromString(tag.getAsString());
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
        ListTag serialized = data(stack).getList(ATTRIBUTES, Tag.TAG_COMPOUND);
        for (int index = 0; index < serialized.size(); index++) {
            CompoundTag tag = serialized.getCompound(index);
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("Attribute"));
            if (id == null) continue;
            Attribute attribute = Registry.ATTRIBUTE.get(id);
            if (attribute == null) continue;
            try {
                AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(tag.getInt("Operation"));
                double amount = tag.getDouble("Amount");
                if (!Double.isFinite(amount)) continue;
                UUID uuid = UUID.nameUUIDFromBytes(("blockarmor:combined:" + slot.getName() + ":" + tag.getString("Attribute") + ":" + operation.toValue()).getBytes(StandardCharsets.UTF_8));
                result.put(attribute, new AttributeModifier(uuid, "Combined armor", amount, operation));
            } catch (RuntimeException ignored) { }
        }
        return result;
    }

    /** The source used for a side; nested combinations intentionally crop to the corresponding existing side. */
    public static CompoundTag source(ItemStack stack, boolean left) {
        if (isCombined(stack)) return data(stack).getCompound(left ? LEFT : RIGHT).copy();
        CompoundTag source = new CompoundTag();
        source.putString("Item", Registry.ITEM.getKey(stack.getItem()).toString());
        CompoundTag visual = stack.save(new CompoundTag());
        visual.remove(KEY);
        visual.remove("BlockArmorStoredItems");
        source.put("Stack", visual);
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
            UUID uuid = UUID.nameUUIDFromBytes(("blockarmor:combined:" + slot.getName() + ":" + entry.getKey().id + ":" + entry.getKey().operation.toValue()).getBytes(StandardCharsets.UTF_8));
            out.put(entry.getKey().attribute, new AttributeModifier(uuid, "Combined armor", entry.getValue(), entry.getKey().operation));
        }
        return out;
    }
    private static Map<Key, Double> totals(ItemStack stack, EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> map;
        if (isCombined(stack)) map = attributes(stack, slot, HashMultimap.create());
        else if (stack.getItem() instanceof BlockArmorItem armor) map = armor.baseAttributes(slot);
        else map = stack.getAttributeModifiers(slot);
        Map<Key, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> entry : map.entries()) {
            AttributeModifier modifier = entry.getValue();
            Key key = new Key(entry.getKey(), Registry.ATTRIBUTE.getKey(entry.getKey()).toString(), modifier.getOperation());
            result.merge(key, modifier.getAmount(), Double::sum);
        }
        return result;
    }
    private static void writeAttributes(CompoundTag out, Multimap<Attribute, AttributeModifier> attributes) {
        ListTag list = new ListTag();
        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entries()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Attribute", Registry.ATTRIBUTE.getKey(entry.getKey()).toString());
            tag.putInt("Operation", entry.getValue().getOperation().toValue());
            tag.putDouble("Amount", entry.getValue().getAmount());
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
    private static void mergeEnchantments(ItemStack output, ItemStack second) {
        Map<Enchantment, Integer> merged = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(output));
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(second).entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = merged.getOrDefault(enchantment, 0);
            boolean compatible = true;
            for (Enchantment existing : merged.keySet()) if (existing != enchantment && !enchantment.isCompatibleWith(existing)) { compatible = false; break; }
            if (!compatible) continue;
            int incoming = entry.getValue();
            merged.put(enchantment, level == incoming ? Math.min(incoming + 1, enchantment.getMaxLevel()) : Math.max(level, incoming));
        }
        EnchantmentHelper.setEnchantments(merged, output);
    }
    private record Key(Attribute attribute, String id, AttributeModifier.Operation operation) { }
}
