package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectSoft_Fall extends SetEffect {
	
	protected SetEffectSoft_Fall() {
		super();
		this.color = ChatFormatting.WHITE;
	}

	/**Prevent fall damage*/
	public static boolean preventsFallDamage(net.minecraft.world.entity.LivingEntity entity, float distance) {
		if (ArmorSet.getWornSetEffects(entity).contains(SetEffect.SOFT_FALL)) {
			if (!entity.level().isClientSide() && distance > 2)
				entity.level().playSound(null, entity.blockPosition(), 
						SoundEvents.WOOL_FALL, SoundSource.PLAYERS, 
						Math.min(distance/20f, 1), entity.level().getRandom().nextFloat()+0.8f);
			return true;
		}
		return false;
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {	
		if (SetEffect.registryNameContains(block, new String[] {"wool", "hay", "soft"}))
			return true;	
		return false;
	}	
}
