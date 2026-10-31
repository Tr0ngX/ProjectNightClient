package night.pingbypass.protocol.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SMineBlockPacket extends PingBypassPacket {
   private final BlockPos pos;

   public C2SMineBlockPacket(BlockPos var1) {
      this.pos = var1;
   }

   public C2SMineBlockPacket(FriendlyByteBuf var1) {
      int var2 = var1.readInt();
      int var3 = var1.readInt();
      int var4 = var1.readInt();
      this.pos = new BlockPos(var2, var3, var4);
   }

   @Override
   public int getPacketId() {
      return 11;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      int var2 = this.pos.getX();
      var1.writeInt(var2);
      int var3 = this.pos.getY();
      var1.writeInt(var3);
      int var4 = this.pos.getZ();
      var1.writeInt(var4);
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
