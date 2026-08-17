package twopiradians.blockArmor.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import twopiradians.blockArmor.common.effect.EffectBlacklist;
import twopiradians.blockArmor.common.seteffect.SetEffect;
import twopiradians.blockArmor.network.EffectBlacklistTogglePayload;

/** Searchable player-local blacklist; disabled effects are grouped first. */
public final class EffectBlacklistScreen extends Screen {
    private static final int ROWS = 12;
    private final Screen parent;
    private EditBox search;
    private int page;
    private final List<Button> entries = new ArrayList<>();
    private Button previous;
    private Button next;

    public EffectBlacklistScreen(Screen parent) {
        super(Component.translatable("screen.blockarmor.effect_blacklist"));
        this.parent = parent;
    }

    private List<SetEffect> effects() {
        String query = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT).trim();
        Set<String> disabled = EffectBlacklist.client(minecraft.player.getUUID());
        return SetEffect.SET_EFFECTS.stream().filter(effect -> query.isEmpty()
                        || effect.name.toLowerCase(Locale.ROOT).contains(query)
                        || description(effect).toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing((SetEffect effect) -> !disabled.contains(EffectBlacklist.id(effect)))
                        .thenComparing(effect -> effect.name))
                .toList();
    }

    private static String description(SetEffect effect) {
        return descriptionComponent(effect).getString();
    }

    private static Component descriptionComponent(SetEffect effect) {
        if (effect.name.equals("Time Control"))
            return Component.literal("Rewinds, stops, or accelerates time depending on the armor block.");
        try { return effect.getDescription(); }
        catch (RuntimeException ignored) { return Component.empty(); }
    }

    @Override protected void init() {
        search = addRenderableWidget(new EditBox(font, width / 2 - 150, 28, 240, 20,
                Component.translatable("screen.blockarmor.effect_blacklist.search")));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 + 96, 28, 54, 20).build());
        for (int row = 0; row < ROWS; row++) {
            final int index = row;
            entries.add(addRenderableWidget(Button.builder(Component.empty(), button -> toggle(index))
                    .bounds(width / 2 - 150, 58 + row * 22, 300, 20).build()));
        }
        previous = addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> { page--; refreshEntries(); })
                .bounds(width / 2 - 150, height - 30, 70, 20).build());
        next = addRenderableWidget(Button.builder(Component.translatable("gui.next"), button -> { page++; refreshEntries(); })
                .bounds(width / 2 + 80, height - 30, 70, 20).build());
        refreshEntries();
    }

    private void toggle(int row) {
        List<SetEffect> effects = effects();
        int index = page * ROWS + row;
        if (index < 0 || index >= effects.size() || minecraft.player == null) return;
        SetEffect effect = effects.get(index);
        Set<String> disabled = EffectBlacklist.client(minecraft.player.getUUID());
        boolean disable = !disabled.contains(EffectBlacklist.id(effect));
        java.util.HashSet<String> updated = new java.util.HashSet<>(disabled);
        if (disable) updated.add(EffectBlacklist.id(effect)); else updated.remove(EffectBlacklist.id(effect));
        EffectBlacklist.setClient(minecraft.player.getUUID(), updated);
        ClientPlayNetworking.send(new EffectBlacklistTogglePayload(EffectBlacklist.id(effect), disable));
        refreshEntries();
    }

    private void refreshEntries() {
        if (entries.isEmpty()) return;
        List<SetEffect> effects = effects();
        int pages = Math.max(1, (effects.size() + ROWS - 1) / ROWS);
        page = Math.max(0, Math.min(page, pages - 1));
        if (previous != null) {
            previous.active = page > 0;
            next.active = page + 1 < pages;
        }
        Set<String> disabled = minecraft.player == null ? Set.of() : EffectBlacklist.client(minecraft.player.getUUID());
        for (int row = 0; row < ROWS; row++) {
            int index = page * ROWS + row;
            Button button = entries.get(row);
            button.visible = index < effects.size();
            button.active = button.visible;
            if (!button.visible) continue;
            SetEffect effect = effects.get(index);
            boolean isDisabled = disabled.contains(EffectBlacklist.id(effect));
            button.setMessage(Component.translatable("setEffect." + effect.name.replace(" ", "_").toLowerCase(Locale.ROOT) + ".name")
                    .withStyle(isDisabled ? ChatFormatting.RED : ChatFormatting.GREEN));
            Component tooltip = descriptionComponent(effect);
            button.setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override public void tick() { super.tick(); refreshEntries(); }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        List<SetEffect> effects = effects();
        int pages = Math.max(1, (effects.size() + ROWS - 1) / ROWS);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("screen.blockarmor.effect_blacklist.disabled_first"), width / 2, height - 54, 0xFFAAAAAA);
        graphics.centeredText(font, Component.literal("Page " + (page + 1) + "/" + pages), width / 2, height - 42, 0xFFAAAAAA);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

}
