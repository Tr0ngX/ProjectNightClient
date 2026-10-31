package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SStayConnectedPacket extends PingBypassPacket {
   private final boolean stayConnected;

   public C2SStayConnectedPacket(FriendlyByteBuf var1) {
      boolean var2 = var1.readBoolean();
      this.stayConnected = var2;
   }

   @Override
   public int getPacketId() {
      return 9;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      boolean var2 = this.stayConnected;
      var1.writeBoolean(var2);
   }

   public boolean isStayConnected() {
      return this.stayConnected;
   }
}
