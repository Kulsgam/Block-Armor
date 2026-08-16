package twopiradians.blockArmor.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import twopiradians.blockArmor.common.CommonProxy;

/** Client-only lifecycle holder. Fabric registration is performed by the client entrypoint. */
public final class ClientProxy {
    private ClientProxy() {}
    public static void setup() {
        // Hoarder uses vanilla generic chest menu types; their vanilla screens
        // are already registered before mod initialization.
    }
}
