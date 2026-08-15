package twopiradians.blockArmor.mixin;

import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCooldowns.class)
public interface ItemCooldownsAccessor {
    @Accessor("cooldowns") Map<Item, Object> blockarmor$getCooldowns();
    @Accessor("tickCount") int blockarmor$getTickCount();
}
