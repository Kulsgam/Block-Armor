package twopiradians.blockArmor.client.config;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import twopiradians.blockArmor.common.item.ArmorSet;

/** Searchable browser, necessary because a typical installation generates hundreds of sets. */
final class ArmorSetListScreen extends Screen {
    private static final int ROWS = 8;
    private final Screen parent;
    private final String query;
    private final int page;
    private EditBox search;

    ArmorSetListScreen(Screen parent) { this(parent, "", 0); }

    private ArmorSetListScreen(Screen parent, String query, int page) {
        super(new TextComponent("Block Armor Sets"));
        this.parent = parent;
        this.query = query;
        this.page = page;
    }

    private List<ArmorSet> matches() {
        String needle = query.toLowerCase(Locale.ROOT).trim();
        return ArmorSet.allSets.stream()
                .filter(set -> needle.isEmpty() || set.registryName.toLowerCase(Locale.ROOT).contains(needle)
                        || set.modid.toLowerCase(Locale.ROOT).contains(needle)
                        || ArmorSet.getItemStackDisplayName(set.item, null).getString().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing((ArmorSet set) -> set.modid).thenComparing(set -> set.registryName))
                .toList();
    }

    @Override
    protected void init() {
        search = addRenderableWidget(new EditBox(font, width / 2 - 130, 30, 200, 20, new TextComponent("Search")));
        search.setValue(query);
        addRenderableWidget(new Button(width / 2 + 74, 30, 56, 20, new TextComponent("Search"),
                button -> minecraft.setScreen(new ArmorSetListScreen(parent, search.getValue(), 0))));

        List<ArmorSet> sets = matches();
        int pages = Math.max(1, (sets.size() + ROWS - 1) / ROWS);
        int safePage = Math.min(page, pages - 1);
        int start = safePage * ROWS;
        for (int row = 0; row < ROWS && start + row < sets.size(); row++) {
            ArmorSet set = sets.get(start + row);
            String label = ArmorSet.getItemStackDisplayName(set.item, null).getString() + " [" + set.modid + "]"
                    + (set.isEnabled() ? "" : " — disabled");
            addRenderableWidget(new Button(width / 2 - 150, 58 + row * 22, 300, 20,
                    new TextComponent(label), button -> minecraft.setScreen(new ArmorSetEditScreen(this, set))));
        }
        Button previous = addRenderableWidget(new Button(width / 2 - 150, height - 50, 70, 20,
                new TextComponent("Previous"), button -> minecraft.setScreen(new ArmorSetListScreen(parent, query, safePage - 1))));
        previous.active = safePage > 0;
        Button next = addRenderableWidget(new Button(width / 2 + 80, height - 50, 70, 20,
                new TextComponent("Next"), button -> minecraft.setScreen(new ArmorSetListScreen(parent, query, safePage + 1))));
        next.active = safePage + 1 < pages;
        addRenderableWidget(new Button(width / 2 - 50, height - 28, 100, 20,
                new TextComponent("Back"), button -> minecraft.setScreen(parent)));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        List<ArmorSet> sets = matches();
        int pages = Math.max(1, (sets.size() + ROWS - 1) / ROWS);
        drawCenteredString(pose, font, title, width / 2, 12, 0xFFFFFF);
        drawCenteredString(pose, font, "Page " + (Math.min(page, pages - 1) + 1) + "/" + pages + " — " + sets.size() + " sets",
                width / 2, height - 44, 0xAAAAAA);
        super.render(pose, mouseX, mouseY, partialTick);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
