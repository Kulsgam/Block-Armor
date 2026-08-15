package twopiradians.blockArmor.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Registers the four armor items generated for every eligible loaded block. */
public final class ModItems {
    public static final List<BlockArmorItem> allArmors = new ArrayList<>();

    private ModItems() {}

    public static void discoverGeneratedArmor() {
		RegistryEntryAddedCallback.event(Registry.ITEM).register((rawId, id, item) -> {
			if (item instanceof net.minecraft.world.item.BlockItem) registerGeneratedArmor(item);
		});
        ArmorSet.setup(Registry.ITEM.stream().toList());
    }

    public static void registerDiscoveredArmor() {
        for (ArmorSet set : ArmorSet.allSets) register(set);
        BlockArmor.LOGGER.info("Registered {} generated Block Armor items", allArmors.size());
    }

    private static void registerGeneratedArmor(Item item) {
        ArmorSet.setup(List.of(item));
        ArmorSet set = ArmorSet.getSet(item);
        if (set == null || set.helmet != null) return;
        SetEffect.setup(set);
        Config.applyToLateSet(set);
        register(set);
    }

    private static void register(ArmorSet set) {
        if (set.helmet != null) return;
        String base = ArmorSet.getItemRegistryName(set.item);
        set.helmet = register(new BlockArmorItem(set.material, EquipmentSlot.HEAD, set), base + "_helmet");
        set.chestplate = register(new BlockArmorItem(set.material, EquipmentSlot.CHEST, set), base + "_chestplate");
        set.leggings = register(new BlockArmorItem(set.material, EquipmentSlot.LEGS, set), base + "_leggings");
        set.boots = register(new BlockArmorItem(set.material, EquipmentSlot.FEET, set), base + "_boots");
        if (set.isEnabled()) set.enable();
    }

    private static BlockArmorItem register(BlockArmorItem item, String path) {
        allArmors.add(item);
        return Registry.register(Registry.ITEM, new ResourceLocation(BlockArmor.MODID, path), item);
    }
}
