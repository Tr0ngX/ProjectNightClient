package night.module.impl.combat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.EventTarget;
import night.events.impl.AttackEntityEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.pingbypass.network.RubberbandTracker;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "Criticals",
   description = "Changes player movement for always landing critical hits.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class Criticals extends Module {
   public ModeSetting mode;
   public BooleanSetting onlyPhased;
   public BooleanSetting onlyStandingStill;
   public BooleanSetting headCovered;
   private long i;

   public Criticals() {
      String[] var1 = new String[]{"Packet", "Grim", "GrimNew"};
      this.mode = new ModeSetting("Mode", "The method that will be used to achieve critical hits.", "Packet", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Grim", "GrimNew"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      this.onlyPhased = new BooleanSetting("OnlyPhased", "Only crits when you are phased into blocks.", var2, true);
      ModeSetting var6 = this.mode;
      String[] var7 = new String[]{"Grim", "GrimNew"};
      ModeSetting.ModeCondition var5 = new ModeSetting.ModeCondition(var6, var7);
      this.onlyStandingStill = new BooleanSetting("OnlyStandingStill", "Only crits when you are standing still.", var5, true);
      ModeSetting var9 = this.mode;
      String[] var10 = new String[]{"Grim", "GrimNew"};
      ModeSetting.ModeCondition var8 = new ModeSetting.ModeCondition(var9, var10);
      this.headCovered = new BooleanSetting("HeadCovered", "Only crits when there is a block covering your head.", var8, true);
      long var11 = 0L;
      this.i = var11;
   }

   @EventTarget
   public void a_void_AttackEntityEvent(AttackEntityEvent var1) {
      if (!this.isProxyDelegated()) {
         if (mc.player != null && mc.level != null) {
            if (!mc.player.isFallFlying()
               && !mc.player.isInWater()
               && !mc.player.isInLava()
               && !mc.player.isSuppressingSlidingDownLadder()
               && !mc.player.hasEffect(MobEffects.BLINDNESS)) {
               Entity var2 = var1.getEntity();
               if (var2 instanceof LivingEntity && ((LivingEntity)var2).isAlive() && !(var2 instanceof EndCrystal) && !(var2 instanceof ItemFrame)) {
                  if (mc.player.isHandsBusy()) {
                     Entity var3 = var2;
                     this.a_void_Entity(var3);
                  } else {
                     this.v();
                     LocalPlayer var4 = mc.player;
                     Entity var5 = var2;
                     var4.crit(var5);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (!this.isProxyDelegated()) {
         if (var1.getPacket() instanceof ServerboundAttackPacket) {
            long var2 = System.currentTimeMillis();
            long var4 = this.i;
            long var6 = var2 - var4;
            long var8 = 50L;
            if (var6 < var8) {
               return;
            }

            if (mc.player == null || mc.level == null) {
               return;
            }

            if (mc.player.isFallFlying()
               || mc.player.isInWater()
               || mc.player.isInLava()
               || mc.player.isSuppressingSlidingDownLadder()
               || mc.player.hasEffect(MobEffects.BLINDNESS)) {
               return;
            }

            this.v();
         }
      }
   }

   private void a_void_Entity(Entity var1) {
      if (this.mode.getValue().equalsIgnoreCase("Packet")) {
         for (int var2 = 0; var2 < 5; var2++) {
            ClientPacketListener var3 = mc.getConnection();
            int var4 = var1.getId();
            var3.send(new ServerboundAttackPacket(var4));
            ClientPacketListener var5 = mc.getConnection();
            Packet var6 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var5.send(var6);
         }
      }
   }

   private void v() {
      long var1 = System.currentTimeMillis();
      this.i = var1;
      double var3 = mc.player.getX();
      double var5 = mc.player.getY();
      double var7 = mc.player.getZ();
      double var9 = var3;
      double var11 = var5;
      double var13 = var7;
      RubberbandTracker.a(() -> {
         switch (this.mode.getValue()) {
            case "Packet":
               double var9x = var9;
               double var11x = var11;
               double var13x = var13;
               this.a(var9x, var11x, var13x);
               break;
            case "Grim":
               double var15 = var9;
               double var17 = var11;
               double var19 = var13;
               this.b(var15, var17, var19);
               break;
            case "GrimNew":
               double var21 = var9;
               double var23 = var11;
               double var25 = var13;
               this.c(var21, var23, var25);
         }
      });
      ((ClientPlayerEntityAccessor)mc.player).setLastOnGround(false);
   }

   private void a(double var1, double var3, double var5) {
      if (mc.player.onGround()) {
         ClientPacketListener var7 = mc.getConnection();
         double var8 = var1;
         double var10 = var3;
         double var12 = 0.0625;
         double var14 = var10 + var12;
         double var16 = var5;
         boolean var18 = mc.player.horizontalCollision;
         var7.send(new Pos(var8, var14, var16, false, var18));
         ClientPacketListener var19 = mc.getConnection();
         double var20 = var1;
         double var22 = var3;
         double var24 = var5;
         boolean var26 = mc.player.horizontalCollision;
         var19.send(new Pos(var20, var22, var24, false, var26));
      }
   }

   private void b(double var1, double var3, double var5) {
      if (mc.player.onGround()) {
         if (this.onlyPhased.getValue()) {
            LocalPlayer var7 = mc.player;
            if (!this.b(var7)) {
               return;
            }
         }

         if (this.onlyStandingStill.getValue()) {
            double var8 = Math.abs(mc.player.getDeltaMovement().x);
            double var10 = 0.01;
            if (var8 > var10) {
               return;
            }

            double var12 = Math.abs(mc.player.getDeltaMovement().y);
            double var14 = 0.01;
            if (var12 > var14) {
               return;
            }

            double var16 = Math.abs(mc.player.getDeltaMovement().z);
            double var18 = 0.01;
            if (var16 > var18) {
               return;
            }
         }

         if (this.headCovered.getValue()) {
            BlockPos var20 = mc.player.blockPosition();
            BlockPos var21;
            if (mc.player.isVisuallyCrawling()) {
               var21 = var20.above(1);
            } else {
               var21 = var20.above(2);
            }

            BlockPos var22 = var21;
            ClientLevel var23 = mc.level;
            BlockPos var24 = var22;
            if (var23.getBlockState(var24).isAir()) {
               return;
            }
         }

         float var25 = Night.rotationManager.getServerYaw();
         float var26 = Night.rotationManager.getServerPitch();
         ClientPacketListener var27 = mc.getConnection();
         double var28 = var1;
         double var30 = var3;
         double var32 = 0.0625;
         double var34 = var30 + var32;
         double var36 = var5;
         float var38 = var25;
         float var39 = var26;
         boolean var40 = mc.player.horizontalCollision;
         var27.send(new PosRot(var28, var34, var36, var38, var39, false, var40));
         ClientPacketListener var41 = mc.getConnection();
         double var42 = var1;
         double var44 = var3;
         double var46 = 0.0625013579;
         double var48 = var44 + var46;
         double var50 = var5;
         float var52 = var25;
         float var53 = var26;
         boolean var54 = mc.player.horizontalCollision;
         var41.send(new PosRot(var42, var48, var50, var52, var53, false, var54));
         ClientPacketListener var55 = mc.getConnection();
         double var56 = var1;
         double var58 = var3;
         double var60 = 1.3579E-6;
         double var62 = var58 + var60;
         double var64 = var5;
         float var66 = var25;
         float var67 = var26;
         boolean var68 = mc.player.horizontalCollision;
         var55.send(new PosRot(var56, var62, var64, var66, var67, false, var68));
      }
   }

   private void c(double var1, double var3, double var5) {
      if (mc.player.onGround()) {
         if (this.onlyPhased.getValue()) {
            LocalPlayer var7 = mc.player;
            if (!this.b(var7)) {
               return;
            }

            LocalPlayer var8 = mc.player;
            if (!this.a_bool_Player(var8)) {
               return;
            }
         }

         if (this.onlyStandingStill.getValue()) {
            double var9 = Math.abs(mc.player.getDeltaMovement().x);
            double var11 = 0.01;
            if (var9 > var11) {
               return;
            }

            double var13 = Math.abs(mc.player.getDeltaMovement().y);
            double var15 = 0.01;
            if (var13 > var15) {
               return;
            }

            double var17 = Math.abs(mc.player.getDeltaMovement().z);
            double var19 = 0.01;
            if (var17 > var19) {
               return;
            }
         }

         if (this.headCovered.getValue()) {
            BlockPos var21 = mc.player.blockPosition();
            BlockPos var22;
            if (mc.player.isVisuallyCrawling()) {
               var22 = var21.above(1);
            } else {
               var22 = var21.above(2);
            }

            BlockPos var23 = var22;
            ClientLevel var24 = mc.level;
            BlockPos var25 = var23;
            if (var24.getBlockState(var25).isAir()) {
               return;
            }
         }

         float var26 = Night.rotationManager.getServerYaw();
         float var27 = Night.rotationManager.getServerPitch();
         double var28 = Math.random();
         double var30 = 2.0;
         double var32 = var28 * var30;
         double var34 = 1.0;
         double var36 = var32 - var34;
         double var38 = 0.001F;
         float var40 = (float)(var36 * var38);
         float var41 = var27;
         float var42 = var40;
         float var43 = Mth.clamp(var41 + var42, -90.0F, 90.0F);
         ClientPacketListener var44 = mc.getConnection();
         double var45 = var1;
         double var47 = var3;
         double var49 = 0.0626;
         double var51 = var47 + var49;
         double var53 = var5;
         float var55 = var26;
         float var56 = var43;
         boolean var57 = mc.player.horizontalCollision;
         var44.send(new PosRot(var45, var51, var53, var55, var56, false, var57));
         ClientPacketListener var58 = mc.getConnection();
         double var59 = var1;
         double var61 = var3;
         double var63 = 0.0455;
         double var65 = var61 + var63;
         double var67 = var5;
         float var69 = var26;
         float var70 = var43;
         boolean var71 = mc.player.horizontalCollision;
         var58.send(new PosRot(var59, var65, var67, var69, var70, false, var71));
      }
   }

   private boolean b(Entity var1) {
      if (var1 != null && mc.level != null) {
         AABB var2 = var1.getBoundingBox();
         int var3 = Mth.floor(var2.minX);
         int var4 = Mth.ceil(var2.maxX);
         int var5 = Mth.floor(var2.minY);
         int var6 = Mth.ceil(var2.maxY);
         int var7 = Mth.floor(var2.minZ);
         int var8 = Mth.ceil(var2.maxZ);
         int var9 = var3;

         while (true) {
            int var10 = var9;
            int var11 = var4;
            if (var10 >= var11) {
               return false;
            }

            int var12 = var5;

            while (true) {
               int var13 = var12;
               int var14 = var6;
               if (var13 >= var14) {
                  var9++;
                  break;
               }

               int var15 = var7;

               while (true) {
                  int var16 = var15;
                  int var17 = var8;
                  if (var16 >= var17) {
                     var12++;
                     break;
                  }

                  int var18 = var9;
                  int var19 = var12;
                  int var20 = var15;
                  BlockPos var21 = new BlockPos(var18, var19, var20);
                  ClientLevel var22 = mc.level;
                  BlockPos var23 = var21;
                  BlockState var24 = var22.getBlockState(var23);
                  ClientLevel var25 = mc.level;
                  BlockPos var26 = var21;
                  VoxelShape var27 = var24.getCollisionShape(var25, var26);
                  if (!var27.isEmpty()) {
                     AABB var28 = var27.bounds();
                     BlockPos var29 = var21;
                     AABB var30 = var28.move(var29);
                     AABB var31 = var2;
                     if (var30.intersects(var31)) {
                        return true;
                     }
                  }

                  var15++;
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean a_bool_Player(Player var1) {
      Vec3 var2 = var1.getEyePosition();
      BlockPos var3 = BlockPos.containing(var2);
      ClientLevel var4 = mc.level;
      BlockPos var5 = var3;
      BlockState var6 = var4.getBlockState(var5);
      if (var6.isAir()) {
         return false;
      }

      BlockState var7 = var6;
      ClientLevel var8 = mc.level;
      BlockPos var9 = var3;
      VoxelShape var10 = var7.getCollisionShape(var8, var9);
      if (var10.isEmpty()) {
         return false;
      }

      for (AABB var12 : var10.toAabbs()) {
         BlockPos var13 = var3;
         AABB var14 = var12.move(var13);
         Vec3 var15 = var2;
         if (var14.contains(var15)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
}
