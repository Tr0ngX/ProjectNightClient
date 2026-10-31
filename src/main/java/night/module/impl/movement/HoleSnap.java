package night.module.impl.movement;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.Render3DEvent;
import night.mixins.accessors.Vec3dAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.HoleSnapAccess;
import night.module.impl.combat.Surround;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.inventory.InventoryUtil;
import night.util.movement.MovementUtil;
import night.util.render.Render3DUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.HoleUtil;

@ModuleInfo(name = "HoleSnap", description = "Pulls you toward your nearest hole.", category = Module.Category.MOVEMENT)
public class HoleSnap extends Module implements HoleSnapAccess {
   public NumberSetting range;
   public BooleanSetting doubleHoles;
   public BooleanSetting quadHoles;
   public BooleanSetting step;
   public BooleanSetting fillHole;
   public ModeSetting switchSetting;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public BooleanSetting crystalDestruction;
   public BooleanSetting render;
   public NumberSetting timer;
   public BooleanSetting renderTarget;
   public ColorSetting renderTargetColor;
   public AABB r;
   private HoleUtil.HoleInfo s;
   private AABB t;
   private boolean u;
   private int v;
   private AABB w;
   private int x;
   private static final int y = 3;
   private int z;
   private final Deque<BlockPos> A;
   private AABB B;
   private int C;
   private static final int D = 20;
   private final List<BlockPos> E;

