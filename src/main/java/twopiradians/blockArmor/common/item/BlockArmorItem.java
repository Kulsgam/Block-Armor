package twopiradians.blockArmor.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import twopiradians.blockArmor.common.ClientTooltipState;
import twopiradians.blockArmor.common.command.CommandDev;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Generated equippable item for one slot of an ArmorSet. */
public class BlockArmorItem extends Item {

    public final ArmorSet set;
    public final EquipmentSlot slot;
    private static final ThreadLocal<LivingEntity> DAMAGE_CONTEXT = new ThreadLocal<>();
    private BlockArmorMaterial material;
    private HashMultimap<Attribute, AttributeModifier> attributes = HashMultimap.create();

    public BlockArmorItem(BlockArmorMaterial material, EquipmentSlot slot, ArmorSet set, Identifier id) {
        super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
                .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                Equippable.builder(slot).build())
                .durability(Math.max(1, material.getDurabilityForSlot(slot)))
                .enchantable(Math.max(0, material.getEnchantmentValue())));
        this.set = set;
        this.slot = slot;
        setMaterial(material);
    }

    public EquipmentSlot getSlot() { return slot; }
    public BlockArmorMaterial getMaterial() { return material; }
    public int getDefense() { return (int) (material.getDefenseForSlot(slot) * Config.globalDamageReductionModifier); }
    public float getToughness() { return (float) (material.getToughness() * Config.globalToughnessModifier); }
    public int getEnchantmentValue() { return (int) (material.getEnchantmentValue() * Config.globalEnchantabilityModifier); }
    public int getConfiguredMaxDamage() { return Math.max(1, (int) (material.getDurabilityForSlot(slot) * Config.globalDurabilityModifier)); }
    public int getConfiguredMaxDamage(ItemStack stack) { return CombinedArmorData.maxDamage(stack, getConfiguredMaxDamage()); }

    public void beforeDamageChanged(ItemStack stack, int oldDamage, int damage) {
        beforeDamageChanged(stack, oldDamage, damage, DAMAGE_CONTEXT.get());
    }

    public void beforeDamageChanged(ItemStack stack, int oldDamage, int damage, @Nullable LivingEntity entity) {
        if (oldDamage < getConfiguredMaxDamage(stack) && damage >= getConfiguredMaxDamage(stack)
                && CombinedArmorData.hasEffect(stack, SetEffect.HOARDER)) {
            if (entity != null) SetEffect.HOARDER.onBreak(stack, entity);
            else SetEffect.HOARDER.onBreak(stack);
        }
    }

    public static void beginDamageContext(@Nullable LivingEntity entity) { DAMAGE_CONTEXT.set(entity); }

    public static void endDamageContext() { DAMAGE_CONTEXT.remove(); }

    @Nullable
    public static LivingEntity currentDamageContext() { return DAMAGE_CONTEXT.get(); }

    public Multimap<Attribute, AttributeModifier> baseAttributes(EquipmentSlot requestedSlot) {
        return requestedSlot == slot ? HashMultimap.create(attributes) : HashMultimap.create();
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot requestedSlot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = baseAttributes(requestedSlot);
        if (requestedSlot != slot) return map;
        map = CombinedArmorData.attributes(stack, requestedSlot, map);
        for (SetEffect effect : CombinedArmorData.effects(stack)) map = effect.getAttributeModifiers(map, requestedSlot, stack);
        return map;
    }

    @Override public Component getName(ItemStack stack) { return ArmorSet.getItemStackDisplayName(stack.getItem(), slot); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        if (CombinedArmorData.isDevSpawned(stack)) tooltip.accept(Component.literal("Dev Spawned").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        boolean expanded = ClientTooltipState.isShiftDown();
        List<SetEffect> effects = CombinedArmorData.effects(stack);
        if (!effects.isEmpty() && effects.getFirst().isEnabled()) {
            if (expanded) tooltip.accept(Component.translatable("item.blockarmor.tooltip.setEffects",
                    Component.translatable("item.blockarmor.tooltip.setEffectsRequire" + (Config.piecesForSet == 4 ? "" : "+"), Config.piecesForSet)
                            .withStyle(ChatFormatting.ITALIC), Config.piecesForSet).withStyle(ChatFormatting.GOLD));
            for (SetEffect effect : effects) if (effect.isEnabled()) {
                List<Component> lines = effect.addInformation(stack, expanded, ClientTooltipState.player(), new ArrayList<>(), flag);
                lines.forEach(tooltip);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot ignoredSlot) {
		if (CombinedArmorData.hasEffect(stack, SetEffect.HOARDER))
			twopiradians.blockArmor.common.seteffect.SetEffectHoarder.trackOwner(stack, entity);
        if ((!set.isEnabled() || (CombinedArmorData.isDevSpawned(stack) && entity instanceof Player player
                && !CommandDev.DEVS.contains(player.getUUID()))) && entity instanceof Player player) {
            stack.setCount(0);
            return;
        }
        // This must also run after an effect was removed from the stack, so its
        // previously injected enchantments can be restored to their old levels.
        SetEffect.reconcileEnchantments(stack, world, entity);
        for (SetEffect effect : CombinedArmorData.effects(stack)) effect.onUpdate(stack, world, entity, slot.getIndex(), false);
    }

    public boolean tickDropped(ItemStack stack, ItemEntity entity) {
		if (CombinedArmorData.hasEffect(stack, SetEffect.HOARDER))
			twopiradians.blockArmor.common.seteffect.SetEffectHoarder.trackOwner(stack, entity);
		if (!set.isEnabled() || CombinedArmorData.isDevSpawned(stack)) {
			if (CombinedArmorData.hasEffect(stack, SetEffect.HOARDER)) SetEffect.HOARDER.onBreak(stack, entity);
			entity.discard(); return true;
		}
        return false;
    }

    public void tickEquipped(ItemStack stack, Level world, Player player) {
        if ((!set.isEnabled() || (CombinedArmorData.isDevSpawned(stack) && !CommandDev.DEVS.contains(player.getUUID())))
                && player.getItemBySlot(slot) == stack) { player.setItemSlot(slot, ItemStack.EMPTY); return; }
        for (SetEffect effect : CombinedArmorData.effects(stack)) if (ArmorSet.getWornSetEffects(player).contains(effect)) effect.onArmorTick(world, player, stack);
    }

    public void setMaterial(BlockArmorMaterial material) {
        this.material = material;
        attributes = HashMultimap.create();
        Identifier id = Identifier.fromNamespaceAndPath("blockarmor", "armor/" + slot.getName());
        attributes.put(Attributes.ARMOR.value(), new AttributeModifier(id, getDefense(), AttributeModifier.Operation.ADD_VALUE));
        attributes.put(Attributes.ARMOR_TOUGHNESS.value(), new AttributeModifier(id, getToughness(), AttributeModifier.Operation.ADD_VALUE));
        if (material.getKnockbackResistance() > 0) attributes.put(Attributes.KNOCKBACK_RESISTANCE.value(),
                new AttributeModifier(id, material.getKnockbackResistance() / 10D * Config.globalKnockbackResistanceModifier,
                        AttributeModifier.Operation.ADD_VALUE));
    }

    public static boolean hasRealEnchantment(ItemStack stack) { return CombinedArmorData.hasRealEnchantments(stack); }
}
