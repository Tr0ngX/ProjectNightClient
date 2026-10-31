package night.pingbypass.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.PingBypassServer;
import night.pingbypass.protocol.c2s.C2SConnectServerPacket;
import night.pingbypass.protocol.c2s.C2SModuleTogglePacket;
import night.pingbypass.protocol.c2s.C2SPasswordResponsePacket;
import night.pingbypass.protocol.c2s.C2SSettingUpdatePacket;
import night.pingbypass.protocol.c2s.C2SStayConnectedPacket;
import night.pingbypass.protocol.s2c.S2CModuleStatePacket;
import night.pingbypass.protocol.s2c.S2CPasswordRequestPacket;
import night.pingbypass.protocol.s2c.S2CProxyDisconnectPacket;
import night.pingbypass.protocol.s2c.S2CSettingSyncPacket;
import night.pingbypass.protocol.s2c.S2CStatusMessagePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassProtocol {
   private static final Logger a = LoggerFactory.getLogger(PingBypassProtocol.class);
   private final Map<Integer, Function<FriendlyByteBuf, PingBypassPacket>> b = new HashMap<>();
   private final Map<Integer, PacketHandler<?>> c = new HashMap<>();

   public PingBypassProtocol() {
      this.a();
   }

   public <T extends PingBypassPacket> void a_void_int_Function_PacketHandler(int var1, Function<FriendlyByteBuf, T> var2, PacketHandler<T> var3) {
      Map var4 = this.b;
      Integer var5 = var1;
      var4.put(var5, var2);
      Map var6 = this.c;
      Integer var7 = var1;
      var6.put(var7, var3);
   }

   public <T extends PingBypassPacket> void a(int var1, Function<FriendlyByteBuf, T> var2) {
      Map var3 = this.b;
      Integer var4 = var1;
      var3.put(var4, var2);
   }

   private void a() {
      this.a(0, C2SConnectServerPacket::new);
      this.a(1, S2CPasswordRequestPacket::new);
      this.a(2, C2SPasswordResponsePacket::new);
      this.a(3, C2SModuleTogglePacket::new);
      this.a(4, C2SSettingUpdatePacket::new);
      this.a(5, S2CModuleStatePacket::new);
      this.a(6, S2CSettingSyncPacket::new);
      this.a(7, S2CStatusMessagePacket::new);
      this.a(8, S2CProxyDisconnectPacket::new);
      this.a(9, C2SStayConnectedPacket::new);
   }

   public void a(PingBypassServer var1) {
      this.a_void_int_Function_PacketHandler(9, C2SStayConnectedPacket::new, (var1x, var2) -> {
         boolean var3 = var1x.isStayConnected();
         var1.setStayConnected(var3);
         Boolean var4 = var1x.isStayConnected();
         a.info("[PbProtocol] Stay Connected set to {} via protocol handler", var4);
      });
   }
}
