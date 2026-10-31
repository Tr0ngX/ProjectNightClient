package night.pingbypass.handler;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import night.pingbypass.PingBypassServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyStatusPacketListener implements ServerStatusPacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyStatusPacketListener.class);
   private final PingBypassServer b;
   private final Connection c;
   private boolean d;

   public ProxyStatusPacketListener(PingBypassServer var1, Connection var2) {
      this.b = var1;
      this.c = var2;
   }

   public void handleStatusRequest(ServerboundStatusRequestPacket var1) {
      if (this.d) {
         Connection var2 = this.c;
         MutableComponent var3 = Component.literal("Status already sent");
         var2.disconnect(var3);
      } else {
         this.d = true;
         Component var4 = this.a();
         ServerStatus var5 = new ServerStatus(var4, Optional.empty(), Optional.empty(), Optional.empty(), false);
         Connection var6 = this.c;
         ServerStatus var7 = var5;
         var6.send(new ClientboundStatusResponsePacket(var7));
      }
   }

   public void handlePingRequest(ServerboundPingRequestPacket var1) {
      Connection var2 = this.c;
      long var3 = var1.getTime();
      var2.send(new ClientboundPongResponsePacket(var3));
      Connection var5 = this.c;
      MutableComponent var6 = Component.literal("Ping done");
      var5.disconnect(var6);
   }

   public void onDisconnect(DisconnectionDetails var1) {
   }

   public boolean isAcceptingMessages() {
      return this.c.isConnected();
   }

   private Component a() {
      Minecraft var1 = Minecraft.getInstance();
      StringBuilder var2 = new StringBuilder();
      var2.append("\u00a7dNight PingBypass\u00a7r\n");
      if (var1.getConnection() != null && var1.player != null && var1.level != null) {
         var1.getConnection().serverBrand();
         var2.append("\u00a7aConnected\u00a7r");
         if (this.b.getBackendConnection() != null) {
            StringBuilder var3 = var2.append(" \u2014 ");
            String var4;
            if (var1.getCurrentServer() != null) {
               var4 = var1.getCurrentServer().ip;
            } else {
               var4 = "unknown";
            }

            var3.append(var4);
         }
      } else {
         var2.append("\u00a77Idle\u00a7r \u2014 not connected to any server");
      }

      return Component.literal(var2.toString());
   }
}
