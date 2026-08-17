package twopiradians.blockArmor.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import twopiradians.blockArmor.common.menu.ArmorEffectTunerMenu;
import net.minecraft.world.item.ItemStack;
import twopiradians.blockArmor.common.item.BlockArmorItem;
import twopiradians.blockArmor.common.item.CombinedArmorData;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/** Client view for the server-owned tuner container. */
public final class ArmorEffectTunerScreen extends AbstractContainerScreen<ArmorEffectTunerMenu> {
    public static final String TITLE_KEY = "container.blockarmor.armor_effect_tuner";
    private static final int ROWS = 6;
    private final List<Button> effectButtons = new ArrayList<>();
    private Button previous;
    private Button next;
    private int page;

    public ArmorEffectTunerScreen(ArmorEffectTunerMenu menu, Inventory inventory, Component title) {
        // The panel has its own contextual prompt; do not render the generic
        // screen title on top of it.
        super(menu, inventory, Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + 40;
        int y = topPos + 8;
        for (int row = 0; row < ROWS; row++) {
            final int buttonRow = row;
            int column = row / 3;
            int compactRow = row % 3;
            effectButtons.add(addRenderableWidget(Button.builder(Component.empty(), button -> {
                int effectIndex = page * ROWS + buttonRow;
                if (minecraft != null && minecraft.gameMode != null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, effectIndex);
            }).bounds(x + column * 68, y + compactRow * 22, 62, 20).build()));
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> { page--; refreshButtons(); })
                .bounds(x, y + 66, 60, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> { page++; refreshButtons(); })
                .bounds(x + 66, y + 66, 60, 20).build());
        refreshButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF202020);
        graphics.fill(leftPos + 7, topPos + 23, leftPos + 25, topPos + 41, 0xFF8B8B8B);
        graphics.fill(leftPos + 8, topPos + 24, leftPos + 24, topPos + 40, 0xFF373737);
    }

    @Override protected void containerTick() {
        super.containerTick();
        refreshButtons();
    }

    private void refreshButtons() {
        ItemStack stack = menu.getSlot(0).getItem();
        List<SetEffect> effects = stack.getItem() instanceof BlockArmorItem
                ? CombinedArmorData.effects(stack) : List.of();
        int pages = Math.max(1, (effects.size() + ROWS - 1) / ROWS);
        page = Math.max(0, Math.min(page, pages - 1));
        for (int row = 0; row < ROWS; row++) {
            int index = page * ROWS + row;
            Button button = effectButtons.get(row);
            button.visible = index < effects.size();
            button.active = button.visible;
            if (!button.visible) continue;
            SetEffect effect = effects.get(index);
            boolean enabled = CombinedArmorData.isEffectEnabled(stack, effect);
            button.setMessage(Component.translatable("setEffect." + effect.name.replace(" ", "_").toLowerCase() + ".name")
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
            button.setTooltip(Tooltip.create(effect.getDescription()));
            button.setTooltipDelay(Duration.ZERO);
        }
        previous.visible = next.visible = pages > 1;
        previous.active = page > 0;
        next.active = page + 1 < pages;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (!(menu.getSlot(0).getItem().getItem() instanceof BlockArmorItem))
            graphics.centeredText(font, Component.translatable("container.blockarmor.armor_effect_tuner.insert"),
                    leftPos + 108, topPos + 31, 0xFFAAAAAA);
    }
}
