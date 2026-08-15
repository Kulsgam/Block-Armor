package twopiradians.blockArmor.client;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import twopiradians.blockArmor.common.seteffect.SetEffectHoarder;
import twopiradians.blockArmor.common.CommonProxy;

/** Client-only lifecycle holder. Fabric registration is performed by the client entrypoint. */
public final class ClientProxy {
    private ClientProxy() {}
    public static void setup() {
        ScreenRegistry.register(SetEffectHoarder.containerType_9x1, ContainerScreen::new);
        ScreenRegistry.register(SetEffectHoarder.containerType_9x2, ContainerScreen::new);
        ScreenRegistry.register(SetEffectHoarder.containerType_9x3, ContainerScreen::new);
        ScreenRegistry.register(SetEffectHoarder.containerType_9x4, ContainerScreen::new);
        ScreenRegistry.register(SetEffectHoarder.containerType_9x5, ContainerScreen::new);
        ScreenRegistry.register(SetEffectHoarder.containerType_9x6, ContainerScreen::new);
    }
    public static void setWorldTime(Level world, long time) {
        if (world instanceof ClientLevel level) level.setDayTime(time);
        else CommonProxy.setWorldTime(world, time);
    }
}
