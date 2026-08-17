package twopiradians.blockArmor.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import twopiradians.blockArmor.common.menu.ArmorEffectTunerMenu;

/** Reusable portable editor for per-stack Block Armor effects. */
public final class ArmorEffectTunerItem extends Item {
    public ArmorEffectTunerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new ArmorEffectTunerMenu(id, inventory),
                    Component.translatable("container.blockarmor.armor_effect_tuner")));
        }
        return InteractionResult.SUCCESS;
    }
}
