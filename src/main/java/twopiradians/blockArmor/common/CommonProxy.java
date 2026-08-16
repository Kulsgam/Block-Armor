package twopiradians.blockArmor.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.registries.Registries;
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
        RecipeMap recipes = accessor.blockarmor$getRecipes();
        List<RecipeHolder<?>> holders = new ArrayList<>(recipes.values());
        holders.removeIf(holder -> holder.value() instanceof RecipeBlockArmor);
        for (ArmorSet set : ArmorSet.allSets) {
            if (!set.isEnabled()) continue;
            ItemStack block = set.getStack();
            holders.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id(set.helmet)),
                    recipe(id(set.helmet), set, "head", 3, 2, block, set.helmet)));
            holders.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id(set.chestplate)),
                    recipe(id(set.chestplate), set, "chest", 3, 3, block, set.chestplate)));
            holders.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id(set.leggings)),
                    recipe(id(set.leggings), set, "legs", 3, 3, block, set.leggings)));
            holders.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id(set.boots)),
                    recipe(id(set.boots), set, "feet", 3, 2, block, set.boots)));
        }
        accessor.blockarmor$setRecipes(RecipeMap.create(holders));
    }

    public static void refreshRecipes(MinecraftServer server) { registerRecipes(server.getRecipeManager()); }

    /** Rebuild every derived value after either a GUI edit or a disk reload. */
    public static void refreshAfterConfigChange(MinecraftServer server) {
        for (ArmorSet set : ArmorSet.allSets) set.createMaterial();
        ArmorSet.refreshConfiguredSetEffects(server);
        refreshRecipes(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) BlockArmor.NETWORK.sendConfig(player);
    }

    private static Identifier id(net.minecraft.world.item.Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
    }

    private static RecipeBlockArmor recipe(Identifier id, ArmorSet set, String slot, int width, int height,
            ItemStack ingredient, net.minecraft.world.item.Item result) {
        Ingredient item = Ingredient.of(ingredient.getItem());
        List<Optional<Ingredient>> ingredients = new ArrayList<>(
                java.util.Collections.nCopies(width * height, Optional.of(item)));
        if ("head".equals(slot)) ingredients.set(width + 1, Optional.empty());
        if ("chest".equals(slot)) ingredients.set(1, Optional.empty());
        if ("legs".equals(slot)) {
            ingredients.set(width + 1, Optional.empty());
            ingredients.set((height - 1) * width + 1, Optional.empty());
        }
        if ("feet".equals(slot)) {
            ingredients.set(1, Optional.empty());
            ingredients.set(width + 1, Optional.empty());
        }
        return new RecipeBlockArmor(id, set, BlockArmor.MODID + "_" + slot, width, height, ingredients, new ItemStack(result));
    }

    public static void onPlayerJoin(ServerPlayer player) {
        BlockArmor.NETWORK.sendConfig(player);
        BlockArmor.NETWORK.sendCooldowns(player);
        BlockArmor.NETWORK.sendDevColors(player);
    }

}
