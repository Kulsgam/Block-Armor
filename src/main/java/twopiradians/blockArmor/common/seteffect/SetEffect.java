package twopiradians.blockArmor.common.seteffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.item.BlockArmorItem;

public class SetEffect {
	/** Stable blacklist identity shared by all instances of an effect type. */
	public static String id(SetEffect effect) { return effect.getClass().getName(); }
	public static boolean isKnownId(String id) {
		return SET_EFFECTS.stream().anyMatch(effect -> id(effect).equals(id));
	}

	public static final Identifier ATTACK_SPEED_UUID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "set_effect/attack_speed");
	public static final Identifier ATTACK_DAMAGE_UUID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "set_effect/attack_damage");
	protected static final Identifier MOVEMENT_SPEED_UUID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "set_effect/movement_speed");
	protected static final Identifier KNOCKBACK_RESISTANCE_UUID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "set_effect/knockback_resistance");
	protected static final Identifier MAX_HEALTH_UUID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "set_effect/max_health");
	protected static final Identifier LUCK_UUID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "set_effect/luck");

	public static HashMap<String, SetEffect> nameToSetEffectMap = Maps.newHashMap();

	/**List of all set effects*/
	public static final ArrayList<SetEffect> SET_EFFECTS = Lists.newArrayList();

	//effects that use the button
	public static final SetEffectCrafter CRAFTER = new SetEffectCrafter();
	public static final SetEffectEnder_Hoarder ENDER_HOARDER = new SetEffectEnder_Hoarder();
	public static final SetEffectIlluminated ILLUMINATED = new SetEffectIlluminated(0);
	public static final SetEffectSnowy SNOWY = new SetEffectSnowy();
	public static final SetEffectEnder ENDER = new SetEffectEnder();
	public static final SetEffectAbsorbent ABSORBENT = new SetEffectAbsorbent();
	public static final SetEffectExplosive EXPLOSIVE = new SetEffectExplosive();
	public static final SetEffectTime_Control TIME_CONTROL = new SetEffectTime_Control(null);
	public static final SetEffectPusher PUSHER = new SetEffectPusher();
	public static final SetEffectPuller PULLER = new SetEffectPuller();
	public static final SetEffectArrow_Defence ARROW_DEFENCE = new SetEffectArrow_Defence();
	public static final SetEffectBonemealer BONEMEALER = new SetEffectBonemealer();
	public static final SetEffectSleepy SLEEPY = new SetEffectSleepy();
	//effects that don't use the button
	public static final SetEffectMusical MUSICAL = new SetEffectMusical();
	public static final SetEffectSlow_Motion SLOW_MOTION = new SetEffectSlow_Motion();
	public static final SetEffectSoft_Fall SOFT_FALL = new SetEffectSoft_Fall();
	public static final SetEffectFeeder FEEDER = new SetEffectFeeder();
	public static final SetEffectLightweight LIGHTWEIGHT = new SetEffectLightweight();
	public static final SetEffectInvisibility INVISIBILITY = new SetEffectInvisibility();
	public static final SetEffectImmovable IMMOVABLE = new SetEffectImmovable(0);
	public static final SetEffectLucky LUCKY = new SetEffectLucky();
	public static final SetEffectFiery FIERY = new SetEffectFiery();
	public static final SetEffectFrosty FROSTY = new SetEffectFrosty();
	public static final SetEffectRegrowth REGROWTH = new SetEffectRegrowth();
	public static final SetEffectPrickly PRICKLY = new SetEffectPrickly();
	public static final SetEffectSlimey SLIMEY = new SetEffectSlimey();
	public static final SetEffectSpeedy SPEEDY = new SetEffectSpeedy();
	public static final SetEffectFlame_Resistant FLAME_RESISTANT = new SetEffectFlame_Resistant();
	public static final SetEffectAutoSmelt AUTOSMELT = new SetEffectAutoSmelt();
	public static final SetEffectHealth_Boost HEALTH_BOOST = new SetEffectHealth_Boost(0);
	public static final SetEffectDiving_Suit DIVING_SUIT = new SetEffectDiving_Suit();
	public static final SetEffectExperience_Giving EXPERIENCE_GIVING = new SetEffectExperience_Giving();
	public static final SetEffectSlippery SLIPPERY = new SetEffectSlippery();
	public static final SetEffectFalling FALLING = new SetEffectFalling();
	public static final SetEffectPowerful POWERFUL = new SetEffectPowerful();
	public static final SetEffectRocky ROCKY = new SetEffectRocky();
	public static final SetEffectRespawn RESPAWN = new SetEffectRespawn();
	public static final SetEffectUndying UNDYING = new SetEffectUndying();
	public static final SetEffectHoarder HOARDER = new SetEffectHoarder();

	/**Does set effect require button to activate*/
	protected boolean usesButton;
	/**Color of effect for tooltip*/
	public ChatFormatting color;
	/**Potion effects that will be applied in onArmorTick*/
	protected ArrayList<MobEffectInstance> potionEffects = new ArrayList<MobEffectInstance>();
	/**Attributes that will be applied in getAttributeModifiers*/
	protected HashMap<Attribute, AttributeModifier> attributes = Maps.newHashMap();
	/**EnchantmentData that will be applied in onUpdate*/
	protected ArrayList<EnchantmentData> enchantments = new ArrayList<EnchantmentData>();
	/**Name of this effect (class name without SetEffect)*/
	public String name;

	protected SetEffect() {
		this.name = this.getClass().getSimpleName().replace("SetEffect", "").replace("_", " ");
		// if this effect is unique (not in the list already) add it to SET_EFFECTS and maps
		boolean unique = true;
		for (SetEffect effect : SET_EFFECTS)
			if (effect.getClass() == this.getClass()) {
				unique = false;
				break;
			}
		if (unique) {
			SET_EFFECTS.add(this);
			nameToSetEffectMap.put(this.name, this);
		}
	}

	/**Goes through allSets and assigns set effects to appropriate sets*/
	public static void setup() {
		for (ArmorSet set : ArmorSet.allSets) setup(set);
	}

	/**Assign the default effects for one set created while another mod is registering items. */
	public static void setup(ArmorSet set) {
		boolean hasEffectWithButton = false;
		set.setEffects = new ArrayList<SetEffect>();
		for (SetEffect effect : SetEffect.SET_EFFECTS)
			if (effect.isValid(set.block) && !(effect.usesButton && hasEffectWithButton)) {
				if (effect.usesButton) hasEffectWithButton = true;
				set.setEffects.add(effect.create(set.block));
			}
		set.defaultSetEffects = new ArrayList(set.setEffects);
	}

	/**Checks if block's registry name contains any of the provided strings (with or without capitalized first letter)*/
	public static boolean registryNameContains(Block block, String... strings) {
		// Item components are not necessarily bound while generated armor sets are
		// discovered on 26.1.  A failed display-name lookup must not suppress the
		// registry-path match that the original implementation primarily used.
		String registryName;
		try {
			registryName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
		} catch (Exception exception) {
			registryName = "";
		}
		String displayName = "";
		try {
			displayName = new ItemStack(block, 1).getHoverName().getString();
		} catch (Exception ignored) {
		}
		for (String string : strings) {
			String capitalized = string.substring(0, 1).toUpperCase() + string.substring(1);
			if (registryName.contains(string) || registryName.contains(capitalized) || displayName.contains(capitalized))
				return true;
		}

		return false;
	}

	/**Is this set effect enabled in the config
	 * Not used anymore - all set effects are enabled, but can be removed from sets*/
	public boolean isEnabled() {
		return true;
	}

	/**Can be overwritten to return a new instance depending on the given block*/
	protected SetEffect create(Block block) {
		return this;
	}

	/**Should block be given this set effect*/
	protected boolean isValid(Block block) {
		return false;
	}

	/**Should player be given potionEffect now*/
	protected boolean shouldApplyEffect(MobEffectInstance potionEffect, Level world, Player player, ItemStack stack) {
		return true;
	}

	/**Damage worn armor with this effect, if enabled in config - split damage amongst items prioritizing highest durability items*/
    protected void damageArmor(LivingEntity entity, int amount, boolean ignoreConfig) {
        if ((!ignoreConfig && !Config.effectsUseDurability) || entity == null || entity.level().isClientSide())
            return;

        // get list of all worn armor with this effect
        ArrayList<ItemStack> armor = new ArrayList<ItemStack>();
        for (EquipmentSlot slot : ArmorSet.SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
			if (stack != null && stack.getItem() instanceof BlockArmorItem &&
					twopiradians.blockArmor.common.item.CombinedArmorData.hasEffect(stack, this) &&
					twopiradians.blockArmor.common.item.CombinedArmorData.isEffectEnabled(stack, this))
                armor.add(stack);
        }

        BlockArmorItem.beginDamageContext(entity);
        try {
            for (int i=0; i<amount; ++i) {
                // find item with highest durability
                ItemStack highestDur = null;
                for (ItemStack stack : armor) {
                    if (!stack.isEmpty() && (highestDur == null ||
                            stack.getMaxDamage()-stack.getDamageValue() >
                    highestDur.getMaxDamage()-highestDur.getDamageValue()))
                        highestDur = stack;
                }
                // if item will break, play sound and spawn particles and remove from armor
                if (highestDur != null && highestDur.getMaxDamage()-highestDur.getDamageValue() == 0) {
                    armor.remove(highestDur);
                    if (highestDur.getItem() instanceof BlockArmorItem
                            && twopiradians.blockArmor.common.item.CombinedArmorData.hasEffect(highestDur, SetEffect.HOARDER))
                        SetEffect.HOARDER.onBreak(highestDur, entity);

                    // play sound - item particles crash on server (because entity.renderBrokenItemStack() doesn't work on server
                    entity.level().playSound(null, entity.blockPosition(), SoundEvents.ITEM_BREAK.value(),
                            SoundSource.PLAYERS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
                    highestDur.shrink(1);
                }
                else if (highestDur != null)
                    highestDur.hurtAndBreak(1, entity, entity.getEquipmentSlotForItem(highestDur));
            }
        } finally {
            BlockArmorItem.endDamageContext();
        }
    }

	/**Set cooldown for all worn BlockArmorItem on player for specified ticks*/
	protected void setCooldown(Player player, int ticks) {
		if (player != null) 
			for (EquipmentSlot slot : ArmorSet.SLOTS) {
				ItemStack stack = player.getItemBySlot(slot);
				if (stack != null && stack.getItem() instanceof BlockArmorItem && 
						twopiradians.blockArmor.common.item.CombinedArmorData.hasEffect(stack, this) &&
						twopiradians.blockArmor.common.item.CombinedArmorData.isEffectEnabled(stack, this))
					player.getCooldowns().addCooldown(stack, ticks);
			}
	}

	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		if (!world.isClientSide() && ArmorSet.getFirstSetItem(player, this) == stack) {			
			//apply potion effects
			for (MobEffectInstance potionEffect : this.potionEffects)
				if (this.shouldApplyEffect(potionEffect, world, player, stack))
					player.addEffect(new MobEffectInstance(potionEffect));
		}
	}

	/**Modified from EnchantmentHelper#getEnchantmentLevel to use loc instead of enchantId*/
	public static int getEnchantmentLevel(Identifier loc, ItemStack stack) {
		if (stack.isEmpty()) return 0;
		for (var entry : stack.getOrDefault(DataComponents.ENCHANTMENTS,
				net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).entrySet())
			if (entry.getKey().unwrapKey().map(key -> key.identifier().equals(loc)).orElse(false))
				return Math.max(0, entry.getIntValue());
		return 0;
	}

	public void onUpdate(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
		if (!this.enchantments.isEmpty()) reconcileEnchantments(stack, world, entity);
	}

	public static void reconcileEnchantments(ItemStack stack, Level world, Entity entity) {
		if (world.isClientSide() || !(entity instanceof LivingEntity living)
				|| !(stack.getItem() instanceof BlockArmorItem armor)) return;
		java.util.Map<ResourceKey<Enchantment>, Integer> wanted = new java.util.LinkedHashMap<>();
		if (living.getItemBySlot(armor.getSlot()) == stack) {
			java.util.Set<SetEffect> worn = ArmorSet.getWornSetEffects(living);
			for (SetEffect effect : twopiradians.blockArmor.common.item.CombinedArmorData.enabledEffects(stack)) {
				if (!effect.isEnabled() || !worn.contains(effect)) continue;
				for (EnchantmentData enchantment : effect.enchantments)
					if (enchantment.slot == armor.getSlot()) wanted.merge(enchantment.ench, (int) enchantment.level, Math::max);
			}
		}
		updateInjectedEnchantments(stack, world, wanted);
	}

	private static final String INJECTED_ENCHANTMENTS = BlockArmor.MODID + " injectedEnchantments";

	/**
	 * Applies set enchantments through the component API and records each
	 * pre-existing level so unequipping a set restores player-applied enchantments
	 * exactly rather than deleting or downgrading them.
	 */
	private static void updateInjectedEnchantments(ItemStack stack, Level world,
			java.util.Map<ResourceKey<Enchantment>, Integer> wanted) {
		var registry = world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
		CompoundTag originals = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
				.getCompound(INJECTED_ENCHANTMENTS).orElseGet(CompoundTag::new);
		var mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
				stack.getOrDefault(DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY));

		for (String id : new java.util.ArrayList<>(originals.keySet())) {
			Identifier parsed = Identifier.tryParse(id);
			if (parsed == null) continue;
			ResourceKey<Enchantment> key = ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, parsed);
			if (wanted.containsKey(key)) continue;
			var holder = registry.get(key).orElse(null);
			if (holder != null) mutable.set(holder, originals.getIntOr(id, 0));
			originals.remove(id);
		}
		for (var wantedEntry : wanted.entrySet()) {
			var holder = registry.get(wantedEntry.getKey()).orElse(null);
			if (holder == null) continue;
			String id = wantedEntry.getKey().identifier().toString();
			if (!originals.contains(id)) originals.putInt(id, mutable.getLevel(holder));
			mutable.set(holder, Math.max(originals.getIntOr(id, 0), wantedEntry.getValue()));
		}
		mutable.removeIf(holder -> mutable.getLevel(holder) <= 0);
		stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
		CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
			if (originals.isEmpty()) data.remove(INJECTED_ENCHANTMENTS);
			else data.put(INJECTED_ENCHANTMENTS, originals);
		});
	}

	/**Update stack nbt to show full set for getAttributeModifiers
	 * Sometimes doesn't update items that are removed because 
	 * the event.to, event.from, and event.slot aren't always accurate*/
	public static void onEquipmentChange(LivingEntity entity) {
		// Do not use ArmorSet's once-per-tick cache here.  Vanilla only rebuilds
		// equipment modifiers when equipment changes, while this mod gates set
		// modifiers behind an ItemStack NBT flag.  If that flag changes a tick
		// later, vanilla has nothing left to trigger a refresh.
		HashSet<SetEffect> effects = ArmorSet.calculateWornSetEffects(entity);
		for (EquipmentSlot slot : EquipmentSlot.values())
			if (slot.getType() == Type.HUMANOID_ARMOR) {
				ItemStack stack = entity.getItemBySlot(slot);
				if (stack != null && stack.getItem() instanceof BlockArmorItem) {
					java.util.List<SetEffect> active = twopiradians.blockArmor.common.item.CombinedArmorData
							.enabledEffects(stack).stream()
							.filter(effect -> effects.contains(effect)
									&& ArmorSet.getFirstSetItem(entity, effect) == stack)
							.toList();
					if (!twopiradians.blockArmor.common.item.CombinedArmorData.setActiveEffects(stack, active)) continue;
					setCustomBoolean(stack, "wearingFullSet", !active.isEmpty());
					// Re-equipping the stack makes the component-era equipment system
					// rebuild its attribute modifiers after the set-state transition.
					if (!entity.level().isClientSide()) entity.setItemSlot(slot, stack);
				}
			}
	}

	/**Handles the attributes when wearing an armor set*/
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(Multimap<Attribute, AttributeModifier> map,
			EquipmentSlot slot, ItemStack stack) {
		return map;
	}

	/**
	 * Set bonuses belong to the entity, not to an arbitrary armor slot. Keeping
	 * them here prevents vanilla from deleting a shared modifier when one of
	 * several qualifying pieces is removed.
	 */
	public static void syncAttributeModifiers(LivingEntity entity, java.util.Collection<SetEffect> activeEffects) {
		if (entity.level().isClientSide()) return;
		java.util.Map<Attribute, java.util.Map<Identifier, AttributeModifier>> wanted = new java.util.HashMap<>();
		for (SetEffect effect : activeEffects)
			for (java.util.Map.Entry<Attribute, AttributeModifier> entry : effect.attributes.entrySet())
				wanted.computeIfAbsent(entry.getKey(), ignored -> new java.util.HashMap<>())
						.put(entry.getValue().id(), entry.getValue());

		java.util.Map<Attribute, java.util.Set<Identifier>> known = new java.util.HashMap<>();
		for (SetEffect effect : SET_EFFECTS)
			for (java.util.Map.Entry<Attribute, AttributeModifier> entry : effect.attributes.entrySet())
				known.computeIfAbsent(entry.getKey(), ignored -> new java.util.HashSet<>()).add(entry.getValue().id());

		for (java.util.Map.Entry<Attribute, java.util.Set<Identifier>> entry : known.entrySet()) {
			var instance = entity.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getKey()));
			if (instance == null) continue;
			java.util.Map<Identifier, AttributeModifier> desired = wanted.getOrDefault(entry.getKey(), java.util.Map.of());
			for (Identifier id : entry.getValue()) if (!desired.containsKey(id)) instance.removeModifier(id);
			for (AttributeModifier modifier : desired.values()) instance.addOrUpdateTransientModifier(modifier);
		}
	}

	/**Set effect name and description if shifting*/
	public List<Component> addInformation(ItemStack stack, boolean isShiftDown, Player player, List<Component> tooltip, TooltipFlag flagIn) {
		MutableComponent comp = Component.literal("");
		// set effect name
		MutableComponent name = Component.translatable("setEffect."+this.name.replaceAll(" ", "_").toLowerCase()+".name");
		// bold if active
		boolean enabledOnStack = twopiradians.blockArmor.common.item.CombinedArmorData.isEffectEnabled(stack, this);
		if (enabledOnStack && player != null && (ArmorSet.getWornSetEffects(player).contains(this) &&
				player.getItemBySlot(((BlockArmorItem)stack.getItem()).getSlot()) == stack))
			name.withStyle(ChatFormatting.BOLD);
		comp.append(name);
		// add description
		if (isShiftDown) {
			MutableComponent description = this.getDescription().copy();
			// add button
			if (this.usesButton)
				description.append(ChatFormatting.BLUE+" <ACTIVATE>");
			comp.append(": ").append(description);
		}
		// strikethrough if not enabled
		if (!this.isEnabled())
			comp.withStyle(ChatFormatting.STRIKETHROUGH);
		// color
		comp.withStyle(color);
		if (!enabledOnStack) {
			comp.append(Component.translatable("item.blockarmor.tooltip.effect_disabled")
					.withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH));
		}
		tooltip.add(comp);

		return tooltip;
	}
	
	public Component getDescription() {
		return Component.translatable("setEffect."+this.name.replaceAll(" ", "_").toLowerCase()+".description", this.getDescriptionObjects());
	}

	/**Extra objects needed for description*/
	public Object[] getDescriptionObjects() {
		return new Object[0];
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**Override so instances of classes are the same as SetEffect.INSTANCE*/
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SetEffect effect && this.writeToString().equals(effect.writeToString());
	}

	/**Override so instances of classes are the same as SetEffect.INSTANCE*/
	@Override
	public int hashCode() {
		return this.writeToString().hashCode();
	}

	/**Write this effect to string for config (variables need to be included)*/
	public String writeToString() {
		return this.name;
	}

	public double mergeStrength() { return 0D; }

	/**Read an effect from this string in config (takes into account variables in parenthesis)*/
	public SetEffect readFromString(String str) throws Exception {
		return this;
	}

	/**Read an effect from this string in config*/
	@Nullable
	public static SetEffect getEffectFromString(String strIn) {
		// ignore anything in parenthesis for finding which effect this is
		String str = strIn;
		if (strIn.contains("("))
			str = strIn.substring(0, strIn.indexOf("(")).trim();
		SetEffect effect = SetEffect.nameToSetEffectMap.get(str);
		// get actual effect for this string (including variables in parenthesis)
		try {
			return effect.readFromString(strIn);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**Used to store data for enchantments easily*/
	protected static class EnchantmentData {
		public ResourceKey<Enchantment> ench;
		public Short level;
		public EquipmentSlot slot;
		public Identifier loc;

		public EnchantmentData(ResourceKey<Enchantment> ench, Short level, EquipmentSlot slot) {
			this.ench = ench;
			this.loc = ench.identifier();
			this.level = level;
			this.slot = slot;
		}
	}

	public static boolean customBoolean(ItemStack stack, String key) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr(key, false);
	}

	public static void setCustomBoolean(ItemStack stack, String key, boolean value) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putBoolean(key, value));
	}

	/**Called when full set is first equipped*/
	public void onStart(Player player) {}

	/**Called when full set is unequipped or player logged out*/
	public void onStop(Player player) {}

}
