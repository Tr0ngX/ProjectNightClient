package night.pingbypass.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.GameProtocols.Context;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.pingbypass.PingBypassState;
import night.pingbypass.handler.ProxyPlayPacketListener;
import night.pingbypass.world.WorldStateReplayer;
import night.util.world.PacketAndRenderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendTransferRewirer {
   private static final Logger a = LoggerFactory.getLogger(BackendTransferRewirer.class);

   public BackendTransferRewirer() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         if (PingBypassState.d) {
            if (Night.pingBypassServer != null) {
               Minecraft var2 = Minecraft.getInstance();
               if (var2.getConnection() != null && var2.player != null && var2.level != null) {
                  Connection var3 = var2.getConnection().getConnection();
                  Connection var4 = Night.pingBypassServer.getBackendConnection();
                  Connection var5 = var3;
                  Connection var6 = var4;
                  if (var5 != var6 && var3.isConnected()) {
                     Integer var7 = Night.pingBypassServer.getConnections().size();
                     a.warn("[PB] Proxy's own server connection changed underneath us (transfer/backend switch) -- rewiring {} connected client(s)", var7);
                     Connection var8 = var3;
                     Night.pingBypassServer.setBackendConnection(var8);
                     RegistryAccess var9 = var2.level.registryAccess();

                     for (Connection var11 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
                        if (var11.isConnected()) {
                           PacketListener var12 = var11.getPacketListener();
                           if (var12 instanceof ProxyPlayPacketListener) {
                              GameProfile var13 = ((ProxyPlayPacketListener)var12).a();

                              try {
                                 Connection var14 = var11;
                                 ProtocolInfo var15 = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var9));
                                 var14.setupOutboundProtocol(var15);
                                 Connection var16 = var11;
                                 S2CPacketForwarder var17 = new S2CPacketForwarder(var16);
                                 var17.a();
                                 Connection var18 = var11;
                                 RegistryAccess var19 = var9;
                                 int var20 = WorldStateReplayer.a_int_Connection_RegistryAccess(var18, var19);
                                 Connection var21 = var11;
                                 GameProfile var22 = var13;
                                 S2CPacketForwarder var23 = var17;
                                 int var24 = var20;
                                 ProxyPlayPacketListener var25 = new ProxyPlayPacketListener(Night.pingBypassServer, var21, var22, var23, var24);
                                 Connection var26 = var11;
                                 Context var27 = PacketAndRenderUtil.c();
                                 ProtocolInfo var28 = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var9), var27);
                                 ProxyPlayPacketListener var29 = var25;
                                 var26.setupInboundProtocol(var28, var29);
                                 String var30 = var13.name();
                                 a.info("[PB] Rewired {} onto the new server connection after transfer", var30);
                              } catch (Exception var34) {
                                 String var32 = var13.name();
                                 a.error("[PB] Failed to rewire {} after transfer", var32, var34);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
