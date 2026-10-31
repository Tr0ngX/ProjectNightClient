package night.pingbypass.protocol.s2c;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class S2CPasswordRequestPacket extends PingBypassPacket {
   public S2CPasswordRequestPacket(FriendlyByteBuf var1) {
   }

   @Override
   public int getPacketId() {
      return 1;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
   }
}
