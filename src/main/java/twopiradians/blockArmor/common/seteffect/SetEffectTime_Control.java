package twopiradians.blockArmor.common.seteffect;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.CommonProxy;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectTime_Control extends SetEffect {

	private Type type;

	protected SetEffectTime_Control(Type type) {
		super();
		this.type = type;
		this.color = ChatFormatting.LIGHT_PURPLE;
		this.usesButton = true;
	}
	
	@Override
	public Component getDescription() {
		return Component.translatable("setEffect."+this.name.replaceAll(" ", "_").toLowerCase()+"."+type.name.toLowerCase()+".description", this.getDescriptionObjects());
	}

	/**Write this effect to string for config (variables need to be included)*/
	@Override
	public String writeToString() {
		return this.name+" ("+this.type.name+")";
	}

	/**Read an effect from this string in config (takes into account variables in parenthesis)*/
	@Override
	public SetEffect readFromString(String str) throws Exception {
		return new SetEffectTime_Control(Type.getType(str.substring(str.indexOf("(")+1, str.indexOf(")"))));
	}


	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(Level world, Player player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if (ArmorSet.getFirstSetItem(player, this) != stack || !BlockArmor.key.isKeyDown(player)) return;
		if (world.dimensionType().defaultClock().isEmpty()) return;
		if (world.isClientSide()) return;
		net.minecraft.server.level.ServerLevel server = (net.minecraft.server.level.ServerLevel) world;
		boolean active = type != Type.STOP || server.getGameRules().get(GameRules.ADVANCE_TIME);
		if (!active) return;
		server.dimensionType().defaultClock().ifPresent(clock -> {
				long current = server.clockManager().getTotalTicks(clock);
				if (type == Type.REWIND) setWorldTime(server, clock, rewind(current));
				else if (type == Type.STOP)
					setWorldTime(server, clock, Math.max(0L, current - 1L));
				else if (type == Type.ACCELERATE) setWorldTime(server, clock, current + 19L);
			});
		if (player.tickCount % (type == Type.ACCELERATE ? 2 : type == Type.REWIND ? 4 : 8) == 0)
			world.playSound(null, player.blockPosition(), type == Type.STOP ? SoundEvents.NOTE_BLOCK_SNARE.value() : SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS,
					type == Type.STOP ? 0.2F : 0.3F, type == Type.ACCELERATE ? 2.0F : 0.0F);
	}

	static long rewind(long current) {
		long changed = current - 21L;
		return changed >= 0 ? changed : Math.floorMod(changed, 24000L);
	}

	private void setWorldTime(net.minecraft.server.level.ServerLevel world,
			net.minecraft.core.Holder<net.minecraft.world.clock.WorldClock> clock, long time) {
		world.clockManager().setTotalTicks(clock, time);
	}

	/**Can be overwritten to return a new instance depending on the given block*/
	@Override
	protected SetEffect create(Block block) {
		return new SetEffectTime_Control(Type.getType(block));
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block) {		
		if (block == Blocks.REPEATING_COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK ||
				block == Blocks.COMMAND_BLOCK)
			return true;
		return false;
	}

	private enum Type {
		REWIND(Blocks.REPEATING_COMMAND_BLOCK, "Rewind"), 
		STOP(Blocks.CHAIN_COMMAND_BLOCK, "Stop"), 
		ACCELERATE(Blocks.COMMAND_BLOCK, "Accelerate");

		public Block block;
		public String name;

		private Type(Block block, String name) {
			this.block = block;
			this.name = name;
		}

		public static Type getType(Block block) {
			for (Type type : Type.values())
				if (type.block == block)
					return type;
			return Type.ACCELERATE;
		}

		public static Type getType(String str) {
			for (Type type : Type.values())
				if (type.name.equalsIgnoreCase(str))
					return type;
			return Type.ACCELERATE;
		}
	}

}
