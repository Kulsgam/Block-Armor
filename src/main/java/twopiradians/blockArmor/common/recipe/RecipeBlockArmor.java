package twopiradians.blockArmor.common.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import java.util.Optional;
import java.util.List;
import twopiradians.blockArmor.common.item.ArmorSet;

public class RecipeBlockArmor extends ShapedRecipe {

	private ArmorSet set;

	public RecipeBlockArmor(Identifier loc, ArmorSet set, String group, int width, int height,
			List<Optional<Ingredient>> ingredients, ItemStack result) {
		super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, group),
				new ShapedRecipePattern(width, height, ingredients, Optional.empty()),
				ItemStackTemplate.fromNonEmptyStack(result));
		this.set = set;
	}
	
	@Override public boolean matches(CraftingInput input, net.minecraft.world.level.Level level) {
		return set.isEnabled() && super.matches(input, level);
	}

}
