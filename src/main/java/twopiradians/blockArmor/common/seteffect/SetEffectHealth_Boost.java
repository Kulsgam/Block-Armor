package twopiradians.blockArmor.common.seteffect;

import java.util.HashSet;

import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.utils.BlockUtils;

public class SetEffectHealth_Boost extends SetEffect {

	private static HashSet<ServerPlayer> playersToCheck = Sets.newHashSet();

	private double healthBoost;

	protected SetEffectHealth_Boost(double healthBoost) {
		super();
		this.healthBoost = healthBoost;
		this.color = ChatFormatting.RED;
		this.attributes.put(Attributes.MAX_HEALTH, new AttributeModifier(MAX_HEALTH_UUID, 
				"Max Health", healthBoost, AttributeModifier.Operation.ADDITION));
	}
	
	/**Extra objects needed for description*/
	@Override
	public Object[] getDescriptionObjects() {
		return new Object[] { (int)healthBoost/2 };
	}

	/**Only called when player wearing full, enabled set*/
	@Override
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		if (!world.isClientSide && playersToCheck.contains(player)) {
			// if max health already, restore health from this effect
			if (player.getHealth() >= 20f) {
				float amount = 0;
				for (SetEffect effect : ArmorSet.getWornSetEffects(player))
					if (effect instanceof SetEffectHealth_Boost)
						amount += ((SetEffectHealth_Boost)effect).healthBoost;
				player.setHealth(player.getHealth()+amount);
			}
			playersToCheck.remove(player);
		}
	}

	/**Restore health when player logging in with health armor*/
	public static void onLogin(ServerPlayer player) {		
		if (!player.level.isClientSide && player instanceof ServerPlayer)
			playersToCheck.add((ServerPlayer) player);
	}
	
	/**Restore health when player respawned with health armor on*/
	public static void onRespawn(ServerPlayer player) {		
		if (!player.level.isClientSide && player instanceof ServerPlayer)
			playersToCheck.add((ServerPlayer) player);
	}

	/**Can be overwritten to return a new instance depending on the given block*/
	@Override
	protected SetEffect create(Block block) {
		double healthBoost = BlockUtils.getHardness(block);
		if (healthBoost == -1 || healthBoost >= 100) //if unbreakable
			healthBoost = 100;
		healthBoost = Math.max(healthBoost*0.4d, 4);
		return new SetEffectHealth_Boost(healthBoost);
	}

	/**Write this effect to string for config (variables need to be included)*/
	@Override
	public String writeToString() {
		return this.name+" ("+this.healthBoost+")";
	}

	/**Read an effect from this string in config (takes into account variables in parenthesis)*/
	@Override
	public SetEffect readFromString(String str) throws Exception {
		return new SetEffectHealth_Boost(Double.valueOf(str.substring(str.indexOf("(")+1, str.indexOf(")"))));
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (SetEffect.registryNameContains(block, new String[] {"bedrock", "obsidian", "brick", "heal", "heart"})) {
			float hardness = BlockUtils.getHardness(block);
			if (hardness == -1 || hardness >= 2)
				return true;	
		}
		return false;
	}

}