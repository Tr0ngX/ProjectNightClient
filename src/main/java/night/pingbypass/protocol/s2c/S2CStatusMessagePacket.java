package night.pingbypass.protocol.s2c;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class S2CStatusMessagePacket extends PingBypassPacket {
   private final String message;

   public S2CStatusMessagePacket(String var1) {
      this.message = var1;
   }

   public S2CStatusMessagePacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.message = var2;
   }

   @Override
   public int getPacketId() {
      return 7;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      String var2 = this.message;
      var1.writeUtf(var2);
   }

   public String getMessage() {
      return this.message;
   }
}
