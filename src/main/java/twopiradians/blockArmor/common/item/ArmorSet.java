package twopiradians.blockArmor.common.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;


import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import twopiradians.blockArmor.creativetab.BlockArmorCreativeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.command.CommandDev;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.utils.BlockUtils;

@SuppressWarnings({ "deprecation" })
public class ArmorSet {

	/**Used to add ItemStacks that will be approved for sets that would otherwise not be valid*/
	private static final ArrayList<Block> MANUALLY_ADDED_SETS;
	static {
		MANUALLY_ADDED_SETS = new ArrayList<Block>() {{
			add(Blocks.SUGAR_CANE);
			add(Blocks.CACTUS);
			add(Blocks.DISPENSER);
			add(Blocks.DROPPER);
			add(Blocks.BEACON);
			add(Blocks.FURNACE);
			add(Blocks.ENCHANTING_TABLE);
			add(Blocks.COMMAND_BLOCK);
			add(Blocks.CHAIN_COMMAND_BLOCK);
			add(Blocks.REPEATING_COMMAND_BLOCK);
			add(Blocks.BROWN_MUSHROOM_BLOCK);
			add(Blocks.RED_MUSHROOM_BLOCK);
			add(Blocks.SOUL_SAND);
			add(Blocks.ENDER_CHEST);
			add(Blocks.BLAST_FURNACE);
			add(Blocks.COMPOSTER);
			add(Blocks.SMOKER);
			add(Blocks.BEE_NEST);
			add(Blocks.BEEHIVE);
			add(Blocks.BARREL);
			add(Blocks.HONEY_BLOCK);
			add(Blocks.CHEST);
			add(Blocks.NOTE_BLOCK);
			add(Blocks.JUKEBOX);
		}};
	}
	/**Armor slots*/
	public static final EquipmentSlot[] SLOTS = new EquipmentSlot[] 
			{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	/**Used to add Items that have overriding textures*/
	public static final HashMap<Block, TextureOverrideInfo> TEXTURE_OVERRIDES;
	static {
		TEXTURE_OVERRIDES = new HashMap<Block, TextureOverrideInfo>() {{
			TextureOverrideInfo info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.CHEST, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/enchanting_table_chest"));
			info.addSlot(EquipmentSlot.LEGS, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/enchanting_table_legs"));
			put(Blocks.ENCHANTING_TABLE, info);
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/cactus_head"));
			info.addSlot(EquipmentSlot.CHEST, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/cactus_chest"));
			info.addSlot(EquipmentSlot.LEGS, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/cactus_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/cactus_feet"));
			put(Blocks.CACTUS, info);
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/sugar_cane_head"));
			info.addSlot(EquipmentSlot.CHEST, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/sugar_cane_chest"));
			info.addSlot(EquipmentSlot.LEGS, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/sugar_cane_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/sugar_cane_feet"));
			put(Blocks.SUGAR_CANE, info);
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/ender_chest_head"));
			info.addSlot(EquipmentSlot.CHEST, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/ender_chest_chest"));
			info.addSlot(EquipmentSlot.LEGS, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/ender_chest_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/ender_chest_feet"));
			put(Blocks.ENDER_CHEST, info);
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/chest_head"));
			info.addSlot(EquipmentSlot.CHEST, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/chest_chest"));
			info.addSlot(EquipmentSlot.LEGS, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/chest_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/chest_feet"));
			put(Blocks.CHEST, info);
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/composter_head"));
			put(Blocks.COMPOSTER, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/shulker_box_feet"));
			put(Blocks.SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.WHITE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.WHITE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.WHITE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.WHITE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.WHITE_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.ORANGE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.ORANGE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.ORANGE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.ORANGE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.ORANGE_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.MAGENTA_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.MAGENTA_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.MAGENTA_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.MAGENTA_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.MAGENTA_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.LIGHT_BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.LIGHT_BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.LIGHT_BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.LIGHT_BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.LIGHT_BLUE_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.YELLOW_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.YELLOW_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.YELLOW_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.YELLOW_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.YELLOW_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.LIME_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.LIME_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.LIME_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.LIME_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.LIME_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.PINK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.PINK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.PINK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.PINK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.PINK_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.GRAY_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.LIGHT_GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.LIGHT_GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.LIGHT_GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.LIGHT_GRAY_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.LIGHT_GRAY_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.CYAN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.CYAN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.CYAN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.CYAN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.CYAN_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.PURPLE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.PURPLE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.PURPLE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.PURPLE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.PURPLE_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.BLUE_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.BLUE_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.BROWN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.BROWN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.BROWN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.BROWN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.BROWN_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.GREEN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.GREEN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.GREEN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.GREEN_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.GREEN_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.RED_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.RED_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.RED_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.RED_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.RED_SHULKER_BOX, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, ((ShulkerBoxBlock)(Blocks.BLACK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_head"));
			info.addSlot(EquipmentSlot.CHEST, ((ShulkerBoxBlock)(Blocks.BLACK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_chest"));
			info.addSlot(EquipmentSlot.LEGS, ((ShulkerBoxBlock)(Blocks.BLACK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_legs"));
			info.addSlot(EquipmentSlot.FEET, ((ShulkerBoxBlock)(Blocks.BLACK_SHULKER_BOX)).getColor().getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_shulker_box_feet"));
			put(Blocks.BLACK_SHULKER_BOX, info);
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.WHITE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.WHITE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.WHITE_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.ORANGE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.ORANGE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.ORANGE_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.MAGENTA.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.MAGENTA.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.MAGENTA_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.LIGHT_BLUE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.LIGHT_BLUE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.LIGHT_BLUE_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.YELLOW.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.YELLOW.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.YELLOW_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.LIME.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.LIME.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.LIME_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.PINK.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.PINK.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.PINK_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.GRAY.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.GRAY.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.GRAY_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.LIGHT_GRAY.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.LIGHT_GRAY.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.LIGHT_GRAY_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.CYAN.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.CYAN.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.CYAN_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.PURPLE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.PURPLE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.PURPLE_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.BLUE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.BLUE.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.BLUE_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.BROWN.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.BROWN.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.BROWN_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.GREEN.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.GREEN.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.GREEN_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.RED.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.RED.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.RED_BED, info); 
			info = new TextureOverrideInfo();
			info.addSlot(EquipmentSlot.HEAD, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_head"));
			info.addSlot(EquipmentSlot.CHEST, DyeColor.BLACK.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_chest"));
			info.addSlot(EquipmentSlot.LEGS, DyeColor.BLACK.getFireworkColor(), Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_legs"));
			info.addSlot(EquipmentSlot.FEET, -1, Identifier.fromNamespaceAndPath(BlockArmor.MODID, "items/overrides/white_bed_feet"));
			put(Blocks.BLACK_BED, info);
		}};
	}
	/**All sets, including disabled sets*/
	public static ArrayList<ArmorSet> allSets = Lists.newArrayList();
	/**All sets, mapped by their stack's display name*/
	public static HashMap<String, ArmorSet> nameToSetMap = Maps.newHashMap();
	/**All sets, mapped by their block's modid*/
	public static HashMap<String, TreeSet<ArmorSet>> modidToSetMap = Maps.newHashMap();
	/**Map of player UUID to their worn set effects*/
	private static HashMap<UUID, HashSet<SetEffect>> playerSetEffectsClient = Maps.newHashMap();
	/**Map of player UUID to their worn set effects*/
	private static HashMap<UUID, HashSet<SetEffect>> playerSetEffectsServer = Maps.newHashMap();

	/**Used to get textures, set recipe, and as repair material*/
	@Nullable
	private ItemStack stack;
	public Item item;
	public Block block;
	public BlockArmorMaterial material;      
	public BlockArmorItem helmet;
	public BlockArmorItem chestplate;
	public BlockArmorItem leggings;
	public BlockArmorItem boots;
	public boolean isFromModdedBlock;
	public ArrayList<SetEffect> setEffects;
	public ArrayList<SetEffect> defaultSetEffects;
	public String modid;
	public String registryName;
	/**should only be modified through enable() and disable(); enabled = in creative tab and has recipe*/
	private boolean enabled;
	/**Only changed on client*/
	public boolean missingTextures; 
	// armor values calculated from block / config
	public float armorDamageReduction;
	public float armorToughness;
	public int armorDurability;
	public int armorEnchantability;
	public int armorKnockbackResistance;

	public ArmorSet(Item item) {
		this.item = item.asItem();
		this.block = ((BlockItem) item).getBlock();
		this.registryName = ArmorSet.getItemRegistryName(this.item);
		try {
			Identifier loc = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this.item);
			this.modid = loc.getNamespace().toLowerCase();
			if (!this.modid.equals("minecraft"))
				isFromModdedBlock = true;
		}
		catch (Exception e) {
			this.modid = "???";
			isFromModdedBlock = true;
		}
		//calculate values for and set material
		boolean indestructible = BlockUtils.getHardness(block) == -1;
		float hardness = Math.min(BlockUtils.getHardness(block), 100);
		if (indestructible) 
			hardness = 100;
		float blastResistance = BlockUtils.getBlastResistance(block);
		boolean requiresTool = BlockUtils.getRequiresTool(block);
		boolean isSolid = BlockUtils.getIsSolid(block);
		//BlockArmor.LOGGER.info(getItemStackDisplayName(stack, null).getString()+": hardness = "+hardness+", blastResistance = "+blastResistance+", requiresTool = "+requiresTool+", isSolid = "+isSolid); 
		this.armorDamageReduction = hardness >= 8 ? (hardness * 0.003f + 4.5f) : (hardness * 0.65f); 
		if (requiresTool)
			this.armorDamageReduction *= 1.2f;
		if (!isSolid)
			this.armorDamageReduction *= 0.5f;
		if (indestructible)
			this.armorDamageReduction *= 20f;
		this.armorDamageReduction = Math.max((armorDamageReduction * 0.6f), 1);
		this.armorDurability = (int) (indestructible ? 0 : Math.min(30, armorDamageReduction * 8f));
		this.armorToughness = Math.min(20, blastResistance > 100 ? blastResistance / 400f : 0);
		this.armorEnchantability = 12;
		this.armorKnockbackResistance = 0;  // leaving 0 cuz of set effects that give it
		this.createMaterial();

		CommandDev.addBlockName(this); 
	}

	/**Create material for this set based on block values and update armor items with it (if they're created already)*/
	public void createMaterial() {
		int[] reductionAmounts = new int[] {(int) (armorDamageReduction), (int) (armorDamageReduction*2f), (int) (armorDamageReduction*2.5f), (int) (armorDamageReduction*1.45f)};
		this.material = new BlockArmorMaterial(getItemStackDisplayName(this.item, null)+" Material", 
				armorDurability, reductionAmounts, armorEnchantability, SoundEvents.ARMOR_EQUIP_GENERIC.value(), armorToughness,
				armorKnockbackResistance, () -> {
					return Ingredient.of(new ItemLike[]{this.item});
				});
		for (EquipmentSlot slot : SLOTS) {
			BlockArmorItem armor = this.getArmorForSlot(slot);
			if (armor != null)
				armor.setMaterial(material);
		}
		//BlockArmor.LOGGER.info(getItemStackDisplayName(stack, null).getString()+": blockHardness = "+armorDamageReduction+", toughness = "+armorToughness+", durability = "+armorDurability+", reductionAmounts = "+Arrays.toString(reductionAmounts)); 
	}

	public ItemStack getStack() {
		if (this.stack == null) 
			this.stack = new ItemStack(this.block);
		return this.stack;
	}

	/**Returns armor item for slot*/
	public BlockArmorItem getArmorForSlot(EquipmentSlot slot) {
		switch(slot) {
		case HEAD:
			return helmet;
		case CHEST:
			return chestplate;
		case LEGS:
			return leggings;
		case FEET:
			return boots;
		default:
			return null;
		}
	}

	/**Creates ArmorSets for each valid registered item and puts them in allSets
	 * Have to use these items instead of looking through block registry or else
	 * creative tabs will have all modded blocks as air for some reason... (from using new ItemStack(block))
	 * 
	 * Do not create ItemStacks yet because it may cause some mod incompatabilities with ItemStacks
	 * created before registry events are finished*/
	public static void setup(Collection<Item> itemsIn) {
		//create list of all ItemStacks with different display names and list of the display names
		ArrayList<String> displayNames = new ArrayList<String>();
		ArrayList<Item> items = new ArrayList<Item>();
		for (ArmorSet existing : allSets)
			displayNames.add(getNormalDisplayName(existing.item));
		for (Item item : itemsIn) {
			try { 
				if (item instanceof BlockItem) {
					Block block = ((BlockItem)item).getBlock();
					boolean manuallyAdded = false;
					for (Block manualBlock : MANUALLY_ADDED_SETS)
						if (block == manualBlock)
							manuallyAdded = true;

					String displayName = getNormalDisplayName(item);
					if (manuallyAdded || (!displayName.isEmpty() && 
							!displayNames.contains(displayName))) {
						items.add(item);
						displayNames.add(displayName);
					}
				}
			} catch (Exception e) {continue;}
		}

		//creates list of names that the items will be registered with to prevent duplicates
		ArrayList<String> registryNames = new ArrayList<String>();
		for (ArmorSet existing : allSets)
			registryNames.add(existing.registryName);

		//checks list of ItemStacks for valid ones and creates set and adds to allSets
		for (Item item : items) {
			if (isValid(item) && ArmorSet.getSet(item) == null) {
				String registryName = getItemRegistryName(item);
				if (!registryNames.contains(registryName) && !registryName.isEmpty()) {
					try {
						ArmorSet set = new ArmorSet(item);
						allSets.add(set);
						nameToSetMap.put(set.registryName, set);
						TreeSet<ArmorSet> list = modidToSetMap.containsKey(set.modid) ? modidToSetMap.get(set.modid) : Sets.newTreeSet(new Comparator<ArmorSet>() {
							@Override
							public int compare(ArmorSet s1, ArmorSet s2) {
								return s1.registryName.compareTo(s2.registryName);
							}
						});
						list.add(set);
						modidToSetMap.put(set.modid, list);
						registryNames.add(registryName);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**Get this item's display name (same way ItemStack would get it normally)*/
	private static String getNormalDisplayName(Item item) {
		if (item instanceof BlockArmorItem)
			item = ((BlockArmorItem)item).set.item;

		return Component.translatable(item.getDescriptionId()).getString();
	}

	/**Used to uniformly create registry name*/
	public static String getItemRegistryName(Item item) {
		try {
			String registryName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase().replace(" ", "_");
			return registryName;
		} 
		catch (Exception e) {
			return "";
		}
	}

	/**Change display name based on the block*/
	public static Component getItemStackDisplayName(Item item, EquipmentSlot slot)	{
		String name = getNormalDisplayName(item);

		//manually set display names
		name = name.replace("Block of ", "") 
				.replace("Block ", "")
				.replace(" Block", "")
				.replace("Sugar Canes", "Sugar Cane")
				.replace("Bricks", "Brick")
				.replace("Planks", "Plank");

		name = ChatFormatting.stripFormatting(name);

		if (slot != null)
			switch (slot) {
			case HEAD:
				return Component.translatable("item.blockarmor.helmet", name);
			case CHEST:
				return Component.translatable("item.blockarmor.chestplate", name);
			case LEGS:
				return Component.translatable("item.blockarmor.leggings", name);
			case FEET:
				return Component.translatable("item.blockarmor.boots", name);
			default:
				break;
			}

		return Component.literal(name);
	}

	/**Returns first piece of armor of the entity's worn set, or null*/
	@Nullable
	public static ItemStack getFirstSetItem(LivingEntity entity, SetEffect effect) {
		ArrayList<ItemStack> stacks = getAllSetItems(entity, effect);
		return stacks.isEmpty() ? null : stacks.get(0);
	}

	/**Returns all pieces of armor of the entity's worn set, or null*/
	@Nullable
	public static ArrayList<ItemStack> getAllSetItems(LivingEntity entity, SetEffect effect) {
		ArrayList<ItemStack> ret = Lists.newArrayList();
		if (effect != null && getWornSetEffects(entity).contains(effect))
			for (EquipmentSlot slot : SLOTS) {
				ItemStack stack = entity.getItemBySlot(slot);
				if (stack != null && stack.getItem() instanceof BlockArmorItem &&
						CombinedArmorData.hasEffect(stack, effect))
					ret.add(stack);
			}
		return ret;
	}

	/**Returns the active set effects of the armor that the entity is wearing*/
	public static HashSet<SetEffect> getWornSetEffects(LivingEntity entity) {
		return entity != null && getPlayerSetEffects(entity.level().isClientSide()).containsKey(entity.getUUID()) ?
				getPlayerSetEffects(entity.level().isClientSide()).get(entity.getUUID()) : Sets.newHashSet();
	}

	/**Does this entity have this worn set effect type active*/
	public static boolean hasSetEffect(LivingEntity entity, SetEffect effect) {
		for (SetEffect effect2 : getWornSetEffects(entity))
			if (effect.getClass() == effect2.getClass())
				return true;
		return false;
	}

	/**Returns armor set corresponding to given block and meta, or null if none exists*/
	public static ArmorSet getSet(Block block) {
		for (ArmorSet set : allSets)
			if (set.block == block)
				return set;
		return null;
	}

	/**Returns armor set corresponding to given item, or null if none exists*/
	public static ArmorSet getSet(Item item) {
		for (ArmorSet set : allSets)
			if (set.item == item)
				return set;
		return null;
	}

	/**Should an armor set be made from this item*/
	private static boolean isValid(Item itemIn) {
		try {			
			// not BlockItem
			if (itemIn == null || !(itemIn instanceof BlockItem))
				return false;
			BlockItem item = (BlockItem) itemIn;
			Block block = item.getBlock();
			// manually added blocks (only vanilla cuz we're overriding the textures)
			if ((block instanceof ShulkerBoxBlock ||
					block instanceof BedBlock) && 
					net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("minecraft")) {
				//BlockArmor.LOGGER.debug("Valid "+itemIn.toString()+": manual");
				return true;
			}
			for (Block manualBlock : MANUALLY_ADDED_SETS)
				if (item != null && (item == manualBlock.asItem())) {
					//BlockArmor.LOGGER.debug("Valid "+itemIn.toString()+": manual"); 
					return true;
				}
			String displayName = getNormalDisplayName(itemIn);
			String registryName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
			String modid = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace();
			// bad modded item, ore/ingot, or unnamed
			if (modid.contains("one_point_twelve_concrete") ||
					modid.contains("railcraft") ||
					registryName.contains("ore") || 
					registryName.contains("ingot") || 
					displayName.contains(".name") || 
					displayName.contains("Ore") ||
					displayName.contains("%") || 
					displayName.contains("Ingot")) {
				//BlockArmor.LOGGER.debug("Invalid "+itemIn.toString()+": display name = "+displayName+", registryName = "+registryName+", modid = "+modid);
				return false;
			}
			// bad blocks
			if (block instanceof LiquidBlock || 
					block instanceof BaseEntityBlock || 
					net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath().contains("ore") || 
					block instanceof CropBlock || 
					block instanceof BushBlock ||
					block == Blocks.BARRIER || 
					block instanceof SlabBlock || 
					block instanceof InfestedBlock ||
					block.defaultBlockState().getRenderShape() != RenderShape.MODEL ||
					block == Blocks.IRON_BLOCK || 
					block == Blocks.GOLD_BLOCK || 
					block == Blocks.DIAMOND_BLOCK ||
					block == Blocks.AIR ||
					block == Blocks.SNOW ||
					block == Blocks.NETHERITE_BLOCK) {
				//BlockArmor.LOGGER.debug("Invalid "+itemIn.toString()+": block = "+block.toString()); 
				return false;
			}
			// bad modded items
			registryName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
			if (registryName.equalsIgnoreCase("evilcraft:darkBlock") || 
					registryName.equalsIgnoreCase("evilcraft:obscuredGlass") ||
					registryName.equalsIgnoreCase("evilcraft:hardenedBlood") ||
					registryName.equalsIgnoreCase("evilcraft:darkPowerGemBlock") ||
					registryName.equalsIgnoreCase("darkutils:filter") || 
					registryName.equalsIgnoreCase("darkutils:filter_inverted") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedanalyser") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedbot") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedmutationstation") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedinscriber") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedhydrophonic") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedresearch") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedpipe") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedironscaff") ||
					registryName.equalsIgnoreCase("tconstruct:clear_glass")) {
				//BlockArmor.LOGGER.debug("Invalid "+itemIn.toString()+": registryName = "+registryName); 
				return false;
			}

			//Check if full block
			if (block.defaultBlockState().getCollisionShape(null, BlockPos.ZERO, CollisionContext.empty()) != Shapes.block()) {
				//BlockArmor.LOGGER.debug("Invalid "+itemIn.toString()+": not full block");
				return false;
			}

			//BlockArmor.LOGGER.debug("Valid "+itemIn.toString()+": display name = "+displayName+", registryName = "+registryName+", modid = "+modid);
			return true;
		}
		catch (Exception e) { 
			//BlockArmor.LOGGER.debug("Invalid "+itemIn.toString()+": error = "+e.toString()); 
			return false; 
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	/**Adds set items to creative tab and adds recipes*/
	public boolean enable() {
		if (this.missingTextures)
			return false;
		this.enabled = true;

		BlockArmorItem[] armors = new BlockArmorItem[] {this.helmet, this.chestplate, this.leggings, this.boots};
		for (BlockArmorItem armor : armors) {
			if (armor != null) {
				CreativeModeTab tab = isFromModdedBlock ? BlockArmorCreativeTab.moddedTab : BlockArmorCreativeTab.vanillaTab;
				java.util.List<ItemStack> stacks = isFromModdedBlock ? BlockArmorCreativeTab.moddedStacks : BlockArmorCreativeTab.vanillaStacks;
				if (stacks.stream().noneMatch(stack -> stack.getItem() == armor)) stacks.add(new ItemStack(armor));
			}
		}

		return true;
	}

	/**Remove set items from creative tab and removes recipes*/
	public boolean disable() {
		if (this.enabled)
			this.enabled = false;
		else
			return false;

		BlockArmorItem[] armors = new BlockArmorItem[] {this.helmet, this.chestplate, this.leggings, this.boots};
		for (BlockArmorItem armor : armors)
			if (armor != null) {
				BlockArmorCreativeTab.vanillaStacks.removeIf(stack -> stack.getItem() == armor);
				BlockArmorCreativeTab.moddedStacks.removeIf(stack -> stack.getItem() == armor);
			}

		return true;
	}

	// ================== WORN SET EFFECTS =================

	private static HashMap<UUID, HashSet<SetEffect>> getPlayerSetEffects(boolean isRemote) {
		return isRemote ? playerSetEffectsClient : playerSetEffectsServer;
	}

	/**Call onStop for set effects on logout - only called serverside in SSP*/
	public static void onLogout(Player player) {
		HashMap<UUID, HashSet<SetEffect>> playerSetEffects = getPlayerSetEffects(player.level().isClientSide());
		if (playerSetEffects.containsKey(player.getUUID())) {
			for (SetEffect effect : playerSetEffects.get(player.getUUID()))
				effect.onStop(player);
			playerSetEffects.remove(player.getUUID());
		}
	}

	/**Update player set effects each tick*/
	public static void tickClient(List<? extends Player> players) {
		updateWornSetEffects(getPlayerSetEffects(true), players);
		for (Player player : players)
			twopiradians.blockArmor.common.ArmorLifecycle.tickPlayer(player);
	}

	/**Update player set effects each tick*/
	public static void tickServer(net.minecraft.server.MinecraftServer server) {
		List<? extends Player> players = server.getPlayerList().getPlayers();
		updateWornSetEffects(getPlayerSetEffects(false), players);
		for (Player player : players)
			twopiradians.blockArmor.common.ArmorLifecycle.tickPlayer(player);
	}

	/**Update player set effects each tick for efficiency and onStart and onStop*/
	private static void updateWornSetEffects(HashMap<UUID, HashSet<SetEffect>> playerSetEffects, 
			List<? extends Player> onlinePlayers) {
		for (Player player : onlinePlayers) {
			// get old/new effect lists
			HashSet<SetEffect> oldEffects = playerSetEffects.get(player.getUUID());
			if (oldEffects == null)
				oldEffects = Sets.newHashSet();
			HashSet<SetEffect> newEffects = calculateWornSetEffects(player);
			// new effects not in old -> onStart
			for (SetEffect newEffect : newEffects)
				if (!oldEffects.contains(newEffect)) 
					newEffect.onStart(player);
			// old effects not in new -> onStop
			for (SetEffect oldEffect : oldEffects) 
				if (!newEffects.contains(oldEffect)) 
					oldEffect.onStop(player);
			// update set effects
			playerSetEffects.put(player.getUUID(), newEffects);
			// Fabric does not expose Forge's LivingEquipmentChangeEvent. Keep the
			// full-set NBT marker current after every calculated equipment state.
			SetEffect.onEquipmentChange(player);
		}
	}

	/**Returns the active set effects of the armor that the entity is wearing to be cached every tick*/
	/**
	 * Calculates the effects from the entity's current equipment rather than the
	 * per-tick cache.  Equipment attribute changes must use this live value: the
	 * cache intentionally updates once per tick and can otherwise leave a set
	 * attribute applied after a piece has been removed.
	 */
	public static HashSet<SetEffect> calculateWornSetEffects(LivingEntity entity) {
		HashSet<SetEffect> effects = Sets.newHashSet();
		HashMap<SetEffect, Integer> setCounts = Maps.newHashMap();
		if (entity != null) {
			for (EquipmentSlot slot : SLOTS) {
				ItemStack stack = entity.getItemBySlot(slot);
				if (stack != null && stack.getItem() instanceof BlockArmorItem) {
					for (SetEffect effect : CombinedArmorData.effects(stack)) {
						if (effect.isEnabled()) {
							int count = 1;
							if (setCounts.containsKey(effect))
								count += setCounts.get(effect);
							setCounts.put(effect, count);
						}
					}
				}
			}
			for (SetEffect effect : setCounts.keySet())
				if (setCounts.get(effect) >= Config.piecesForSet) 
					effects.add(effect);
		}
		return effects;
	}

}
