package twopiradians.blockArmor.common.seteffect;

import java.util.Map.Entry;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectExperience_Giving extends SetEffect {

	protected SetEffectExperience_Giving() {
		super();
		this.color = ChatFormatting.GREEN;
	}

	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if (ArmorSet.getFirstSetItem(player, this) == stack && !world.isClientSide() && 
				!player.getCooldowns().isOnCooldown(stack)) {
			this.setCooldown(player, 50);
			
			// give exp this way for mending
			// modified from EntityXPOrb#onCollideWithPlayer
			boolean hasMending = false;
			EnchantedItemInUse entry = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged).orElse(null);
			if (entry != null) {
				ItemStack itemstack = entry.itemStack();
				if (!itemstack.isEmpty() && itemstack.isDamaged()) {
					itemstack.setDamageValue(itemstack.getDamageValue() - 1);
					hasMending = true;
				}
			}

			if (!hasMending)
				player.giveExperiencePoints(1);

			this.damageArmor(player, 1, false);
		}
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {	
		if (SetEffect.registryNameContains(block, new String[] {"lapis", "enchant", "experience",
				"amethyst", "ruby", "peridot", "topaz", "tanzanite", "malachite", "sapphire", "amber"}))
			return true;		
		return false;
	}
}
