package twopiradians.blockArmor.client.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import twopiradians.blockArmor.common.BlockArmor;

/** Client-only visual options stored in a user-editable TOML file. */
public final class BlockArmorClientConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("blockarmor-client.toml");
    public static boolean alwaysShowArmorGlint = false;

    private BlockArmorClientConfig() { }

    public static void load() {
        alwaysShowArmorGlint = false;
        if (Files.exists(FILE)) {
            try {
                for (String raw : Files.readAllLines(FILE)) {
                    String line = raw.substring(0, raw.indexOf('#') >= 0 ? raw.indexOf('#') : raw.length()).trim();
                    int equals = line.indexOf('=');
                    if (equals < 0) continue;
                    if (!line.substring(0, equals).trim().equals("always_show_armor_glint")) continue;
                    String value = line.substring(equals + 1).trim();
                    if (value.equalsIgnoreCase("true")) alwaysShowArmorGlint = true;
                    else if (value.equalsIgnoreCase("false")) alwaysShowArmorGlint = false;
                }
            } catch (IOException exception) {
                BlockArmor.LOGGER.warn("Could not read {}", FILE, exception);
            }
        }
        save();
    }

    public static void save() {
        String contents = "# Show the enchantment glint on Block Armor without enchantments.\n"
                + "# false = glint only when enchanted (default)\n"
                + "always_show_armor_glint = " + alwaysShowArmorGlint + "\n";
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, contents);
        } catch (IOException exception) {
            BlockArmor.LOGGER.warn("Could not write {}", FILE, exception);
        }
    }
}
