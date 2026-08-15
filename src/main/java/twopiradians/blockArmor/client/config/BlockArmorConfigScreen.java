package twopiradians.blockArmor.client.config;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import twopiradians.blockArmor.common.config.Config;

/** Global options page; per-armor options are available through the browser button. */
public final class BlockArmorConfigScreen extends Screen {
    private final Screen parent;
    private final List<EditBox> values = new ArrayList<>();
    private String error = "";

    public BlockArmorConfigScreen(Screen parent) {
        super(new TextComponent("Block Armor Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        values.clear();
        int left = width / 2 + 30;
        int y = 44;
        addValue(left, y, Integer.toString(Config.piecesForSet)); y += 24;
        addValue(left, y, Double.toString(Config.globalToughnessModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalEnchantabilityModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalDamageReductionModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalKnockbackResistanceModifier)); y += 24;
        addValue(left, y, Double.toString(Config.globalDurabilityModifier));

        Button durability = addRenderableWidget(new Button(width / 2 - 100, 190, 200, 20,
                durabilityLabel(), button -> {
                    Config.effectsUseDurability = !Config.effectsUseDurability;
                    button.setMessage(durabilityLabel());
                }));
        durability.active = ConfigScreenSupport.mayEdit();
        addRenderableWidget(new Button(width / 2 - 100, 216, 200, 20,
                new TextComponent("Configure Armor Sets…"), button -> minecraft.setScreen(new ArmorSetListScreen(this))));
        Button save = addRenderableWidget(new Button(width / 2 - 100, height - 28, 98, 20,
                new TextComponent("Save & Apply"), button -> save()));
        save.active = ConfigScreenSupport.mayEdit();
        addRenderableWidget(new Button(width / 2 + 2, height - 28, 98, 20,
                new TextComponent("Done"), button -> minecraft.setScreen(parent)));
    }

    private Component durabilityLabel() {
        return new TextComponent("Effects use durability: " + (Config.effectsUseDurability ? "ON" : "OFF"));
    }

    private void addValue(int x, int y, String value) {
        EditBox box = new EditBox(font, x, y, 100, 18, TextComponent.EMPTY);
        box.setValue(value);
        box.setEditable(ConfigScreenSupport.mayEdit());
        values.add(addRenderableWidget(box));
    }

    private void save() {
        try {
            Config.piecesForSet = ConfigScreenSupport.parseInt(values.get(0).getValue(), 1, 4, "Required pieces");
            Config.globalToughnessModifier = ConfigScreenSupport.parseDouble(values.get(1).getValue(), "Toughness modifier");
            Config.globalEnchantabilityModifier = ConfigScreenSupport.parseDouble(values.get(2).getValue(), "Enchantability modifier");
            Config.globalDamageReductionModifier = ConfigScreenSupport.parseDouble(values.get(3).getValue(), "damage-reduction modifier");
            Config.globalKnockbackResistanceModifier = ConfigScreenSupport.parseDouble(values.get(4).getValue(), "knockback-resistance modifier");
            Config.globalDurabilityModifier = ConfigScreenSupport.parseDouble(values.get(5).getValue(), "durability modifier");
            ConfigScreenSupport.saveAndApply();
            error = "Saved and applied immediately";
        } catch (IllegalArgumentException exception) {
            error = exception.getMessage();
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        drawCenteredString(pose, font, title, width / 2, 14, 0xFFFFFF);
        String[] labels = {"Pieces required for effects (1–4)", "Global toughness modifier",
                "Global enchantability modifier", "Global damage-reduction modifier",
                "Global knockback-resistance modifier", "Global durability modifier"};
        for (int i = 0; i < labels.length; i++) drawString(pose, font, labels[i], width / 2 - 210, 49 + i * 24, 0xDDDDDD);
        if (!ConfigScreenSupport.mayEdit())
            drawCenteredString(pose, font, new TextComponent("Dedicated-server configuration is read-only here").withStyle(ChatFormatting.RED), width / 2, 244, 0xFFFFFF);
        if (!error.isEmpty()) drawCenteredString(pose, font, error, width / 2, height - 42,
                error.startsWith("Saved") ? 0x55FF55 : 0xFF5555);
        super.render(pose, mouseX, mouseY, partialTick);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
