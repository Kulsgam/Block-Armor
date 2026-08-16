package twopiradians.blockArmor.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import twopiradians.blockArmor.client.model.ModelBAArmor;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.CombinedArmorData;
import twopiradians.blockArmor.common.item.ModItems;

/** One-shot post-reload validation for runtime-generated models and legacy icon assets. */
final class BlockArmorClientDiagnostics {
    private static boolean complete;
    private static boolean equippedRenderProbeStarted;
    private static int equippedRenderProbeTicks;

    private BlockArmorClientDiagnostics() { }

    static void validateOnce(Minecraft client) {
        if (complete || client.getModelManager() == null || ModItems.allArmors.isEmpty()) return;
        try {
            // In 26.1 item components are world-data driven and vanilla holders
            // remain unbound at the title screen. Validate only after joining.
            if (!Items.IRON_HELMET.builtInRegistryHolder().areComponentsBound()) return;

            verifyArmorRendererRegistration();

            int validatedItems = 0;
            int itemRendererCallsBefore = BlockArmorItemRenderer.updateCalls();
            for (BlockArmorItem armor : ModItems.allArmors) {
                ItemStack normal = new ItemStack(armor);
                requireRenderable(client, normal, "generated armor " + armor.getDescriptionId());
                BlockArmorTextures.Info texture = BlockArmorTextures.find(armor);
                var source = BlockArmorTextures.sourceTexture(texture);
                if (client.getResourceManager().getResource(source).isEmpty())
                    throw new IllegalStateException("Missing direct worn texture " + source
                            + " for " + armor.getDescriptionId());
                validatedItems++;
            }
            if (BlockArmorItemRenderer.updateCalls() - itemRendererCallsBefore != validatedItems)
                throw new IllegalStateException("Shared Block Armor item model did not select the per-stack renderer");

            BlockArmorItem helmet = ModItems.allArmors.stream()
                    .filter(item -> item.getSlot() == EquipmentSlot.HEAD).findFirst().orElseThrow();
            ItemStack normal = new ItemStack(helmet);
            ItemStack combined = CombinedArmorData.combine(normal, new ItemStack(Items.IRON_HELMET), "");
            if (!CombinedArmorData.isCombined(combined)
                    || CombinedArmorData.source(combined, true).getStringOr("Item", "").isBlank()
                    || !"minecraft:iron_helmet".equals(CombinedArmorData.source(combined, false)
                            .getStringOr("Item", "")))
                throw new IllegalStateException("Combined armor did not retain distinct left/right visual sources");
            requireRenderable(client, combined, "combined armor");

            for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (new ModelBAArmor(slot).root().getAllParts().stream().allMatch(part -> part.isEmpty()))
                    throw new IllegalStateException("Empty Block Armor mesh for " + slot.getName());
            }
            complete = true;
            BlockArmor.LOGGER.info("Validated all {} Block Armor item silhouettes and worn source textures, combined sources, and equipped meshes",
                    validatedItems);
        } catch (NullPointerException | IllegalStateException notReady) {
            // Initial client ticks overlap model installation. Retry after the reload finishes.
            if (client.isGameLoadFinished()) throw notReady;
        }
    }

    /** Opt-in development probe: equip a real combined helmet and require our renderer to be invoked. */
    static void tickEquippedRenderProbe(Minecraft client) {
        if (!Boolean.getBoolean("blockarmor.verifyEquippedRenderer") || client.player == null
                || client.getSingleplayerServer() == null || ModItems.allArmors.isEmpty()) return;
        if (!equippedRenderProbeStarted) {
            equippedRenderProbeStarted = true;
            BlockArmorItem helmet = ModItems.allArmors.stream()
                    .filter(item -> item.getSlot() == EquipmentSlot.HEAD).findFirst().orElseThrow();
            ItemStack combined = CombinedArmorData.combine(new ItemStack(helmet), new ItemStack(Items.IRON_HELMET), "");
            var server = client.getSingleplayerServer();
            java.util.UUID playerId = client.player.getUUID();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(playerId);
                if (player != null) player.setItemSlot(EquipmentSlot.HEAD, combined);
            });
            client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
        }
        if (++equippedRenderProbeTicks == 100) {
            if (BlockArmorRenderer.renderCalls() == 0)
                throw new IllegalStateException("Block Armor equipped renderer was registered but never selected");
            BlockArmor.LOGGER.info("Verified equipped combined-armor renderer selection with {} render calls",
                    BlockArmorRenderer.renderCalls());
        }
    }

    private static void requireRenderable(Minecraft client, ItemStack stack, String description) {
        ItemStackRenderState state = new ItemStackRenderState();
        client.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GUI, client.level, null, 0);
        if (state.isEmpty()) throw new IllegalStateException("No runtime model layers for " + description);
    }

    private static void verifyArmorRendererRegistration() {
        try {
            Class<?> registry = Class.forName("net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl");
            var get = registry.getDeclaredMethod("get", net.minecraft.world.item.Item.class);
            get.setAccessible(true);
            for (BlockArmorItem armor : ModItems.allArmors) {
                if (get.invoke(null, armor) == null)
                    throw new IllegalStateException("Fabric did not install Block Armor's equipped renderer for "
                            + armor.getDescriptionId());
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to verify Fabric equipped-renderer selection", exception);
        }
    }

    static void reset() { complete = false; }
}
