package twopiradians.blockArmor.common.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;

import net.fabricmc.loader.api.FabricLoader;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.seteffect.SetEffect;

/**
 * Server-side configuration. The simple key/value format deliberately keeps the
 * old option names stable while avoiding a Forge configuration dependency.
 */
public final class Config {
    // 1.1 builds could persist incomplete effect lists produced during the
    // initial 26.1 port.  Never carry those lists forward: the Forge-derived
    // defaults are the source of truth unless the user edits them again after
    // this migration.
    private static final String CONFIG_VERSION = "1.3";
    // Read by both the render/client thread and the integrated-server thread.
    // Config screens publish changes on the server executor, but the client UI
    // and tooltips must observe the same authoritative value immediately.
    public static volatile int piecesForSet = 2;
    public static volatile boolean effectsUseDurability = false;
    public static volatile double globalToughnessModifier = 1D;
    public static volatile double globalEnchantabilityModifier = 1D;
    public static volatile double globalDamageReductionModifier = 1D;
    public static volatile double globalKnockbackResistanceModifier = 1D;
    public static volatile double globalDurabilityModifier = 1D;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("blockarmor.properties");
    private static Properties loadedValues = new Properties();
    private static boolean fabricFileExisted;

    private Config() {}

    public static void load() {
        Properties values = new Properties();
        fabricFileExisted = Files.exists(FILE);
        if (Files.exists(FILE)) {
            try (var input = Files.newInputStream(FILE)) { values.load(input); }
            catch (IOException exception) { BlockArmor.LOGGER.warn("Could not read {}", FILE, exception); }
        }
        if (!fabricFileExisted) importForgeToml(values, FabricLoader.getInstance().getConfigDir().resolve("blockarmor-server.toml"));
        boolean oldVersion = fabricFileExisted && !CONFIG_VERSION.equals(values.getProperty("Config version"));
        if (oldVersion) values.keySet().removeIf(key -> key.toString().endsWith(".Set_Effects"));
        piecesForSet = integer(values, "Armor pieces required for Set Effects", 2, 1, 4);
        effectsUseDurability = bool(values, "Set Effects use durability", false);
        if (!values.containsKey("Global Toughness Modifier") && values.containsKey("Global Tougness Modifier"))
            values.setProperty("Global Toughness Modifier", values.getProperty("Global Tougness Modifier"));
        globalToughnessModifier = decimal(values, "Global Toughness Modifier", 1D);
        globalEnchantabilityModifier = decimal(values, "Global Enchantability Modifier", 1D);
        globalDamageReductionModifier = decimal(values, "Global Damage Reduction Modifier", 1D);
        globalKnockbackResistanceModifier = decimal(values, "Global Knockback Resistance Modifier", 1D);
        globalDurabilityModifier = decimal(values, "Global Durability Modifier", 1D);
        applyArmorSetOptions(values);
        loadedValues = values;
        save(values);
    }

