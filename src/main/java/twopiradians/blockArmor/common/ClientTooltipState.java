package twopiradians.blockArmor.common;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;

/** Client queries installed by the client entrypoint without common-side class references. */
public final class ClientTooltipState {
    private static BooleanSupplier shift = () -> false;
    private static Supplier<Player> player = () -> null;
    private ClientTooltipState() {}
    public static void install(BooleanSupplier shiftQuery, Supplier<Player> playerQuery) { shift=shiftQuery; player=playerQuery; }
    public static boolean isShiftDown() { return shift.getAsBoolean(); }
    public static Player player() { return player.get(); }
}
