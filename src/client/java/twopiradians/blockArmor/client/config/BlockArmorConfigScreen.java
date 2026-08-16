package twopiradians.blockArmor.client.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import twopiradians.blockArmor.common.config.Config;

/** Global Block Armor options, ported to the 26.1 extracted GUI renderer. */
public class BlockArmorConfigScreen extends Screen {
    private final Screen parent;
    private final List<EditBox> values = new ArrayList<>();
    private String error = "";

    public BlockArmorConfigScreen(Screen parent) { super(Component.literal("Block Armor Configuration")); this.parent = parent; }

    @Override protected void init() {
        values.clear();
        int left = width / 2 + 30, y = 44;
        addValue(left, y, Integer.toString(Config.piecesForSet)); y += 24;
        addValue(left, y, Double.toString(Config.globalToughnessModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalEnchantabilityModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalDamageReductionModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalKnockbackResistanceModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalDurabilityModifier));
        addRenderableWidget(Button.builder(glintLabel(), button -> {
            BlockArmorClientConfig.alwaysShowArmorGlint = !BlockArmorClientConfig.alwaysShowArmorGlint;
            BlockArmorClientConfig.save();
            button.setMessage(glintLabel());
        }).bounds(width / 2 - 100, 190, 200, 20).build());
        Button durability = addRenderableWidget(Button.builder(durabilityLabel(), button -> {
            boolean enabled = !Config.effectsUseDurability;
            Config.effectsUseDurability = enabled;
            ConfigScreenSupport.saveAndApply(() -> Config.effectsUseDurability = enabled);
            button.setMessage(durabilityLabel());
        }).bounds(width / 2 - 100, 214, 200, 20).build());
        durability.active = ConfigScreenSupport.mayEdit();
        addRenderableWidget(Button.builder(Component.literal("Configure Armor Sets…"), button -> minecraft.setScreen(new ArmorSetListScreen(this)))
                .bounds(width / 2 - 100, 240, 200, 20).build());
        Button save = addRenderableWidget(Button.builder(Component.literal("Save & Apply"), button -> save())
                .bounds(width / 2 - 100, height - 28, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> minecraft.setScreen(parent))
                .bounds(width / 2 + 2, height - 28, 98, 20).build());
        for (EditBox value : values) value.setResponder(ignored -> applyValues(false));
    }
    private Component glintLabel() { return Component.literal("Always show armor glint: " + (BlockArmorClientConfig.alwaysShowArmorGlint ? "ON" : "OFF")); }
    private Component durabilityLabel() { return Component.literal("Effects use durability: " + (Config.effectsUseDurability ? "ON" : "OFF")); }
    private void addValue(int x, int y, String value) {
        EditBox box = new EditBox(font, x, y, 100, 18, Component.empty());
        box.setValue(value); box.setEditable(ConfigScreenSupport.mayEdit()); values.add(addRenderableWidget(box));
    }
    private void save() { applyValues(true); }
    private void applyValues(boolean showStatus) {
        try {
            int pieces = ConfigScreenSupport.parseInt(values.get(0).getValue(), 1, 4, "Required pieces");
            double toughness = ConfigScreenSupport.parseDouble(values.get(1).getValue(), "Toughness modifier");
            double enchantability = ConfigScreenSupport.parseDouble(values.get(2).getValue(), "Enchantability modifier");
            double reduction = ConfigScreenSupport.parseDouble(values.get(3).getValue(), "Damage-reduction modifier");
            double knockback = ConfigScreenSupport.parseDouble(values.get(4).getValue(), "Knockback-resistance modifier");
            double durability = ConfigScreenSupport.parseDouble(values.get(5).getValue(), "Durability modifier");
            // Update the screen/tooltips now, then publish the same snapshot on
            // the authoritative integrated-server thread.
            applyGlobals(pieces, toughness, enchantability, reduction, knockback, durability);
            ConfigScreenSupport.saveAndApply(() ->
                    applyGlobals(pieces, toughness, enchantability, reduction, knockback, durability));
            if (showStatus) error = "Saved and applied immediately";
            else if (error.startsWith("Saved")) error = "";
        } catch (IllegalArgumentException exception) {
            if (showStatus) error = exception.getMessage();
        }
    }
    private static void applyGlobals(int pieces, double toughness, double enchantability,
            double reduction, double knockback, double durability) {
        Config.piecesForSet = pieces;
        Config.globalToughnessModifier = toughness;
        Config.globalEnchantabilityModifier = enchantability;
        Config.globalDamageReductionModifier = reduction;
        Config.globalKnockbackResistanceModifier = knockback;
        Config.globalDurabilityModifier = durability;
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 14, 0xFFFFFFFF);
        String[] labels = {"Pieces required for effects (1–4)", "Global toughness modifier", "Global enchantability modifier", "Global damage-reduction modifier", "Global knockback-resistance modifier", "Global durability modifier"};
        for (int i = 0; i < labels.length; i++) graphics.text(font, labels[i], width / 2 - 210, 49 + i * 24, 0xFFDDDDDD);
        if (!ConfigScreenSupport.mayEdit()) graphics.centeredText(font, Component.literal("Server options are read-only; armor glint is client-side").withStyle(ChatFormatting.RED), width / 2, 266, 0xFFFFFFFF);
        if (!error.isEmpty()) graphics.centeredText(font, error, width / 2, height - 42, error.startsWith("Saved") ? 0xFF55FF55 : 0xFFFF5555);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
