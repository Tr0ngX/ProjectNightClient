package night.util.math;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class RotationJitterUtil {
   private final Map<BlockPos, Vec3> a = new HashMap<>();
   private final Map<BlockPos, Long> b = new HashMap<>();
   private final Map<Integer, Vec3> c = new HashMap<>();
   private final Map<Integer, Long> d = new HashMap<>();
   private float e = Float.NaN;
   private float f;

   public Vec3 a_Vec3_Entity(Entity var1) {
      AABB var7;
      label11: {
         long var2 = System.currentTimeMillis();
         Map var4 = this.d;
         Integer var5 = var1.getId();
         Long var6 = (Long)var4.get(var5);
         var7 = var1.getBoundingBox();
         if (var6 != null) {
            long var8 = var2;
            long var10 = var6;
            long var12 = var8 - var10;
            long var14 = 200L;
            if (var12 <= var14) {
               break label11;
            }
         }

         Map var16 = this.c;
         Integer var17 = var1.getId();
         double var18 = b();
         double var20 = var7.getXsize();
         double var22 = var18 * var20;
         double var24 = 0.45;
         double var26 = 0.4;
         double var28 = c().nextDouble();
         double var30 = var26 * var28;
         double var32 = var24 + var30;
         double var34 = var7.getYsize();
         double var36 = var32 * var34;
         double var38 = b();
         double var40 = var7.getZsize();
         double var42 = var38 * var40;
         var16.put(var17, new Vec3(var22, var36, var42));
         Map var44 = this.d;
         Integer var45 = var1.getId();
         Long var46 = var2;
         var44.put(var45, var46);
      }

      double var48 = var7.minX;
      double var50 = var7.minY;
      double var52 = var7.minZ;
      Vec3 var47 = new Vec3(var48, var50, var52);
      Map var54 = this.c;
      Integer var55 = var1.getId();
      Vec3 var56 = (Vec3)var54.get(var55);
      return var47.add(var56);
   }

   public Vec3 a(BlockPos var1, Direction var2, Vec3 var3) {
      long var4 = System.currentTimeMillis();
      Long var6 = this.b.get(var1);
      if (var6 != null) {
         long var7 = var4;
         long var9 = var6;
         long var11 = var7 - var9;
         long var13 = 200L;
         if (var11 <= var13) {
            return this.a.get(var1);
         }
      }

      Vec3 var15 = this.b(var1, var2, var3);
      Vec3 var16 = var15;
      if (!a_bool_BlockPos_Vec3(var1, var16)) {
         var15 = var3;
      }

      Map var17 = this.a;
      Vec3 var18 = var15;
      var17.put(var1, var18);
      Map var19 = this.b;
      Long var20 = var4;
      var19.put(var1, var20);
      return var15;
   }

   public void a_void_BlockPos(BlockPos var1) {
      this.b.remove(var1);
      this.a.remove(var1);
   }

   public float[] a_floatArr_floatArr(float[] var1) {
      float var2 = var1[0];
      float var3 = var1[1];
      if (!Float.isNaN(this.e)) {
         float var4 = var2;
         float var5 = this.e;
         double var6 = Math.abs(var4 - var5);
         double var8 = 0.05;
         if (var6 < var8) {
            float var10 = var3;
            float var11 = this.f;
            double var12 = Math.abs(var10 - var11);
            double var14 = 0.05;
            if (var12 < var14) {
               float var16 = var2;
               double var17 = 0.06;
               double var19 = 0.25;
               float var21 = a_float_double_double(var17, var19);
               var2 = var16 + var21;
               float var22 = var3;
               double var23 = 0.04;
               double var25 = 0.18;
               float var27 = a_float_double_double(var23, var25);
               float var28 = var22 + var27;
               float var29 = Math.min(90.0F, var28);
               var3 = Math.max(-90.0F, var29);
            }
         }
      }

      float var30 = var2;
      this.e = var30;
      float var31 = var3;
      this.f = var31;
      float[] var32 = new float[2];
      float var33 = var2;
      var32[0] = var33;
      float var34 = var3;
      var32[1] = var34;
      return var32;
   }

   public boolean a_bool_double(double var1) {
      double var3 = var1;
      double var5 = 0.0;
      if (var3 > var5) {
         double var7 = c().nextDouble();
         double var9 = 100.0;
         double var11 = var7 * var9;
         double var13 = var1;
         if (var11 < var13) {
            return true;
         }
      }

      return false;
   }

   public void a() {
      this.a.clear();
      this.b.clear();
      this.c.clear();
      this.d.clear();
      this.e = Float.NaN;
   }

   private Vec3 b(BlockPos var1, Direction var2, Vec3 var3) {
      double var4 = var3.x;
      double var6 = var3.y;
      double var8 = var3.z;
      switch (var2.getAxis()) {
         case Y:
            double var10 = var4;
            double var12 = b(0.16);
            double var14 = var10 + var12;
            int var16 = var1.getX();
            var4 = a_double_double_int(var14, var16);
            double var17 = var8;
            double var19 = b(0.16);
            double var21 = var17 + var19;
            int var23 = var1.getZ();
            var8 = a_double_double_int(var21, var23);
            break;
         case X:
            double var24 = var6;
            double var26 = b(0.16);
            double var28 = var24 + var26;
            int var30 = var1.getY();
            var6 = a_double_double_int(var28, var30);
            double var31 = var8;
            double var33 = b(0.16);
            double var35 = var31 + var33;
            int var37 = var1.getZ();
            var8 = a_double_double_int(var35, var37);
            break;
         case Z:
            double var38 = var4;
            double var40 = b(0.16);
            double var42 = var38 + var40;
            int var44 = var1.getX();
            var4 = a_double_double_int(var42, var44);
            double var45 = var6;
            double var47 = b(0.16);
            double var49 = var45 + var47;
            int var51 = var1.getY();
            var6 = a_double_double_int(var49, var51);
      }

      double var52 = var4;
      double var54 = var6;
      double var56 = var8;
      return new Vec3(var52, var54, var56);
   }

   private static boolean a_bool_BlockPos_Vec3(BlockPos var0, Vec3 var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null && var2.level != null) {
         ClientLevel var3 = var2.level;
         Vec3 var4 = var2.player.getEyePosition();
         LocalPlayer var5 = var2.player;
         BlockHitResult var6 = var3.clip(new ClipContext(var4, var1, Block.COLLIDER, Fluid.NONE, var5));
         boolean var7;
         if (var6.getType() != Type.MISS && !var6.getBlockPos().equals(var0)) {
            var7 = false;
         } else {
            var7 = true;
         }

         return var7;
      } else {
         return false;
      }
   }

   private static double a_double_double_int(double var0, int var2) {
      double var3 = var2;
      double var5 = 0.12;
      double var7 = var3 + var5;
      double var9 = var2 + 1;
      double var11 = 0.12;
      double var13 = var9 - var11;
      double var15 = var0;
      double var17 = Math.min(var13, var15);
      return Math.max(var7, var17);
   }

   private static double b() {
      double var0 = 0.15;
      double var2 = 0.85;
      double var4 = 0.5;
      double var6 = c().nextGaussian();
      double var8 = 0.17;
      double var10 = var6 * var8;
      double var12 = var4 + var10;
      double var14 = Math.min(var2, var12);
      return Math.max(var0, var14);
   }

   private static double b(double var0) {
      double var2 = c().nextGaussian();
      double var4 = var0;
      return var2 * var4;
   }

   private static float a_float_double_double(double var0, double var2) {
      double var4 = var0;
      double var6 = c().nextDouble();
      double var8 = var2;
      double var10 = var0;
      double var12 = var8 - var10;
      double var14 = var6 * var12;
      float var16 = (float)(var4 + var14);
      float var17;
      if (c().nextBoolean()) {
         var17 = var16;
      } else {
         float var18 = var16;
         var17 = 0.0F - var18;
      }

      return var17;
   }

   private static ThreadLocalRandom c() {
      return ThreadLocalRandom.current();
   }
}
