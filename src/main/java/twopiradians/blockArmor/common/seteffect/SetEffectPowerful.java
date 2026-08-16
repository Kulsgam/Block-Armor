package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;

public class SetEffectPowerful extends SetEffect {

	protected SetEffectPowerful() {
		super();
		this.color = ChatFormatting.WHITE;
		this.attributes.put(Attributes.ATTACK_SPEED.value(), new AttributeModifier(ATTACK_SPEED_UUID, 1d, AttributeModifier.Operation.ADD_VALUE));
		this.attributes.put(Attributes.ATTACK_DAMAGE.value(), new AttributeModifier(ATTACK_DAMAGE_UUID, 3d, AttributeModifier.Operation.ADD_VALUE));
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (SetEffect.registryNameContains(block, new String[] {"quartz", "strong", "power"}))
			return true;		
		return false;
	}
}