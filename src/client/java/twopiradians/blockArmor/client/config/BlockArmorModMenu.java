package twopiradians.blockArmor.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Optional Mod Menu entrypoint. Mod Menu is not required to run Block Armor. */
public final class BlockArmorModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BlockArmorConfigScreen::new;
    }
}
