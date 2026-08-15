package twopiradians.blockArmor.packet;

import net.minecraft.network.FriendlyByteBuf;
import twopiradians.blockArmor.common.config.Config;

/** Versioned authoritative server configuration snapshot. */
public final class SConfigSyncPacket {
    public static final int VERSION = 1;

    private SConfigSyncPacket() {}

    public static FriendlyByteBuf encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(VERSION);
        Config.writeNetwork(buffer);
        return buffer;
    }

    public static void decode(FriendlyByteBuf buffer) {
        int version = buffer.readVarInt();
        if (version != VERSION) throw new IllegalStateException("Unsupported Block Armor config packet " + version);
        Config.readNetwork(buffer);
    }
}
