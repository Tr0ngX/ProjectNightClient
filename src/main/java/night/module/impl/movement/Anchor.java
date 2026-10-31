package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.PlayerMoveEvent;
import night.mixins.accessors.ClientInputAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.world.HoleUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "Anchor", description = "Pulls you toward your nearest hole when looking down.", category = Module.Category.MOVEMENT)
public class Anchor extends Module {
   public NumberSetting pitch;
   public BooleanSetting doubles;
   public BooleanSetting stopMotion;
   public BooleanSetting fastFall;
   public NumberSetting fallSpeed;
   public BooleanSetting alreadyInHole;
   public BooleanSetting checkTerrainHole;
   public BooleanSetting checkBurrowed;

   public Anchor() {
      Integer var1 = 60;
      Integer var2 = 0;
      Integer var3 = 90;
      this.pitch = new NumberSetting("Pitch", "Minimum pitch angle to activate anchoring.", var1, var2, var3);
      this.doubles = new BooleanSetting("Doubles", "Include 2x1 holes when anchoring.", true);
      this.stopMotion = new BooleanSetting("StopMotion", "Stop horizontal movement while anchoring.", true);
      this.fastFall = new BooleanSetting("FastFall", "Accelerate fall speed while anchoring.", true);
      Float var4 = 1.0F;
      Float var5 = 1.0F;
      Float var6 = 5.0F;
      this.fallSpeed = new NumberSetting("FallSpeed", "Speed multiplier for falling.", var4, var5, var6);
      this.alreadyInHole = new BooleanSetting("AlreadyInHole", "Don't anchor if already in a hole.", true);
      this.checkTerrainHole = new BooleanSetting("CheckTerrainHole", "Don't anchor toward terrain holes.", false);
      this.checkBurrowed = new BooleanSetting("CheckBurrowed", "Don't anchor if burrowed.", true);
   }

   @EventTarget
   public void a_void_PlayerMoveEvent(PlayerMoveEvent var1) {
      if (!this.nullCheck() && mc.player != null && mc.level != null) {
         if (!this.alreadyInHole.getValue() || !this.v()) {
            if (!this.checkBurrowed.getValue() || !this.w()) {
               float var2 = mc.player.getXRot();
               float var3 = this.pitch.getValue().floatValue();
               if (!(var2 < var3)) {
                  BlockPos var4 = PositionUtil.a_BlockPos_Entity(mc.player);
                  if (!mc.player.isFallFlying()) {
                     for (int var5 = 0; var5 <= 5; var5++) {
                        BlockPos var6 = var4;
                        int var7 = var5 + 1;
                        BlockPos var8 = var6.below(var7);
                        ClientLevel var9 = mc.level;
                        BlockPos var10 = var8;
                        if (var9.getBlockState(var10).isAir()) {
                           return;
                        }

                        BlockPos var11 = var8;
                        double var12 = 1.0;
                        HoleUtil.HoleInfo var14 = HoleUtil.a_HoleInfo_BlockPos_double(var11, var12);
                        if (var14 != null) {
                           if (this.checkTerrainHole.getValue()) {
                              HoleUtil.HoleInfo var15 = var14;
                              if (this.a_bool_HoleInfo(var15)) {
                                 continue;
                              }
                           }

                           HoleUtil.HoleInfo var28 = var14;
                           this.a(var1, var28);
                           return;
                        } else {
                           if (this.doubles.getValue()) {
                              BlockPos var16 = var8;
                              double var17 = 1.0;
                              HoleUtil.HoleInfo var19 = HoleUtil.b(var16, var17);
                              if (var19 != null) {
                                 if (this.checkTerrainHole.getValue()) {
                                    HoleUtil.HoleInfo var20 = var19;
                                    if (this.a_bool_HoleInfo(var20)) {
                                       continue;
                                    }
                                 }

                                 HoleUtil.HoleInfo var26 = var19;
                                 this.a(var1, var26);
                                 return;
                              }
                           }

                           BlockPos var21 = var8;
                           double var22 = 1.0;
                           HoleUtil.HoleInfo var24 = HoleUtil.c(var21, var22);
                           if (var24 != null) {
                              if (this.checkTerrainHole.getValue()) {
                                 HoleUtil.HoleInfo var25 = var24;
                                 if (this.a_bool_HoleInfo(var25)) {
                                    continue;
                                 }
                              }

                              HoleUtil.HoleInfo var27 = var24;
                              this.a(var1, var27);
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void a(PlayerMoveEvent var1, HoleUtil.HoleInfo var2) {
      if (mc.player != null) {
         Vec3 var3 = var2.box().getCenter();
         if (this.stopMotion.getValue()) {
            Vec3 var4 = mc.player.getDeltaMovement();
            if (var4 != null) {
               LocalPlayer var5 = mc.player;
               double var6 = 0.0;
               double var8 = var4.y;
               double var10 = 0.0;
               var5.setDeltaMovement(var6, var8, var10);
            }

            ((ClientInputAccessor)mc.player.input).setMoveVector(Vec2.ZERO);
         }

         if (this.fastFall.getValue()) {
            Vec3 var12 = mc.player.getDeltaMovement();
            if (var12 != null) {
               double var13 = var12.y;
               double var15 = -0.1;
               if (var13 > var15) {
                  double var17 = this.fallSpeed.getValue().doubleValue();
                  double var19 = 10.0;
                  double var21 = var17 / var19;
                  LocalPlayer var23 = mc.player;
                  double var24 = var12.x;
                  double var26 = -var21;
                  double var28 = var12.z;
                  var23.setDeltaMovement(var24, var26, var28);
               }
            }
         }

         double var30 = var3.x;
         double var32 = mc.player.getX();
         double var34 = var30 - var32;
         double var36 = var3.z;
         double var38 = mc.player.getZ();
         double var40 = var36 - var38;
         double var42 = var34;
         double var44 = 2.0;
         double var46 = var42 / var44;
         var1.setY(var46);
         double var48 = var40;
         double var50 = 2.0;
         double var52 = var48 / var50;
         var1.setX(var52);
      }
   }

   private boolean v() {
      return HoleUtil.a_bool_Player(mc.player);
   }

   private boolean w() {
      BlockPos var1 = PositionUtil.a_BlockPos_Entity(mc.player);
      Direction[] var2 = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
      Direction[] var3 = var2;
      int var4 = 0;
      Direction[] var5 = var3;
      int var6 = var5.length;
      int var7 = 0;

      while (true) {
         int var8 = var7;
         int var9 = var6;
         if (var8 >= var9) {
            boolean var20;
            if (var4 == 4) {
               var20 = true;
            } else {
               var20 = false;
            }

            return var20;
         }

         Direction[] var10 = var5;
         int var11 = var7;
         Direction var12 = var10[var11];
         BlockPos var13 = var1;
         Direction var14 = var12;
         BlockPos var15 = var13.relative(var14);
         ClientLevel var16 = mc.level;
         BlockPos var17 = var15;
         if (!var16.getBlockState(var17).isAir()) {
            ClientLevel var18 = mc.level;
            BlockPos var19 = var15;
            if (!var18.getBlockState(var19).canBeReplaced()) {
               var4++;
            }
         }

         var7++;
      }
   }

   private boolean a_bool_HoleInfo(HoleUtil.HoleInfo var1) {
      if (var1 == null) {
         return false;
      }

      boolean var2;
      if (var1.holeSafety() != HoleUtil.HoleSafety.UNSAFE && var1.holeSafety() != HoleUtil.HoleSafety.SAFE) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }
}
