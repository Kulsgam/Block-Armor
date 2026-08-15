package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import twopiradians.blockArmor.mixin.EntityWaterStateAccessor;

public class SetEffectRocky extends SetEffect {

	protected SetEffectRocky() {
		super();
		this.color = ChatFormatting.GRAY;
	}

	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		Vec3 motion = player.getDeltaMovement();
		if (player.isInWater()) {
			player.setSwimming(false); // prevent swimming
			EntityWaterStateAccessor state = (EntityWaterStateAccessor) player;
			state.blockarmor$setWasTouchingWater(false);
			state.blockarmor$setFirstTick(true);
			// slow down under water a bit (if no depth strider)
			if ((Math.abs(motion.x) > 0 || Math.abs(motion.z) > 0 || motion.y > 0) && 
					EnchantmentHelper.getDepthStrider(player) == 0) {
				player.setSprinting(false); // prevent sprinting
				player.setDeltaMovement(
						motion.x()*(player.isOnGround() ? 0.14d : 1d), 
						motion.y(), 
						motion.z()*(player.isOnGround() ? 0.14d : 1d));
				if (player.isOnGround())
					player.hurtMarked = true;
				player.hasImpulse = true;
			}
		}
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (SetEffect.registryNameContains(block, new String[] {"rock", "stone", "deepslate", "tuff"}))
			return true;
		return false;
	}
}
