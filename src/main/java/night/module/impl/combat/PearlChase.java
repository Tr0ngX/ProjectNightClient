package night.module.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
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
import night.events.impl.EntitySpawnEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;
import night.util.network.PacketPredictionUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "PearlChase",
   description = "Automatically throws an Ender Pearl to chase enemy pearl throws with trajectory prediction and obstacle avoidance.",
   category = Module.Category.COMBAT
)
public class PearlChase extends Module {
   public ModeSetting target;
   public BooleanSetting ignoreNaked;
   public ModeSetting rotate;
   public ModeSetting switchSetting;
   public NumberSetting offset;
   public NumberSetting distance;
   public ModeSetting arcMode;
   public BooleanSetting avoidObstacles;
   public NumberSetting enemyRange;
   public NumberSetting delay;
   public BooleanSetting chainThrow;
   public NumberSetting landHeight;
   public BooleanSetting whileEating;
   public BooleanSetting itemDisable;
   public BooleanSetting render;
   public NumberSetting renderTime;
   private final Set<Integer> u;
   private PearlChase.ThrowSolution v;
   private int w;
   private Vec3 x;
   private final TimerUtil y;
   private Vec3 z;
   private List<Vec3> A;

   public PearlChase() {
      String[] var1 = new String[]{"Lowest", "Furthest", "Nearest"};
      this.target = new ModeSetting("Target", "Priority used to select which enemy to chase.", "Nearest", var1);
      this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore naked players, even if wearing only elytra.", false);
      String[] var2 = new String[]{"None", "Normal", "Silent"};
      this.rotate = new ModeSetting("Rotate", "Rotation mode used before throwing the pearl.", "Silent", var2);
      this.switchSetting = new ModeSetting("Switch", "The mode that will be used for automatically switching to ender pearls.", "Silent", InventoryUtil.a);
      Double var3 = 0.0;
      Double var4 = -10.0;
      Double var5 = 10.0;
      this.offset = new NumberSetting(
         "Offset", "Distance offset relative to target landing position along flight path (positive = ahead, negative = behind).", var3, var4, var5
      );
      Double var6 = 10.0;
      Double var7 = 0.0;
      Double var8 = 50.0;
      this.distance = new NumberSetting("Distance", "Minimum horizontal travel distance that enemy will move for pearl to be chased.", var6, var7, var8);
      String[] var9 = new String[]{"Smart", "Low", "High"};
      this.arcMode = new ModeSetting(
         "ArcMode", "Trajectory selection mode: Smart (low if clear, high if blocked), Low (fast direct), High (maximum lob).", "Smart", var9
      );
      this.avoidObstacles = new BooleanSetting("AvoidObstacles", "Calculates alternate high arcs or yaw offsets if direct path is blocked by terrain.", true);
      Double var10 = 64.0;
      Double var11 = 8.0;
      Double var12 = 128.0;
      this.enemyRange = new NumberSetting("EnemyRange", "Maximum range to enemy player throwing pearl.", var10, var11, var12);
      Integer var13 = 0;
      Integer var14 = 0;
      Integer var15 = 20;
      this.delay = new NumberSetting("Delay", "Delay in ticks before throwing the pearl.", var13, var14, var15);
      this.chainThrow = new BooleanSetting(
         "ChainThrow",
         "Only throw the next pearl once your previous one is about to land; the enemy's predicted landing is queued during the wait so the target isn't lost.",
         true
      );
      BooleanSetting var17 = this.chainThrow;
      BooleanSetting.BooleanCondition var16 = new BooleanSetting.BooleanCondition(var17, true);
      Double var18 = 4.0;
      Double var19 = 1.0;
      Double var20 = 20.0;
      this.landHeight = new NumberSetting(
         "LandHeight", "Height (blocks) above ground at which your previous pearl counts as 'about to land' for ChainThrow.", var16, var18, var19, var20
      );
      this.whileEating = new BooleanSetting("WhileEating", "Allows throwing pearl while eating.", true);
      this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off module if no ender pearls are found in inventory.", false);
      this.render = new BooleanSetting("Render", "Renders predicted landing position and pearl trajectory line.", true);
      Double var21 = 3.0;
      Double var22 = 0.5;
      Double var23 = 10.0;
      this.renderTime = new NumberSetting("RenderTime", "Duration in seconds to render trajectory after throwing.", var21, var22, var23);
      this.u = new HashSet<>();
      this.v = null;
      this.w = -1;
      this.x = null;
      this.y = new TimerUtil();
      this.z = null;
      this.A = null;
   }

