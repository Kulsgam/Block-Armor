package twopiradians.blockArmor.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.CommonProxy;
import twopiradians.blockArmor.common.config.Config;
import twopiradians.blockArmor.common.item.ArmorSet;

final class ConfigScreenSupport {
    private ConfigScreenSupport() {}

    static boolean mayEdit() {
        Minecraft client = Minecraft.getInstance();
        // Never let a remote client edit a dedicated server's authoritative config.
        return client.getConnection() == null || client.getSingleplayerServer() != null;
    }

    static void saveAndApply(Runnable mutation) {
        Minecraft client = Minecraft.getInstance();
        MinecraftServer integratedServer = client.getSingleplayerServer();
        if (integratedServer != null) {
            integratedServer.execute(() -> {
                mutation.run();
                Config.saveCurrent();
                CommonProxy.refreshAfterConfigChange(integratedServer);
                client.execute(() -> twopiradians.blockArmor.client.BlockArmorClientCaches.invalidate(client));
            });
        } else if (client.getConnection() == null) {
            mutation.run();
            for (ArmorSet set : ArmorSet.allSets) set.createMaterial();
            Config.saveCurrent();
            twopiradians.blockArmor.client.BlockArmorClientCaches.invalidate(client);
        }
    }

    static int parseInt(String value, int minimum, int maximum, String name) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < minimum || parsed > maximum) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
    }

    static double parseDouble(String value, String name) {
        try {
            double parsed = Double.parseDouble(value.trim());
            if (!Double.isFinite(parsed) || parsed < 0D || parsed > 999999D) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be between 0 and 999999");
        }
    }
}
