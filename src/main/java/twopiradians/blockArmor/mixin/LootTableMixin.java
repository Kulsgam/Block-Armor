package twopiradians.blockArmor.mixin;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.seteffect.SetEffectAutoSmelt;
import twopiradians.blockArmor.common.seteffect.SetEffectLucky;
import twopiradians.blockArmor.common.loot.LuckyLootContext;

/** Applies block-mining loot effects after Minecraft has generated the drops. */
@Mixin(LootTable.class)
abstract class LootTableMixin {
    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Ljava/util/List;",
            at = @At("HEAD"))
    private void blockarmor$beginLootContext(LootContext context, CallbackInfoReturnable<List<ItemStack>> cir) {
        LuckyLootContext.push(context);
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private void blockarmor$modifyGeneratedLoot(LootContext context,
            CallbackInfoReturnable<List<ItemStack>> cir) {
        Object thisEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        LivingEntity entity = thisEntity instanceof LivingEntity living ? living : null;
        Object killerEntity = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        LivingEntity killer = killerEntity instanceof LivingEntity living ? living : null;

        List<ItemStack> loot = cir.getReturnValue();
        // Block loot uses THIS_ENTITY (the mining player); mob loot uses the
        // killer.  Preserve the original safeguard that never smelts player drops.
        Object state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        net.minecraft.world.phys.Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (entity != null && state instanceof net.minecraft.world.level.block.state.BlockState)
            loot = SetEffectAutoSmelt.transformLoot(loot, entity,
                    (net.minecraft.world.level.block.state.BlockState) state, tool, origin, context.getLevel());
        else if (killer != null && !(thisEntity instanceof Player))
            loot = SetEffectAutoSmelt.transformLoot(loot, killer, null, null, origin, context.getLevel());
        if (entity != null && state instanceof net.minecraft.world.level.block.state.BlockState blockState)
            loot = SetEffectLucky.transformOreLoot(loot, entity, blockState, tool, origin, context.getLevel());
        cir.setReturnValue(loot);
        LuckyLootContext.pop();
    }
}
