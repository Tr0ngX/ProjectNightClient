package night.module.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.Comparator;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.render.Render3DUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "SpearKill", description = "Lunges at players and attacks with a spear/trident.", category = Module.Category.COMBAT)
public class SpearKill extends Module {
   public NumberSetting range;
   public NumberSetting wallRange;
   public NumberSetting hitRange;
   public NumberSetting delay;
   public BooleanSetting attackDelay;
   public NumberSetting attackSpeed;
   public ModeSetting autoSwap;
   public BooleanSetting rotate;
   public BooleanSetting lunge;
   public NumberSetting lungeSpeed;
   public NumberSetting lungeY;
   public NumberSetting stopDistance;
   public NumberSetting lungeDelay;
   public BooleanSetting swing;
   public BooleanSetting render;
   public BooleanSetting renderLine;
   public ColorSetting fillColor;
   public ColorSetting lineColor;
   private long w;
   private long x;
   private Player y;

   public SpearKill() {
      Float var1 = 12.0F;
      Float var2 = 1.0F;
      Float var3 = 100.0F;
      this.range = new NumberSetting("Range", "Range to search for players.", var1, var2, var3);
      Float var4 = 4.0F;
      Float var5 = 0.0F;
      Float var6 = 100.0F;
      this.wallRange = new NumberSetting("WallRange", "Range to search through walls.", var4, var5, var6);
      Float var7 = 4.2F;
      Float var8 = 1.0F;
      Float var9 = 100.0F;
      this.hitRange = new NumberSetting("HitRange", "Range to send spear attacks.", var7, var8, var9);
      Integer var10 = 100;
      Integer var11 = 0;
      Integer var12 = 1000;
      this.delay = new NumberSetting("Delay", "Minimum delay in milliseconds between attacks.", var10, var11, var12);
      this.attackDelay = new BooleanSetting("AttackDelay", "Uses vanilla attack cooldown before attacking.", true);
      BooleanSetting var14 = this.attackDelay;
      BooleanSetting.BooleanCondition var13 = new BooleanSetting.BooleanCondition(var14, false);
      Float var15 = 8.0F;
      Float var16 = 1.0F;
      Float var17 = 20.0F;
      this.attackSpeed = new NumberSetting("AttackSpeed", "Attacks per second when AttackDelay is off.", var13, var15, var16, var17);
      String[] var18 = new String[]{"Off", "Normal", "Silent"};
      this.autoSwap = new ModeSetting("AutoSwap", "Swaps to a spear before attacking.", "Normal", var18);
      this.rotate = new BooleanSetting("Rotate", "Rotates to the target before lunging and attacking.", true);
      this.lunge = new BooleanSetting("Lunge", "Pushes you toward the target.", true);
      BooleanSetting var20 = this.lunge;
      BooleanSetting.BooleanCondition var19 = new BooleanSetting.BooleanCondition(var20, true);
      Float var21 = 1.25F;
      Float var22 = 0.1F;
      Float var23 = 5.0F;
      this.lungeSpeed = new NumberSetting("LungeSpeed", "Horizontal lunge speed.", var19, var21, var22, var23);
      BooleanSetting var25 = this.lunge;
      BooleanSetting.BooleanCondition var24 = new BooleanSetting.BooleanCondition(var25, true);
      Float var26 = 0.08F;
      Float var27 = 0.0F;
      Float var28 = 1.0F;
      this.lungeY = new NumberSetting("LungeY", "Upward velocity added while lunging.", var24, var26, var27, var28);
      BooleanSetting var30 = this.lunge;
      BooleanSetting.BooleanCondition var29 = new BooleanSetting.BooleanCondition(var30, true);
      Float var31 = 2.4F;
      Float var32 = 0.0F;
      Float var33 = 6.0F;
      this.stopDistance = new NumberSetting("StopDistance", "Stops lunging inside this distance.", var29, var31, var32, var33);
      BooleanSetting var35 = this.lunge;
      BooleanSetting.BooleanCondition var34 = new BooleanSetting.BooleanCondition(var35, true);
      Integer var36 = 150;
      Integer var37 = 0;
      Integer var38 = 1000;
      this.lungeDelay = new NumberSetting("LungeDelay", "Delay in milliseconds between lunges.", var34, var36, var37, var38);
      this.swing = new BooleanSetting("Swing", "Swings the hand after attacking.", true);
      this.render = new BooleanSetting("Render", "Renders the current SpearKill target.", true);
      BooleanSetting var40 = this.render;
      BooleanSetting.BooleanCondition var39 = new BooleanSetting.BooleanCondition(var40, true);
      this.renderLine = new BooleanSetting("RenderLine", "Renders a line to the current target.", var39, true);
      BooleanSetting var42 = this.render;
      BooleanSetting.BooleanCondition var41 = new BooleanSetting.BooleanCondition(var42, true);
      Color var43 = new Color(255, 210, 90, 45);
      this.fillColor = new ColorSetting("FillColor", "Fill color for the target render.", var41, new ColorSetting.ColorValue(var43, false, false));
      BooleanSetting var45 = this.render;
      BooleanSetting.BooleanCondition var44 = new BooleanSetting.BooleanCondition(var45, true);
      Color var46 = new Color(255, 210, 90, 180);
      this.lineColor = new ColorSetting("LineColor", "Outline and line color for the target render.", var44, new ColorSetting.ColorValue(var46, false, false));
      long var47 = 0L;
      this.w = var47;
      long var49 = 0L;
      this.x = var49;
   }

