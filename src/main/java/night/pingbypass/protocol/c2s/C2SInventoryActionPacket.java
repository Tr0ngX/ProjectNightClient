package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SInventoryActionPacket extends PingBypassPacket {
   private final int actionType;
   private final int slot;
   private final int button;

   public C2SInventoryActionPacket(int var1, int var2, int var3) {
      this.actionType = var1;
      this.slot = var2;
      this.button = var3;
   }

   @Override
   public int getPacketId() {
      return 10;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      int var2 = this.actionType;
      var1.writeVarInt(var2);
      int var3 = this.slot;
      var1.writeVarInt(var3);
      int var4 = this.button;
      var1.writeVarInt(var4);
   }
}
