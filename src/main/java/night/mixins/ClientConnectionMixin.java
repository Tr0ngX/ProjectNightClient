package night.mixins;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import night.Night;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.manager.RotationManager;
import night.module.impl.misc.FakePlayer;
import night.module.impl.misc.NoPacketKick;
import night.pingbypass.PingBypassTimer;
import night.util.world.PacketAndRenderUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionMixin {
   private static final Logger LOGGER = LoggerFactory.getLogger("Night/Disconnect");
   @Unique
   private static boolean night$invalidMoveLogged;
   private static final long PROTOCOL_TRANSITION_WINDOW_MS = 1000L;

   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
   private void send$HEAD(Packet<?> var1, @Nullable ChannelFutureListener var2, boolean var3, CallbackInfo var4) {
      if (var1 instanceof ClientIntentionPacket) {
         Connection var5 = (Connection)(Object)(Object)this;
         PacketListener var6 = var5.getPacketListener();
         if (var6 != null && var6.protocol() != ConnectionProtocol.HANDSHAKING) {
            LOGGER.error(
               "[PB] Caught an outgoing ClientIntentionPacket on {} -- this connection is already past handshake, this send will crash it",
               var5.getLoggableAddress(false),
               new Throwable("call site")
            );
         }
      }

      if (!RotationManager.flushingQueue) {
         PacketSendEvent var7 = new PacketSendEvent(var1);
         Night.EVENT_BUS.post(var7);
         if (var7.isCancelled()) {
            var4.cancel();
         } else {
            String var8 = PacketAndRenderUtil.a_String_Packet(var1);
            if (var8 != null) {
               if (!night$invalidMoveLogged) {
                  night$invalidMoveLogged = true;
                  LOGGER.error(
                     "[Night] dropped invalid movement packet ({}); player rot={},{}",
                     new Object[]{
                        var8,
                        Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getYRot() : 0.0F,
                        Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getXRot() : 0.0F,
                        new Throwable("call site")
                     }
                  );
               }

               var4.cancel();
            }
         }
      }
   }

   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("TAIL"), cancellable = true)
   private void send$TAIL(Packet<?> var1, @Nullable ChannelFutureListener var2, boolean var3, CallbackInfo var4) {
      Night.EVENT_BUS.post(new PacketSendEvent.Post(var1));
   }

   @Inject(method = "doSendPacket", at = @At("HEAD"), cancellable = true)
   private void doSendPacket$HEAD(Packet<?> var1, @Nullable ChannelFutureListener var2, boolean var3, CallbackInfo var4) {
      boolean var5 = System.currentTimeMillis() - PingBypassTimer.b() < 1000L;
      String var6 = var1.getClass().getPackageName();
      boolean var7 = var6.equals("net.minecraft.network.protocol.game") || var6.equals("net.minecraft.network.protocol.handshake");
      if (var5 && var7) {
         LOGGER.warn(
            "[PB] Dropped {} queued right as the connection was mid protocol-transition -- would have crashed the session", var1.getClass().getSimpleName()
         );
         var4.cancel();
      } else {
         if (var1 instanceof ClientIntentionPacket) {
            PacketListener var8 = ((Connection)(Object)(Object)this).getPacketListener();
            if (var8 != null && var8.protocol() != ConnectionProtocol.HANDSHAKING) {
               return;
            }
         }
      }
   }

   @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
   private void channelRead0(ChannelHandlerContext var1, Packet<?> var2, CallbackInfo var3) {
      PacketReceiveEvent var4 = new PacketReceiveEvent(var2, (Connection)(Object)(Object)this);
      Night.EVENT_BUS.post(var4);
      if (var2 instanceof ClientboundBundlePacket) {
         for (Packet var6 : (Iterable<Packet>)(Object)((ClientboundBundlePacket)var2).subPackets()) {
            Night.EVENT_BUS.post(new PacketReceiveEvent(var6, (Connection)(Object)(Object)this));
         }
      }

      if (var4.isCancelled()) {
         var3.cancel();
      }
   }

   @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
   private void disconnect(DisconnectionDetails var1, CallbackInfo var2) {
      Connection var3 = (Connection)(Object)(Object)this;
      String var4 = var1.reason() != null ? var1.reason().getString() : "unknown";
      if (Night.pingBypassConfig != null
         && Night.pingBypassConfig.isServerMode()
         && Night.pingBypassServer != null
         && var3 == Night.pingBypassServer.getBackendConnection()) {
         LOGGER.warn("[PB] Proxy's connection to the real server dropped: {}", var4);
         FakePlayer var5 = Night.moduleManager.getModule(FakePlayer.class);
         if (var5 != null && var5.isEnabled()) {
            var5.setEnabled(false);
         }
      } else {
         LOGGER.info("Connection disconnected ({}): {}", var3.getLoggableAddress(false), var4);
      }

      Night.EVENT_BUS.post(new ClientDisconnectEvent());
   }

   @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
   private void exceptionCaught$HEAD(ChannelHandlerContext var1, Throwable var2, CallbackInfo var3) {
      NoPacketKick var4 = Night.moduleManager != null ? Night.moduleManager.getModule(NoPacketKick.class) : null;
      if (var4 != null && var4.isEnabled() && var4.a(var2)) {
         var4.b(var2);
         var3.cancel();
      }
   }
}