   @Override
   public String getDisplayInfo() {
      String var1;
      if (this.y != null) {
         var1 = this.y.getName().getString();
      } else {
         var1 = "Spear";
      }

      return var1;
   }

   @Override
   public void onDisable() {
      this.y = null;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null && !mc.player.isSpectator()) {
         Player var2 = this.v();
         this.y = var2;
         if (this.y != null) {
            int var3 = this.y();
            if (var3 != -1 || this.x()) {
               boolean var4 = false;
               int var5 = mc.player.getInventory().getSelectedSlot();
               if (!this.x()) {
                  if (this.autoSwap.getValue().equalsIgnoreCase("Off")) {
                     return;
                  }

                  if (this.autoSwap.getValue().equalsIgnoreCase("Normal")) {
                     int var6 = var3;
                     int var7 = var5;
                     InventoryUtil.a_bool_String_int_int("Normal", var6, var7);
                  } else if (this.autoSwap.getValue().equalsIgnoreCase("Silent")) {
                     int var8 = var3;
                     int var9 = var5;
                     InventoryUtil.a_bool_String_int_int("Silent", var8, var9);
                     var4 = true;
                  }
               }

               if (this.rotate.getValue()) {
                  double var10 = this.y.getX();
                  double var12 = this.y.getY();
                  double var14 = this.y.getEyeHeight();
                  double var16 = 2.0;
                  double var18 = var14 / var16;
                  double var20 = var12 + var18;
                  double var22 = this.y.getZ();
                  float[] var24 = RotationMathUtil.a(var10, var20, var22);
                  float var25 = var24[0];
                  float var26 = var24[1];
                  int var27 = Night.rotationManager.getModulePriority(this);
                  Night.rotationManager.rotate(var25, var26, this, var27);
               }

               LocalPlayer var28 = mc.player;
               Player var29 = this.y;
               double var30 = var28.distanceTo(var29);
               if (this.lunge.getValue()) {
                  double var32 = var30;
                  double var34 = this.stopDistance.getValue().doubleValue();
                  if (var32 > var34) {
                     long var36 = System.currentTimeMillis();
                     long var38 = this.x;
                     long var40 = var36 - var38;
                     long var42 = this.lungeDelay.getValue().longValue();
                     if (var40 >= var42) {
                        Player var44 = this.y;
                        this.b(var44);
                        long var45 = System.currentTimeMillis();
                        this.x = var45;
                     }
                  }
               }

               double var47 = var30;
               double var49 = this.hitRange.getValue().doubleValue();
               if (var47 <= var49 && this.w()) {
                  Player var51 = this.y;
                  this.c(var51);
                  long var52 = System.currentTimeMillis();
                  this.w = var52;
               }

               if (var4) {
                  int var54 = var3;
                  int var55 = var5;
                  InventoryUtil.b("Silent", var54, var55);
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.render.getValue() && this.y != null && this.y.isAlive() && !this.y.isRemoved()) {
         PoseStack var2 = var1.getMatrices();
         Player var3 = this.y;
         float var4 = var1.getTickDelta();
         Vec3 var5 = EntityUtil.a_Vec3_Entity_float(var3, var4);
         AABB var6 = this.y.getBoundingBox();
         double var7 = var5.x;
         double var9 = this.y.getX();
         double var11 = var7 - var9;
         double var13 = var5.y;
         double var15 = this.y.getY();
         double var17 = var13 - var15;
         double var19 = var5.z;
         double var21 = this.y.getZ();
         double var23 = var19 - var21;
         AABB var25 = var6.move(var11, var17, var23);
         PoseStack var26 = var2;
         AABB var27 = var25;
         Color var28 = this.fillColor.getColor();
         Render3DUtil.a_void_PoseStack_AABB_Color(var26, var27, var28);
         PoseStack var29 = var2;
         AABB var30 = var25;
         Color var31 = this.lineColor.getColor();
         Render3DUtil.b(var29, var30, var31);
         if (this.renderLine.getValue()) {
            LocalPlayer var32 = mc.player;
            float var33 = var1.getTickDelta();
            Vec3 var34 = EntityUtil.a_Vec3_Entity_float(var32, var33);
            double var35 = 0.0;
            LocalPlayer var37 = mc.player;
            Pose var38 = mc.player.getPose();
            double var39 = var37.getEyeHeight(var38);
            double var41 = 0.0;
            Vec3 var43 = var34.add(var35, var39, var41);
            PoseStack var44 = var2;
            Vec3 var45 = var43;
            Vec3 var46 = var25.getCenter();
            Color var47 = this.lineColor.getColor();
            Render3DUtil.a_void_PoseStack_Vec3_Vec3_Color(var44, var45, var46, var47);
         }
      }
   }

   private Player v() {
      return mc.level.players().stream().filter(this::a_bool_Player).min(Comparator.comparingDouble(var0 -> mc.player.distanceToSqr(var0))).orElse(null);
   }

   private boolean a_bool_Player(Player var1) {
      LocalPlayer var2 = mc.player;
      if (var1 == var2 || !var1.isAlive() || var1.isRemoved() || var1.isSpectator()) {
         return false;
      }

      if (EntityUtil.c_bool_Entity(var1)) {
         return false;
      }

      String var3 = var1.getName().getString();
      if (Night.friendManager.isFriend(var3)) {
         return false;
      }

      double var4 = mc.player.distanceTo(var1);
      double var6 = this.range.getValue().doubleValue();
      if (var4 > var6) {
         return false;
      }

      if (!mc.player.hasLineOfSight(var1)) {
         double var8 = var4;
         double var10 = this.wallRange.getValue().doubleValue();
         if (!(var8 <= var10)) {
            return false;
         }
      }

      return true;
   }

   private boolean w() {
      long var1 = System.currentTimeMillis();
      long var3 = this.w;
      long var5 = var1 - var3;
      long var7 = this.delay.getValue().longValue();
      if (var5 < var7) {
         return false;
      }

      if (this.attackDelay.getValue()) {
         boolean var9;
         if (mc.player.getAttackStrengthScale(0.5F) >= 1.0F) {
            var9 = true;
         } else {
            var9 = false;
         }

         return var9;
      } else {
         long var10 = System.currentTimeMillis();
         long var12 = this.w;
         long var14 = var10 - var12;
         float var16 = Math.max(this.attackSpeed.getValue().floatValue(), 0.1F);
         long var17 = (long)(1000.0F / var16);
         boolean var19;
         if (var14 >= var17) {
            var19 = true;
         } else {
            var19 = false;
         }

         return var19;
      }
   }

   private void b(Player var1) {
      Vec3 var2 = var1.position();
      Vec3 var3 = mc.player.position();
      Vec3 var4 = var2.subtract(var3);
      double var5 = var4.x;
      double var7 = 0.0;
      double var9 = var4.z;
      Vec3 var11 = new Vec3(var5, var7, var9);
      double var12 = var11.length();
      double var14 = 1.0E-4;
      if (!(var12 < var14)) {
         Vec3 var16 = var11.normalize();
         double var17 = mc.player.getDeltaMovement().y;
         double var19 = this.lungeY.getValue().doubleValue();
         double var21 = Math.max(var17, var19);
         LocalPlayer var23 = mc.player;
         double var24 = var16.x;
         double var26 = this.lungeSpeed.getValue().doubleValue();
         double var28 = var24 * var26;
         double var30 = var21;
         double var32 = var16.z;
         double var34 = this.lungeSpeed.getValue().doubleValue();
         double var36 = var32 * var34;
         var23.setDeltaMovement(var28, var30, var36);
      }
   }

   private void c(Player var1) {
      MultiPlayerGameMode var2 = mc.gameMode;
      LocalPlayer var3 = mc.player;
      var2.attack(var3, var1);
      mc.player.resetAttackStrengthTicker();
      if (this.swing.getValue()) {
         PacketAndRenderUtil.a(mc.player, InteractionHand.MAIN_HAND);
      } else {
         ClientPacketListener var4 = mc.getConnection();
         Packet var5 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
         var4.send(var5);
      }
   }

   private boolean x() {
      if (mc.player == null) {
         return false;
      }

      ItemStack var1 = mc.player.getMainHandItem();
      boolean var2;
      if (!var1.is(Items.TRIDENT) && !(var1.getItem() instanceof TridentItem)) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   private int y() {
      if (mc.player == null) {
         return -1;
      }

      for (int var1 = 0; var1 < 9; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         ItemStack var4 = var2.getItem(var3);
         if (var4.is(Items.TRIDENT) || var4.getItem() instanceof TridentItem) {
            return var1;
         }
      }

      return -1;
   }
}
