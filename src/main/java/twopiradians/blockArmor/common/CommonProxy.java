package twopiradians.blockArmor.common;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.recipe.RecipeBlockArmor;
import twopiradians.blockArmor.mixin.RecipeManagerAccessor;

/** Common Fabric lifecycle operations. */
public final class CommonProxy {
    private CommonProxy() {}

    public static void setup() {
        BlockArmor.NETWORK.registerReceivers();
    }

    public static void onServerStarted(MinecraftServer server) {
        registerRecipes(server.getRecipeManager());
        BlockArmor.LOGGER.info("Block Armor server ready");
    }

    /** Add the recipes for the armor items generated from the loaded block registry. */
    private static void registerRecipes(RecipeManager recipeManager) {
        RecipeManagerAccessor accessor = (RecipeManagerAccessor) recipeManager;
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = new HashMap<>(accessor.blockarmor$getRecipes());
        Map<ResourceLocation, Recipe<?>> crafting = new HashMap<>(recipes.get(RecipeType.CRAFTING));
        crafting.entrySet().removeIf(entry -> entry.getValue() instanceof RecipeBlockArmor);
        for (ArmorSet set : ArmorSet.allSets) {
            if (!set.isEnabled()) continue;
            ItemStack block = set.getStack();
            crafting.put(id(set.helmet), recipe(id(set.helmet), set, "head", 3, 2, block, set.helmet));
            crafting.put(id(set.chestplate), recipe(id(set.chestplate), set, "chest", 3, 3, block, set.chestplate));
            crafting.put(id(set.leggings), recipe(id(set.leggings), set, "legs", 3, 3, block, set.leggings));
            crafting.put(id(set.boots), recipe(id(set.boots), set, "feet", 3, 2, block, set.boots));
        }
        recipes.put(RecipeType.CRAFTING, ImmutableMap.copyOf(crafting));
        accessor.blockarmor$setRecipes(ImmutableMap.copyOf(recipes));
    }

    public static void refreshRecipes(MinecraftServer server) { registerRecipes(server.getRecipeManager()); }

    private static ResourceLocation id(net.minecraft.world.item.Item item) {
        return net.minecraft.core.Registry.ITEM.getKey(item);
    }

    private static RecipeBlockArmor recipe(ResourceLocation id, ArmorSet set, String slot, int width, int height,
            ItemStack ingredient, net.minecraft.world.item.Item result) {
        Ingredient item = Ingredient.of(ingredient);
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, item);
        if ("head".equals(slot)) ingredients.set(width + 1, Ingredient.EMPTY);
        if ("chest".equals(slot)) ingredients.set(1, Ingredient.EMPTY);
        if ("legs".equals(slot)) {
            ingredients.set(width + 1, Ingredient.EMPTY);
            ingredients.set((height - 1) * width + 1, Ingredient.EMPTY);
        }
        if ("feet".equals(slot)) {
            ingredients.set(1, Ingredient.EMPTY);
            ingredients.set(width + 1, Ingredient.EMPTY);
        }
        return new RecipeBlockArmor(id, set, BlockArmor.MODID + "_" + slot, width, height, ingredients, new ItemStack(result));
    }

    public static void onPlayerJoin(ServerPlayer player) {
        BlockArmor.NETWORK.sendConfig(player);
        BlockArmor.NETWORK.sendCooldowns(player);
        BlockArmor.NETWORK.sendDevColors(player);
    }

    public static void setWorldTime(Level world, long time) {
        if (world instanceof ServerLevel serverLevel) serverLevel.setDayTime(time);
    }
}