   @Override
   public void onEnable() {
      this.u.clear();
      this.v = null;
      this.z = null;
      this.A = null;
      this.w = -1;
      this.x = null;
   }

   @Override
   public void onDisable() {
      this.u.clear();
      this.v = null;
      this.z = null;
      this.A = null;
      this.w = -1;
      this.x = null;
   }

   @EventTarget
   public void a_void_EntitySpawnEvent(EntitySpawnEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (var1.getEntity() instanceof ThrownEnderpearl var3) {
            Entity var4 = var3.getOwner();
            LocalPlayer var5 = mc.player;
            if (var4 == var5) {
               int var6 = var3.getId();
               this.w = var6;
            } else if (this.whileEating.getValue() || !EntityUtil.a()) {
               Player var7 = null;
               Entity var8 = var3.getOwner();
               if (var8 instanceof Player) {
                  var7 = (Player)var8;
               } else {
                  double var9 = 16.0;

                  for (Player var12 : (Iterable<Player>)(Object)mc.level.players()) {
                     if (this.a_bool_Player(var12)) {
                        Player var13 = var12;
                        Vec3 var14 = var3.position();
                        double var15 = var13.distanceToSqr(var14);
                        double var17 = var9;
                        if (var15 < var17) {
                           var9 = var15;
                           var7 = var12;
                        }
                     }
                  }
               }

               if (var7 != null) {
                  Player var19 = var7;
                  if (this.a_bool_Player(var19)) {
                     Player var20 = this.x();
                     if (var20 != null) {
                        Player var21 = var7;
                        Player var22 = var20;
                        if (var21 != var22) {
                           return;
                        }
                     }

                     Set var23 = this.u;
                     Integer var24 = var3.getId();
                     if (!var23.add(var24)) {
                        return;
                     }

                     if (this.u.size() > 100) {
                        this.u.clear();
                     }

                     ThrownEnderpearl var25 = var3;
                     Player var26 = var7;
                     Vec3 var27 = this.a(var25, var26);
                     if (var27 == null) {
                        return;
                     }

                     double var28 = var27.x;
                     double var30 = var3.getX();
                     double var32 = var28 - var30;
                     double var34 = var27.z;
                     double var36 = var3.getZ();
                     double var38 = var34 - var36;
                     double var40 = var32;
                     double var42 = var32;
                     double var44 = var40 * var42;
                     double var46 = var38;
                     double var48 = var38;
                     double var50 = var46 * var48;
                     double var52 = Math.sqrt(var44 + var50);
                     double var54 = this.distance.getValue().doubleValue();
                     if (var52 < var54) {
                        return;
                     }

                     double var56 = var32;
                     double var58 = 0.0;
                     double var60 = var38;
                     Vec3 var62 = new Vec3(var56, var58, var60);
                     double var63 = var52;
                     double var65 = 1.0E-4;
                     Vec3 var74;
                     if (var63 > var65) {
                        Vec3 var67 = var62;
                        double var68 = 1.0;
                        double var70 = var52;
                        double var72 = var68 / var70;
                        var74 = var67.scale(var72);
                     } else {
                        var74 = Vec3.ZERO;
                     }

                     Vec3 var75 = var27;
                     Vec3 var76 = var74;
                     double var77 = this.offset.getValue().doubleValue();
                     Vec3 var79 = var76.scale(var77);
                     Vec3 var80 = var75.add(var79);
                     ClientLevel var81 = mc.level;
                     Vec3 var82 = var80;
                     double var83 = 0.0;
                     double var85 = 2.0;
                     double var87 = 0.0;
                     Vec3 var89 = var82.add(var83, var85, var87);
                     Vec3 var90 = var80;
                     double var91 = 0.0;
                     double var93 = 10.0;
                     double var95 = 0.0;
                     Vec3 var97 = var90.subtract(var91, var93, var95);
                     LocalPlayer var98 = mc.player;
                     BlockHitResult var99 = var81.clip(new ClipContext(var89, var97, Block.COLLIDER, Fluid.NONE, var98));
                     if (var99.getType() != Type.MISS) {
                        var80 = var99.getLocation();
                     }

                     Vec3 var100 = var80;
                     PearlChase.AngleTrajectory var101 = this.a_AngleTrajectory_Vec3(var100);
                     if (var101 == null) {
                        return;
                     }

                     if (this.chainThrow.getValue() && this.v() && !this.w()) {
                        Vec3 var102 = var80;
                        this.x = var102;
                        return;
                     }

                     int var103 = this.delay.getValue().intValue();
                     if (var103 <= 0) {
                        float var104 = var101.value();
                        float var105 = var101.value2();
                        Vec3 var106 = var80;
                        List var107 = var101.list();
                        this.a_void_float_float_Vec3_List(var104, var105, var106, var107);
                     } else {
                        Vec3 var108 = var80;
                        float var109 = var101.value();
                        float var110 = var101.value2();
                        int var111 = var103;
                        List var112 = var101.list();
                        this.v = new PearlChase.ThrowSolution(var108, var109, var110, var111, var112);
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.v != null) {
            if (!this.whileEating.getValue() && EntityUtil.a()) {
               return;
            }

            int var2 = this.v.count() - 1;
            if (var2 <= 0) {
               Vec3 var3 = this.v.vec();
               PearlChase.AngleTrajectory var4 = this.a_AngleTrajectory_Vec3(var3);
               if (var4 != null) {
                  float var5 = var4.value();
                  float var6 = var4.value2();
                  Vec3 var7 = this.v.vec();
                  List var8 = var4.list();
                  this.a_void_float_float_Vec3_List(var5, var6, var7, var8);
               } else {
                  float var9 = this.v.value();
                  float var10 = this.v.value2();
                  Vec3 var11 = this.v.vec();
                  List var12 = this.v.list();
                  this.a_void_float_float_Vec3_List(var9, var10, var11, var12);
               }

               this.v = null;
            } else {
               Vec3 var13 = this.v.vec();
               float var14 = this.v.value();
               float var15 = this.v.value2();
               int var16 = var2;
               List var17 = this.v.list();
               this.v = new PearlChase.ThrowSolution(var13, var14, var15, var16, var17);
            }
         }

         if (this.x != null && (!this.v() || this.w()) && (this.whileEating.getValue() || !EntityUtil.a())) {
            Vec3 var18 = this.x;
            this.x = null;
            Vec3 var19 = var18;
            PearlChase.AngleTrajectory var20 = this.a_AngleTrajectory_Vec3(var19);
            if (var20 != null) {
               float var21 = var20.value();
               float var22 = var20.value2();
               Vec3 var23 = var18;
               List var24 = var20.list();
               this.a_void_float_float_Vec3_List(var21, var22, var23, var24);
            }
         }
      }
   }

   private boolean v() {
      if (this.w != -1 && mc.level != null) {
         ClientLevel var1 = mc.level;
         int var2 = this.w;
         if (var1.getEntity(var2) instanceof ThrownEnderpearl) {
            return true;
         }
      }

      return false;
   }

   private boolean w() {
      ClientLevel var1 = mc.level;
      int var2 = this.w;
      if (var1.getEntity(var2) instanceof ThrownEnderpearl var4) {
         ClientLevel var5 = mc.level;
         Vec3 var6 = var4.position();
         Vec3 var7 = var4.position();
         double var8 = 0.0;
         double var10 = this.landHeight.getValue().doubleValue();
         double var12 = 0.0;
         Vec3 var14 = var7.subtract(var8, var10, var12);
         ThrownEnderpearl var15 = var4;
         boolean var16;
         if (var5.clip(new ClipContext(var6, var14, Block.COLLIDER, Fluid.NONE, var15)).getType() != Type.MISS) {
            var16 = true;
         } else {
            var16 = false;
         }

         return var16;
      } else {
         return true;
      }
   }

   private boolean a_bool_float(float var1) {
      Vec3 var2 = mc.player.getEyePosition();
      Vec3 var3 = a(var1, 0.0F);
      double var4 = 3.0;
      Vec3 var6 = var3.scale(var4);
      Vec3 var7 = var2.add(var6);
      ClientLevel var8 = mc.level;
      Vec3 var9 = var2;
      Vec3 var10 = var7;
      LocalPlayer var11 = mc.player;
      boolean var12;
      if (var8.clip(new ClipContext(var9, var10, Block.COLLIDER, Fluid.NONE, var11)).getType() != Type.MISS) {
         var12 = true;
      } else {
         var12 = false;
      }

      return var12;
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.render.getValue() && this.z != null && mc.player != null && mc.level != null) {
         long var2 = this.y.getElapsedMs();
         double var4 = this.renderTime.getValue().doubleValue();
         double var6 = 1000.0;
         long var8 = (long)(var4 * var6);
         long var10 = var2;
         long var12 = var8;
         if (var10 > var12) {
            this.z = null;
            this.A = null;
         } else {
            float var14 = (float)var2;
            float var15 = (float)var8;
            float var16 = var14 / var15;
            float var17 = 1.0F - var16;
            int var18 = (int)(60.0F * var17);
            float var19 = var17;
            int var20 = (int)(220.0F * var19);
            if (var18 > 0 || var20 > 0) {
               int var21 = var18;
               Color var22 = new Color(0, 230, 255, var21);
               int var23 = var20;
               Color var24 = new Color(0, 230, 255, var23);
               int var25 = var20;
               Color var26 = new Color(80, 255, 180, var25);
               double var27 = this.z.x;
               double var29 = 0.35;
               double var31 = var27 - var29;
               double var33 = this.z.y;
               double var35 = this.z.z;
               double var37 = 0.35;
               double var39 = var35 - var37;
               double var41 = this.z.x;
               double var43 = 0.35;
               double var45 = var41 + var43;
               double var47 = this.z.y;
               double var49 = 0.7;
               double var51 = var47 + var49;
               double var53 = this.z.z;
               double var55 = 0.35;
               double var57 = var53 + var55;
               AABB var59 = new AABB(var31, var33, var39, var45, var51, var57);
               PoseStack var60 = var1.getMatrices();
               AABB var61 = var59;
               Color var62 = var22;
               Render3DUtil.a_void_PoseStack_AABB_Color(var60, var61, var62);
               PoseStack var63 = var1.getMatrices();
               AABB var64 = var59;
               Color var65 = var24;
               Render3DUtil.b(var63, var64, var65);
               if (this.A != null && this.A.size() > 1) {
                  int var66 = 0;

                  while (true) {
                     int var67 = var66;
                     int var68 = this.A.size() - 1;
                     if (var67 >= var68) {
                        break;
                     }

                     PoseStack var69 = var1.getMatrices();
                     List var70 = this.A;
                     int var71 = var66;
                     Vec3 var72 = (Vec3)var70.get(var71);
                     List var73 = this.A;
                     int var74 = var66 + 1;
                     Vec3 var75 = (Vec3)var73.get(var74);
                     Color var76 = var26;
                     Render3DUtil.a_void_PoseStack_Vec3_Vec3_Color(var69, var72, var75, var76);
                     var66++;
                  }
               }
            }
         }
      }
   }

   private void a_void_float_float_Vec3_List(float var1, float var2, Vec3 var3, List<Vec3> var4) {
      if (this.switchSetting.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
         if (this.itemDisable.getValue()) {
            String var5 = this.getName();
            Night.chatManager.sendTaggedMessage("You are currently not holding an Ender Pearl.", var5);
            this.setEnabled(false);
         }
      } else {
         Item var6 = Items.ENDER_PEARL;
         byte var7;
         if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
            var7 = 8;
         } else {
            var7 = 35;
         }

         int var8 = InventoryUtil.a_int_Item_int_int(var6, 0, var7);
         int var9 = mc.player.getInventory().getSelectedSlot();
         if (var8 == -1) {
            if (this.itemDisable.getValue()) {
               String var10 = this.getName();
               Night.chatManager.sendTaggedMessage("No Ender Pearls could be found in your inventory.", var10);
               this.setEnabled(false);
            }
         } else {
            if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
               Night.rotationManager.sendRotationPacket(var1, var2, true);
            } else if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
               float[] var11 = new float[]{var1, var2};
               int var12 = Night.rotationManager.getModulePriority(this);
               Night.rotationManager.rotate(var11, var12);
               mc.player.setYRot(var1);
               mc.player.setXRot(var2);
            }

            String var13 = this.switchSetting.getValue();
            int var14 = var8;
            int var15 = var9;
            InventoryUtil.a_bool_String_int_int(var13, var14, var15);
            PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var2x, var1, var2));
            ClientPacketListener var16 = mc.getConnection();
            Packet var17 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var16.send(var17);
            String var18 = this.switchSetting.getValue();
            int var19 = var8;
            int var20 = var9;
            InventoryUtil.b(var18, var19, var20);
            this.z = var3;
            this.A = var4;
            this.y.reset();
         }
      }
   }

   private Vec3 a(ThrownEnderpearl var1, Player var2) {
      Vec3 var3 = var1.position();
      Vec3 var4 = var1.getDeltaMovement();
      double var5 = var4.lengthSqr();
      double var7 = 1.0E-4;
      if (var5 < var7 && var2 != null) {
         float var9 = var2.getYRot();
         float var10 = var2.getXRot();
         Vec3 var11 = var2.getDeltaMovement();
         boolean var12 = var2.onGround();
         var4 = a_Vec3_float_float_Vec3_bool(var9, var10, var11, var12);
      }

      for (int var13 = 0; var13 < 300; var13++) {
         Vec3 var14 = var3;
         Vec3 var15 = var4;
         Vec3 var16 = var14.add(var15);
         ClientLevel var17 = mc.level;
         Vec3 var18 = var3;
         Vec3 var19 = var16;
         BlockHitResult var20 = var17.clip(new ClipContext(var18, var19, Block.COLLIDER, Fluid.NONE, var1));
         if (var20.getType() != Type.MISS) {
            return var20.getLocation();
         }

         var3 = var16;
         Vec3 var21 = var4;
         double var22 = 0.99;
         Vec3 var24 = var21.scale(var22);
         double var25 = 0.0;
         double var27 = 0.03;
         double var29 = 0.0;
         var4 = var24.subtract(var25, var27, var29);
      }

      return var3;
   }

   private PearlChase.AngleTrajectory a_AngleTrajectory_Vec3(Vec3 var1) {
      Vec3 var2;
      float var8;
      float var9;
      float var10;
      double var11;
      double var41;
      boolean var44;
      label81: {
         var2 = mc.player.getEyePosition();
         Vec3 var3 = var1.subtract(var2);
         double var4 = -var3.x;
         double var6 = var3.z;
         var8 = (float)Math.toDegrees(Math.atan2(var4, var6));
         var9 = var8;
         var10 = 0.0F;
         var11 = Double.MAX_VALUE;
         double var13 = var3.x;
         double var15 = var3.x;
         double var17 = var13 * var15;
         double var19 = var3.z;
         double var21 = var3.z;
         double var23 = var19 * var21;
         double var25 = Math.sqrt(var17 + var23);
         double var27 = 0.6;
         double var29 = var25;
         double var31 = 0.012;
         double var33 = var29 * var31;
         double var35 = var27 - var33;
         double var37 = 0.03;
         double var39 = 0.6;
         var41 = Mth.clamp(var35, var37, var39);
         if (this.avoidObstacles.getValue()) {
            float var43 = var8;
            if (this.a_bool_float(var43)) {
               var44 = true;
               break label81;
            }
         }

         var44 = false;
      }

      boolean var45 = var44;
      float[] var46;
      if (!this.avoidObstacles.getValue()) {
         var46 = new float[]{0.0F};
      } else if (var45) {
         var46 = new float[]{0.0F, -2.5F, 2.5F, -5.0F, 5.0F, -8.0F, 8.0F, -12.0F, 12.0F};
      } else {
         var46 = new float[]{0.0F, -1.0F, 1.0F, -2.5F, 2.5F, -4.0F, 4.0F};
      }

      float[] var47 = var46;
      int var48 = var47.length;
      int var49 = 0;

      while (true) {
         int var50 = var49;
         int var51 = var48;
         if (var50 >= var51) {
            break;
         }

         float[] var52 = var47;
         int var53 = var49;
         float var54 = var52[var53];
         float var55 = var8;
         float var56 = var54;
         float var57 = var55 + var56;

         for (float var58 = -85.0F; var58 <= 85.0F; var58++) {
            Vec3 var59 = var2;
            float var60 = var57;
            float var61 = var58;
            PearlChase.SimulatedLanding var62 = this.a(var59, var60, var61, var1, false);
            double var63 = var62.a();
            if (var62.b()) {
               double var65 = var63;
               double var67 = 500.0;
               var63 = var65 + var67;
            }

            double var69 = 0.0;
            if (this.arcMode.getValue().equalsIgnoreCase("Low")) {
               float var71 = var58;
               float var72 = 0.0F - var71;
               double var73 = Math.max(0.0F, var72);
               double var75 = 0.1;
               var69 = var73 * var75;
            } else if (this.arcMode.getValue().equalsIgnoreCase("High")) {
               double var81 = 0.0;
               double var83 = var58;
               double var85 = 50.0;
               double var87 = var83 + var85;
               double var89 = Math.max(var81, var87);
               double var91 = 0.2;
               var69 = var89 * var91;
            } else if (this.arcMode.getValue().equalsIgnoreCase("Smart")) {
               float var93 = var58;
               float var94 = 0.0F - var93;
               double var95 = Math.max(0.0F, var94);
               double var97 = var41;
               var69 = var95 * var97;
            }

            double var99 = 1.5;
            double var101 = var63;
            double var103 = 1.5;
            double var109;
            if (var101 > var103) {
               double var105 = var63;
               double var107 = 1000.0;
               var109 = var105 * var107;
            } else {
               var109 = 0.0;
            }

            double var111 = var69;
            double var113 = var109 + var111;
            double var115 = var11;
            if (var113 < var115) {
               var11 = var113;
               var10 = var58;
               var9 = var57;
            }
         }

         double var77 = var11;
         double var79 = 1.0;
         if (var77 < var79) {
            break;
         }

         var49++;
      }

      float var117 = var10;
      Vec3 var118 = var2;
      float var119 = var9;
      float var120 = var10;
      double var121 = this.a(var118, var119, var120, var1, false).a();
      float var123 = var10 - 1.5F;

      while (true) {
         float var124 = var123;
         float var125 = var10 + 1.5F;
         if (!(var124 <= var125)) {
            Vec3 var138 = var2;
            float var139 = var9;
            float var140 = var117;
            PearlChase.SimulatedLanding var141 = this.a(var138, var139, var140, var1, true);
            float var142 = var9;
            float var143 = var117;
            List var144 = var141.c();
            return new PearlChase.AngleTrajectory(var142, var143, var144);
         }

         Vec3 var126 = var2;
         float var127 = var9;
         float var128 = var123;
         PearlChase.SimulatedLanding var129 = this.a(var126, var127, var128, var1, false);
         double var130 = var129.a();
         double var132;
         if (var129.b()) {
            var132 = 500.0;
         } else {
            var132 = 0.0;
         }

         double var134 = var130 + var132;
         double var136 = var121;
         if (var134 < var136) {
            var121 = var134;
            var117 = var123;
         }

         var123 += 0.1F;
      }
   }

   private PearlChase.SimulatedLanding a(Vec3 var1, float var2, float var3, Vec3 var4, boolean var5) {
      Vec3 var6;
      if (mc.player != null) {
         var6 = mc.player.getDeltaMovement();
      } else {
         var6 = Vec3.ZERO;
      }

      Vec3 var7 = var6;
      boolean var8;
      if (mc.player != null && !mc.player.onGround()) {
         var8 = false;
      } else {
         var8 = true;
      }

      boolean var9 = var8;
      Vec3 var10 = var7;
      boolean var11 = var9;
      Vec3 var12 = a_Vec3_float_float_Vec3_bool(var2, var3, var10, var11);
      Vec3 var13 = var1;
      ArrayList var14;
      if (var5) {
         var14 = new ArrayList();
      } else {
         var14 = null;
      }

      ArrayList var15 = var14;
      if (var5) {
         ArrayList var16 = var15;
         Vec3 var17 = var13;
         var16.add(var17);
      }

      Vec3 var18 = null;
      boolean var19 = false;
      double var20 = var4.distanceTo(var1);

      for (int var22 = 0; var22 < 250; var22++) {
         Vec3 var23 = var13;
         Vec3 var24 = var12;
         Vec3 var25 = var23.add(var24);
         ClientLevel var26 = mc.level;
         Vec3 var27 = var13;
         Vec3 var28 = var25;
         LocalPlayer var29 = mc.player;
         BlockHitResult var30 = var26.clip(new ClipContext(var27, var28, Block.COLLIDER, Fluid.NONE, var29));
         if (var30.getType() != Type.MISS) {
            var18 = var30.getLocation();
            if (var5) {
               ArrayList var31 = var15;
               Vec3 var32 = var18;
               var31.add(var32);
            }

            label74: {
               double var33 = var18.distanceTo(var1);
               double var35 = var18.distanceTo(var4);
               double var37 = var33;
               double var39 = 2.5;
               if (var37 < var39) {
                  double var41 = var35;
                  double var43 = 2.0;
                  if (var41 > var43) {
                     break label74;
                  }
               }

               double var45 = var33;
               double var47 = var20;
               double var49 = 0.7;
               double var51 = var47 * var49;
               if (!(var45 < var51)) {
                  break;
               }

               double var53 = var35;
               double var55 = 3.0;
               if (!(var53 > var55)) {
                  break;
               }
            }

            var19 = true;
            break;
         }

         var13 = var25;
         if (var5) {
            ArrayList var57 = var15;
            Vec3 var58 = var13;
            var57.add(var58);
         }

         Vec3 var59 = var12;
         double var60 = 0.99;
         Vec3 var62 = var59.scale(var60);
         double var63 = 0.0;
         double var65 = 0.03;
         double var67 = 0.0;
         var12 = var62.subtract(var63, var65, var67);
      }

      if (var18 == null) {
         var18 = var13;
      }

      double var69 = var18.distanceTo(var4);
      Vec3 var71 = var18;
      double var72 = var69;
      boolean var74 = var19;
      int var75;
      if (var15 != null) {
         var75 = var15.size();
      } else {
         var75 = 0;
      }

      ArrayList var76 = var15;
      return new PearlChase.SimulatedLanding(var71, var72, var74, var75, var76);
   }

   private static Vec3 a(float var0, float var1) {
      float var2 = Mth.sin(var0 * (float) (Math.PI / 180.0));
      float var3 = Mth.cos(var1 * (float) (Math.PI / 180.0));
      float var4 = var2 * var3;
      float var5 = 0.0F - var4;
      float var6 = Mth.sin(var1 * (float) (Math.PI / 180.0));
      float var7 = 0.0F - var6;
      float var8 = Mth.cos(var0 * (float) (Math.PI / 180.0));
      float var9 = Mth.cos(var1 * (float) (Math.PI / 180.0));
      float var10 = var8 * var9;
      double var11 = var5;
      double var13 = var7;
      double var15 = var10;
      return new Vec3(var11, var13, var15).normalize();
   }

   private static Vec3 a_Vec3_float_float_Vec3_bool(float var0, float var1, Vec3 var2, boolean var3) {
      Vec3 var4 = a(var0, var1);
      double var5 = var2.x;
      double var7;
      if (var3) {
         var7 = 0.0;
      } else {
         var7 = var2.y;
      }

      double var9 = var2.z;
      Vec3 var11 = new Vec3(var5, var7, var9);
      Vec3 var12 = var4;
      double var13 = 1.5;
      Vec3 var15 = var12.scale(var13);
      Vec3 var16 = var11;
      return var15.add(var16);
   }

   private boolean a_bool_Player(Player var1) {
      if (var1 != null) {
         LocalPlayer var2 = mc.player;
         if (var1 != var2) {
            if (var1.isDeadOrDying()) {
               return false;
            }

            double var3 = mc.player.distanceToSqr(var1);
            double var5 = Mth.square(this.enemyRange.getValue().doubleValue());
            if (var3 > var5) {
               return false;
            }

            String var7 = var1.getName().getString();
            if (Night.friendManager.isFriend(var7)) {
               return false;
            }

            if (this.ignoreNaked.getValue() && EntityUtil.b_bool_Player(var1)) {
               return false;
            }

            return true;
         }
      }

      return false;
   }

   private Player x() {
      if (mc.player != null && mc.level != null) {
         Player var1 = null;
         double var2 = 0.0;

         for (Player var5 : (Iterable<Player>)(Object)mc.level.players()) {
            if (this.a_bool_Player(var5)) {
               double var10;
               switch (this.target.getValue()) {
                  case "Lowest":
                     float var8 = var5.getHealth();
                     float var9 = var5.getAbsorptionAmount();
                     var10 = var8 + var9;
                     break;
                  case "Furthest":
                     LocalPlayer var12 = mc.player;
                     Player var13 = var5;
                     var10 = -var12.distanceToSqr(var13);
                     break;
                  default:
                     LocalPlayer var14 = mc.player;
                     Player var15 = var5;
                     var10 = var14.distanceToSqr(var15);
               }

               if (var1 != null) {
                  double var16 = var10;
                  double var18 = var2;
                  if (!(var16 < var18)) {
                     continue;
                  }
               }

               var1 = var5;
               var2 = var10;
            }
         }

         return var1;
      } else {
         return null;
      }
   }

   record AngleTrajectory(float value, float value2, List<Vec3> list) {
   }

   record SimulatedLanding(Vec3 vec, double distance, boolean flag, int count, List<Vec3> list) {
      SimulatedLanding(Vec3 vec, double distance, boolean flag, int count, List<Vec3> list) {
         this.vec = vec;
         double var7 = distance;
         this.distance = var7;
         this.flag = flag;
         this.count = count;
         this.list = list;
      }

      public double a() {
         return this.distance;
      }

      public boolean b() {
         return this.flag;
      }

      public List<Vec3> c() {
         return this.list;
      }
   }

   record ThrowSolution(Vec3 vec, float value, float value2, int count, List<Vec3> list) {
   }
}
