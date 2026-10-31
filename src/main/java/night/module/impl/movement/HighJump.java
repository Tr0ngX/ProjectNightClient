package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity.AnimationStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "HighJump", description = "Makes you jump high with a shulker.", category = Module.Category.MOVEMENT)
public class HighJump extends Module {
   private static final double k = 142.85714285714286;
   private static final double l = 0.61;
   private static final double m = 0.51;
   private static final int n = 2;
   private static final double o = 1.15;
   private static final double p = 0.42;
   private static final double q = 0.98F;
   public NumberSetting strength;
   public BooleanSetting grim;
   public static volatile boolean g = false;
   public BooleanSetting infinite;
   public NumberSetting hover;
   public NumberSetting hoverTimer;
   private long r;
   private BlockPos s;
   private Direction t;
   private boolean u;
   private int v;
   private int w;
   private double x;
   private double y;
   private double z;

   public HighJump() {
      Setting.VisibilityCondition var1 = new Setting.VisibilityCondition();
      Float var2 = 1.4F;
      Float var3 = 0.1F;
      Float var4 = 3.0F;
      Float var5 = 0.1F;
      this.strength = new NumberSetting("Strength", "Strength", "Launch height. 1.4 = 200 blocks.", var1, var2, var3, var4, var5);
      this.grim = new BooleanSetting(
         "Grim", "Cap launch to GrimAC-verified safe height (~50 blocks, tested Paper 1.21.8 + Grim 2.3.74). Off = full Strength height.", false
      );
      this.infinite = new BooleanSetting("Infinite", "Launches again mid-air at the apex while Space is held, instead of falling back down.", false);
      Setting.VisibilityCondition var6 = new Setting.VisibilityCondition();
      Float var7 = 0.0F;
      Float var8 = 0.0F;
      Float var9 = 10.0F;
      Float var10 = 0.5F;
      this.hover = new NumberSetting("Hover", "Hover", "Seconds of slow-motion at the apex. 0 = off.", var6, var7, var8, var9, var10);
      Setting.VisibilityCondition var11 = new Setting.VisibilityCondition();
      Float var12 = 0.2F;
      Float var13 = 0.05F;
      Float var14 = 1.0F;
      Float var15 = 0.05F;
      this.hoverTimer = new NumberSetting(
         "HoverTimer", "HoverTimer", "Game speed while hovering. Lower = hang longer, travel less per second.", var11, var12, var13, var14, var15
      );
   }

   @Override
   public void onEnable() {
      this.v();
   }

   @Override
   public void onDisable() {
      this.v();
   }

   private void v() {
      this.s = null;
      this.u = false;
      g = false;
      long var1 = this.r;
      long var3 = 0L;
      if (var1 != var3) {
         Night.worldStateManager.setTimerMultiplier(1.0F);
      }

      long var5 = 0L;
      this.r = var5;
   }

