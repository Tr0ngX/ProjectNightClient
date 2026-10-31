package night.pingbypass.protocol.s2c;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class S2CHeldSlotSyncPacket extends PingBypassPacket {
   private final int slot;

   public S2CHeldSlotSyncPacket(int var1) {
      this.slot = var1;
   }

   public S2CHeldSlotSyncPacket(FriendlyByteBuf var1) {
      int var2 = var1.readVarInt();
      this.slot = var2;
   }

   @Override
   public int getPacketId() {
      return 13;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      int var2 = this.slot;
      var1.writeVarInt(var2);
   }

   public int getSlot() {
      return this.slot;
   }
}
