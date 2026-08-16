package twopiradians.blockArmor.creativetab;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import twopiradians.blockArmor.common.item.ArmorSet;

/** Content providers for the two generated tabs; registration occurs after item discovery. */
public final class BlockArmorCreativeTab {
    public static final List<ItemStack> vanillaStacks = new ArrayList<>();
    public static final List<ItemStack> moddedStacks = new ArrayList<>();
    public static final CreativeModeTab vanillaTab = tab("itemGroup.blockarmor.vanilla", vanillaStacks);
    public static final CreativeModeTab moddedTab = tab("itemGroup.blockarmor.modded", moddedStacks);
    private BlockArmorCreativeTab() { }

    private static CreativeModeTab tab(String title, List<ItemStack> entries) {
        return FabricCreativeModeTab.builder()
                .title(Component.translatable(title))
                .icon(() -> entries.isEmpty() ? new ItemStack(Items.IRON_CHESTPLATE) : entries.getFirst().copy())
                .displayItems((parameters, output) -> entries.forEach(output::accept))
                .build();
    }

    private static boolean initialized;

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        // Search is built from registered CATEGORY tabs. Adding directly to the
        // SEARCH event is too late for its text index, so register the same two
        // generated tabs used by the working 1.18.1 Fabric port.
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath("blockarmor", "block_armor_vanilla"), vanillaTab);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath("blockarmor", "block_armor_modded"), moddedTab);
    }
}
