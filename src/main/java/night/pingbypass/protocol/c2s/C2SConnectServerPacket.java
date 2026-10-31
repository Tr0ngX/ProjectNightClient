package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SConnectServerPacket extends PingBypassPacket {
   private final String host;
   private final int port;

   public C2SConnectServerPacket(String var1, int var2) {
      this.host = var1;
      this.port = var2;
   }

   public C2SConnectServerPacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.host = var2;
      int var3 = var1.readVarInt();
      this.port = var3;
   }

   @Override
   public int getPacketId() {
      return 0;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      String var2 = this.host;
      var1.writeUtf(var2);
      int var3 = this.port;
      var1.writeVarInt(var3);
   }
}
