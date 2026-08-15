package twopiradians.blockArmor.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import twopiradians.blockArmor.common.item.ModItems;
import twopiradians.blockArmor.mixin.CooldownInstanceAccessor;
import twopiradians.blockArmor.mixin.ItemCooldownsAccessor;

/** Full Block Armor cooldown snapshot used after login and dimension travel. */
public final class SSyncCooldownsPacket {
    private SSyncCooldownsPacket() {}

    public static FriendlyByteBuf encode(Player player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) player.getCooldowns();
        int now = accessor.blockarmor$getTickCount();
        var entries = accessor.blockarmor$getCooldowns().entrySet().stream()
                .filter(entry -> entry.getKey() instanceof twopiradians.blockArmor.common.item.BlockArmorItem)
                .toList();
        buffer.writeVarInt(entries.size());
        for (var entry : entries) {
            buffer.writeResourceLocation(Registry.ITEM.getKey(entry.getKey()));
            buffer.writeVarInt(Math.max(0, ((CooldownInstanceAccessor) (Object) entry.getValue()).blockarmor$getEndTime() - now));
        }
        return buffer;
    }

    public static void decode(Player player, FriendlyByteBuf buffer) {
        for (Item item : ModItems.allArmors) player.getCooldowns().removeCooldown(item);
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            int remaining = buffer.readVarInt();
            Item item = Registry.ITEM.get(id);
            if (item instanceof twopiradians.blockArmor.common.item.BlockArmorItem && remaining > 0)
                player.getCooldowns().addCooldown(item, remaining);
        }
    }
}
