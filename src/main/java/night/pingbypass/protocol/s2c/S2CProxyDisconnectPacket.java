package night.pingbypass.protocol.s2c;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class S2CProxyDisconnectPacket extends PingBypassPacket {
   private final String reason;

   public S2CProxyDisconnectPacket(String var1) {
      this.reason = var1;
   }

   public S2CProxyDisconnectPacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.reason = var2;
   }

   @Override
   public int getPacketId() {
      return 8;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      String var2 = this.reason;
      var1.writeUtf(var2);
   }

   public String getReason() {
      return this.reason;
   }
}
