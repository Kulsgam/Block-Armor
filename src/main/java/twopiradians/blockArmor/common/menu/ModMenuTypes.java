package twopiradians.blockArmor.common.menu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import twopiradians.blockArmor.common.BlockArmor;

public final class ModMenuTypes {
    public static final MenuType<ArmorEffectTunerMenu> ARMOR_EFFECT_TUNER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(BlockArmor.MODID, "armor_effect_tuner"),
            new MenuType<>(ArmorEffectTunerMenu::new, FeatureFlagSet.of()));

    private ModMenuTypes() {}
    public static void initialize() {}
}
