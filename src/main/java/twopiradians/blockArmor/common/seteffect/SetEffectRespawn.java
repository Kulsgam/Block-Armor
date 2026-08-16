package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.block.Block;

import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectRespawn extends SetEffect {

	protected SetEffectRespawn() {
		super();
		this.color = ChatFormatting.DARK_PURPLE;
	}

	/**Teleport player instead of them dying*/
	public static boolean onDeath(ServerPlayer player) {
		try {
			if (player != null) {
				if (!player.level().isClientSide() && 
						ArmorSet.hasSetEffect(player, SetEffect.RESPAWN) && 
						!player.getCooldowns().isOnCooldown(ArmorSet.getFirstSetItem(player, SetEffect.RESPAWN))) {
					// set health to 1 and clear effects
					player.setHealth(1);
					player.removeAllEffects();
					player.level().broadcastEntityEvent(player, (byte)3);
					player.clearFire();
					player.getCombatTracker().recheckStatus();
					// set player's position and dimension to spawn point
					TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
					ServerLevel respawnWorld = transition.newLevel();
					player.teleport(transition);
					// cooldown, damage, sound
					SetEffect.RESPAWN.setCooldown(player, 6000);
					SetEffect.RESPAWN.damageArmor(player, 100, true);
					respawnWorld.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
					// cancel event so player doesn't die
					return true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (SetEffect.registryNameContains(block, new String[] {"respawn"}))
			return true;
		return false;
	}

}
