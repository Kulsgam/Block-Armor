package twopiradians.blockArmor.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twopiradians.blockArmor.common.input.SetEffectKeyState;
import twopiradians.blockArmor.network.BlockArmorNetwork;

/** Shared constants and services. Loader entrypoints live in the Fabric-specific packages. */
public final class BlockArmor {
    public static final String MODNAME = "Block Armor";
    public static final String MODID = "blockarmor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final BlockArmorNetwork NETWORK = new BlockArmorNetwork();
    public static final SetEffectKeyState key = new SetEffectKeyState();

    private BlockArmor() {}
}