   @EventTarget(priority = -2147483647)
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (this.nullCheck()) {
         this.v();
      } else {
         Vec3 var2 = mc.player.getDeltaMovement();
         if (this.u) {
            double var3 = var2.y;
            double var5 = 0.0;
            if (!(var3 > var5)) {
               if (this.infinite.getValue() && PacketAndRenderUtil.a_bool_int(32)) {
                  double var92 = mc.player.getY();
                  this.z = var92;
                  double var94 = this.z;
                  double var96 = this.strength.getValue().doubleValue();
                  double var98 = 142.85714285714286;
                  double var100 = var96 * var98;
                  double var102 = var94 + var100;
                  this.y = var102;
                  double var104 = this.y;
                  double var106 = this.z;
                  double var108 = var104 - var106;
                  double var110 = mc.player.getGravity();
                  double var112 = a(var108, var110);
                  LocalPlayer var114 = mc.player;
                  double var115 = var2.x;
                  double var117 = var112;
                  double var119 = var2.z;
                  var114.setDeltaMovement(var115, var117, var119);
               } else {
                  if (this.hover.getValue().floatValue() > 0.0F) {
                     long var121 = System.currentTimeMillis();
                     long var123 = this.r;
                     long var125 = 0L;
                     if (var123 == var125) {
                        long var127 = var121;
                        long var129 = (long)(this.hover.getValue().floatValue() * 1000.0F);
                        long var131 = var127 + var129;
                        this.r = var131;
                        float var133 = this.hoverTimer.getValue().floatValue();
                        Night.worldStateManager.setTimerMultiplier(var133);
                     }

                     long var134 = var121;
                     long var136 = this.r;
                     if (var134 < var136) {
                        return;
                     }
                  }

                  this.v();
               }
            }
         } else {
            if (this.s == null) {
               if (!mc.player.onGround() || !PacketAndRenderUtil.a_bool_int(32) || !this.w()) {
                  g = false;
                  return;
               }

               double var7 = mc.player.getY();
               this.z = var7;
               double var9 = this.z;
               double var11 = this.strength.getValue().doubleValue();
               double var13 = 142.85714285714286;
               double var15 = var11 * var13;
               double var17 = var9 + var15;
               this.y = var17;
               this.v = 0;
               this.w = 0;
               this.u = false;
               g = true;
            }

            Vec3 var19 = mc.player.position();
            if (this.a_bool_Vec3(var19)) {
               int var20 = this.v;
               this.w = var20;
            }

            int var21 = this.v;
            int var22 = this.w;
            int var23 = var21 - var22;
            double var24 = mc.player.getGravity();
            double var26;
            if (this.v == 0) {
               var26 = 0.42;
            } else {
               var26 = this.x;
            }

            double var28 = var26;
            double var38;
            if (var23 <= 2) {
               double var30 = var28;
               double var32 = 0.61;
               double var34 = var30 + var32;
               double var36 = 0.51;
               var38 = Math.max(var34, var36);
            } else {
               var38 = var28;
            }

            double var40 = var38;
            double var42 = this.y;
            double var44 = var19.y;
            double var46 = var42 - var44;
            double var48 = var24;
            double var50 = a(var46, var48);
            double var52 = var40;
            double var60;
            if (!(var50 <= var52) && var23 < 2) {
               var60 = var40;
               Vec3 var62 = var19;
               double var63 = 0.0;
               double var65 = var40;
               double var67 = 0.0;
               Vec3 var69 = var62.add(var63, var65, var67);
               if (!this.a_bool_Vec3(var69)) {
                  double var70 = var60;
                  double var72 = 1.15;
                  var60 = var70 + var72;
               }

               double var74 = var40;
               double var76 = var24;
               double var78 = var74 - var76;
               double var80 = 0.98F;
               double var82 = var78 * var80;
               this.x = var82;
            } else {
               double var58;
               if (this.grim.getValue()) {
                  double var54 = var50;
                  double var56 = var40;
                  var58 = Math.min(var54, var56);
               } else {
                  var58 = var50;
               }

               var60 = var58;
               this.u = true;
            }

            LocalPlayer var84 = mc.player;
            double var85 = var2.x;
            double var87 = var60;
            double var89 = var2.z;
            var84.setDeltaMovement(var85, var87, var89);
            int var91 = this.v + 1;
            this.v = var91;
         }
      }
   }

   private boolean w() {
      BlockPos var1 = mc.player.blockPosition();
      BlockPos var2 = var1.offset(-2, -2, -2);
      BlockPos var3 = var1.offset(2, 2, 2);

      for (BlockPos var5 : (Iterable<BlockPos>)(Object)BlockPos.betweenClosed(var2, var3)) {
         ClientLevel var6 = mc.level;
         BlockPos var7 = var5;
         BlockEntity var8 = var6.getBlockEntity(var7);
         if (var8 instanceof ShulkerBoxBlockEntity) {
            AnimationStatus var9 = ((ShulkerBoxBlockEntity)var8).getAnimationStatus();
            if (var9 == AnimationStatus.OPENING || var9 == AnimationStatus.OPENED) {
               ClientLevel var10 = mc.level;
               BlockPos var11 = var5;
               BlockState var12 = var10.getBlockState(var11);
               if (var12.hasProperty(ShulkerBoxBlock.FACING)) {
                  Direction var13 = (Direction)var12.getValue(ShulkerBoxBlock.FACING);
                  if (var13.getAxis() == Axis.Y) {
                     BlockPos var14 = var5.immutable();
                     this.s = var14;
                     Direction var15 = var13;
                     this.t = var15;
                     Vec3 var16 = mc.player.position();
                     if (this.a_bool_Vec3(var16)) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      this.s = null;
      return false;
   }

   private boolean a_bool_Vec3(Vec3 var1) {
      BlockPos var3 = this.s;
      AABB var2 = new AABB(var3);
      double var4 = this.t.getStepX();
      double var6 = this.t.getStepY();
      double var8 = this.t.getStepZ();
      AABB var10 = var2.expandTowards(var4, var6, var8);
      double var11 = var10.maxX;
      double var13 = var1.x;
      double var15 = 1.0005;
      double var17 = var13 - var15;
      if (var11 >= var17) {
         double var19 = var10.minX;
         double var21 = var1.x;
         double var23 = 1.0005;
         double var25 = var21 + var23;
         if (var19 <= var25) {
            double var27 = var10.maxY;
            double var29 = var1.y;
            double var31 = 1.0;
            double var33 = var29 - var31;
            if (var27 >= var33) {
               double var35 = var10.minY;
               double var37 = var1.y;
               double var39 = 1.001;
               double var41 = var37 + var39;
               if (var35 <= var41) {
                  double var43 = var10.maxZ;
                  double var45 = var1.z;
                  double var47 = 1.0005;
                  double var49 = var45 - var47;
                  if (var43 >= var49) {
                     double var51 = var10.minZ;
                     double var53 = var1.z;
                     double var55 = 1.0005;
                     double var57 = var53 + var55;
                     if (var51 <= var57) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private static double a(double var0, double var2) {
      double var4 = var0;
      double var6 = 0.0;
      if (var4 <= var6) {
         return 0.0;
      }

      double var8 = 0.0;
      double var10 = 64.0;

      for (int var12 = 0; var12 < 40; var12++) {
         double var13 = var8;
         double var15 = var10;
         double var17 = var13 + var15;
         double var19 = 2.0;
         double var21 = var17 / var19;
         double var23 = var2;
         double var25 = b(var21, var23);
         double var27 = var0;
         if (var25 < var27) {
            var8 = var21;
         } else {
            var10 = var21;
         }
      }

      return var10;
   }

   private static double b(double var0, double var2) {
      double var4 = 0.0;
      int var6 = 0;

      while (true) {
         double var7 = var0;
         double var9 = 0.0;
         if (!(var7 > var9) || var6 >= 2000) {
            return var4;
         }

         double var11 = var4;
         double var13 = var0;
         var4 = var11 + var13;
         double var15 = var0;
         double var17 = var2;
         double var19 = var15 - var17;
         double var21 = 0.98F;
         var0 = var19 * var21;
         var6++;
      }
   }

   @Override
   public String getDisplayInfo() {
      Object[] var1 = new Object[1];
      Double var2 = this.strength.getValue().doubleValue();
      var1[0] = var2;
      return String.format("%.1f", var1);
   }
}
