package night.pingbypass.protocol.s2c;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;
import org.jetbrains.annotations.Nullable;

public class S2CRenderPlacePosPacket extends PingBypassPacket {
   @Nullable
   private final BlockPos pos;

   public S2CRenderPlacePosPacket(@Nullable BlockPos var1) {
      this.pos = var1;
   }

   public S2CRenderPlacePosPacket(FriendlyByteBuf var1) {
      if (var1.readBoolean()) {
         int var2 = var1.readInt();
         int var3 = var1.readInt();
         int var4 = var1.readInt();
         this.pos = new BlockPos(var2, var3, var4);
      } else {
         this.pos = null;
      }
   }

   @Override
   public int getPacketId() {
      return 10;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      boolean var2;
      if (this.pos != null) {
         var2 = true;
      } else {
         var2 = false;
      }

      var1.writeBoolean(var2);
      if (this.pos != null) {
         int var3 = this.pos.getX();
         var1.writeInt(var3);
         int var4 = this.pos.getY();
         var1.writeInt(var4);
         int var5 = this.pos.getZ();
         var1.writeInt(var5);
      }
   }

   @Nullable
   public BlockPos getPos() {
      return this.pos;
   }
}
