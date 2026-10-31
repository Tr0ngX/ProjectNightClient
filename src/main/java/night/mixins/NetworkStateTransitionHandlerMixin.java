package night.mixins;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import night.pingbypass.PingBypassState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtocolSwapHandler.class)
public interface NetworkStateTransitionHandlerMixin {
   @Inject(method = "handleInboundTerminalPacket", at = @At("HEAD"), cancellable = true)
   private static void handleInboundTerminalPacket(ChannelHandlerContext var0, Packet<?> var1, CallbackInfo var2) {
      if (PingBypassState.b) {
         ChannelHandler var3 = var0.pipeline().get("packet_handler");
         if (var3 instanceof Connection && ((Connection)var3).getReceiving() == PacketFlow.CLIENTBOUND) {
            var2.cancel();
         }
      }
   }

   @Inject(method = "handleOutboundTerminalPacket", at = @At("HEAD"), cancellable = true)
   private static void handleOutboundTerminalPacket(ChannelHandlerContext var0, Packet<?> var1, CallbackInfo var2) {
      if (PingBypassState.b) {
         ChannelHandler var3 = var0.pipeline().get("packet_handler");
         if (var3 instanceof Connection && ((Connection)var3).getReceiving() == PacketFlow.CLIENTBOUND) {
            var2.cancel();
         }
      }
   }
}
