package twopiradians.blockArmor.common.item;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import twopiradians.blockArmor.common.command.CommandDev;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.creativetab.BlockArmorCreativeTab;
import twopiradians.blockArmor.common.ClientTooltipState;

public class BlockArmorItem extends ArmorItem {
	public static boolean hasRealEnchantment(ItemStack stack) {
		ListTag enchantments = stack.getEnchantmentTags();
		for (int i=0;i<enchantments.size();i++)
			if (!enchantments.getCompound(i).getBoolean(twopiradians.blockArmor.common.BlockArmor.MODID+" enchant")) return true;
		return false;
	}
	/** Copied from ArmorItem bc private */
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] { UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
			UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
			UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
			UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150") };
	/** The ArmorSet that this item belongs to */
	public ArmorSet set;
	/** Creative tab/group for this item (bc Item.group is final) */
	@Nullable
	public CreativeModeTab group;
	/** Armor material (bc ArmorItem.material is final) */
	private ArmorMaterial material;
	private HashMultimap<Attribute, AttributeModifier> attributes;

	public BlockArmorItem(BlockArmorMaterial material, EquipmentSlot slot, ArmorSet set) {
		super(material, slot, new Item.Properties().tab(set.isFromModdedBlock ? BlockArmorCreativeTab.moddedTab : BlockArmorCreativeTab.vanillaTab).durability(material.getDurabilityForSlot(slot)));
		this.set = set;
		this.group = set.isEnabled() ? (set.isFromModdedBlock ? BlockArmorCreativeTab.moddedTab : BlockArmorCreativeTab.vanillaTab) : null;
		this.setMaterial(material);
	}

	/** Don't display item in creative tab/JEI if disabled */
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (!set.isEnabled())
			return;
		else
			super.fillItemCategory(group, items);
	}

	@Override protected boolean allowdedIn(CreativeModeTab group) {
		return set.isEnabled() && group != null && (group == CreativeModeTab.TAB_SEARCH || group == this.group);
	}

	public void setGroup(CreativeModeTab group) {
		this.group = group;
	}

	public void beforeDamageChanged(ItemStack stack, int oldDamage, int damage) {
		if (oldDamage < stack.getMaxDamage() && damage >= stack.getMaxDamage()
				&& CombinedArmorData.hasEffect(stack, SetEffect.HOARDER))
			SetEffect.HOARDER.onBreak(stack);
	}

	/** Maximum damage must remain config-driven after registry construction. */
	public int getConfiguredMaxDamage() {
		return Math.max(0, (int) (material.getDurabilityForSlot(slot) * Config.globalDurabilityModifier));
	}
	public int getConfiguredMaxDamage(ItemStack stack) { return CombinedArmorData.maxDamage(stack, getConfiguredMaxDamage()); }
	public Multimap<Attribute, AttributeModifier> baseAttributes(EquipmentSlot requestedSlot) {
		return requestedSlot == this.slot ? HashMultimap.create(this.attributes) : HashMultimap.create();
	}

	/** Change display name based on the block */
	@Override
	public Component getName(ItemStack stack) {
		return ArmorSet.getItemStackDisplayName(stack.getItem(), this.getSlot());
	}

	/** Handles the attributes when wearing an armor set */
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> map = baseAttributes(slot);
		if (slot != this.slot)
			return map;

		map = CombinedArmorData.attributes(stack, slot, map);
		for (SetEffect effect : CombinedArmorData.effects(stack))
			map = effect.getAttributeModifiers(map, slot, stack);

		return map;
	}

	/** Set to have tooltip color show if item has effect */
	@Override
	public Rarity getRarity(ItemStack stack) {
		if (stack.isEnchanted())
			return Rarity.RARE;
		else if (!CombinedArmorData.effects(stack).isEmpty())
			return Rarity.UNCOMMON;
		else
			return Rarity.COMMON;
	}

	/** Deals with armor tooltips */
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("devSpawned"))
			tooltip.add(new TextComponent(ChatFormatting.DARK_PURPLE + "" + ChatFormatting.BOLD + "Dev Spawned"));

		List<SetEffect> stackEffects = CombinedArmorData.effects(stack);
		if (!stackEffects.isEmpty() && stackEffects.get(0).isEnabled()) {
			// add header if shifting
			// The client screen supplies the expanded tooltip through its normal
			// advanced-tooltip path; common item code must not reference client classes.
			boolean expanded = ClientTooltipState.isShiftDown();
			if (expanded)
				tooltip.add(new TranslatableComponent("item.blockarmor.tooltip.setEffects", 
						new TranslatableComponent("item.blockarmor.tooltip.setEffectsRequire"+(Config.piecesForSet == 4 ? "" : "+"), Config.piecesForSet)
						.withStyle(ChatFormatting.ITALIC), Config.piecesForSet)
						.withStyle(ChatFormatting.GOLD));

			// set effect names and descriptions if shifting
			for (SetEffect effect : stackEffects)
				if (effect.isEnabled())
					tooltip = effect.addInformation(stack, expanded, ClientTooltipState.player(),
							tooltip, flagIn);
		}
	}

	/** Mostly handles nbt and enchanting armor */
	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
		// delete dev spawned items if not in dev's inventory and delete disabled items
		// (except missingTexture items in SMP)
		if (stack.isEmpty() || (!set.isEnabled() && !world.isClientSide & entity instanceof Player)
				|| (!world.isClientSide && entity instanceof Player && stack.hasTag()
						&& stack.getTag().contains("devSpawned") && !CommandDev.DEVS.contains(entity.getUUID()))) {
			if (((Player) entity).getInventory().getItem(itemSlot) == stack)
				((Player) entity).getInventory().setItem(itemSlot, ItemStack.EMPTY);
			return;
		}

		if (!stack.hasTag())
			stack.setTag(new CompoundTag());

		for (SetEffect effect : CombinedArmorData.effects(stack))
			effect.onUpdate(stack, world, entity, slot.getFilterFlag(), isSelected);
	}

	/** Delete dev spawned dropped items */
	public boolean tickDropped(ItemStack stack, ItemEntity entityItem) {
		// delete dev spawned items if not worn by dev and delete disabled items (except
		// missingTexture items in SMP)
		if ((!set.isEnabled() && !entityItem.level.isClientSide)
				|| (!entityItem.level.isClientSide && entityItem != null && entityItem.getItem() != null
				&& entityItem.getItem().hasTag() && entityItem.getItem().getTag().contains("devSpawned"))) {
			entityItem.discard();
			return true;
		}
		return false;
	}

	/** Handles most of the armor set special effects and bonuses. */
	public void tickEquipped(ItemStack stack, Level world, Player player) {
		// delete dev spawned items if not worn by dev and delete disabled items (except
		// missingTexture items in SMP)
		if (stack.isEmpty() || (!set.isEnabled() && !world.isClientSide)
				|| (!world.isClientSide && stack != null && stack.hasTag() && stack.getTag().contains("devSpawned")
				&& !CommandDev.DEVS.contains(player.getUUID())
				&& player.getItemBySlot(this.slot) == stack)) {
			player.setItemSlot(this.slot, ItemStack.EMPTY);
			return;
		}

		if (!stack.hasTag())
			stack.setTag(new CompoundTag());

		for (SetEffect effect : CombinedArmorData.effects(stack))
			if (ArmorSet.getWornSetEffects(player).contains(effect))
				effect.onArmorTick(world, player, stack);
	}

	// ======================= CHANGE MATERIAL FROM CONFIG =======================

	public void setMaterial(ArmorMaterial material) {
		this.material = material;
		// recreate attributes
		this.attributes = HashMultimap.create();
		UUID uuid = ARMOR_MODIFIERS[slot.getIndex()];
		this.attributes.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier",
				(double) this.getDefense(), AttributeModifier.Operation.ADDITION));
		this.attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness",
				(double) this.getToughness(), AttributeModifier.Operation.ADDITION));
		if (this.material.getKnockbackResistance() > 0)
			this.attributes.put(Attributes.KNOCKBACK_RESISTANCE,
					new AttributeModifier(uuid, "Armor knockback resistance",
							(this.material.getKnockbackResistance()) / 10d * Config.globalKnockbackResistanceModifier,
							AttributeModifier.Operation.ADDITION));
	}

	@Override
	public ArmorMaterial getMaterial() {
		return this.material;
	}

	@Override
	public int getDefense() {
		return (int) (this.material.getDefenseForSlot(slot) * Config.globalDamageReductionModifier);
	}

	@Override
	public float getToughness() {
		return (float) (this.material.getToughness() * Config.globalToughnessModifier);
	}

	@Override
	public int getEnchantmentValue() {
		return (int) (this.material.getEnchantmentValue() * Config.globalEnchantabilityModifier);
	}

}
