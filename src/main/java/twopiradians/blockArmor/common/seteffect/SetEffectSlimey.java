package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectSlimey extends SetEffect {

	/**
	 * Static is fine bc this is only used on client - chances of two players
	 * bouncing same tick is very slim
	 */
	private static LivingEntity bouncingEntity;
	private static double motionY;

	protected SetEffectSlimey() {
		super();
		this.color = ChatFormatting.GREEN;
	}

	/** Only called when player wearing full, enabled set */
	@Override
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if (ArmorSet.getFirstSetItem(player, this) == stack && world.isClientSide() && !player.isShiftKeyDown()) {	
			//increased movement speed while bouncing
			if (!player.onGround() && !player.isFallFlying()) 
				player.setDeltaMovement(player.getDeltaMovement().x*1.07d, player.getDeltaMovement().y, player.getDeltaMovement().z*1.07d);
		
			if (!player.getCooldowns().isOnCooldown(stack) && player.horizontalCollision 
					&& Math.sqrt(Math.pow(player.getX() - player.xOld, 2) + 
							Math.pow(player.getZ() - player.zOld, 2)) >= 1.1D) {	
				this.setCooldown(player, 10);
				double multiplier = 0.1d;
				if (player.getDeltaMovement().x == 0) 
					player.setDeltaMovement(
							-(player.getX() - player.xOld)*multiplier, 
							player.getDeltaMovement().y+0.1d, 
							(player.getZ() - player.zOld)*multiplier);
				else if (player.getDeltaMovement().z == 0) 
					player.setDeltaMovement(
							(player.getX() - player.xOld)*multiplier, 
							player.getDeltaMovement().y+0.1d, 
							-(player.getZ() - player.zOld)*multiplier);
				world.playSound(player, player.getX(), player.getY(), player.getZ(), 
						SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 0.4F, 1.0F);
			}
		}
	}

	public static boolean preventsFallDamage(Player player, float distance) {
		if (!ArmorSet.hasSetEffect(player, SetEffect.SLIMEY) || player.isShiftKeyDown()) return false;
		if (player.level().isClientSide() && distance > 2D) {
			double multiplier = distance > 100 ? 2D : distance > 40 ? 1.5D : 1D;
			player.setDeltaMovement(player.getDeltaMovement().x,
					Math.abs(player.getDeltaMovement().y * .9D * multiplier), player.getDeltaMovement().z);
			player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
					distance > 40 ? SoundEvents.SLIME_JUMP : SoundEvents.SLIME_SQUISH,
					SoundSource.PLAYERS, .4F, 1F);
			player.setOnGround(false);
			player.hurtMarked = true;
			bouncingEntity = player;
			motionY = player.getDeltaMovement().y;
		}
		return true;
	}

	public static void finishBounce(Player player) {
		if (bouncingEntity == player && player != null && player.level().isClientSide()) {
			player.setDeltaMovement(player.getDeltaMovement().x, motionY, player.getDeltaMovement().z);
			player.fallDistance = 0;
			bouncingEntity = null;
		}
	}

	/** Should block be given this set effect */
	@Override
	protected boolean isValid(Block block) {
		if (SetEffect.registryNameContains(block, new String[] { "slime" }))
			return true;
		return false;
	}
}
