package night.module.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.movement.ElytraFly;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.inventory.InventoryUtil;
import night.util.render.Render3DUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "AutoMace", description = "Elytra-boosts above the target, free-falls in, and mace-smashes on repeat.", category = Module.Category.COMBAT)
public class AutoMace extends Module {
   public NumberSetting range;
   public NumberSetting minHeight;
   public NumberSetting attackRange;
   public NumberSetting angle;
   public BooleanSetting smartTerrain;
   public BooleanSetting fakeFly;
   public BooleanSetting swing;
   public ModeSetting swap;
   public BooleanSetting render;
   public ModeSetting renderMode;
   public CategorySetting entitiesCategory;
   public BooleanSetting players;
   public BooleanSetting friends;
   public BooleanSetting hostiles;
   public BooleanSetting neutrals;
   public BooleanSetting animals;
   public ColorSetting targetColor;
   private static final long w = 300L;
   private static final float x = 1.5F;
   private static final int y = 3;
   private static final float z = 85.0F;
   private static final double A = 4.0;
   private static final int B = 20;
   private static final double C = 8.0;
   private static final double D = 6.0;
   private static final double E = 2.5;
   private static final float[] F;
   private static final float[] G;
   public Entity v;
   private Entity H;
   private long I;
   private int J;
   private boolean K;
   private boolean L;
   private int M;
   private int N;
   private double O;
   private double P;
   private boolean Q;
   private int R;
   private int S;
   private int T;
   private int U;
   private static final int V = 10;
   private static final int W = 2;
   private boolean X;
   private float Y;
   private float Z;

