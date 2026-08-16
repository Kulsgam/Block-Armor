package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectSleepy extends SetEffect {

	protected SetEffectSleepy() {
		super();
		this.color = ChatFormatting.WHITE;
		this.usesButton = true;
	}

	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if (!world.isClientSide() && BlockArmor.key.isKeyDown(player) && ArmorSet.getFirstSetItem(player, this) == stack &&
				!player.getCooldowns().isOnCooldown(stack)) {
			// in nether - use explosive effect
			if (player.level().dimensionType().hasFixedTime() || !player.level().dimensionType().hasSkyLight())
				SetEffectExplosive.tryExplode(this, world, player);
			// sleep
			else if (world.getSkyDarken() > 0 && world instanceof ServerLevel) {
				ServerLevel serverLevel = (ServerLevel) world;
				if (serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME))
					serverLevel.dimensionType().defaultClock().ifPresent(clock -> {
						long current = serverLevel.clockManager().getTotalTicks(clock);
						serverLevel.clockManager().setTotalTicks(clock,
								current + (24000L - Math.floorMod(current, 24000L)));
					});
				for (ServerPlayer sleepingPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
					if (sleepingPlayer.level() == serverLevel && sleepingPlayer.isSleeping())
						sleepingPlayer.stopSleepInBed(false, false);
				}
				if (world.isRaining()) serverLevel.resetWeatherCycle();

				world.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.5F, 1.4F);
				this.setCooldown(player, 100);
			}
			// not night time
			else if (player instanceof ServerPlayer) {
				world.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, world.getRandom().nextFloat() + 0.5F);
				this.setCooldown(player, 10);
			}
		}
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (SetEffect.registryNameContains(block, new String[] {"bed", "sleep", "hammock"}) &&
				!SetEffect.registryNameContains(block, new String[] {"bedrock"}))
			return true;		
		return false;
	}
}
