package twopiradians.blockArmor.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twopiradians.blockArmor.client.gui.OpenGuiEvent;

/** Restores the Forge client-chat bridge used by the dormant developer display GUI. */
@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handleChat", at = @At("TAIL"))
    private void blockarmor$openDeveloperDisplay(ClientboundChatPacket packet, CallbackInfo ci) {
        OpenGuiEvent.openFor(packet.getSender());
    }
}
