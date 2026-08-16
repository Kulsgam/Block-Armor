package twopiradians.blockArmor.client.config;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import twopiradians.blockArmor.common.item.ArmorSet;

/** Searchable, paginated browser for generated armor sets. */
final class ArmorSetListScreen extends Screen {
    private static final int ROWS = 8;
    private final Screen parent; private final String query; private final int page; private EditBox search;
    ArmorSetListScreen(Screen parent) { this(parent, "", 0); }
    private ArmorSetListScreen(Screen parent, String query, int page) { super(Component.literal("Block Armor Sets")); this.parent = parent; this.query = query; this.page = page; }
    private List<ArmorSet> matches() {
        String needle = query.toLowerCase(Locale.ROOT).trim();
        return ArmorSet.allSets.stream().filter(set -> needle.isEmpty() || set.registryName.toLowerCase(Locale.ROOT).contains(needle)
                || set.modid.toLowerCase(Locale.ROOT).contains(needle) || ArmorSet.getItemStackDisplayName(set.item, null).getString().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing((ArmorSet set) -> set.modid).thenComparing(set -> set.registryName)).toList();
    }
    @Override protected void init() {
        search = addRenderableWidget(new EditBox(font, width / 2 - 130, 30, 200, 20, Component.literal("Search"))); search.setValue(query);
        addRenderableWidget(Button.builder(Component.literal("Search"), button -> minecraft.setScreen(new ArmorSetListScreen(parent, search.getValue(), 0))).bounds(width / 2 + 74, 30, 56, 20).build());
        List<ArmorSet> sets = matches(); int pages = Math.max(1, (sets.size() + ROWS - 1) / ROWS); int safePage = Math.min(page, pages - 1); int start = safePage * ROWS;
        for (int row = 0; row < ROWS && start + row < sets.size(); row++) {
            ArmorSet set = sets.get(start + row); String label = ArmorSet.getItemStackDisplayName(set.item, null).getString() + " [" + set.modid + "]" + (set.isEnabled() ? "" : " — disabled");
            addRenderableWidget(Button.builder(Component.literal(label), button -> minecraft.setScreen(new ArmorSetEditScreen(this, set))).bounds(width / 2 - 150, 58 + row * 22, 300, 20).build());
        }
        Button previous = addRenderableWidget(Button.builder(Component.literal("Previous"), button -> minecraft.setScreen(new ArmorSetListScreen(parent, query, safePage - 1))).bounds(width / 2 - 150, height - 50, 70, 20).build()); previous.active = safePage > 0;
        Button next = addRenderableWidget(Button.builder(Component.literal("Next"), button -> minecraft.setScreen(new ArmorSetListScreen(parent, query, safePage + 1))).bounds(width / 2 + 80, height - 50, 70, 20).build()); next.active = safePage + 1 < pages;
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> minecraft.setScreen(parent)).bounds(width / 2 - 50, height - 28, 100, 20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        List<ArmorSet> sets = matches(); int pages = Math.max(1, (sets.size() + ROWS - 1) / ROWS);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF); graphics.centeredText(font, "Page " + (Math.min(page, pages - 1) + 1) + "/" + pages + " — " + sets.size() + " sets", width / 2, height - 44, 0xFFAAAAAA);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