   public AutoMace() {
      Double var1 = 20.0;
      Double var2 = 5.0;
      Double var3 = 50.0;
      this.range = new NumberSetting("Range", "Search radius to find a target.", var1, var2, var3);
      Double var4 = 15.0;
      Double var5 = 3.0;
      Double var6 = 30.0;
      this.minHeight = new NumberSetting("MinHeight", "Vertical height above the target to reach before diving.", var4, var5, var6);
      Double var7 = 4.0;
      Double var8 = 1.0;
      Double var9 = 6.0;
      this.attackRange = new NumberSetting("AttackRange", "Distance to the target that triggers the mace attack.", var7, var8, var9);
      Setting.VisibilityCondition var10 = new Setting.VisibilityCondition();
      Double var11 = 60.0;
      Double var12 = 0.0;
      Double var13 = 90.0;
      Integer var14 = 1;
      this.angle = new NumberSetting(
         "Angle", "Angle", "Flight angle relative to the target: 0 = horizontal, 90 = straight vertical.", var10, var11, var12, var13, var14
      );
      this.smartTerrain = new BooleanSetting(
         "SmartTerrain", "Predicts terrain ahead during ascend/dive and steepens the boost to fly over/around it instead of Angle's fixed value.", true
      );
      this.fakeFly = new BooleanSetting(
         "FakeFly",
         "Keeps real armor on and cycles the elytra in/out (GrimV3 trick) instead of wearing it the whole combo. Opt-in, reliability unverified.",
         false
      );
      this.swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
      String[] var15 = new String[]{"None", "Normal", "Silent"};
      this.swap = new ModeSetting("Swap", "Weapon switch mode.", "Silent", var15);
      this.render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
      BooleanSetting var17 = this.render;
      BooleanSetting.BooleanCondition var16 = new BooleanSetting.BooleanCondition(var17, true);
      String[] var18 = new String[]{"Box", "Circle", "Both"};
      this.renderMode = new ModeSetting("RenderMode", "Target render mode.", var16, "Circle", var18);
      this.entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
      CategorySetting var19 = this.entitiesCategory;
      this.players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.CategoryCondition(var19), true);
      CategorySetting var20 = this.entitiesCategory;
      this.friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.CategoryCondition(var20), false);
      CategorySetting var21 = this.entitiesCategory;
      this.hostiles = new BooleanSetting("Hostiles", "Target hostile mobs.", new CategorySetting.CategoryCondition(var21), false);
      CategorySetting var22 = this.entitiesCategory;
      this.neutrals = new BooleanSetting("Neutrals", "Target neutral mobs.", new CategorySetting.CategoryCondition(var22), false);
      CategorySetting var23 = this.entitiesCategory;
      this.animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.CategoryCondition(var23), false);
      BooleanSetting var25 = this.render;
      BooleanSetting.BooleanCondition var24 = new BooleanSetting.BooleanCondition(var25, true);
      ColorSetting.ColorValue var26 = ColorUtil.b();
      this.targetColor = new ColorSetting("TargetColor", "Color of the target render box.", var24, var26);
      this.v = null;
      this.H = null;
      long var27 = 0L;
      this.I = var27;
      this.J = -1;
      this.K = true;
      this.L = false;
      this.M = -1;
      this.N = 0;
      double var29 = 0.0;
      this.O = var29;
      double var31 = -1.0;
      this.P = var31;
      this.Q = false;
      this.R = -1;
      this.S = 0;
      this.T = 0;
      this.U = 0;
      this.X = false;
   }

   public Entity v() {
      return this.v;
   }

   public boolean w() {
      boolean var1;
      if (this.isEnabled() && this.v != null) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public float x() {
      return this.Y;
   }

   public float y() {
      return this.Z;
   }

   public void a_void_double_double(double var1, double var3) {
      if (!this.X && mc.player != null) {
         float var5 = mc.player.getYRot();
         this.Y = var5;
         float var6 = mc.player.getXRot();
         this.Z = var6;
         this.X = true;
      }

      float var7 = this.Y;
      double var8 = var1;
      double var10 = 0.15;
      float var12 = (float)(var8 * var10);
      float var13 = var7 + var12;
      this.Y = var13;
      float var14 = this.Z;
      double var15 = var3;
      double var17 = 0.15;
      float var19 = (float)(var15 * var17);
      float var20 = Mth.clamp(var14 + var19, -90.0F, 90.0F);
      this.Z = var20;
   }

   @Override
   public void onEnable() {
      this.v = null;
      this.H = null;
      this.K = true;
      this.L = false;
      this.M = -1;
      this.N = 0;
      double var1 = 0.0;
      this.O = var1;
      double var3 = -1.0;
      this.P = var3;
      this.J = -1;
      this.X = false;
      this.Q = false;
      this.R = -1;
      this.S = 0;
      this.T = 0;
      this.U = 0;
      if (mc.player != null && InventoryUtil.a_int_Item(Items.MACE) == -1) {
         this.setEnabled(false);
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         this.F();
         this.D();
         if (this.J != -1) {
            int var1 = this.J;
            int var2 = mc.player.getInventory().getSelectedSlot();
            InventoryUtil.a_bool_String_int_int("Normal", var1, var2);
            this.J = -1;
         }

         this.v = null;
         this.H = null;
      }
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null && !mc.player.isDeadOrDying()) {
         if (this.v == null || !this.v.isAlive()) {
            Entity var2 = this.H();
            this.v = var2;
         }

         if (this.v == null) {
            this.F();
            this.D();
            this.K = true;
            this.X = false;
         } else {
            if (!this.X) {
               float var3 = mc.player.getYRot();
               this.Y = var3;
               float var4 = mc.player.getXRot();
               this.Z = var4;
               this.X = true;
            }

            ElytraFly var5 = Night.moduleManager.getModule(ElytraFly.class);
            if (var5 != null) {
               if (this.K) {
                  ElytraFly var6 = var5;
                  this.a(var6);
               } else {
                  ElytraFly var7 = var5;
                  this.b(var7);
               }
            }
         }
      }
   }

   private void a(ElytraFly var1) {
      this.F();
      double var2 = mc.player.getY();
      double var4 = this.v.getY();
      double var6 = var2 - var4;
      double var8;
      if (this.smartTerrain.getValue()) {
         var8 = this.b(0.0F, -90.0F);
      } else {
         var8 = 8.0;
      }

      double var10 = var8;
      double var12 = this.minHeight.getValue().doubleValue();
      double var14 = var6;
      double var16 = var10;
      double var18 = var14 + var16;
      double var20 = 1.0;
      double var22 = var18 - var20;
      double var24 = Math.min(var12, var22);
      double var26 = 2.5;
      double var28 = Math.max(var24, var26);
      double var30 = var6;
      double var32 = var28;
      if (var30 >= var32) {
         double var34 = mc.player.getX();
         double var36 = this.v.getX();
         double var38 = var34 - var36;
         double var40 = mc.player.getZ();
         double var42 = this.v.getZ();
         double var44 = var40 - var42;
         double var46 = Math.hypot(var38, var44);
         Vec3 var48 = mc.player.getDeltaMovement();
         Object[] var49 = new Object[7];
         Double var50 = var6;
         var49[0] = var50;
         Double var51 = var28;
         var49[1] = var51;
         Double var52 = var10;
         var49[2] = var52;
         Double var53 = var46;
         var49[3] = var53;
         Double var54 = var48.x;
         var49[4] = var54;
         Double var55 = var48.y;
         var49[5] = var55;
         Double var56 = var48.z;
         var49[6] = var56;
         Night.LOGGER.info("[MaceDbg] ASCEND->DIVE height={} diveHeight={} headroom={} horizDist={} vel=({},{},{})", var49);
         this.K = false;
         this.N = 0;
         this.S = 0;
      } else if (this.c(var1)) {
         Vec3 var57 = this.v.getBoundingBox().getCenter();
         LocalPlayer var58 = mc.player;
         Vec3 var59 = var57;
         float var60 = (float)RotationMathUtil.a_double_Entity_Vec3(var58, var59) + 180.0F;
         float var61 = this.angle.getValue().floatValue();
         float var62 = 0.0F - var61;
         if (this.T > 0) {
            int var63 = this.T - 1;
            this.T = var63;
            var62 = -85.0F;
         }

         boolean var64 = this.smartTerrain.getValue();
         double var65 = mc.player.getDeltaMovement().horizontalDistanceSqr();
         double var67 = 0.0025;
         boolean var69;
         if (var65 < var67) {
            var69 = true;
         } else {
            var69 = false;
         }

         boolean var70 = var69;
         float[] var73;
         if (var64 && !var70) {
            float var71 = var60;
            float var72 = var62;
            var73 = this.c(var71, var72);
         } else {
            var73 = null;
         }

         float[] var74 = var73;
         if (var74 != null) {
            this.S = 0;
            float var75 = var74[0];
            float var76 = var74[1];
            this.a_void_float_float(var75, var76);
            float var77 = var74[0];
            float var78 = var74[1];
            var1.a_void_float_float(var77, var78);
         } else {
            boolean var79;
            if (var64 && this.B()) {
               var79 = true;
            } else {
               var79 = false;
            }

            boolean var80 = var79;
            boolean var81;
            if (!var64 || !this.A() && !var70) {
               var81 = false;
            } else {
               var81 = true;
            }

            boolean var82 = var81;
            if (!var80 && !var82) {
               this.S = 0;
            } else {
               int var83 = this.S + 1;
               this.S = var83;
            }

            if (var80 || this.S > 20) {
               var62 = -15.0F;
               float var84 = var60;
               float var85 = this.S / 20 % 4;
               float var86 = 90.0F * var85;
               var60 = var84 + var86;
            } else if (var82) {
               var62 = -85.0F;
            }

            float var87 = var60;
            float var88 = var62;
            this.a_void_float_float(var87, var88);
            float var89 = var60;
            float var90 = var62;
            var1.a_void_float_float(var89, var90);
         }
      }
   }

   private void b(ElytraFly var1) {
      Vec3 var2 = this.v.getBoundingBox().getCenter();
      Vec3 var3 = this.v.getDeltaMovement();
      double var4 = 4.0;
      Vec3 var6 = var3.scale(var4);
      Vec3 var7 = var2.add(var6);
      LocalPlayer var8 = mc.player;
      Vec3 var9 = var7;
      float var10 = (float)RotationMathUtil.a_double_Entity_Vec3(var8, var9);
      LocalPlayer var11 = mc.player;
      Vec3 var12 = var7;
      float var13 = (float)RotationMathUtil.b_double_Entity_Vec3(var11, var12);
      if (this.L) {
         Vec3 var73 = mc.player.getEyePosition(1.0F);
         AABB var74 = this.v.getBoundingBox();
         Vec3 var75 = RotationMathUtil.a_Vec3_Vec3_AABB(var73, var74);
         Vec3 var76 = var73;
         Vec3 var77 = var75;
         float var78 = RotationMathUtil.a_float_Vec3_Vec3(var76, var77);
         Vec3 var79 = var73;
         Vec3 var80 = var75;
         float var81 = RotationMathUtil.b_float_Vec3_Vec3(var79, var80);
         Night.rotationManager.sendOrBatchPosRot(var78, var81);
         int var82 = this.N + 1;
         this.N = var82;
         Vec3 var83 = var73;
         Vec3 var84 = var75;
         double var85 = var83.distanceTo(var84);
         Object[] var87 = new Object[6];
         Integer var88 = this.N;
         var87[0] = var88;
         Double var89 = var85;
         var87[1] = var89;
         Double var90 = mc.player.fallDistance;
         var87[2] = var90;
         Double var91 = mc.player.getY();
         var87[3] = var91;
         Double var92 = this.v.getY();
         var87[4] = var92;
         Boolean var93 = mc.player.onGround();
         var87[5] = var93;
         Night.LOGGER.info("[MaceDbg] FREEFALL tick={} dist={} fallDistance={} y={} targetY={} onGround={}", var87);
         if (this.N >= 3) {
            double var94 = var85;
            double var96 = this.attackRange.getValue().doubleValue();
            if (var94 <= var96) {
               double var98 = mc.player.fallDistance;
               double var100 = 1.5;
               if (var98 >= var100) {
                  Object[] var102 = new Object[4];
                  Double var103 = var85;
                  var102[0] = var103;
                  Double var104 = this.attackRange.getValue().doubleValue();
                  var102[1] = var104;
                  Double var105 = mc.player.fallDistance;
                  var102[2] = var105;
                  Boolean var106 = mc.player.onGround();
                  var102[3] = var106;
                  Night.LOGGER.info("[MaceDbg] ATTACK dist={} attackRange={} fallDistance={} onGround={}", var102);
                  Entity var107 = this.v;
                  this.a(var107);
                  var1.H();
                  this.T = 10;
                  this.F();
                  var1.J();
                  this.K = true;
                  this.Q = false;
                  return;
               }
            }
         }

         if (mc.player.onGround()) {
            Object[] var108 = new Object[3];
            Double var109 = var85;
            var108[0] = var109;
            Double var110 = this.attackRange.getValue().doubleValue();
            var108[1] = var110;
            Integer var111 = this.N;
            var108[2] = var111;
            Night.LOGGER.info("[MaceDbg] MISS-ONGROUND dist={} attackRange={} freefallTicks={}", var108);
            var1.H();
            this.F();
            var1.J();
            this.K = true;
         } else {
            double var112 = mc.player.fallDistance;
            double var114 = 0.01;
            double var116 = var112 + var114;
            double var118 = this.O;
            if (var116 < var118) {
               this.F();
               this.K = true;
            } else {
               double var120 = mc.player.fallDistance;
               this.O = var120;
               double var122 = this.P;
               double var124 = 0.0;
               if (var122 >= var124) {
                  double var126 = var85;
                  double var128 = this.P;
                  double var130 = 0.05;
                  double var132 = var128 + var130;
                  if (var126 > var132) {
                     Double var134 = var85;
                     Double var135 = this.P;
                     Night.LOGGER.info("[MaceDbg] MISS-OVERSHOOT-DETECTOR dist={} prevDist={}", var134, var135);
                     this.F();
                     double var136 = -1.0;
                     this.P = var136;
                     return;
                  }
               }

               double var138 = var85;
               this.P = var138;
            }
         }
      } else {
         double var14 = mc.player.getY();
         double var16 = this.v.getY();
         double var18 = var14 - var16;
         double var20 = this.attackRange.getValue().doubleValue();
         double var22 = 3.0;
         double var24 = var20 + var22;
         double var26 = mc.player.getX();
         double var28 = var2.x;
         double var30 = var26 - var28;
         double var32 = 2.0;
         double var34 = Math.pow(var30, var32);
         double var36 = mc.player.getZ();
         double var38 = var2.z;
         double var40 = var36 - var38;
         double var42 = 2.0;
         double var44 = Math.pow(var40, var42);
         double var46 = Math.sqrt(var34 + var44);
         double var48 = this.attackRange.getValue().doubleValue();
         double var50 = 1.5;
         double var52 = var48 * var50;
         Vec3 var54 = mc.player.getDeltaMovement();
         Object[] var55 = new Object[7];
         Double var56 = var18;
         var55[0] = var56;
         Double var57 = var46;
         var55[1] = var57;
         Double var58 = var24;
         var55[2] = var58;
         Double var59 = var52;
         var55[3] = var59;
         Double var60 = var54.x;
         var55[4] = var60;
         Double var61 = var54.y;
         var55[5] = var61;
         Double var62 = var54.z;
         var55[6] = var62;
         Night.LOGGER.info("[MaceDbg] DIVE-HOMING height={} horizDist={} cutHeight={} cutHorizDist={} vel=({},{},{})", var55);
         double var63 = var18;
         double var65 = 1.0;
         if (var63 < var65) {
            double var67 = var46;
            double var69 = var52;
            if (var67 > var69) {
               Double var71 = var18;
               Double var72 = var46;
               Night.LOGGER.info("[MaceDbg] DIVE ABORT-TO-ASCEND (ran out of height, still far) height={} horizDist={}", var71, var72);
               this.K = true;
               return;
            }
         }

         double var140 = var18;
         double var142 = var24;
         if (!(var140 > var142)) {
            double var144 = var46;
            double var146 = var52;
            if (!(var144 > var146)) {
               Vec3 var174 = mc.player.getDeltaMovement();
               Object[] var175 = new Object[5];
               Double var176 = var18;
               var175[0] = var176;
               Double var177 = var46;
               var175[1] = var177;
               Double var178 = var174.x;
               var175[2] = var178;
               Double var179 = var174.y;
               var175[3] = var179;
               Double var180 = var174.z;
               var175[4] = var180;
               Night.LOGGER.info("[MaceDbg] CUT-ELYTRA (commit to freefall) height={} horizDist={} vel=({},{},{})", var175);
               this.E();
               this.N = 0;
               double var181 = mc.player.fallDistance;
               this.O = var181;
               double var183 = -1.0;
               this.P = var183;
               return;
            }
         }

         if (this.c(var1)) {
            float var148 = var13;
            double var149 = mc.player.getDeltaMovement().horizontalDistanceSqr();
            double var151 = 0.0025;
            boolean var153;
            if (var149 < var151) {
               var153 = true;
            } else {
               var153 = false;
            }

            boolean var154 = var153;
            float[] var157;
            if (this.smartTerrain.getValue() && !var154) {
               float var155 = var10;
               float var156 = var13;
               var157 = this.c(var155, var156);
            } else {
               var157 = null;
            }

            float[] var158 = var157;
            if (var158 != null) {
               this.S = 0;
               float var159 = var158[0];
               float var160 = var158[1];
               this.a_void_float_float(var159, var160);
               float var161 = var158[0];
               float var162 = var158[1];
               var1.a_void_float_float(var161, var162);
            } else {
               boolean var167;
               label78: {
                  label77: {
                     if (this.smartTerrain.getValue()) {
                        if (this.A()) {
                           break label77;
                        }

                        double var163 = mc.player.getDeltaMovement().horizontalDistanceSqr();
                        double var165 = 0.0025;
                        if (var163 < var165) {
                           break label77;
                        }
                     }

                     var167 = false;
                     break label78;
                  }

                  var167 = true;
               }

               if (var167) {
                  int var168 = this.S + 1;
                  this.S = var168;
                  if (this.S > 20) {
                     this.K = true;
                     return;
                  }

                  float var169;
                  if (var13 < 0.0F) {
                     var169 = -85.0F;
                  } else {
                     var169 = 85.0F;
                  }

                  var148 = var169;
               } else {
                  this.S = 0;
               }

               float var170 = var10;
               float var171 = var148;
               this.a_void_float_float(var170, var171);
               float var172 = var10;
               float var173 = var148;
               var1.a_void_float_float(var172, var173);
            }
         }
      }
   }

   private void a_void_float_float(float var1, float var2) {
      mc.player.setYRot(var1);
      mc.player.setXRot(var2);
      mc.player.setYBodyRot(var1);
      mc.player.setYHeadRot(var1);
   }

   private boolean A() {
      if (mc.level == null) {
         return false;
      }

      Vec3 var1 = mc.player.getDeltaMovement();
      double var2 = var1.lengthSqr();
      double var4 = 1.0E-4;
      if (var2 < var4) {
         return false;
      }

      Vec3 var6 = mc.player.position();
      double var7 = 0.0;
      double var9 = mc.player.getBbHeight();
      double var11 = 0.5;
      double var13 = var9 * var11;
      double var15 = 0.0;
      Vec3 var17 = var6.add(var7, var13, var15);
      double var18 = 3.0;
      double var20 = var1.length();
      double var22 = 6.0;
      double var24 = var20 * var22;
      double var26 = Math.max(var18, var24);
      Vec3 var28 = var17;
      Vec3 var29 = var1.normalize();
      double var30 = var26;
      Vec3 var32 = var29.scale(var30);
      Vec3 var33 = var28.add(var32);
      ClientLevel var34 = mc.level;
      Vec3 var35 = var17;
      Vec3 var36 = var33;
      LocalPlayer var37 = mc.player;
      boolean var38;
      if (var34.clip(new ClipContext(var35, var36, Block.COLLIDER, Fluid.NONE, var37)).getType() == Type.BLOCK) {
         var38 = true;
      } else {
         var38 = false;
      }

      return var38;
   }

   private boolean B() {
      if (mc.level == null) {
         return false;
      }

      Vec3 var1 = mc.player.position();
      double var2 = 0.0;
      double var4 = mc.player.getBbHeight();
      double var6 = 0.0;
      Vec3 var8 = var1.add(var2, var4, var6);
      double var9 = 0.0;
      double var11 = 5.0;
      double var13 = 0.0;
      Vec3 var15 = var8.add(var9, var11, var13);
      ClientLevel var16 = mc.level;
      Vec3 var17 = var8;
      Vec3 var18 = var15;
      LocalPlayer var19 = mc.player;
      boolean var20;
      if (var16.clip(new ClipContext(var17, var18, Block.COLLIDER, Fluid.NONE, var19)).getType() == Type.BLOCK) {
         var20 = true;
      } else {
         var20 = false;
      }

      return var20;
   }

   private double b(float var1, float var2) {
      if (mc.level == null) {
         return 0.0;
      }

      double var3 = Math.toRadians(var1);
      double var5 = Math.toRadians(var2);
      double var7 = Math.cos(var5);
      double var9 = -Math.sin(var3);
      double var11 = var7;
      double var13 = var9 * var11;
      double var15 = -Math.sin(var5);
      double var17 = Math.cos(var3);
      double var19 = var7;
      double var21 = var17 * var19;
      Vec3 var23 = new Vec3(var13, var15, var21);
      Vec3 var24 = mc.player.position();
      double var25 = 0.0;
      double var27 = mc.player.getBbHeight();
      double var29 = 0.5;
      double var31 = var27 * var29;
      double var33 = 0.0;
      Vec3 var35 = var24.add(var25, var31, var33);
      ClientLevel var36 = mc.level;
      Vec3 var37 = var35;
      Vec3 var38 = var35;
      Vec3 var39 = var23;
      double var40 = 8.0;
      Vec3 var42 = var39.scale(var40);
      Vec3 var43 = var38.add(var42);
      LocalPlayer var44 = mc.player;
      BlockHitResult var45 = var36.clip(new ClipContext(var37, var43, Block.COLLIDER, Fluid.NONE, var44));
      double var48;
      if (var45.getType() == Type.BLOCK) {
         Vec3 var46 = var45.getLocation();
         Vec3 var47 = var35;
         var48 = var46.distanceTo(var47);
      } else {
         var48 = 8.0;
      }

      return var48;
   }

   private float[] c(float var1, float var2) {
      if (mc.level == null) {
         return new float[]{var1, var2};
      }

      double var4 = this.b(var1, var2);
      double var6 = 6.0;
      if (var4 >= var6) {
         return new float[]{var1, var2};
      }

      float[] var9 = null;
      double var10 = Double.MAX_VALUE;
      float[] var12 = null;
      double var13 = -1.0;
      float[] var15 = F;
      int var16 = var15.length;
      int var17 = 0;

      while (true) {
         int var18 = var17;
         int var19 = var16;
         if (var18 >= var19) {
            if (var9 != null) {
               return var9;
            }

            double var67 = var13;
            double var69 = 2.5;
            float[] var71;
            if (var67 >= var69) {
               var71 = var12;
            } else {
               var71 = null;
            }

            return var71;
         }

         float[] var20 = var15;
         int var21 = var17;
         float var22 = var20[var21];
         float[] var23 = G;
         int var24 = var23.length;
         int var25 = 0;

         while (true) {
            int var26 = var25;
            int var27 = var24;
            if (var26 >= var27) {
               var17++;
               break;
            }

            float[] var28 = var23;
            int var29 = var25;
            float var30 = var28[var29];
            float var31 = var22;
            float var32 = var1 + var31;
            float var33 = var30;
            float var34 = Mth.clamp(var2 + var33, -85.0F, 85.0F);
            float var35 = var32;
            float var36 = var34;
            double var37 = this.b(var35, var36);
            double var39 = Math.abs(var22);
            double var41 = Math.abs(var34 - var2);
            double var43 = 0.5;
            double var45 = var41 * var43;
            double var47 = var39 + var45;
            double var49 = var37;
            double var51 = 6.0;
            if (var49 >= var51) {
               double var53 = var47;
               double var55 = var10;
               if (var53 < var55) {
                  var10 = var47;
                  float[] var57 = new float[2];
                  float var58 = var32;
                  var57[0] = var58;
                  float var59 = var34;
                  var57[1] = var59;
                  var9 = var57;
               }
            }

            double var60 = var37;
            double var62 = var13;
            if (var60 > var62) {
               var13 = var37;
               float[] var64 = new float[2];
               float var65 = var32;
               var64[0] = var65;
               float var66 = var34;
               var64[1] = var66;
               var12 = var64;
            }

            var25++;
         }
      }
   }

   private boolean c(ElytraFly var1) {
      if (mc.player.onGround()) {
         Vec3 var2 = mc.player.getDeltaMovement();
         double var3 = var2.y;
         double var5 = 0.42;
         if (var3 < var5) {
            LocalPlayer var7 = mc.player;
            double var8 = var2.x;
            double var10 = 0.45;
            double var12 = var2.z;
            var7.setDeltaMovement(var8, var10, var12);
         }

         this.Q = true;
         return false;
      } else {
         if (this.Q) {
            this.Q = false;
            this.C();
            this.U = 0;
            var1.J();
            return false;
         }

         this.d(var1);
         if (mc.player.isFallFlying()) {
            return true;
         }

         this.C();
         var1.J();
         return false;
      }
   }

   private void d(ElytraFly var1) {
      if (this.fakeFly.getValue() && !this.L && !mc.player.onGround()) {
         int var2 = this.U;
         int var3 = var2 + 1;
         this.U = var3;
         if (var2 >= 2) {
            this.U = 0;
            var1.J();
         }
      }
   }

   public boolean z() {
      boolean var1;
      if (this.isEnabled() && this.fakeFly.getValue() && this.v != null && !this.L && mc.player != null && !mc.player.onGround()) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   private void C() {
      if (!this.fakeFly.getValue()) {
         if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            int var1 = InventoryUtil.a_int_Item(Items.ELYTRA);
            if (var1 != -1) {
               if (this.R == -1 && !mc.player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                  int var2 = this.G();
                  if (var2 != -1) {
                     InventoryUtil.a(var2, 6);
                     int var3 = var2;
                     this.R = var3;
                  }
               }

               InventoryUtil.a(var1, 6);
            }
         }
      }
   }

   private void D() {
      if (this.R != -1) {
         InventoryUtil.a(this.R, 6);
         this.R = -1;
      }
   }

   private void E() {
      if (!this.L) {
         if (this.fakeFly.getValue()) {
            this.L = true;
         } else if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            int var1 = this.G();
            if (var1 != -1) {
               InventoryUtil.a(var1, 6);
               int var2 = var1;
               this.M = var2;
               this.L = true;
            }
         }
      }
   }

   private void F() {
      if (this.L) {
         if (this.M != -1) {
            InventoryUtil.a(this.M, 6);
         }

         this.L = false;
         this.M = -1;
      }
   }

   private int G() {
      for (int var1 = 0; var1 <= 35; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         if (var2.getItem(var3).isEmpty()) {
            return var1;
         }
      }

      return -1;
   }

   private void a(Entity var1) {
      int var2 = mc.player.getInventory().getSelectedSlot();
      int var3 = InventoryUtil.a_int_Item(Items.MACE);
      boolean var4 = false;
      if (var3 != -1) {
         int var5 = var3;
         int var6 = var2;
         if (var5 != var6) {
            if (this.swap.getValue().equalsIgnoreCase("Normal")) {
               if (this.J == -1) {
                  int var7 = var2;
                  this.J = var7;
               }

               int var8 = var3;
               int var9 = var2;
               InventoryUtil.a_bool_String_int_int("Normal", var8, var9);
            } else if (this.swap.getValue().equalsIgnoreCase("Silent")) {
               int var10 = var3;
               int var11 = var2;
               InventoryUtil.a_bool_String_int_int("Silent", var10, var11);
               var4 = true;
            }
         }
      }

      ClientPacketListener var12 = mc.getConnection();
      int var13 = var1.getId();
      var12.send(new ServerboundAttackPacket(var13));
      mc.player.resetAttackStrengthTicker();
      if (this.swing.getValue()) {
         PacketAndRenderUtil.a(mc.player, InteractionHand.MAIN_HAND);
         ClientPacketListener var14 = mc.getConnection();
         Packet var15 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
         var14.send(var15);
      }

      if (var4) {
         int var16 = var3;
         int var17 = var2;
         InventoryUtil.b("Silent", var16, var17);
      }
   }

   private Entity H() {
      if (mc.player != null && mc.level != null) {
         ArrayList var1 = new ArrayList();
         Vec3 var2 = mc.player.getEyePosition(1.0F);
         double var3 = this.range.getValue().doubleValue();

         for (Entity var6 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            LocalPlayer var7 = mc.player;
            if (var6 != var7 && var6.isAlive() && !EntityUtil.c_bool_Entity(var6)) {
               Vec3 var8 = var2;
               Vec3 var9 = var2;
               AABB var10 = var6.getBoundingBox();
               Vec3 var11 = RotationMathUtil.a_Vec3_Vec3_AABB(var9, var10);
               double var12 = var8.distanceTo(var11);
               double var14 = var3;
               if (!(var12 > var14)) {
                  if (var6 instanceof Player var16) {
                     if (this.players.getValue()) {
                        if (!this.friends.getValue()) {
                           String var17 = var16.getName().getString();
                           if (Night.friendManager.isFriend(var17)) {
                              continue;
                           }
                        }

                        ArrayList var18 = var1;
                        Entity var19 = var6;
                        var18.add(var19);
                     }
                  } else if (this.neutrals.getValue() && EntityUtil.e(var6)) {
                     ArrayList var20 = var1;
                     Entity var21 = var6;
                     var20.add(var21);
                  } else if (this.hostiles.getValue() && EntityUtil.g(var6)) {
                     ArrayList var22 = var1;
                     Entity var23 = var6;
                     var22.add(var23);
                  } else if (this.animals.getValue() && EntityUtil.h(var6)) {
                     ArrayList var24 = var1;
                     Entity var25 = var6;
                     var24.add(var25);
                  }
               }
            }
         }

         return ((List<Entity>)var1).stream().min(Comparator.comparingDouble(var1x -> {
            AABB var2x = var1x.getBoundingBox();
            Vec3 var3x = RotationMathUtil.a_Vec3_Vec3_AABB(var2, var2x);
            return var2.distanceTo(var3x);
         })).orElse(null);
      } else {
         return null;
      }
   }

   @EventTarget
   public void a(Render3DEvent var1) {
      if (this.render.getValue() && mc.player != null && !mc.player.isDeadOrDying()) {
         Entity var2 = this.v;
         float var3 = 1.0F;
         if (var2 != null) {
            Entity var4 = var2;
            this.H = var4;
            long var5 = System.currentTimeMillis();
            this.I = var5;
         } else if (this.H != null) {
            long var7 = System.currentTimeMillis();
            long var9 = this.I;
            long var11 = var7 - var9;
            long var13 = 300L;
            if (var11 < var13) {
               var2 = this.H;
               float var15 = (float)var11 / 300.0F;
               var3 = 1.0F - var15;
            } else {
               this.H = null;
            }
         }

         if (var2 != null) {
            Color var16 = this.targetColor.getColor();
            float var17 = this.targetColor.getColor().getAlpha();
            float var18 = var3;
            int var19 = (int)(var17 * var18);
            Color var20 = ColorUtil.a(var16, var19);
            if (this.renderMode.getValue().equalsIgnoreCase("Box") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
               Entity var21 = var2;
               float var22 = var1.getTickDelta();
               Vec3 var23 = EntityUtil.a_Vec3_Entity_float(var21, var22);
               AABB var24 = var2.getBoundingBox();
               double var25 = var23.x;
               double var27 = var2.getX();
               double var29 = var25 - var27;
               double var31 = var23.y;
               double var33 = var2.getY();
               double var35 = var31 - var33;
               double var37 = var23.z;
               double var39 = var2.getZ();
               double var41 = var37 - var39;
               AABB var43 = var24.move(var29, var35, var41);
               PoseStack var44 = var1.getMatrices();
               AABB var45 = var43;
               Color var46 = var20;
               Render3DUtil.a_void_PoseStack_AABB_Color(var44, var45, var46);
               PoseStack var47 = var1.getMatrices();
               AABB var48 = var43;
               Color var49 = var20;
               Render3DUtil.b(var47, var48, var49);
            }

            if (this.renderMode.getValue().equalsIgnoreCase("Circle") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
               PoseStack var50 = var1.getMatrices();
               Entity var51 = var2;
               float var52 = var1.getTickDelta();
               Color var53 = var20;
               Render3DUtil.a_void_PoseStack_Entity_float_Color(var50, var51, var52, var53);
            }
         }
      }
   }

   static {
      float[] var0 = new float[]{0.0F, -25.0F, 25.0F, -50.0F, 50.0F, -75.0F, 75.0F};
      F = var0;
      float[] var1 = new float[]{0.0F, -25.0F, 25.0F, -55.0F, 55.0F};
      G = var1;
   }
}
