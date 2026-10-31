package night.pingbypass.network;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.pingbypass.PingBypassState;
import night.pingbypass.PingBypassTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2CPacketForwarder {
   private static final Logger a = LoggerFactory.getLogger(S2CPacketForwarder.class);
   private final Connection b;
   private final List<Packet<?>> c = new CopyOnWriteArrayList<>();
   private final AtomicBoolean d = new AtomicBoolean(false);
   private volatile boolean e;

   public S2CPacketForwarder(Connection var1) {
      this.b = var1;
      this.e = false;
   }

   public void a() {
      this.e = true;
      PingBypassState.d = true;
      Night.EVENT_BUS.register(this);
      a.info("S2C forwarder started");
   }

   public void b() {
      this.e = false;
      PingBypassState.d = false;
      Night.EVENT_BUS.unregister(this);
      a.info("S2C forwarder stopped");
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (this.e && this.b.isConnected()) {
         Connection var2;
         if (Night.pingBypassServer != null) {
            var2 = Night.pingBypassServer.getBackendConnection();
         } else {
            var2 = null;
         }

         Connection var3 = var2;
         if (var3 != null) {
            Connection var4 = var1.getConnection();
            Connection var5 = var3;
            if (var4 == var5) {
               Packet var6 = var1.getPacket();
               if (!(var6 instanceof ClientboundKeepAlivePacket) && !(var6 instanceof ClientboundPingPacket)) {
                  if (!(var6 instanceof ClientboundTransferPacket) && !(var6 instanceof ClientboundStartConfigurationPacket)) {
                     PacketListener var8;
                     if (Night.pingBypassServer != null && Night.pingBypassServer.getBackendConnection() != null) {
                        var8 = Night.pingBypassServer.getBackendConnection().getPacketListener();
                     } else {
                        var8 = null;
                     }

                     PacketListener var9 = var8;
                     if (var9 != null && var9.protocol() != ConnectionProtocol.PLAY) {
                        String var10 = var6.getClass().getSimpleName();
                        ConnectionProtocol var11 = var9.protocol();
                        a.info("Suppressed {} while proxy's server connection is mid-reconfigure (protocol={})", var10, var11);
                        PingBypassTimer.a();
                        return;
                     }

                     if (var6 instanceof ClientboundBundlePacket) {
                        return;
                     }

                     synchronized (this.d) {
                        if (this.d.get()) {
                           List var13 = this.c;
                           Packet var14 = var6;
                           var13.add(var14);
                        } else {
                           Packet var15 = var6;
                           this.a(var15);
                        }

                        return;
                     }
                  }

                  String var7 = var6.getClass().getSimpleName();
                  a.info("Suppressed {} from being forwarded to client (proxy handles it internally)", var7);
                  PingBypassTimer.a();
                  return;
               }

               return;
            }
         }
      }
   }

   private void a(Packet<?> var1) {
      if (this.b.isConnected()) {
         this.b.send(var1);
      }
   }
}
