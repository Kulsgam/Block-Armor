package twopiradians.blockArmor.mixin;

import java.util.UUID;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import twopiradians.blockArmor.client.HumanoidRenderStateExtension;

@Mixin(LivingEntityRenderState.class)
abstract class LivingEntityRenderStateMixin implements HumanoidRenderStateExtension {
    @Unique private UUID blockarmor$entityUuid;
    @Override public UUID blockarmor$getEntityUuid() { return blockarmor$entityUuid; }
    @Override public void blockarmor$setEntityUuid(UUID uuid) { blockarmor$entityUuid = uuid; }
}
