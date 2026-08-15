package twopiradians.blockArmor.common.seteffect;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import twopiradians.blockArmor.common.item.ArmorSet;

/** Lucky loot handling; called by the Fabric LootTable mixin. */
public class SetEffectLucky extends SetEffect {
    protected SetEffectLucky() { super(); color = ChatFormatting.DARK_GREEN; attributes.put(Attributes.LUCK, new AttributeModifier(LUCK_UUID, "Luck", 3, AttributeModifier.Operation.ADDITION)); }
    @Override protected boolean isValid(Block block) { return SetEffect.registryNameContains(block, "emerald", "luck"); }
    public static void doParticlesAndSound(Level world, BlockPos pos, LivingEntity player, float amplifier) {
        if (world instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5,
                    5, .4, .4, .4, 0);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, .05F*amplifier, world.random.nextFloat()+.9F);
        }
    }
    public static List<ItemStack> transformOreLoot(List<ItemStack> loot, LivingEntity entity, BlockState state,
            ItemStack tool, Vec3 origin, Level world) {
        if (!(state.getBlock() instanceof OreBlock) || !ArmorSet.hasSetEffect(entity, LUCKY)
                || (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0)) return loot;
        for (ItemStack stack : loot) if (stack.getItem() == state.getBlock().asItem()) return loot;
        int gained = 0;
        for (ItemStack stack : loot) { int old = stack.getCount(); stack.setCount(Math.min(old * 2, stack.getMaxStackSize())); gained += stack.getCount() - old; }
        if (gained > 0) { BlockPos pos = origin == null ? entity.blockPosition() : new BlockPos(origin); doParticlesAndSound(world, pos, entity, gained); LUCKY.damageArmor(entity, gained, false); }
        return loot;
    }
}
