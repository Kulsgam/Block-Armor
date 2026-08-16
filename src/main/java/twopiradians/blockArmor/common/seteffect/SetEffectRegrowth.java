package twopiradians.blockArmor.common.seteffect;


import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.NetherrackBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.tags.BlockTags;

public class SetEffectRegrowth extends SetEffect {

	protected SetEffectRegrowth() {
		super();
		this.color = ChatFormatting.DARK_GREEN;
	}
	
	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		super.onArmorTick(world, player, stack);
		
		if (!world.isClientSide() && world.getRandom().nextInt(200) == 0 && stack.isDamaged())
			stack.setDamageValue(stack.getDamageValue()-1);
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {	
		// ignore netherrack (would be accepted otherwise bc it's IGrowable)
		if (block instanceof NetherrackBlock)
			return false;
		
		if (block instanceof BonemealableBlock || 
				SetEffect.registryNameContains(block, new String[] {"moss", "plant", "mycelium", "mushroom", "flower",
						"log", "wood", "stem", "plank", "grass", "nether_wart"}))
			return true;	
		
		// VegetationBlock/GrowingPlantBlock are the 26.1 equivalents of the old
		// IPlantable and plant-material branches. Tags preserve the old material
		// coverage for leaves, saplings, crops, flowers, logs, planks and bamboo,
		// including modded blocks that participate in vanilla conventions.
		if (block instanceof VegetationBlock || block instanceof GrowingPlantBlock)
			return true;
		// setup() runs before datapack tags are bound. Tag membership is useful
		// for late-created/modded sets, but must remain optional during initial
		// registration just as the working port's material checks were.
		try {
			var state = block.defaultBlockState();
			if (state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS)
					|| state.is(BlockTags.CROPS) || state.is(BlockTags.FLOWERS)
					|| state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)
					|| state.is(BlockTags.BAMBOO_BLOCKS) || state.is(BlockTags.WART_BLOCKS))
				return true;
		} catch (IllegalStateException tagsNotBound) {
			// Class and registry/display-name checks above are the safe early path.
		}
		
		return false;
	}
}
