package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SPasswordResponsePacket extends PingBypassPacket {
   private final String password;

   public C2SPasswordResponsePacket(String var1) {
      this.password = var1;
   }

   public C2SPasswordResponsePacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.password = var2;
   }

   @Override
   public int getPacketId() {
      return 2;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      String var2 = this.password;
      var1.writeUtf(var2);
   }
}
