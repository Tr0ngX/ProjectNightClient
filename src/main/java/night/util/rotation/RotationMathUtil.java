package night.util.rotation;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import night.util.MinecraftInstance;

public class RotationMathUtil implements MinecraftInstance {
   public static float[] a(Entity var0) {
      double var1 = var0.getX();
      double var3 = var0.getY();
      Pose var5 = var0.getPose();
      double var6 = var0.getEyeHeight(var5);
      double var8 = 2.0;
      double var10 = var6 / var8;
      double var12 = var3 + var10;
      double var14 = var0.getZ();
      return a(var1, var12, var14);
   }

   public static float[] a(Vec3 var0) {
      double var1 = var0.x;
      double var3 = var0.y;
      double var5 = var0.z;
      return a(var1, var3, var5);
   }

   public static float[] a(double var0, double var2, double var4) {
      LocalPlayer var6 = mc.player;
      double var7 = var0;
      double var9 = var2;
      double var11 = var4;
      return a(var6, var7, var9, var11);
   }

   public static float[] a(Entity var0, double var1, double var3, double var5) {
      Vec3 var7 = var0.position();
      double var8 = 0.0;
      Pose var10 = var0.getPose();
      double var11 = var0.getEyeHeight(var10);
      double var13 = 0.0;
      Vec3 var15 = var7.add(var8, var11, var13);
      double var16 = var1;
      double var18 = var15.x;
      double var20 = var16 - var18;
      double var22 = var3;
      double var24 = var15.y;
      double var26 = var22 - var24;
      double var28 = -1.0;
      double var30 = var26 * var28;
      double var32 = var5;
      double var34 = var15.z;
      double var36 = var32 - var34;
      double var38 = var20;
      double var40 = var20;
      double var42 = var38 * var40;
      double var44 = var36;
      double var46 = var36;
      double var48 = var44 * var46;
      double var50 = Mth.sqrt((float)(var42 + var48));
      double var52 = var36;
      double var54 = var20;
      double var56 = Math.toDegrees(Math.atan2(var52, var54));
      double var58 = 90.0;
      float var60 = (float)Mth.wrapDegrees(var56 - var58);
      double var61 = var30;
      double var63 = var50;
      double var65 = Mth.wrapDegrees(Math.toDegrees(Math.atan2(var61, var63)));
      double var67 = -90.0;
      double var69 = 90.0;
      float var71 = (float)Mth.clamp(var65, var67, var69);
      float[] var72 = new float[2];
      float var73 = var60;
      float var74 = ((float)Math.random() - 0.5F) * 4.0F;
      float var75 = var73 + var74;
      var72[0] = var75;
      float var76 = var71;
      float var77 = ((float)Math.random() - 0.5F) * 4.0F;
      float var78 = var76 + var77;
      var72[1] = var78;
      return var72;
   }

   public static double a_double_Entity_Vec3(Entity var0, Vec3 var1) {
      double var2 = var1.x;
      double var4 = var0.getX();
      double var6 = var2 - var4;
      double var8 = var1.z;
      double var10 = var0.getZ();
      double var12 = var8 - var10;
      double var14 = var6;
      double var16 = Math.toDegrees(Math.atan2(var12, var14));
      double var18 = 90.0;
      return Mth.wrapDegrees(var16 - var18);
   }

   public static double b_double_Entity_Vec3(Entity var0, Vec3 var1) {
      double var2 = var1.x;
      double var4 = var0.getX();
      double var6 = var2 - var4;
      double var8 = var1.y;
      double var10 = var0.getY();
      double var12 = var0.getEyeHeight();
      double var14 = var10 + var12;
      double var16 = var8 - var14;
      double var18 = var1.z;
      double var20 = var0.getZ();
      double var22 = var18 - var20;
      double var24 = var6;
      double var26 = var6;
      double var28 = var24 * var26;
      double var30 = var22;
      double var32 = var22;
      double var34 = var30 * var32;
      double var36 = Math.sqrt(var28 + var34);
      double var38 = var16;
      double var40 = var36;
      double var42 = Mth.wrapDegrees(-Math.toDegrees(Math.atan2(var38, var40)));
      double var44 = -90.0;
      double var46 = 90.0;
      return Mth.clamp(var42, var44, var46);
   }

   public static float a_float_Vec3_Vec3(Vec3 var0, Vec3 var1) {
      double var2 = var1.x;
      double var4 = var0.x;
      double var6 = var2 - var4;
      double var8 = var1.z;
      double var10 = var0.z;
      double var12 = var8 - var10;
      double var14 = var6;
      double var16 = Math.toDegrees(Math.atan2(var12, var14));
      double var18 = 90.0;
      return (float)Mth.wrapDegrees(var16 - var18);
   }

   public static float b_float_Vec3_Vec3(Vec3 var0, Vec3 var1) {
      double var2 = var1.x;
      double var4 = var0.x;
      double var6 = var2 - var4;
      double var8 = var1.y;
      double var10 = var0.y;
      double var12 = var8 - var10;
      double var14 = var1.z;
      double var16 = var0.z;
      double var18 = var14 - var16;
      double var20 = var12;
      double var22 = var6;
      double var24 = var6;
      double var26 = var22 * var24;
      double var28 = var18;
      double var30 = var18;
      double var32 = var28 * var30;
      double var34 = Math.sqrt(var26 + var32);
      return (float)(-Math.toDegrees(Math.atan2(var20, var34)));
   }

   public static Vec3 a_Vec3_Vec3_AABB(Vec3 var0, AABB var1) {
      double var2 = var0.x;
      double var4 = var1.minX;
      double var6 = var1.maxX;
      double var8 = Mth.clamp(var2, var4, var6);
      double var10 = var0.y;
      double var12 = var1.minY;
      double var14 = var1.maxY;
      double var16 = Mth.clamp(var10, var12, var14);
      double var18 = var0.z;
      double var20 = var1.minZ;
      double var22 = var1.maxZ;
      double var24 = Mth.clamp(var18, var20, var22);
      double var26 = var8;
      double var28 = var16;
      double var30 = var24;
      return new Vec3(var26, var28, var30);
   }

   public static Vec3 a_Vec3_float_float(float var0, float var1) {
      float var2 = var1 * (float) (Math.PI / 180.0);
      float var3 = (0.0F - var0) * (float) (Math.PI / 180.0);
      float var4 = Mth.cos(var3);
      float var5 = Mth.sin(var3);
      float var6 = Mth.cos(var2);
      float var7 = Mth.sin(var2);
      float var8 = var5;
      float var9 = var6;
      double var10 = var8 * var9;
      float var12 = var7;
      double var13 = 0.0F - var12;
      float var15 = var4;
      float var16 = var6;
      double var17 = var15 * var16;
      return new Vec3(var10, var13, var17);
   }

   public static EntityHitResult a(Vec3 var0, Entity var1, double var2, float var4, float var5) {
      Vec3 var6 = a_Vec3_float_float(var4, var5);
      double var7 = var2;
      Vec3 var9 = var6.scale(var7);
      Vec3 var10 = var0.add(var9);
      float var11 = var1.getPickRadius();
      AABB var12 = var1.getBoundingBox();
      double var13 = Math.max(var11, 0.05F);
      AABB var15 = var12.inflate(var13);
      Vec3 var16 = var10;
      return var15.clip(var0, var16).isPresent() ? new EntityHitResult(var1) : null;
   }

   public static float[] a(long var0, float var2, float var3) {
      return new float[]{var2, var3};
   }
}
