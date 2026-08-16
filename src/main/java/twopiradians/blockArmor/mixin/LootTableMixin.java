package twopiradians.blockArmor.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twopiradians.blockArmor.common.seteffect.SetEffectAutoSmelt;
import twopiradians.blockArmor.common.seteffect.SetEffectLucky;

/** Applies block-mining loot effects after Minecraft has generated the drops. */
@Mixin(LootTable.class)
abstract class LootTableMixin {
    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"))
    private void blockarmor$pushLuckyContext(LootContext context, java.util.function.Consumer<ItemStack> consumer, CallbackInfo ci) {
        twopiradians.blockArmor.common.loot.LuckyLootContext.push(context);
    }

    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("RETURN"))
    private void blockarmor$popLuckyContext(LootContext context, java.util.function.Consumer<ItemStack> consumer, CallbackInfo ci) {
        twopiradians.blockArmor.common.loot.LuckyLootContext.pop();
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"), cancellable = true)
    private void blockarmor$modifyGeneratedLoot(LootParams params, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        var context = params.contextMap();
        LivingEntity entity = context.getOptional(LootContextParams.THIS_ENTITY) instanceof LivingEntity living ? living : null;
        LivingEntity killer = context.getOptional(LootContextParams.ATTACKING_ENTITY) instanceof LivingEntity living ? living : null;
        Object state = context.getOptional(LootContextParams.BLOCK_STATE);
        net.minecraft.world.item.ItemInstance toolInstance = context.getOptional(LootContextParams.TOOL);
        ItemStack tool = toolInstance instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        net.minecraft.world.phys.Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
        List<ItemStack> loot = cir.getReturnValue();
        if (entity != null && state instanceof net.minecraft.world.level.block.state.BlockState blockState) {
            loot = SetEffectAutoSmelt.transformLoot(loot, entity, blockState, tool, origin, params.getLevel());
            loot = SetEffectLucky.transformOreLoot(loot, entity, blockState, tool, origin, params.getLevel());
        } else if (killer != null && !(entity instanceof Player)) {
            loot = SetEffectAutoSmelt.transformLoot(loot, killer, null, null, origin, params.getLevel());
        }
        cir.setReturnValue(new ObjectArrayList<>(loot));
    }
}
