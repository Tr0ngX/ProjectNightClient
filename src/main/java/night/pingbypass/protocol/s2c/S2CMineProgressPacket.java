package night.pingbypass.protocol.s2c;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;
import org.jetbrains.annotations.Nullable;

public class S2CMineProgressPacket extends PingBypassPacket {
   private final boolean hasPrimary;
   @Nullable
   private final BlockPos primaryPos;
   private final float primaryProgress;
   private final boolean hasSecondary;
   @Nullable
   private final BlockPos secondaryPos;
   private final float secondaryProgress;

   public S2CMineProgressPacket(@Nullable BlockPos var1, float var2, @Nullable BlockPos var3, float var4) {
      boolean var5;
      if (var1 != null) {
         var5 = true;
      } else {
         var5 = false;
      }

      this.hasPrimary = var5;
      this.primaryPos = var1;
      this.primaryProgress = var2;
      boolean var6;
      if (var3 != null) {
         var6 = true;
      } else {
         var6 = false;
      }

      this.hasSecondary = var6;
      this.secondaryPos = var3;
      this.secondaryProgress = var4;
   }

   public S2CMineProgressPacket(FriendlyByteBuf var1) {
      boolean var2 = var1.readBoolean();
      this.hasPrimary = var2;
      if (this.hasPrimary) {
         int var3 = var1.readInt();
         int var4 = var1.readInt();
         int var5 = var1.readInt();
         this.primaryPos = new BlockPos(var3, var4, var5);
         float var6 = var1.readFloat();
         this.primaryProgress = var6;
      } else {
         this.primaryPos = null;
         this.primaryProgress = 0.0F;
      }

      boolean var7 = var1.readBoolean();
      this.hasSecondary = var7;
      if (this.hasSecondary) {
         int var8 = var1.readInt();
         int var9 = var1.readInt();
         int var10 = var1.readInt();
         this.secondaryPos = new BlockPos(var8, var9, var10);
         float var11 = var1.readFloat();
         this.secondaryProgress = var11;
      } else {
         this.secondaryPos = null;
         this.secondaryProgress = 0.0F;
      }
   }

   @Override
   public int getPacketId() {
      return 12;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      boolean var2 = this.hasPrimary;
      var1.writeBoolean(var2);
      if (this.hasPrimary) {
         int var3 = this.primaryPos.getX();
         var1.writeInt(var3);
         int var4 = this.primaryPos.getY();
         var1.writeInt(var4);
         int var5 = this.primaryPos.getZ();
         var1.writeInt(var5);
         float var6 = this.primaryProgress;
         var1.writeFloat(var6);
      }

      boolean var7 = this.hasSecondary;
      var1.writeBoolean(var7);
      if (this.hasSecondary) {
         int var8 = this.secondaryPos.getX();
         var1.writeInt(var8);
         int var9 = this.secondaryPos.getY();
         var1.writeInt(var9);
         int var10 = this.secondaryPos.getZ();
         var1.writeInt(var10);
         float var11 = this.secondaryProgress;
         var1.writeFloat(var11);
      }
   }

   @Nullable
   public BlockPos getPrimaryPos() {
      return this.primaryPos;
   }

   public float getPrimaryProgress() {
      return this.primaryProgress;
   }

   @Nullable
   public BlockPos getSecondaryPos() {
      return this.secondaryPos;
   }

   public float getSecondaryProgress() {
      return this.secondaryProgress;
   }
}
