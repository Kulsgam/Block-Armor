package twopiradians.blockArmor.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;

/** Invalidates all client-side views derived from the authoritative config. */
public final class BlockArmorClientCaches {
    private BlockArmorClientCaches() {}

    public static void invalidate(Minecraft client) {
        BlockArmorItemRenderer.clearCaches();
        BlockArmorRenderer.clearCaches();
        BlockArmorTextures.clearCaches();
        BlockArmorClientDiagnostics.reset();
        if (client.level != null) {
            CreativeModeTabs.tryRebuildTabContents(client.level.enabledFeatures(),
                    client.player != null && client.player.canUseGameMasterBlocks(),
                    client.level.registryAccess());
        }
    }
}