    private static int integer(Properties values, String key, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(values.getProperty(key, String.valueOf(fallback))))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static double decimal(Properties values, String key, double fallback) {
        try {
            double value = Double.parseDouble(values.getProperty(key, String.valueOf(fallback)));
            return Double.isFinite(value) ? Math.max(0D, Math.min(999999D, value)) : fallback;
        }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static boolean bool(Properties values, String key, boolean fallback) {
        String value = values.getProperty(key);
        if (value == null) return fallback;
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        return fallback;
    }

    private static void save(Properties existing) {
        Properties output = new Properties();
        output.putAll(existing);
        output.setProperty("Config version", CONFIG_VERSION);
        output.setProperty("Armor pieces required for Set Effects", String.valueOf(piecesForSet));
        output.setProperty("Set Effects use durability", String.valueOf(effectsUseDurability));
        output.setProperty("Global Toughness Modifier", String.valueOf(globalToughnessModifier));
        output.setProperty("Global Enchantability Modifier", String.valueOf(globalEnchantabilityModifier));
        output.setProperty("Global Damage Reduction Modifier", String.valueOf(globalDamageReductionModifier));
        output.setProperty("Global Knockback Resistance Modifier", String.valueOf(globalKnockbackResistanceModifier));
        output.setProperty("Global Durability Modifier", String.valueOf(globalDurabilityModifier));

        for (ArmorSet set : ArmorSet.allSets) {
            String prefix = setPrefix(set);
            output.setProperty(prefix + "Enabled", String.valueOf(set.isEnabled()));
            output.setProperty(prefix + "Armor_Durability", String.valueOf(set.armorDurability));
            output.setProperty(prefix + "Armor_Damage_Reduction", String.valueOf(set.armorDamageReduction));
            output.setProperty(prefix + "Armor_Toughness", String.valueOf(set.armorToughness));
            output.setProperty(prefix + "Armor_Knockback_Resistance", String.valueOf(set.armorKnockbackResistance));
            output.setProperty(prefix + "Armor_Enchantability", String.valueOf(set.armorEnchantability));
            output.setProperty(prefix + "Set_Effects", set.setEffects.stream()
                    .map(SetEffect::writeToString).collect(Collectors.joining(";")));
        }
        try (var writer = Files.newBufferedWriter(FILE)) { output.store(writer, "Block Armor server settings"); }
        catch (IOException exception) { BlockArmor.LOGGER.warn("Could not create {}", FILE, exception); }
    }

    /** Applies the per-set options that were previously provided by ForgeConfigSpec. */
    private static void applyArmorSetOptions(Properties values) {
        for (ArmorSet set : ArmorSet.allSets) applyArmorSetOptions(set, values);
    }

    /** Apply already-loaded settings to a set registered after this mod's initializer. */
    public static void applyToLateSet(ArmorSet set) {
        applyArmorSetOptions(set, loadedValues);
        save(loadedValues);
    }

    public static void reload() { load(); }

    /** Persist the values currently applied to the live armor-set objects. */
    public static synchronized void saveCurrent() {
        save(loadedValues);
        fabricFileExisted = true;
    }

    public static boolean importWorldForgeConfig(net.minecraft.server.MinecraftServer server) {
        if (fabricFileExisted) return false;
        Path legacy = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("serverconfig/blockarmor-server.toml");
        Properties values = new Properties();
        if (!importForgeToml(values, legacy)) return false;
        loadedValues.putAll(values);
        applyGlobals(loadedValues);
        applyArmorSetOptions(loadedValues);
        save(loadedValues);
        fabricFileExisted = true;
        return true;
    }

    private static void applyGlobals(Properties values) {
        piecesForSet = integer(values, "Armor pieces required for Set Effects", piecesForSet, 1, 4);
        effectsUseDurability = bool(values, "Set Effects use durability", effectsUseDurability);
        globalToughnessModifier = decimal(values, "Global Toughness Modifier", globalToughnessModifier);
        globalEnchantabilityModifier = decimal(values, "Global Enchantability Modifier", globalEnchantabilityModifier);
        globalDamageReductionModifier = decimal(values, "Global Damage Reduction Modifier", globalDamageReductionModifier);
        globalKnockbackResistanceModifier = decimal(values, "Global Knockback Resistance Modifier", globalKnockbackResistanceModifier);
        globalDurabilityModifier = decimal(values, "Global Durability Modifier", globalDurabilityModifier);
    }

    /** Imports only the known Forge 2.6.9 TOML schema and leaves unknown keys untouched. */
    private static boolean importForgeToml(Properties output, Path file) {
        if (!Files.exists(file)) return false;
        try {
            String section = "";
            for (String raw : Files.readAllLines(file)) {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length()-1).replace("\"", "");
                    continue;
                }
                int equals = line.indexOf('=');
                if (equals < 0) continue;
                String key = line.substring(0, equals).trim().replace("\"", "");
                String value = line.substring(equals+1).trim();
                if (value.startsWith("[") && value.endsWith("]")) {
                    value = value.substring(1,value.length()-1).replace("\"", "").replace(",", ";").replace(" ", "");
                } else value = value.replace("\"", "");
                if (section.startsWith("Armor_Sets.")) output.setProperty(section + "." + key, value);
                else if (key.startsWith("Config version")) output.setProperty("Config version", value);
                else output.setProperty(key.equals("Global Tougness Modifier") ? "Global Toughness Modifier" : key, value);
            }
            BlockArmor.LOGGER.info("Imported legacy Forge Block Armor configuration from {}", file);
            return true;
        } catch (IOException exception) {
            BlockArmor.LOGGER.warn("Could not import legacy Forge config {}", file, exception);
            return false;
        }
    }

    private static void applyArmorSetOptions(ArmorSet set, Properties values) {
        String prefix = setPrefix(set);
        set.armorDurability = integer(values, prefix + "Armor_Durability", set.armorDurability, 0, Integer.MAX_VALUE);
        set.armorDamageReduction = (float) decimal(values, prefix + "Armor_Damage_Reduction", set.armorDamageReduction);
        set.armorToughness = (float) decimal(values, prefix + "Armor_Toughness", set.armorToughness);
        set.armorKnockbackResistance = integer(values, prefix + "Armor_Knockback_Resistance", set.armorKnockbackResistance, 0, Integer.MAX_VALUE);
        set.armorEnchantability = integer(values, prefix + "Armor_Enchantability", set.armorEnchantability, 0, Integer.MAX_VALUE);
        String effectList = values.getProperty(prefix + "Set_Effects", set.defaultSetEffects.stream()
                .map(SetEffect::writeToString).collect(Collectors.joining(";")));
        ArrayList<SetEffect> configuredEffects = new ArrayList<>();
        for (String text : effectList.split(";")) {
            if (text.isBlank()) continue;
            SetEffect effect = SetEffect.getEffectFromString(text.trim());
            if (effect != null) configuredEffects.add(effect);
            else BlockArmor.LOGGER.warn("Invalid set effect '{}' for {}", text, set.registryName);
        }
        // Armor-set data is shared by the logical client and server in an
        // integrated game. Publish a complete replacement so a server tick can
        // never observe this list while it is being cleared and repopulated.
        set.setEffects = configuredEffects;
        set.createMaterial();
        boolean enabled = bool(values, prefix + "Enabled", true);
        if (enabled) set.enable(); else set.disable();
    }

    private static String setPrefix(ArmorSet set) {
        return "Armor_Sets." + set.modid + "." + set.registryName + ".";
    }

    public static void writeNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(piecesForSet);
        buffer.writeBoolean(effectsUseDurability);
        buffer.writeDouble(globalToughnessModifier);
        buffer.writeDouble(globalEnchantabilityModifier);
        buffer.writeDouble(globalDamageReductionModifier);
        buffer.writeDouble(globalKnockbackResistanceModifier);
        buffer.writeDouble(globalDurabilityModifier);
        buffer.writeVarInt(ArmorSet.allSets.size());
        for (ArmorSet set : ArmorSet.allSets) {
            buffer.writeUtf(set.modid);
            buffer.writeUtf(set.registryName);
            buffer.writeBoolean(set.isEnabled());
            buffer.writeVarInt(set.armorDurability);
            buffer.writeFloat(set.armorDamageReduction);
            buffer.writeFloat(set.armorToughness);
            buffer.writeVarInt(set.armorKnockbackResistance);
            buffer.writeVarInt(set.armorEnchantability);
            buffer.writeVarInt(set.setEffects.size());
            for (SetEffect effect : set.setEffects) buffer.writeUtf(effect.writeToString());
        }
    }

    public static void readNetwork(FriendlyByteBuf buffer) {
        int decodedPieces = buffer.readVarInt();
        if (decodedPieces < 1 || decodedPieces > 4) throw new IllegalArgumentException("Invalid set-effect threshold");
        boolean decodedUsesDurability = buffer.readBoolean();
        double decodedToughness = networkDecimal(buffer.readDouble(), "toughness modifier");
        double decodedEnchantability = networkDecimal(buffer.readDouble(), "enchantability modifier");
        double decodedReduction = networkDecimal(buffer.readDouble(), "damage-reduction modifier");
        double decodedKnockbackModifier = networkDecimal(buffer.readDouble(), "knockback modifier");
        double decodedDurabilityModifier = networkDecimal(buffer.readDouble(), "durability modifier");
        int count = buffer.readVarInt();
        if (count < 0 || count > 10000) throw new IllegalArgumentException("Invalid armor-set count");
        List<NetworkSetOptions> updates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String modid = buffer.readUtf(256);
            String registryName = buffer.readUtf(256);
            ArmorSet set = ArmorSet.allSets.stream()
                    .filter(candidate -> candidate.modid.equals(modid) && candidate.registryName.equals(registryName))
                    .findFirst().orElse(null);
            boolean enabled = buffer.readBoolean();
            int durability = buffer.readVarInt();
            float reduction = buffer.readFloat();
            float toughness = buffer.readFloat();
            int knockback = buffer.readVarInt();
            int enchantability = buffer.readVarInt();
            int effects = buffer.readVarInt();
            if (durability < 0 || reduction < 0 || !Float.isFinite(reduction) || toughness < 0 || !Float.isFinite(toughness)
                    || knockback < 0 || enchantability < 0 || effects < 0 || effects > 128)
                throw new IllegalArgumentException("Invalid options for " + modid + ":" + registryName);
            ArrayList<SetEffect> decodedEffects = new ArrayList<>();
            for (int e = 0; e < effects; e++) {
                String encoded = buffer.readUtf(256);
                SetEffect effect = SetEffect.getEffectFromString(encoded);
                if (effect == null) throw new IllegalArgumentException("Unknown set effect " + encoded);
                decodedEffects.add(effect);
            }
            updates.add(new NetworkSetOptions(set, enabled, durability, reduction, toughness, knockback, enchantability, decodedEffects));
        }

        // Apply only after the entire payload has passed validation.
        piecesForSet = decodedPieces;
        effectsUseDurability = decodedUsesDurability;
        globalToughnessModifier = decodedToughness;
        globalEnchantabilityModifier = decodedEnchantability;
        globalDamageReductionModifier = decodedReduction;
        globalKnockbackResistanceModifier = decodedKnockbackModifier;
        globalDurabilityModifier = decodedDurabilityModifier;
        for (NetworkSetOptions update : updates) {
            ArmorSet set = update.set;
            if (set == null) continue;
            set.armorDurability = update.durability;
            set.armorDamageReduction = update.reduction;
            set.armorToughness = update.toughness;
            set.armorKnockbackResistance = update.knockback;
            set.armorEnchantability = update.enchantability;
            // Do not mutate the list currently being traversed by gameplay code.
            // This matters in integrated games, where client and server threads
            // share the static ArmorSet instances.
            set.setEffects = new ArrayList<>(update.effects);
            set.createMaterial();
            if (update.enabled) set.enable(); else set.disable();
        }
    }

    private static double networkDecimal(double value, String name) {
        if (!Double.isFinite(value) || value < 0D || value > 999999D)
            throw new IllegalArgumentException("Invalid " + name);
        return value;
    }

    private record NetworkSetOptions(ArmorSet set, boolean enabled, int durability, float reduction,
                                     float toughness, int knockback, int enchantability,
                                     ArrayList<SetEffect> effects) {}
}
