package twopiradians.blockArmor.common.seteffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
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
        if (!world.isClientSide && ArmorSet.getFirstSetItem(player, this) == stack && BlockArmor.key.isKeyDown(player)
                && !player.getCooldowns().isOnCooldown(stack.getItem())) {
            boolean disabled = !stack.getOrCreateTag().getBoolean("deactivated");
            stack.getTag().putBoolean("deactivated", disabled);
            player.sendMessage(new TranslatableComponent(ChatFormatting.GRAY + "" + ChatFormatting.ITALIC + "AutoSmelt set effect " +
                    (disabled ? ChatFormatting.RED + "disabled" : ChatFormatting.GREEN + "enabled")), UUID.randomUUID());
            setCooldown(player, 10);
        }
    }
    @Override protected boolean isValid(Block block) { return SetEffect.registryNameContains(block, "furnace", "fire", "flame", "smelt", "smoker", "coal") && !SetEffect.registryNameContains(block, "coral"); }
    @Nullable public static ItemStack smelt(ItemStack stack, Level world) {
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), world)
                .map(SmeltingRecipe::getResultItem).filter(result -> !result.isEmpty())
                .map(result -> { ItemStack copy = result.copy(); copy.setCount(stack.getCount() * result.getCount()); return copy; }).orElse(null);
    }
    public static List<ItemStack> transformLoot(List<ItemStack> generated, LivingEntity entity, @Nullable BlockState state,
            @Nullable ItemStack tool, @Nullable Vec3 origin, Level world) {
        ItemStack armor = ArmorSet.getFirstSetItem(entity, AUTOSMELT);
        if (armor == null || armor.getOrCreateTag().getBoolean("deactivated")
                || (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0))
            return generated;
        List<ItemStack> result = new ArrayList<>(); boolean changed = false;
        for (ItemStack stack : generated) { ItemStack smelted = smelt(stack, world); result.add(smelted == null ? stack : smelted); changed |= smelted != null; }
        if (!changed && state != null) {
            ItemStack smelted = smelt(new ItemStack(state.getBlock()), world);
            if (smelted != null) { result.add(smelted); changed = true; }
        }
        if (changed && world instanceof ServerLevel server) {
            Vec3 pos = origin == null ? entity.position() : origin;
            server.sendParticles(ParticleTypes.SMOKE, pos.x + .5, pos.y + .5, pos.z + .5, 10, .3, .3, .3, 0);
            world.playSound(null, new net.minecraft.core.BlockPos(pos), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, .1F, world.random.nextFloat() + .7F);
            AUTOSMELT.damageArmor(entity, 1, false);
        }
        return result;
    }
}
