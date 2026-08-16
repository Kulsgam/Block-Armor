package twopiradians.blockArmor.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twopiradians.blockArmor.client.gui.OpenGuiEvent;

/** Opens the developer armor display after a chat message from a configured developer. */
@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerChat", at = @At("TAIL"))
    private void blockarmor$openDeveloperDisplay(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        OpenGuiEvent.openFor(packet.sender());
    }
}
