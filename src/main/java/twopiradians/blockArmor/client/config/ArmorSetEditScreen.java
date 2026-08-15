package twopiradians.blockArmor.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Complete editor for one generated armor set. */
final class ArmorSetEditScreen extends Screen {
    private final Screen parent;
    private final ArmorSet set;
    private final List<EditBox> values = new ArrayList<>();
    private EditBox effects;
    private boolean enabled;
    private String status = "";

    ArmorSetEditScreen(Screen parent, ArmorSet set) {
        super(new TextComponent("Configure " + ArmorSet.getItemStackDisplayName(set.item, null).getString()));
        this.parent = parent;
        this.set = set;
        this.enabled = set.isEnabled();
    }

    @Override
    protected void init() {
        values.clear();
        int x = width / 2 + 35;
        int y = 52;
        addValue(x, y, Integer.toString(set.armorDurability)); y += 24;
        addValue(x, y, Float.toString(set.armorDamageReduction)); y += 24;
        addValue(x, y, Float.toString(set.armorToughness)); y += 24;
        addValue(x, y, Integer.toString(set.armorKnockbackResistance)); y += 24;
        addValue(x, y, Integer.toString(set.armorEnchantability));
        Button enabledButton = addRenderableWidget(new Button(width / 2 - 110, 28, 220, 20, enabledLabel(), button -> {
            enabled = !enabled;
            button.setMessage(enabledLabel());
        }));
        enabledButton.active = ConfigScreenSupport.mayEdit();

        effects = new EditBox(font, width / 2 - 180, 188, 360, 20, new TextComponent("Set effects"));
        effects.setMaxLength(4096);
        effects.setValue(set.setEffects.stream().map(SetEffect::writeToString).collect(Collectors.joining(";")));
        effects.setEditable(ConfigScreenSupport.mayEdit());
        addRenderableWidget(effects);

        Button save = addRenderableWidget(new Button(width / 2 - 102, height - 28, 100, 20,
                new TextComponent("Save & Apply"), button -> save()));
        save.active = ConfigScreenSupport.mayEdit();
        addRenderableWidget(new Button(width / 2 + 2, height - 28, 100, 20,
                new TextComponent("Back"), button -> minecraft.setScreen(parent)));
    }

    private net.minecraft.network.chat.Component enabledLabel() {
        return new TextComponent("Armor set enabled: " + (enabled ? "ON" : "OFF"));
    }

    private void addValue(int x, int y, String value) {
        EditBox box = new EditBox(font, x, y, 110, 18, TextComponent.EMPTY);
        box.setValue(value);
        box.setEditable(ConfigScreenSupport.mayEdit());
        values.add(addRenderableWidget(box));
    }

    private void save() {
        try {
            int durability = ConfigScreenSupport.parseInt(values.get(0).getValue(), 0, Integer.MAX_VALUE, "Durability");
            double reduction = ConfigScreenSupport.parseDouble(values.get(1).getValue(), "Damage reduction");
            double toughness = ConfigScreenSupport.parseDouble(values.get(2).getValue(), "Toughness");
            int knockback = ConfigScreenSupport.parseInt(values.get(3).getValue(), 0, Integer.MAX_VALUE, "Knockback resistance");
            int enchantability = ConfigScreenSupport.parseInt(values.get(4).getValue(), 0, Integer.MAX_VALUE, "Enchantability");
            ArrayList<SetEffect> parsedEffects = new ArrayList<>();
            for (String text : effects.getValue().split(";")) {
                if (text.isBlank()) continue;
                SetEffect effect = SetEffect.getEffectFromString(text.trim());
                if (effect == null) throw new IllegalArgumentException("Unknown effect: " + text.trim());
                parsedEffects.add(effect);
            }
            set.armorDurability = durability;
            set.armorDamageReduction = (float) reduction;
            set.armorToughness = (float) toughness;
            set.armorKnockbackResistance = knockback;
            set.armorEnchantability = enchantability;
            set.setEffects.clear();
            set.setEffects.addAll(parsedEffects);
            if (enabled) set.enable(); else set.disable();
            ConfigScreenSupport.saveAndApply();
            status = "Saved and applied immediately";
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        drawCenteredString(pose, font, title, width / 2, 10, 0xFFFFFF);
        String[] labels = {"Base durability", "Damage reduction", "Toughness", "Knockback resistance", "Enchantability"};
        for (int i = 0; i < labels.length; i++) drawString(pose, font, labels[i], width / 2 - 180, 57 + i * 24, 0xDDDDDD);
        drawString(pose, font, "Set effects (semicolon separated)", width / 2 - 180, 176, 0xDDDDDD);
        drawCenteredString(pose, font, "Examples: Speedy;Health Boost(4.0). Blank removes all effects.", width / 2, 212, 0x999999);
        if (!ConfigScreenSupport.mayEdit())
            drawCenteredString(pose, font, new TextComponent("This set is controlled by the dedicated server").withStyle(ChatFormatting.RED), width / 2, 230, 0xFFFFFF);
        if (!status.isEmpty()) drawCenteredString(pose, font, status, width / 2, height - 42,
                status.startsWith("Saved") ? 0x55FF55 : 0xFF5555);
        super.render(pose, mouseX, mouseY, partialTick);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
