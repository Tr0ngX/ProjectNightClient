package night.pingbypass.handler;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import night.pingbypass.PingBypassServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyHandshakePacketListener implements ServerHandshakePacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyHandshakePacketListener.class);
   private final PingBypassServer b;
   private final Connection c;

   public ProxyHandshakePacketListener(PingBypassServer var1, Connection var2) {
      this.b = var1;
      this.c = var2;
   }

   public void handleIntention(ClientIntentionPacket var1) {
      switch (var1.intention()) {
         case LOGIN:
            this.a(var1);
            break;
         case STATUS:
            this.a();
            break;
         default:
            throw new UnsupportedOperationException("Invalid intention " + var1.intention());
      }
   }

   private void a(ClientIntentionPacket var1) {
      this.c.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
      if (var1.protocolVersion() != 775) {
         MutableComponent var3;
         if (var1.protocolVersion() < 775) {
            Object[] var2 = new Object[]{"26.1.2"};
            var3 = Component.translatable("multiplayer.disconnect.outdated_client", var2);
         } else {
            Object[] var14 = new Object[]{"26.1.2"};
            var3 = Component.translatable("multiplayer.disconnect.incompatible", var14);
         }

         Connection var15 = this.c;
         MutableComponent var16 = var3;
         var15.send(new ClientboundLoginDisconnectPacket(var16));
         Connection var17 = this.c;
         MutableComponent var18 = var3;
         var17.disconnect(var18);
         String var19 = this.c.getLoggableAddress(false);
         Integer var20 = var1.protocolVersion();
         a.info("Rejected connection from {}: wrong protocol version {}", var19, var20);
      } else if (this.b()) {
         MutableComponent var4 = Component.literal("This PingBypass server is already in use!");
         Connection var5 = this.c;
         MutableComponent var6 = var4;
         var5.send(new ClientboundLoginDisconnectPacket(var6));
         Connection var7 = this.c;
         MutableComponent var8 = var4;
         var7.disconnect(var8);
         String var9 = this.c.getLoggableAddress(false);
         a.info("Rejected connection from {}: another client is already connected", var9);
      } else {
         Connection var10 = this.c;
         PingBypassServer var11 = this.b;
         Connection var12 = this.c;
         var10.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ProxyLoginPacketListener(var11, var12));
         String var13 = this.c.getLoggableAddress(false);
         a.info("Client {} starting login", var13);
      }
   }

   private void a() {
      this.c.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
      Connection var1 = this.c;
      PingBypassServer var2 = this.b;
      Connection var3 = this.c;
      var1.setupInboundProtocol(StatusProtocols.SERVERBOUND, new ProxyStatusPacketListener(var2, var3));
      a.debug("Received STATUS handshake, serving proxy status");
   }

   private boolean b() {
      for (Connection var2 : (Iterable<Connection>)(Object)this.b.getConnections()) {
         Connection var3 = this.c;
         if (var2 != var3 && var2.isConnected() && var2.getPacketListener() != null && var2.getPacketListener().protocol() == ConnectionProtocol.PLAY) {
            return true;
         }
      }

      return false;
   }

   public void onDisconnect(DisconnectionDetails var1) {
   }

   public boolean isAcceptingMessages() {
      return this.c.isConnected();
   }
}
