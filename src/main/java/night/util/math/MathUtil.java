package night.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MathUtil {
   public static double a_double_double_double(double var0, double var2) {
      double var4 = Math.random();
      double var6 = var0;
      double var8 = var2;
      double var10 = var6 - var8;
      double var12 = var4 * var10;
      double var14 = var2;
      return var12 + var14;
   }

   public static double a_double_double_int(double var0, int var2) {
      double var3 = 10.0;
      double var5 = var2;
      int var7 = (int)Math.pow(var3, var5);
      double var8 = var0;
      double var10 = var7;
      double var12 = Math.round(var8 * var10);
      double var14 = var7;
      return var12 / var14;
   }

   public static Vec3 a_Vec3_BlockPos(BlockPos var0) {
      double var1 = var0.getX();
      double var3 = var0.getY();
      double var5 = var0.getZ();
      return new Vec3(var1, var3, var5);
   }

   public static AABB a_AABB_Vec3(Vec3 var0) {
      double var1 = var0.x;
      double var3 = var0.y;
      double var5 = var0.z;
      double var7 = var0.x;
      double var9 = 1.0;
      double var11 = var7 + var9;
      double var13 = var0.y;
      double var15 = 1.0;
      double var17 = var13 + var15;
      double var19 = var0.z;
      double var21 = 1.0;
      double var23 = var19 + var21;
      return new AABB(var1, var3, var5, var11, var17, var23);
   }

   public static Vec3 a_Vec3_Vec3_float(Vec3 var0, float var1) {
      double var2 = var1;
      return var0.scale(var2);
   }

   public static double a(double var0, double var2, double var4) {
      double var6 = var0;
      double var8 = var2;
      double var10 = var0;
      double var12 = var8 - var10;
      double var14 = var4;
      double var16 = var12 * var14;
      return var6 + var16;
   }

   public static float a_float_float(float var0) {
      float var1 = var0 % 360.0F;
      if (var1 < 0.0F) {
         var1 += 360.0F;
      }

      return var1;
   }
}
