package twopiradians.blockArmor.packet;

import java.util.ArrayList;
import java.util.UUID;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import twopiradians.blockArmor.common.command.CommandDev;

public final class SDevColorsPacket {
    private SDevColorsPacket() {}

    public static FriendlyByteBuf encode() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ArrayList<UUID> ids = new ArrayList<>(CommandDev.devColors.keySet());
        buffer.writeVarInt(ids.size());
        for (UUID id : ids) {
            Float[] color = CommandDev.devColors.get(id);
            buffer.writeUUID(id);
            buffer.writeFloat(color[0]); buffer.writeFloat(color[1]); buffer.writeFloat(color[2]);
        }
        return buffer;
    }

    public static void decode(FriendlyByteBuf buffer) {
        CommandDev.devColors = Maps.newHashMap();
        for (int count = buffer.readVarInt(); count > 0; count--) {
            CommandDev.devColors.put(buffer.readUUID(), new Float[] {
                    buffer.readFloat(), buffer.readFloat(), buffer.readFloat() });
        }
    }
}
