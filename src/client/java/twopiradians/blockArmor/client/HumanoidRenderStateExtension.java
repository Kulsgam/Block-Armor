package twopiradians.blockArmor.client;

import java.util.UUID;

/** Extra entity identity carried into Minecraft's detached 26.1 render state. */
public interface HumanoidRenderStateExtension {
    UUID blockarmor$getEntityUuid();
    void blockarmor$setEntityUuid(UUID uuid);
}
