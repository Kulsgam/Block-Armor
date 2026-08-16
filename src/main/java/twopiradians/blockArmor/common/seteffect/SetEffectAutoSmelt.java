package twopiradians.blockArmor.common.seteffect;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.ArmorSet;

/** AutoSmelt behaviour; LootTableMixin supplies loot context on Fabric. */
public class SetEffectAutoSmelt extends SetEffect {
    protected SetEffectAutoSmelt() { super(); color = ChatFormatting.DARK_RED; usesButton = true; }
    @Override public void onArmorTick(Level world, Player player, ItemStack stack) {
        super.onArmorTick(world, player, stack);
        if (!world.isClientSide() && ArmorSet.getFirstSetItem(player, this) == stack && BlockArmor.key.isKeyDown(player)
                && !player.getCooldowns().isOnCooldown(stack)) {
			boolean disabled = !SetEffect.customBoolean(stack, "deactivated");
			SetEffect.setCustomBoolean(stack, "deactivated", disabled);
			player.sendSystemMessage(Component.translatable(ChatFormatting.GRAY + "" + ChatFormatting.ITALIC + "AutoSmelt set effect " +
					(disabled ? ChatFormatting.RED + "disabled" : ChatFormatting.GREEN + "enabled")));
            setCooldown(player, 10);
        }
    }
    @Override protected boolean isValid(Block block) { return SetEffect.registryNameContains(block, "furnace", "fire", "flame", "smelt", "smoker", "coal") && !SetEffect.registryNameContains(block, "coral"); }
    @Nullable public static ItemStack smelt(ItemStack stack, Level world) {
        if (world.getServer() == null) return null;
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return world.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, world)
                .map(recipe -> recipe.value().assemble(input)).filter(result -> !result.isEmpty())
                .map(ItemStack::copy).orElse(null);
    }

    static void addLegalStacks(List<ItemStack> output, ItemStack stack, long count) {
        long remaining = count;
        while (remaining > 0) {
            ItemStack split = stack.copy();
            int splitCount = (int)Math.min(remaining, split.getMaxStackSize());
            split.setCount(splitCount);
            output.add(split);
            remaining -= splitCount;
        }
    }
    public static List<ItemStack> transformLoot(List<ItemStack> generated, LivingEntity entity, @Nullable BlockState state,
            @Nullable ItemStack tool, @Nullable Vec3 origin, Level world) {
        ItemStack armor = ArmorSet.getFirstSetItem(entity, AUTOSMELT);
		if (armor == null || SetEffect.customBoolean(armor, "deactivated")
				|| (tool != null && EnchantmentHelper.getItemEnchantmentLevel(
						world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), tool) > 0))
            return generated;
        List<ItemStack> result = new ArrayList<>(); boolean changed = false;
        for (ItemStack stack : generated) {
            ItemStack smelted = smelt(stack, world);
            if (smelted != null) {
                addLegalStacks(result, smelted, (long)stack.getCount() * smelted.getCount());
                changed = true;
            } else if (state == null) result.add(stack);
        }
        if (!changed && state != null) {
            ItemStack smelted = smelt(new ItemStack(state.getBlock()), world);
            if (smelted != null) {
                addLegalStacks(result, smelted, smelted.getCount());
                changed = true;
            }
        }
        if (!changed) return generated;
        if (world instanceof ServerLevel server) {
            Vec3 pos = origin == null ? entity.position() : origin;
            server.sendParticles(ParticleTypes.SMOKE, pos.x + .5, pos.y + .5, pos.z + .5, 10, .3, .3, .3, 0);
			world.playSound(null, net.minecraft.core.BlockPos.containing(pos), SoundEvents.BLAZE_SHOOT,
					SoundSource.PLAYERS, state == null ? .3F : .1F, world.getRandom().nextFloat() + .7F);
            AUTOSMELT.damageArmor(entity, 1, false);
        }
        return result;
    }
}
