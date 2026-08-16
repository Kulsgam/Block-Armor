package twopiradians.blockArmor.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Editor for one generated armor set. */
final class ArmorSetEditScreen extends Screen {
    private final Screen parent; private final ArmorSet set; private final List<EditBox> values = new ArrayList<>(); private EditBox effects; private boolean enabled; private String status = "";
    ArmorSetEditScreen(Screen parent, ArmorSet set) { super(Component.literal("Configure " + ArmorSet.getItemStackDisplayName(set.item, null).getString())); this.parent = parent; this.set = set; this.enabled = set.isEnabled(); }
    @Override protected void init() {
        values.clear(); int x = width / 2 + 35, y = 52;
        addValue(x, y, Integer.toString(set.armorDurability)); y += 24; addValue(x, y, Float.toString(set.armorDamageReduction)); y += 24; addValue(x, y, Float.toString(set.armorToughness)); y += 24; addValue(x, y, Integer.toString(set.armorKnockbackResistance)); y += 24; addValue(x, y, Integer.toString(set.armorEnchantability));
        Button enabledButton = addRenderableWidget(Button.builder(enabledLabel(), button -> { enabled = !enabled; button.setMessage(enabledLabel()); save(false); }).bounds(width / 2 - 110, 28, 220, 20).build()); enabledButton.active = ConfigScreenSupport.mayEdit();
        effects = new EditBox(font, width / 2 - 180, 188, 360, 20, Component.literal("Set effects")); effects.setMaxLength(4096); effects.setValue(set.setEffects.stream().map(SetEffect::writeToString).collect(Collectors.joining(";"))); effects.setEditable(ConfigScreenSupport.mayEdit()); addRenderableWidget(effects);
        Button save = addRenderableWidget(Button.builder(Component.literal("Save & Apply"), button -> save(true)).bounds(width / 2 - 102, height - 28, 100, 20).build()); save.active = ConfigScreenSupport.mayEdit();
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> minecraft.setScreen(parent)).bounds(width / 2 + 2, height - 28, 100, 20).build());
        for (EditBox value : values) value.setResponder(ignored -> save(false));
        effects.setResponder(ignored -> save(false));
    }
    private Component enabledLabel() { return Component.literal("Armor set enabled: " + (enabled ? "ON" : "OFF")); }
    private void addValue(int x, int y, String value) { EditBox box = new EditBox(font, x, y, 110, 18, Component.empty()); box.setValue(value); box.setEditable(ConfigScreenSupport.mayEdit()); values.add(addRenderableWidget(box)); }
    private void save(boolean showStatus) { try {
        int durability = ConfigScreenSupport.parseInt(values.get(0).getValue(), 0, Integer.MAX_VALUE, "Durability"); double reduction = ConfigScreenSupport.parseDouble(values.get(1).getValue(), "Damage reduction"); double toughness = ConfigScreenSupport.parseDouble(values.get(2).getValue(), "Toughness"); int knockback = ConfigScreenSupport.parseInt(values.get(3).getValue(), 0, Integer.MAX_VALUE, "Knockback resistance"); int enchantability = ConfigScreenSupport.parseInt(values.get(4).getValue(), 0, Integer.MAX_VALUE, "Enchantability");
        ArrayList<SetEffect> parsed = new ArrayList<>(); for (String text : effects.getValue().split(";")) { if (text.isBlank()) continue; SetEffect effect = SetEffect.getEffectFromString(text.trim()); if (effect == null) throw new IllegalArgumentException("Unknown effect: " + text.trim()); parsed.add(effect); }
        boolean enabledSnapshot = enabled;
        applySet(durability, reduction, toughness, knockback, enchantability, parsed, enabledSnapshot);
        ConfigScreenSupport.saveAndApply(() -> applySet(durability, reduction, toughness, knockback, enchantability, parsed, enabledSnapshot));
        if (showStatus) status = "Saved and applied immediately"; else if (status.startsWith("Saved")) status = "";
    } catch (IllegalArgumentException exception) { if (showStatus) status = exception.getMessage(); } }
    private void applySet(int durability, double reduction, double toughness, int knockback,
            int enchantability, ArrayList<SetEffect> effects, boolean enabled) {
        set.armorDurability = durability; set.armorDamageReduction = (float) reduction;
        set.armorToughness = (float) toughness; set.armorKnockbackResistance = knockback;
        set.armorEnchantability = enchantability; set.setEffects = new ArrayList<>(effects);
        if (enabled) set.enable(); else set.disable();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 10, 0xFFFFFFFF); String[] labels = {"Base durability", "Damage reduction", "Toughness", "Knockback resistance", "Enchantability"}; for (int i = 0; i < labels.length; i++) graphics.text(font, labels[i], width / 2 - 180, 57 + i * 24, 0xFFDDDDDD);
        graphics.text(font, "Set effects (semicolon separated)", width / 2 - 180, 176, 0xFFDDDDDD); graphics.centeredText(font, "Examples: Speedy;Health Boost(4.0). Blank removes all effects.", width / 2, 212, 0xFF999999); if (!ConfigScreenSupport.mayEdit()) graphics.centeredText(font, Component.literal("This set is controlled by the dedicated server").withStyle(ChatFormatting.RED), width / 2, 230, 0xFFFFFFFF); if (!status.isEmpty()) graphics.centeredText(font, status, width / 2, height - 42, status.startsWith("Saved") ? 0xFF55FF55 : 0xFFFF5555);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
