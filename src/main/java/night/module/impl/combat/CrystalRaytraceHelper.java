package night.module.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.util.MinecraftInstance;

public class CrystalRaytraceHelper implements MinecraftInstance {
   public static CrystalRaytraceHelper.HitCandidate a(BlockPos var0) {
      if (mc.player != null && mc.level != null) {
         Vec3 var1 = mc.player.getEyePosition();
         Direction[] var2 = new Direction[]{Direction.UP, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN};
         CrystalRaytraceHelper.HitCandidate var3 = null;
         double var4 = Double.MAX_VALUE;
         double[][] var6 = new double[][]{{0.0, 0.5}, {1.0, 0.5}, {0.5, 0.0}, {0.5, 1.0}, {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};

         for (Direction var10 : var2) {
            BlockPos var11 = var0.relative(var10);
            if (var10 == Direction.UP || mc.level.getBlockState(var11).canBeReplaced() || !mc.level.getBlockState(var11).isSolidRender()) {
               CrystalRaytraceHelper.HitCandidate var12 = a(var0, var10, var1, 0.5, 0.5);
               if (var12 != null) {
                  double var33 = var1.distanceToSqr(var12.b);
                  if (var33 < var4) {
                     var4 = var33;
                     var3 = var12;
                  }
               } else {
                  for (double[] var17 : var6) {
                     for (int var18 = 1; var18 <= 10; var18++) {
                        double var19 = 1.0 - 1.0 / (1 << var18);
                        double var21 = 0.5 + (var17[0] - 0.5) * var19;
                        double var23 = 0.5 + (var17[1] - 0.5) * var19;
                        CrystalRaytraceHelper.HitCandidate var25 = a(var0, var10, var1, var21, var23);
                        if (var25 != null) {
                           double var26 = var1.distanceToSqr(var25.b);
                           if (var26 < var4) {
                              var4 = var26;
                              var3 = var25;
                           }
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (var3 != null) {
            return var3;
         }

         Direction var28 = null;
         double var29 = Double.MAX_VALUE;

         for (Direction var34 : var2) {
            BlockPos var35 = var0.relative(var34);
            BlockState var36 = mc.level.getBlockState(var35);
            if (var36.canBeReplaced()) {
               Vec3 var37 = b(var0, var34, 0.5, 0.5);
               double var38 = var1.distanceToSqr(var37);
               if (var38 < var29) {
                  var29 = var38;
                  var28 = var34;
               }
            }
         }

         return var28 != null
            ? new CrystalRaytraceHelper.HitCandidate(var28, b(var0, var28, 0.5, 0.5), false)
            : new CrystalRaytraceHelper.HitCandidate(Direction.UP, Vec3.atCenterOf(var0).add(0.0, 0.5, 0.0), false);
      } else {
         return new CrystalRaytraceHelper.HitCandidate(Direction.UP, Vec3.atCenterOf(var0).add(0.0, 0.5, 0.0), false);
      }
   }

   private static CrystalRaytraceHelper.HitCandidate a(BlockPos var0, Direction var1, Vec3 var2, double var3, double var5) {
      Vec3 var7 = a(var0, var1, var3, var5);
      BlockHitResult var8 = mc.level.clip(new ClipContext(var2, var7, Block.COLLIDER, Fluid.NONE, mc.player));
      if (var8 == null) {
         return null;
      } else if (var8.getType() == Type.MISS) {
         return new CrystalRaytraceHelper.HitCandidate(var1, b(var0, var1, var3, var5), true);
      } else {
         return var8.getType() == Type.BLOCK && var8.getBlockPos().equals(var0)
            ? new CrystalRaytraceHelper.HitCandidate(var8.getDirection(), var8.getLocation(), true)
            : null;
      }
   }

   public static boolean b(BlockPos var0) {
      if (mc.player != null && mc.level != null) {
         CrystalRaytraceHelper.HitCandidate var1 = a(var0);
         return var1 != null && var1.c;
      } else {
         return false;
      }
   }

   public static Vec3 a(BlockPos var0, Direction var1, double var2, double var4) {
      double var6 = var0.getX();
      double var8 = var0.getY();
      double var10 = var0.getZ();
      switch (var1) {
         case UP:
            return new Vec3(var6 + var2, var8 + 0.999, var10 + var4);
         case DOWN:
            return new Vec3(var6 + var2, var8 + 0.001, var10 + var4);
         case SOUTH:
            return new Vec3(var6 + var2, var8 + var4, var10 + 0.999);
         case NORTH:
            return new Vec3(var6 + var2, var8 + var4, var10 + 0.001);
         case EAST:
            return new Vec3(var6 + 0.999, var8 + var2, var10 + var4);
         case WEST:
            return new Vec3(var6 + 0.001, var8 + var2, var10 + var4);
         default:
            return new Vec3(var6 + 0.5, var8 + 0.5, var10 + 0.5);
      }
   }

   public static Vec3 b(BlockPos var0, Direction var1, double var2, double var4) {
      double var6 = var0.getX();
      double var8 = var0.getY();
      double var10 = var0.getZ();
      switch (var1) {
         case UP:
            return new Vec3(var6 + var2, var8 + 1.0, var10 + var4);
         case DOWN:
            return new Vec3(var6 + var2, var8 + 0.0, var10 + var4);
         case SOUTH:
            return new Vec3(var6 + var2, var8 + var4, var10 + 1.0);
         case NORTH:
            return new Vec3(var6 + var2, var8 + var4, var10 + 0.0);
         case EAST:
            return new Vec3(var6 + 1.0, var8 + var2, var10 + var4);
         case WEST:
            return new Vec3(var6 + 0.0, var8 + var2, var10 + var4);
         default:
            return new Vec3(var6 + 0.5, var8 + 0.5, var10 + 0.5);
      }
   }

   public static class HitCandidate {
      public Direction a;
      public Vec3 b;
      public boolean c;

      public HitCandidate(Direction var1, Vec3 var2, boolean var3) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
      }
   }
}
