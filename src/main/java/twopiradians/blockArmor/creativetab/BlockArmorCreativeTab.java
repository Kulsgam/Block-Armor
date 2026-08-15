package twopiradians.blockArmor.creativetab;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.ArmorSet;

public final class BlockArmorCreativeTab {
    public static final List<ItemStack> vanillaStacks = new ArrayList<>();
    public static final List<ItemStack> moddedStacks = new ArrayList<>();
    public static final CreativeModeTab vanillaTab = build("block_armor_vanilla", false, vanillaStacks);
    public static final CreativeModeTab moddedTab = build("block_armor_modded", true, moddedStacks);
    private BlockArmorCreativeTab() {}

    private static CreativeModeTab build(String name, boolean modded, List<ItemStack> stacks) {
        return FabricItemGroupBuilder.create(new ResourceLocation(BlockArmor.MODID, name))
                .icon(() -> icon(modded, stacks)).appendItems(items -> items.addAll(stacks)).build();
    }

    private static ItemStack icon(boolean modded, List<ItemStack> stacks) {
        if (modded && !stacks.isEmpty()) return stacks.get(0);
        ArmorSet bedrock = ArmorSet.getSet(Blocks.BEDROCK);
        if (bedrock != null && bedrock.chestplate != null) return new ItemStack(bedrock.chestplate);
        return stacks.isEmpty() ? new ItemStack(Items.IRON_CHESTPLATE) : stacks.get(0);
    }
}
