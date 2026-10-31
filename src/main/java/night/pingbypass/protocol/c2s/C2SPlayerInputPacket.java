package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SPlayerInputPacket extends PingBypassPacket {
   private final boolean inventoryOpen;

   public C2SPlayerInputPacket(boolean var1) {
      this.inventoryOpen = var1;
   }

   public C2SPlayerInputPacket(FriendlyByteBuf var1) {
      boolean var2 = var1.readBoolean();
      this.inventoryOpen = var2;
   }

   @Override
   public int getPacketId() {
      return 15;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      boolean var2 = this.inventoryOpen;
      var1.writeBoolean(var2);
   }

   public boolean isInventoryOpen() {
      return this.inventoryOpen;
   }
}