   public HoleSnap() {
      Integer var1 = 5;
      Integer var2 = 1;
      Integer var3 = 8;
      this.range = new NumberSetting("Range", "Range for the holes.", var1, var2, var3);
      this.doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to snap you to double holes.", true);
      this.quadHoles = new BooleanSetting("QuadHoles", "Whether or not to snap you to quad holes.", true);
      this.step = new BooleanSetting("Step", "Keeps snapping to the next nearest hole instead of stopping once you've reached one.", false);
      BooleanSetting var5 = this.step;
      BooleanSetting.BooleanCondition var4 = new BooleanSetting.BooleanCondition(var5, true);
      this.fillHole = new BooleanSetting(
         "FillHole", "Fills every cell of the hole you just stepped out of, once you've fully left its footprint (needs Step).", var4, false
      );
      this.switchSetting = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a);
      this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      Float var6 = 1.0F;
      Float var7 = 0.5F;
      Float var8 = 10.0F;
      this.timer = new NumberSetting("Timer", "The tick-speed multiplier to run at while HoleSnap is active (1.0 = normal speed).", var6, var7, var8);
      this.renderTarget = new BooleanSetting("RenderTarget", "Draws an outline around the hole HoleSnap is currently walking toward.", true);
      BooleanSetting var10 = this.renderTarget;
      BooleanSetting.BooleanCondition var9 = new BooleanSetting.BooleanCondition(var10, true);
      ColorSetting.ColorValue var11 = ColorUtil.c();
      this.renderTargetColor = new ColorSetting("RenderTargetColor", "The color of the target hole outline.", var9, var11);
      this.r = null;
      this.s = null;
      this.t = null;
      this.u = false;
      this.v = 0;
      this.w = null;
      this.x = 0;
      this.z = 0;
      this.A = new ArrayDeque<>();
      this.B = null;
      this.C = 0;
      this.E = new ArrayList<>();
   }

   @Override
   public boolean isStepEnabled() {
      return this.step.getValue();
   }

   public static boolean a_bool_BlockPos(BlockPos var0) {
      double var1 = 1.0;
      if (HoleUtil.a_HoleInfo_BlockPos_double_bool(var0, var1, false) == null) {
         double var3 = 1.0;
         if (HoleUtil.b(var0, var3) == null) {
            double var5 = 1.0;
            if (HoleUtil.c(var0, var5) == null) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public void onEnable() {
      this.r = null;
      this.s = null;
      this.A.clear();
      this.B = null;
      this.E.clear();
      this.C = 0;
      this.v = 0;
      this.z = 0;
      this.w = null;
      this.x = 0;
      float var1 = this.timer.getValue().floatValue();
      Night.worldStateManager.setTimerMultiplier(var1);
      if (this.step.getValue()) {
         Surround var2 = Night.moduleManager.getModule(Surround.class);
         if (var2.isEnabled()) {
            var2.setEnabled(false);
         }
      }
   }

   @Override
   public void onDisable() {
      Night.worldStateManager.setTimerMultiplier(1.0F);
      if (this.u) {
         mc.options.keyJump.setDown(false);
         this.u = false;
      }

      this.z = 0;
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.renderTarget.getValue() && this.r != null) {
         PoseStack var2 = var1.getMatrices();
         AABB var3 = this.r;
         Color var4 = this.renderTargetColor.getColor();
         Render3DUtil.b(var2, var3, var4);
      }
   }

   @EventTarget
   public void a_void_PlayerMoveEvent(PlayerMoveEvent var1) {
      float var2 = this.timer.getValue().floatValue();
      Night.worldStateManager.setTimerMultiplier(var2);
      if (!this.nullCheck()) {
         if (this.B != null) {
            AABB var3 = this.B;
            double var4 = mc.player.getX();
            double var6 = this.B.minY;
            double var8 = 0.5;
            double var10 = var6 + var8;
            double var12 = mc.player.getZ();
            if (var3.contains(var4, var10, var12)) {
               int var14 = this.C;
               int var15 = var14 + 1;
               this.C = var15;
               if (var14 < 20) {
                  return;
               }
            }

            if (!this.A.isEmpty()) {
               this.x();
            } else {
               this.B = null;
               this.E.clear();
               this.setEnabled(false);
            }
         } else {
            double var16 = mc.player.fallDistance;
            double var18 = 5.0;
            if (!(var16 >= var18) || this.r != null) {
               if (this.r != null || this.v()) {
                  if (this.r != null) {
                     AABB var20 = this.r;
                     if (!this.a_bool_AABB(var20)) {
                        AABB var21 = this.r;
                        this.w = var21;
                        this.r = null;
                        this.s = null;
                        this.v = 0;
                        int var22 = this.x + 1;
                        this.x = var22;
                        if (var22 >= 3) {
                           String var23 = this.getName();
                           Night.chatManager.sendTaggedMessage("Gave up reaching a hole " + this.x + " times in a row, disabling.", var23);
                           this.setEnabled(false);
                           return;
                        }

                        if (!this.v()) {
                           return;
                        }
                     }
                  }

                  if (this.r != null) {
                     AABB var24 = this.r;
                     if (!this.b_bool_AABB(var24)) {
                        AABB var25 = this.r;
                        this.w = var25;
                        this.r = null;
                        this.s = null;
                        this.v = 0;
                        if (!this.v()) {
                           return;
                        }
                     }
                  }

                  boolean var50;
                  label120: {
                     double var26 = mc.player.getX();
                     double var28 = this.r.getCenter().x;
                     double var30 = Math.abs(var26 - var28);
                     double var32 = 0.03;
                     if (var30 < var32) {
                        double var34 = mc.player.getY();
                        double var36 = this.r.minY;
                        double var38 = Math.abs(var34 - var36);
                        double var40 = 0.05;
                        if (var38 < var40) {
                           double var42 = mc.player.getZ();
                           double var44 = this.r.getCenter().z;
                           double var46 = Math.abs(var42 - var44);
                           double var48 = 0.03;
                           if (var46 < var48) {
                              var50 = true;
                              break label120;
                           }
                        }
                     }

                     var50 = false;
                  }

                  if (var50) {
                     this.v = 0;
                     this.x = 0;
                     double var72 = 0.0;
                     double var74 = var1.getMovement().y;
                     double var76 = 0.0;
                     var1.setMovement(new Vec3(var72, var74, var76));
                     Vec3dAccessor var78 = (Vec3dAccessor)mc.player.getDeltaMovement();
                     double var79 = 0.0;
                     var78.setX(var79);
                     Vec3dAccessor var81 = (Vec3dAccessor)mc.player.getDeltaMovement();
                     double var82 = 0.0;
                     var81.setZ(var82);
                     var1.setCancelled(true);
                     if (Night.moduleManager.getModule(Step.class).isEnabled()) {
                        Night.moduleManager.getModule(Step.class).setEnabled(false);
                     }

                     if (Night.moduleManager.getModule(Speed.class).isEnabled()) {
                        Night.moduleManager.getModule(Speed.class).setEnabled(false);
                     }

                     if (Night.moduleManager.getModule(TickShift.class).isEnabled()) {
                        Night.moduleManager.getModule(TickShift.class).setEnabled(false);
                     }

                     if (this.fillHole.getValue() && this.step.getValue() && this.s != null) {
                        AABB var84 = this.s.box();
                        this.B = var84;
                        this.C = 0;
                        AABB var85 = this.s.box();
                        this.c_void_AABB(var85);
                        this.r = null;
                        this.s = null;
                     } else {
                        this.setEnabled(false);
                        this.r = null;
                        this.s = null;
                     }
                  } else {
                     if (mc.player.horizontalCollision && mc.player.onGround() && !this.u) {
                        Vec3 var71;
                        label109: {
                           double var51 = this.r.getCenter().x;
                           double var53 = mc.player.getX();
                           double var55 = var51 - var53;
                           double var57 = this.r.getCenter().z;
                           double var59 = mc.player.getZ();
                           double var61 = var57 - var59;
                           double var63 = var55;
                           double var65 = 0.0;
                           if (var63 == var65) {
                              double var67 = var61;
                              double var69 = 0.0;
                              if (var67 == var69) {
                                 var71 = Vec3.ZERO;
                                 break label109;
                              }
                           }

                           double var86 = var55;
                           double var88 = 0.0;
                           double var90 = var61;
                           var71 = new Vec3(var86, var88, var90).normalize();
                        }

                        Vec3 var92 = var71;
                        AABB var93 = mc.player.getBoundingBox();
                        double var95 = var93.minX;
                        double var97 = mc.player.getY();
                        double var99 = 2.0;
                        double var101 = var97 + var99;
                        double var103 = var93.minZ;
                        double var105 = var93.maxX;
                        double var107 = mc.player.getY();
                        double var109 = 2.5;
                        double var111 = var107 + var109;
                        double var113 = var93.maxZ;
                        AABB var94 = new AABB(var95, var101, var103, var105, var111, var113);
                        double var115 = var92.x;
                        double var117 = 0.5;
                        double var119 = var115 * var117;
                        double var121 = 0.0;
                        double var123 = var92.z;
                        double var125 = 0.5;
                        double var127 = var123 * var125;
                        AABB var129 = var94.move(var119, var121, var127);
                        ClientLevel var130 = mc.level;
                        AABB var131 = var129;
                        if (!var130.noCollision(var131)) {
                           AABB var132 = this.r;
                           this.w = var132;
                           this.r = null;
                           this.s = null;
                           this.v = 0;
                           int var133 = this.x + 1;
                           this.x = var133;
                           if (var133 >= 3) {
                              String var134 = this.getName();
                              Night.chatManager.sendTaggedMessage("Gave up reaching a hole " + this.x + " times in a row, disabling.", var134);
                              this.setEnabled(false);
                              return;
                           }

                           this.v();
                           return;
                        }

                        mc.options.keyJump.setDown(true);
                        this.u = true;
                        this.z = 0;
                        int var135 = this.v + 1;
                        this.v = var135;
                        if (this.v > 5) {
                           mc.options.keyJump.setDown(false);
                           this.u = false;
                           this.v = 0;
                           AABB var136 = this.r;
                           this.w = var136;
                           this.r = null;
                           this.s = null;
                           int var137 = this.x + 1;
                           this.x = var137;
                           if (var137 >= 3) {
                              String var138 = this.getName();
                              Night.chatManager.sendTaggedMessage("Gave up reaching a hole " + this.x + " times in a row, disabling.", var138);
                              this.setEnabled(false);
                           }

                           return;
                        }
                     }

                     if (this.u) {
                        int var139 = this.z + 1;
                        this.z = var139;
                        if (!mc.player.onGround() || this.z > 10) {
                           mc.options.keyJump.setDown(false);
                           this.u = false;
                           this.z = 0;
                        }
                     }

                     Vec3 var140 = this.r.getCenter();
                     double var141 = MovementUtil.b(MovementUtil.a);
                     MovementUtil.a(var1, var140, var141);
                  }
               }
            }
         }
      }
   }

   private boolean a_bool_AABB(AABB var1) {
      double var2 = var1.minX;
      double var4 = 0.5;
      double var6 = var2 + var4;
      double var8 = var1.minY;
      double var10 = var1.minZ;
      double var12 = 0.5;
      double var14 = var10 + var12;
      BlockPos var16 = BlockPos.containing(var6, var8, var14);
      int var17 = (int)Math.floor(mc.player.getY());
      int var18 = var16.getY();
      int var19 = var17;
      int var20 = var18;
      if (var19 > var20) {
         int var21 = var18 + 3;

         while (true) {
            int var22 = var21;
            int var23 = var17 + 1;
            if (var22 > var23) {
               return true;
            }

            ClientLevel var24 = mc.level;
            int var25 = var16.getX();
            int var26 = var21;
            int var27 = var16.getZ();
            if (!var24.getBlockState(new BlockPos(var25, var26, var27)).canBeReplaced()) {
               return false;
            }

            var21++;
         }
      } else {
         int var28 = var18;
         int var29 = var17;
         if (var28 - var29 >= 2) {
            BlockPos var30 = mc.player.blockPosition().above(2);
            ClientLevel var31 = mc.level;
            BlockPos var32 = var30;
            if (!var31.getBlockState(var32).canBeReplaced()) {
               return false;
            }

            ClientLevel var33 = mc.level;
            BlockPos var34 = var30.above();
            if (!var33.getBlockState(var34).canBeReplaced()) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean v() {
      HoleUtil.HoleInfo var4;
      label44: {
         HoleUtil.HoleInfo var1 = this.w();
         if (var1 != null && this.t != null) {
            AABB var2 = var1.box();
            AABB var3 = this.t;
            if (var2.equals(var3)) {
               var4 = null;
               break label44;
            }
         }

         var4 = var1;
      }

      HoleUtil.HoleInfo var5 = var4;
      List<HoleUtil.HoleInfo> var6;
      if (var5 != null && !this.step.getValue()) {
         var6 = List.of(var5);
      } else if (var5 != null) {
         List var7 = this.y().stream().filter(var1x -> {
            AABB var2x = var1x.box();
            AABB var3x = var5.box();
            boolean var4x;
            if (!var2x.equals(var3x)) {
               var4x = true;
            } else {
               var4x = false;
            }

            return var4x;
         }).toList();
         var6 = this.a_List_List(var7);
      } else {
         var6 = this.y();
      }

      if (this.t != null) {
         List var8 = var6.stream().filter(var1x -> {
            AABB var2x = var1x.box();
            AABB var3x = this.t;
            boolean var4x;
            if (!var2x.equals(var3x)) {
               var4x = true;
            } else {
               var4x = false;
            }

            return var4x;
         }).toList();
         if (!var8.isEmpty()) {
            var6 = var8;
         }
      }

      if (this.w != null) {
         var6 = var6.stream().filter(var1x -> {
            AABB var2x = var1x.box();
            AABB var3x = this.w;
            boolean var4x;
            if (!var2x.equals(var3x)) {
               var4x = true;
            } else {
               var4x = false;
            }

            return var4x;
         }).toList();
      }

      if (var6.isEmpty()) {
         this.setEnabled(false);
         return false;
      }

      HoleUtil.HoleInfo var9 = var5;
      this.s = var9;
      AABB var10 = ((HoleUtil.HoleInfo)var6.get(0)).box();
      this.r = var10;
      AABB var11 = this.r;
      AABB var12 = this.t;
      if (!var11.equals(var12)) {
         AABB var13 = this.r;
         this.t = var13;
      }

      return true;
   }

   private boolean b_bool_AABB(AABB var1) {
      double var2 = var1.minX;
      double var4 = 0.5;
      double var6 = var2 + var4;
      double var8 = var1.minY;
      double var10 = var1.minZ;
      double var12 = 0.5;
      double var14 = var10 + var12;
      BlockPos var16 = BlockPos.containing(var6, var8, var14);
      double var17 = 1.0;
      HoleUtil.HoleInfo var19 = HoleUtil.a_HoleInfo_BlockPos_double(var16, var17);
      if (var19 != null && var19.box().equals(var1)) {
         return true;
      }

      if (this.doubleHoles.getValue()) {
         BlockPos var20 = var16;
         double var21 = 1.0;
         HoleUtil.HoleInfo var23 = HoleUtil.b(var20, var21);
         if (var23 != null && var23.box().equals(var1)) {
            return true;
         }
      }

      if (this.quadHoles.getValue()) {
         BlockPos var24 = var16;
         double var25 = 1.0;
         HoleUtil.HoleInfo var27 = HoleUtil.c(var24, var25);
         if (var27 != null && var27.box().equals(var1)) {
            return true;
         }
      }

      return false;
   }

   private HoleUtil.HoleInfo w() {
      BlockPos var1 = mc.player.blockPosition();
      double var2 = var1.getX();
      double var4 = 0.5;
      double var6 = var2 + var4;
      double var8 = var1.getY();
      double var10 = 0.5;
      double var12 = var8 + var10;
      double var14 = var1.getZ();
      double var16 = 0.5;
      double var18 = var14 + var16;

      for (int var20 = -1; var20 <= 0; var20++) {
         for (int var21 = -1; var21 <= 0; var21++) {
            BlockPos var22 = var1;
            int var23 = var20;
            int var24 = var21;
            BlockPos var25 = var22.offset(var23, 0, var24);
            double var26 = 1.0;
            HoleUtil.HoleInfo var28 = HoleUtil.a_HoleInfo_BlockPos_double(var25, var26);
            if (var28 != null) {
               AABB var29 = var28.box();
               double var30 = var6;
               double var32 = var12;
               double var34 = var18;
               if (var29.contains(var30, var32, var34)) {
                  return var28;
               }
            }

            if (this.doubleHoles.getValue()) {
               BlockPos var36 = var25;
               double var37 = 1.0;
               HoleUtil.HoleInfo var39 = HoleUtil.b(var36, var37);
               if (var39 != null) {
                  AABB var40 = var39.box();
                  double var41 = var6;
                  double var43 = var12;
                  double var45 = var18;
                  if (var40.contains(var41, var43, var45)) {
                     return var39;
                  }
               }
            }

            if (this.quadHoles.getValue()) {
               BlockPos var47 = var25;
               double var48 = 1.0;
               HoleUtil.HoleInfo var50 = HoleUtil.c(var47, var48);
               if (var50 != null) {
                  AABB var51 = var50.box();
                  double var52 = var6;
                  double var54 = var12;
                  double var56 = var18;
                  if (var51.contains(var52, var54, var56)) {
                     return var50;
                  }
               }
            }
         }
      }

      return null;
   }

   private List<HoleUtil.HoleInfo> a_List_List(List<HoleUtil.HoleInfo> var1) {
      return var1.stream().sorted(Comparator.comparingInt(var0 -> {
         byte var1x;
         if (var0.holeType() == HoleUtil.HoleType.SINGLE) {
            var1x = 0;
         } else {
            var1x = 1;
         }

         return var1x;
      })).toList();
   }

   private void c_void_AABB(AABB var1) {
      int var2 = (int)Math.floor(var1.minX);
      int var3 = (int)Math.ceil(var1.maxX) - 1;
      int var4 = (int)Math.floor(var1.minZ);
      int var5 = (int)Math.ceil(var1.maxZ) - 1;
      int var6 = (int)var1.minY;
      int var7 = var2;

      while (true) {
         int var8 = var7;
         int var9 = var3;
         if (var8 > var9) {
            return;
         }

         int var10 = var4;

         while (true) {
            int var11 = var10;
            int var12 = var5;
            if (var11 > var12) {
               var7++;
               break;
            }

            Deque var13 = this.A;
            int var14 = var7;
            int var15 = var6;
            int var16 = var10;
            var13.add(new BlockPos(var14, var15, var16));
            var10++;
         }
      }
   }

   private void x() {
      BlockPos var1 = this.A.peek();
      if (var1 != null) {
         if (!BlockInteractionUtil.c_bool_BlockPos(var1)) {
            this.A.poll();
         } else {
            boolean var2;
            if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
               var2 = false;
            } else {
               var2 = true;
            }

            byte var3;
            if (var2) {
               var3 = 35;
            } else {
               var3 = 8;
            }

            int var4 = InventoryUtil.b(0, var3);
            if (var4 == -1) {
               this.A.clear();
            } else {
               int var5 = mc.player.getInventory().getSelectedSlot();
               BlockPos var6 = var1;
               List var7 = this.E;
               boolean var8 = this.strictDirection.getValue();
               Direction var9 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var6, var7, var8);
               if (var9 == null) {
                  BlockPos var10 = var1.offset(0, -1, 0);
                  if (!BlockInteractionUtil.c_bool_BlockPos(var10)) {
                     this.A.poll();
                     return;
                  }

                  BlockPos var11 = var10;
                  List var12 = this.E;
                  boolean var13 = this.strictDirection.getValue();
                  Direction var14 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var11, var12, var13);
                  if (var14 == null) {
                     this.A.poll();
                     return;
                  }

                  String var15 = this.switchSetting.getValue();
                  int var16 = var4;
                  int var17 = var5;
                  InventoryUtil.a_bool_String_int_int(var15, var16, var17);
                  BlockPos var18 = var10;
                  Direction var19 = var14;
                  boolean var20 = this.rotate.getValue();
                  boolean var21 = this.crystalDestruction.getValue();
                  boolean var22 = this.render.getValue();
                  boolean var23 = BlockInteractionUtil.a(var18, var19, InteractionHand.MAIN_HAND, var20, var21, var22);
                  String var24 = this.switchSetting.getValue();
                  int var25 = var4;
                  int var26 = var5;
                  InventoryUtil.b(var24, var25, var26);
                  if (!var23) {
                     return;
                  }

                  List var27 = this.E;
                  BlockPos var28 = var10;
                  var27.add(var28);
                  BlockPos var29 = var1;
                  List var30 = this.E;
                  boolean var31 = this.strictDirection.getValue();
                  var9 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var29, var30, var31);
                  if (var9 == null) {
                     return;
                  }
               }

               String var32 = this.switchSetting.getValue();
               int var33 = var4;
               int var34 = var5;
               InventoryUtil.a_bool_String_int_int(var32, var33, var34);
               BlockPos var35 = var1;
               Direction var36 = var9;
               boolean var37 = this.rotate.getValue();
               boolean var38 = this.crystalDestruction.getValue();
               boolean var39 = this.render.getValue();
               boolean var40 = BlockInteractionUtil.a(var35, var36, InteractionHand.MAIN_HAND, var37, var38, var39);
               String var41 = this.switchSetting.getValue();
               int var42 = var4;
               int var43 = var5;
               InventoryUtil.b(var41, var42, var43);
               if (var40) {
                  List var44 = this.E;
                  BlockPos var45 = var1;
                  var44.add(var45);
                  this.A.poll();
               }
            }
         }
      }
   }

   private List<HoleUtil.HoleInfo> y() {
      ArrayList<HoleUtil.HoleInfo> var1 = new ArrayList<>();
      int var2 = 0;

      while (true) {
         int var3 = var2;
         double var4 = this.range.getValue().doubleValue();
         int var6 = Night.worldStateManager.getSpherePointCount(var4);
         if (var3 >= var6) {
            return var1.stream().sorted(Comparator.comparing(var0 -> {
               LocalPlayer var1x = mc.player;
               double var2x = var0.box().getCenter().x;
               double var4x = var0.box().getCenter().y;
               double var6x = var0.box().getCenter().z;
               return var1x.distanceToSqr(var2x, var4x, var6x);
            })).toList();
         }

         BlockPos var7 = mc.player.blockPosition();
         int var8 = var2;
         Vec3i var9 = Night.worldStateManager.getSphereOffset(var8);
         BlockPos var10 = var7.offset(var9);
         double var11 = var10.getY();
         double var13 = mc.player.getY();
         if (!(var11 > var13)) {
            BlockPos var15 = var10;
            double var16 = 1.0;
            HoleUtil.HoleInfo var18 = HoleUtil.a_HoleInfo_BlockPos_double(var15, var16);
            if (var18 != null) {
               ArrayList var19 = var1;
               HoleUtil.HoleInfo var20 = var18;
               var19.add(var20);
            } else {
               label25: {
                  if (this.doubleHoles.getValue()) {
                     BlockPos var21 = var10;
                     double var22 = 1.0;
                     HoleUtil.HoleInfo var24 = HoleUtil.b(var21, var22);
                     if (var24 != null) {
                        ArrayList var25 = var1;
                        HoleUtil.HoleInfo var26 = var24;
                        var25.add(var26);
                        break label25;
                     }
                  }

                  if (this.quadHoles.getValue()) {
                     BlockPos var27 = var10;
                     double var28 = 1.0;
                     HoleUtil.HoleInfo var30 = HoleUtil.c(var27, var28);
                     if (var30 != null) {
                        ArrayList var31 = var1;
                        HoleUtil.HoleInfo var32 = var30;
                        var31.add(var32);
                     }
                  }
               }
            }
         }

         var2++;
      }
   }
}
