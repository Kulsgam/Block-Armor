package twopiradians.blockArmor.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Lightweight developer display compatible with Minecraft 26.1's GUI API. */
public final class GuiArmorDisplay extends Screen {
    public static boolean DISPLAY_ARMOR_GUI = false;

    public GuiArmorDisplay() { super(Component.literal("Block Armor")); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 16, 0xFFFFFF);
        graphics.centeredText(font, "Developer armor display", width / 2, 38, 0xAAAAAA);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
