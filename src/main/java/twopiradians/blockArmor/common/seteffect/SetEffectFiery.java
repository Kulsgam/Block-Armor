package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectFiery extends SetEffect {

	protected SetEffectFiery() {
		super();
		this.color = ChatFormatting.RED;
	}

	/**Ignites attackers/attackees*/ 
	public static void onAttack(LivingEntity attacker, LivingEntity attacked) {		
		if (SetEffect.FIERY.isEnabled() && !attacker.level().isClientSide()) {

			//Lights the entity that attacks the wearer of the armor
			if (ArmorSet.hasSetEffect(attacked, SetEffect.FIERY) && !attacker.isInWater())	{
				if (!attacker.isOnFire() && !attacker.fireImmune())
					attacker.level().playSound(null, attacker.getX(), 
							attacker.getY(), attacker.getZ(), SoundEvents.FIRECHARGE_USE, 
							SoundSource.PLAYERS, 0.2f, 1.0f);
				attacker.setRemainingFireTicks(100);
			}
			//Lights the target of the wearer when the wearer attacks
			if (ArmorSet.hasSetEffect(attacker, SetEffect.FIERY) && !attacked.isInWater())	{
				if (!attacked.isOnFire() && !attacked.fireImmune())
					attacker.level().playSound(null, attacked.getX(), attacked.getY(), attacked.getZ(), 
							SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4f, 1.0f);
				attacked.setRemainingFireTicks(100);
			}
		}
	}
	
	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (SetEffect.registryNameContains(block, new String[] {"netherrack", "magma", "fire", "flame", "lava", "nylium"}) &&
				!SetEffect.registryNameContains(block, new String[] {"coral"}))
			return true;		
		return false;
	}
}
