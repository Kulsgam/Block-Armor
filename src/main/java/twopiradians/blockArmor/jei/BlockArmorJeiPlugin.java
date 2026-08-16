package twopiradians.blockArmor.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.ModItems;

/** Exposes runtime-generated armor to JEI and keeps disabled sets out of its ingredient list. */
@JeiPlugin
public final class BlockArmorJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(BlockArmor.MODID, "jei");

    @Override public Identifier getPluginUid() { return UID; }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(enabledStacks());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        var manager = runtime.getIngredientManager();
        List<ItemStack> disabled = ModItems.allArmors.stream().filter(item -> !item.set.isEnabled())
                .map(ItemStack::new).toList();
        if (!disabled.isEmpty()) manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, disabled);
        BlockArmor.LOGGER.info("JEI integration registered {} enabled Block Armor items and hid {} disabled items",
                enabledStacks().size(), disabled.size());
    }

    private static List<ItemStack> enabledStacks() {
        return ModItems.allArmors.stream().filter(item -> item.set.isEnabled()).map(ItemStack::new).toList();
    }
}
