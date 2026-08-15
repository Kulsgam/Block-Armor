package twopiradians.blockArmor.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityWaterStateAccessor {
    @Accessor("wasTouchingWater") void blockarmor$setWasTouchingWater(boolean value);
    @Accessor("firstTick") void blockarmor$setFirstTick(boolean value);
}
