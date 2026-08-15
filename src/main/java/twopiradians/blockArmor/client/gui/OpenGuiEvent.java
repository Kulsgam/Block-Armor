package twopiradians.blockArmor.client.gui;

import net.minecraft.client.Minecraft;
import twopiradians.blockArmor.common.command.CommandDev;

/** Client GUI opening is invoked by the Fabric chat receiver. */
public final class OpenGuiEvent {
    private OpenGuiEvent() {}
    public static void openFor(java.util.UUID sender) {
        if (GuiArmorDisplay.DISPLAY_ARMOR_GUI && CommandDev.DEVS.contains(sender))
            Minecraft.getInstance().setScreen(new GuiArmorDisplay());
    }
}
