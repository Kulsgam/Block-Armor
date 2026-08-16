package twopiradians.blockArmor.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.equipment.Equippable;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Registers the four armor items generated for every eligible loaded block. */
public final class ModItems {
    public static final List<BlockArmorItem> allArmors = new ArrayList<>();

    private ModItems() {}

    public static void discoverGeneratedArmor() {
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(
				item -> item instanceof BlockArmorItem,
				(builder, registries, item) -> {
					BlockArmorItem armor = (BlockArmorItem) item;
					builder.addAll(defaultComponents(armor));
				}));
		RegistryEntryAddedCallback.event(BuiltInRegistries.ITEM).register((rawId, id, item) -> {
			if (item instanceof net.minecraft.world.item.BlockItem) registerGeneratedArmor(item);
		});
        ArmorSet.setup(BuiltInRegistries.ITEM.stream().toList());
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
        set.helmet = register(set, EquipmentSlot.HEAD, base + "_helmet");
        set.chestplate = register(set, EquipmentSlot.CHEST, base + "_chestplate");
        set.leggings = register(set, EquipmentSlot.LEGS, base + "_leggings");
        set.boots = register(set, EquipmentSlot.FEET, base + "_boots");
        if (set.isEnabled()) set.enable();
    }

    private static BlockArmorItem register(ArmorSet set, EquipmentSlot slot, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(BlockArmor.MODID, path);
        BlockArmorItem item = new BlockArmorItem(set.material, slot, set, id);
        allArmors.add(item);
        BlockArmorItem registered = Registry.register(BuiltInRegistries.ITEM, id, item);
        // Startup code constructs stacks before the first data-component resource
        // pass. The Fabric event above reapplies this map after every such pass.
        registered.builtInRegistryHolder().bindComponents(defaultComponents(registered));
        return registered;
    }

    public static DataComponentMap defaultComponents(BlockArmorItem armor) {
        return DataComponentMap.builder()
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(DataComponents.MAX_DAMAGE, armor.getConfiguredMaxDamage())
                .set(DataComponents.ENCHANTABLE,
                        new Enchantable(Math.max(0, armor.getEnchantmentValue())))
                // The working Fabric port owns equipped rendering completely.
                // Declaring IRON here made vanilla's iron equipment model the
                // fallback whenever our Fabric renderer was not selected.
                .set(DataComponents.EQUIPPABLE, Equippable.builder(armor.getSlot()).build())
                // All generated armor stacks use the shared runtime model. The
                // stack itself still identifies which block texture to render.
                .set(DataComponents.ITEM_MODEL,
                        Identifier.fromNamespaceAndPath(BlockArmor.MODID, "block_armor"))
                .build();
    }

}
