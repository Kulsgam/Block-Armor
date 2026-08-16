package twopiradians.blockArmor.mixin;

import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Named accessor replacing Forge's obfuscation reflection for dynamic recipes. */
@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes")
    RecipeMap blockarmor$getRecipes();

    @Mutable
    @Accessor("recipes")
    void blockarmor$setRecipes(RecipeMap recipes);
}
