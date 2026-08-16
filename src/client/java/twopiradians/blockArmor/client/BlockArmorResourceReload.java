package twopiradians.blockArmor.client;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import twopiradians.blockArmor.common.BlockArmor;

final class BlockArmorResourceReload implements SimpleSynchronousResourceReloadListener {
    static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BlockArmorResourceReload());
    }

    @Override public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(BlockArmor.MODID, "renderer_cache");
    }

    @Override public void onResourceManagerReload(ResourceManager manager) {
        BlockArmorItemRenderer.clearCaches();
        BlockArmorRenderer.clearCaches();
        BlockArmorTextures.clearCaches();
        BlockArmorClientDiagnostics.reset();
    }
}
