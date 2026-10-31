package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.GameProtocols.Context;
import night.Night;
import night.pingbypass.PingBypassServer;
import night.pingbypass.world.VanillaRegistryCache;
import night.util.world.PacketAndRenderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyConfigurationPacketListener implements TickablePacketListener, ServerConfigurationPacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyConfigurationPacketListener.class);
   private static final Context b = PacketAndRenderUtil.c();
   private final PingBypassServer c;
   private final Connection d;
   private final GameProfile e;
   private boolean f;

   public ProxyConfigurationPacketListener(PingBypassServer var1, Connection var2, GameProfile var3) {
      this.c = var1;
      this.d = var2;
      this.e = var3;
   }

   public void tick() {
      if (!this.f) {
         this.f = true;
         VanillaRegistryCache var1 = this.c.getRegistryCache();
         if (var1.b()) {
            Connection var2 = this.d;
            List var3 = var1.d();
            var2.send(new ClientboundSelectKnownPacks(var3));
         } else {
            Connection var4 = this.d;
            List var5 = List.of();
            var4.send(new ClientboundSelectKnownPacks(var5));
         }

         String var6 = this.e.name();
         a.info("Sent SelectKnownPacks to {}", var6);
      }
   }

   public void handleSelectKnownPacks(ServerboundSelectKnownPacks var1) {
      String var2 = this.e.name();
      a.info("Client {} responded with known packs, sending registries", var2);
      VanillaRegistryCache var3 = this.c.getRegistryCache();
      if (var3.b()) {
         for (Packet var5 : (Iterable<Packet>)(Object)var3.c()) {
            Connection var6 = this.d;
            Packet var7 = var5;
            var6.send(var7);
         }
      }

      this.d.send(ClientboundFinishConfigurationPacket.INSTANCE);
      Logger var8 = a;
      int var9;
      if (var3.b()) {
         var9 = var3.c().size();
      } else {
         var9 = 0;
      }

      Integer var10 = var9;
      String var11 = this.e.name();
      var8.info("Sent {} registry packets and FINISH_CONFIGURATION to {}", var10, var11);
   }

   public void handleConfigurationFinished(ServerboundFinishConfigurationPacket var1) {
      String var2 = this.e.name();
      a.info("Client {} acknowledged FINISH_CONFIGURATION, transitioning to PLAY", var2);
      VanillaRegistryCache var3 = this.c.getRegistryCache();
      Object var4;
      if (var3.b()) {
         var4 = var3.e();
      } else {
         var4 = RegistryAccess.EMPTY;
      }

      Object var5 = var4;
      Connection var6 = this.d;
      ProtocolInfo var7 = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator((RegistryAccess)var5));
      var6.setupOutboundProtocol(var7);
      if (Night.pingBypassConfig.hasPassword()) {
         Connection var8 = this.d;
         ProtocolInfo var9 = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), b);
         PingBypassServer var10 = this.c;
         Connection var11 = this.d;
         GameProfile var12 = this.e;
         Object var13 = var5;
         var8.setupInboundProtocol(var9, new ProxyPasswordPacketListener(var10, var11, var12, (RegistryAccess)var13));
         String var14 = this.e.name();
         a.info("Transitioning {} to password verification", var14);
      } else {
         Connection var15 = this.d;
         ProtocolInfo var16 = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), b);
         PingBypassServer var17 = this.c;
         Connection var18 = this.d;
         GameProfile var19 = this.e;
         Object var20 = var5;
         var15.setupInboundProtocol(var16, new ProxyWaitingPacketListener(var17, var18, var19, (RegistryAccess)var20));
         String var21 = this.e.name();
         a.info("Transitioning {} to waiting state", var21);
      }
   }

   public void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket var1) {
   }

   public void onDisconnect(DisconnectionDetails var1) {
      String var2 = this.e.name();
      Component var3 = var1.reason();
      a.info("Client {} disconnected during configuration: {}", var2, var3);
   }

   public boolean isAcceptingMessages() {
      return this.d.isConnected();
   }

   public void handleClientInformation(ServerboundClientInformationPacket var1) {
   }

   public void handleKeepAlive(ServerboundKeepAlivePacket var1) {
   }

   public void handlePong(ServerboundPongPacket var1) {
   }

   public void handleResourcePackResponse(ServerboundResourcePackPacket var1) {
   }

   public void handleCookieResponse(ServerboundCookieResponsePacket var1) {
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket var1) {
   }

   public void handleCustomClickAction(ServerboundCustomClickActionPacket var1) {
   }
}
