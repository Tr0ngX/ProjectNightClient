package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.Vec3dAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.math.TimerUtil;
import night.util.movement.MovementUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "Velocity", description = "Modifies the amount of knockback that you receive.", category = Module.Category.MOVEMENT)
public class Velocity extends Module {
   public ModeSetting mode;
   public NumberSetting horizontal;
   public NumberSetting vertical;
   public BooleanSetting onlyOnGround;
   public BooleanSetting explosions;
   public BooleanSetting pause;
   public BooleanSetting onlyPhased;
   public BooleanSetting onlyCoveredHead;
   public NumberSetting fallDistance;
   public BooleanSetting conceal;
   public CategorySetting antiPushCategory;
   public BooleanSetting antiPush;
   public BooleanSetting antiLiquidPush;
   public BooleanSetting antiBlockPush;
   public BooleanSetting antiFishingRod;
   private boolean t;
   private boolean u;
   private boolean v;

   public Velocity() {
      String[] var1 = new String[]{"Normal", "Walls", "Cancel", "Grim"};
      this.mode = new ModeSetting("Mode", "The method that will be used to achieve the knockback modification.", "Normal", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Normal", "Walls"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      Integer var5 = 0;
      Integer var6 = 0;
      Integer var7 = 100;
      this.horizontal = new NumberSetting("Horizontal", "The amount of horizontal knockback that you will receive.", var2, var5, var6, var7);
      ModeSetting var9 = this.mode;
      String[] var10 = new String[]{"Normal", "Walls"};
      ModeSetting.ModeCondition var8 = new ModeSetting.ModeCondition(var9, var10);
      Integer var11 = 0;
      Integer var12 = 0;
      Integer var13 = 100;
      this.vertical = new NumberSetting("Vertical", "The amount of vertical knockback that you will receive.", var8, var11, var12, var13);
      ModeSetting var14 = this.mode;
      String[] var15 = new String[]{"Walls"};
      this.onlyOnGround = new BooleanSetting(
         "OnlyOnGround", "Only reduces knockback while phased AND on the ground.", new ModeSetting.ModeCondition(var14, var15), false
      );
      this.explosions = new BooleanSetting("Explosions", "Modifies knockback received from explosions.", true);
      ModeSetting var16 = this.mode;
      String[] var17 = new String[]{"Cancel", "Grim"};
      this.pause = new BooleanSetting(
         "Pause", "Pauses the velocity for a certain duration whenever you get rubberbanded.", new ModeSetting.ModeCondition(var16, var17), true
      );
      ModeSetting var18 = this.mode;
      String[] var19 = new String[]{"Grim"};
      this.onlyPhased = new BooleanSetting("OnlyPhased", "Only reduces knockback while phased (Grim mode).", new ModeSetting.ModeCondition(var18, var19), false);
      ModeSetting var20 = this.mode;
      String[] var21 = new String[]{"Grim"};
      this.onlyCoveredHead = new BooleanSetting(
         "OnlyCoveredHead", "Only reduces knockback when a block is above your head (Grim mode).", new ModeSetting.ModeCondition(var20, var21), false
      );
      Double var22 = 10.0;
      Double var23 = 0.0;
      Double var24 = 50.0;
      this.fallDistance = new NumberSetting(
         "FallDistance", "Pause velocity if falling from more than this distance in blocks (0 to disable).", var22, var23, var24
      );
      this.conceal = new BooleanSetting("Conceal", "Ignores fake 0-0-0 velocity packets sent after a setback.", false);
      this.antiPushCategory = new CategorySetting("AntiPush", "Prevents certain things from pushing you.");
      CategorySetting var25 = this.antiPushCategory;
      this.antiPush = new BooleanSetting(
         "AntiPush", "Entities", "Prevents other entities from pushing you.", new CategorySetting.CategoryCondition(var25), true
      );
      CategorySetting var26 = this.antiPushCategory;
      this.antiLiquidPush = new BooleanSetting(
         "AntiLiquidPush", "Liquids", "Prevents liquids from pushing you.", new CategorySetting.CategoryCondition(var26), false
      );
      CategorySetting var27 = this.antiPushCategory;
      this.antiBlockPush = new BooleanSetting(
         "AntiBlockPush", "Blocks", "Prevents you from being pushed outside of blocks.", new CategorySetting.CategoryCondition(var27), true
      );
      CategorySetting var28 = this.antiPushCategory;
      this.antiFishingRod = new BooleanSetting(
         "AntiFishingRod", "FishingRods", "Prevents fishing rods from pushing you.", new CategorySetting.CategoryCondition(var28), false
      );
      this.u = false;
      this.v = false;
   }

   @Override
   public void onEnable() {
      this.v = false;
      this.u = false;
   }

   @Override
   public void onDisable() {
      if (this.v) {
         if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            this.v();
         }

         this.v = false;
         this.u = false;
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null) {
         if (this.v && this.mode.getValue().equalsIgnoreCase("Grim")) {
            this.v();
         }

         this.v = false;
         this.u = false;
         this.t = false;
      }
   }

   private void v() {
      float var1 = Night.rotationManager.getServerYaw();
      float var2 = Night.rotationManager.getServerPitch();
      double var3 = Math.random();
      double var5 = 2.0;
      double var7 = var3 * var5;
      double var9 = 1.0;
      double var11 = var7 - var9;
      double var13 = 0.001F;
      float var15 = (float)(var11 * var13);
      float var16 = var2;
      float var17 = var15;
      float var18 = Mth.clamp(var16 + var17, -90.0F, 90.0F);
      ClientPacketListener var19 = mc.getConnection();
      double var20 = mc.player.getX();
      double var22 = mc.player.getY();
      double var24 = mc.player.getZ();
      float var26 = var1;
      float var27 = var18;
      boolean var28 = mc.player.onGround();
      boolean var29 = mc.player.horizontalCollision;
      var19.send(new PosRot(var20, var22, var24, var26, var27, var28, var29));
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null) {
         double var2 = this.fallDistance.getValue().doubleValue();
         double var4 = 0.0;
         if (var2 > var4) {
            double var6 = mc.player.fallDistance;
            double var8 = this.fallDistance.getValue().floatValue();
            if (var6 >= var8) {
               return;
            }
         }

         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket && this.conceal.getValue()) {
            this.u = true;
         }

         if (var1.getPacket() instanceof ClientboundSetEntityMotionPacket var11) {
            if (!EntityUtil.a_bool_int(var11.id())) {
               return;
            }

            if (this.u) {
               double var12 = var11.movement().x;
               double var14 = 0.0;
               if (var12 == var14) {
                  double var16 = var11.movement().y;
                  double var18 = 0.0;
                  if (var16 == var18) {
                     double var20 = var11.movement().z;
                     double var22 = 0.0;
                     if (var20 == var22) {
                        this.u = false;
                        return;
                     }
                  }
               }
            }

            switch (this.mode.getValue()) {
               case "Normal":
                  LocalPlayer var26 = mc.player;
                  if (!this.a_bool_Entity(var26) || this.onlyOnGround.getValue() && !mc.player.onGround()) {
                     return;
                  }

                  ClientboundSetEntityMotionPacket var27 = var11;
                  this.a(var1, var27);
                  break;
               case "Cancel":
                  if (this.pause.getValue()) {
                     TimerUtil var34 = Night.serverManager.getSetbackTimer();
                     Long var35 = 100L;
                     if (!var34.passedMs(var35)) {
                        return;
                     }
                  }

                  var1.setCancelled(true);
                  break;
               case "Grim":
                  if (this.pause.getValue()) {
                     TimerUtil var28 = Night.serverManager.getSetbackTimer();
                     Long var29 = 100L;
                     if (!var28.passedMs(var29)) {
                        return;
                     }
                  }

                  if (this.onlyPhased.getValue()) {
                     LocalPlayer var30 = mc.player;
                     if (!this.a_bool_Entity(var30)) {
                        return;
                     }
                  }

                  if (this.onlyCoveredHead.getValue()) {
                     BlockPos var31 = mc.player.blockPosition().above(2);
                     if (mc.player.isVisuallyCrawling()) {
                        var31 = mc.player.blockPosition().above(1);
                     }

                     ClientLevel var32 = mc.level;
                     BlockPos var33 = var31;
                     if (var32.getBlockState(var33).isAir()) {
                        return;
                     }
                  }

                  ClientboundSetEntityMotionPacket var36 = var11;
                  this.a(var1, var36);
                  this.v = true;
            }
         }

         if (var1.getPacket() instanceof ClientboundExplodePacket var38 && this.explosions.getValue()) {
            switch (this.mode.getValue()) {
               case "Normal":
                  ClientboundExplodePacket var49 = var38;
                  this.a_void_ClientboundExplodePacket(var49);
                  break;
               case "Walls":
                  LocalPlayer var41 = mc.player;
                  if (this.a_bool_Entity(var41)) {
                     ClientboundExplodePacket var42 = var38;
                     this.a_void_ClientboundExplodePacket(var42);
                  }
                  break;
               case "Cancel":
                  if (this.pause.getValue()) {
                     TimerUtil var50 = Night.serverManager.getSetbackTimer();
                     Long var51 = 100L;
                     if (!var50.passedMs(var51)) {
                        return;
                     }
                  }

                  var1.setCancelled(true);
                  break;
               case "Grim":
                  if (this.pause.getValue()) {
                     TimerUtil var43 = Night.serverManager.getSetbackTimer();
                     Long var44 = 100L;
                     if (!var43.passedMs(var44)) {
                        return;
                     }
                  }

                  if (this.onlyPhased.getValue()) {
                     LocalPlayer var45 = mc.player;
                     if (!this.a_bool_Entity(var45)) {
                        return;
                     }
                  }

                  if (this.onlyCoveredHead.getValue()) {
                     BlockPos var46 = mc.player.blockPosition().above(2);
                     if (mc.player.isVisuallyCrawling()) {
                        var46 = mc.player.blockPosition().above(1);
                     }

                     ClientLevel var47 = mc.level;
                     BlockPos var48 = var46;
                     if (var47.getBlockState(var48).isAir()) {
                        return;
                     }
                  }

                  ClientboundExplodePacket var52 = var38;
                  this.a_void_ClientboundExplodePacket(var52);
                  this.v = true;
            }

            if (var1.isCancelled()) {
               mc.executeBlocking(() -> {
                  Vec3 var1x = var38.center();
                  SoundManager var2x = mc.getSoundManager();
                  SoundEvent var3 = (SoundEvent)var38.explosionSound().value();
                  float var4x = mc.level.getRandom().nextFloat();
                  float var5 = mc.level.getRandom().nextFloat();
                  float var6x = (var4x - var5) * 0.2F;
                  float var7 = (1.0F + var6x) * 0.7F;
                  RandomSource var8x = mc.level.getRandom();
                  double var9 = var1x.x;
                  double var11x = var1x.y;
                  double var13 = var1x.z;
                  var2x.play(new SimpleSoundInstance(var3, SoundSource.BLOCKS, 4.0F, var7, var8x, var9, var11x, var13));
                  ClientLevel var15 = mc.level;
                  ParticleOptions var16x = var38.explosionParticle();
                  double var17 = var1x.x;
                  double var19 = var1x.y;
                  double var21 = var1x.z;
                  double var23 = 1.0;
                  double var25 = 0.0;
                  double var27x = 0.0;
                  var15.addParticle(var16x, var17, var19, var21, var23, var25, var27x);
               });
            }
         }
      }
   }

   private void a(PacketReceiveEvent var1, ClientboundSetEntityMotionPacket var2) {
      Vec3 var3 = mc.player.getDeltaMovement();
      Vec3 var4 = var2.movement();
      double var5 = var4.x;
      double var7 = var3.x;
      double var9 = var5 - var7;
      double var11 = 1.0;
      double var13 = 1.0;
      double var15 = this.horizontal.getValue().doubleValue();
      double var17 = 100.0;
      double var19 = var15 / var17;
      double var21 = var13 - var19;
      double var23 = MovementUtil.a_double_int(11);
      double var25 = var21 * var23;
      double var27 = var11 - var25;
      double var29 = var9 * var27;
      double var31 = var3.x;
      double var33 = var29 + var31;
      double var35 = var4.y;
      double var37 = var3.y;
      double var39 = var35 - var37;
      double var41 = 1.0;
      double var43 = 1.0;
      double var45 = this.vertical.getValue().doubleValue();
      double var47 = 100.0;
      double var49 = var45 / var47;
      double var51 = var43 - var49;
      double var53 = MovementUtil.a_double_int(11);
      double var55 = var51 * var53;
      double var57 = var41 - var55;
      double var59 = var39 * var57;
      double var61 = var3.y;
      double var63 = var59 + var61;
      double var65 = var4.z;
      double var67 = var3.z;
      double var69 = var65 - var67;
      double var71 = 1.0;
      double var73 = 1.0;
      double var75 = this.horizontal.getValue().doubleValue();
      double var77 = 100.0;
      double var79 = var75 / var77;
      double var81 = var73 - var79;
      double var83 = MovementUtil.a_double_int(11);
      double var85 = var81 * var83;
      double var87 = var71 - var85;
      double var89 = var69 * var87;
      double var91 = var3.z;
      double var93 = var89 + var91;
      var1.setCancelled(true);
      LocalPlayer var95 = mc.player;
      double var96 = var33;
      double var98 = var63;
      double var100 = var93;
      var95.lerpMotion(new Vec3(var96, var98, var100));
   }

   private void a_void_ClientboundExplodePacket(ClientboundExplodePacket var1) {
      if (var1.playerKnockback().isPresent()) {
         Vec3dAccessor var2 = (Vec3dAccessor)var1.playerKnockback().get();
         double var3 = ((Vec3)var1.playerKnockback().get()).x;
         double var5 = 1.0;
         double var7 = 1.0;
         double var9 = this.horizontal.getValue().doubleValue();
         double var11 = 100.0;
         double var13 = var9 / var11;
         double var15 = var7 - var13;
         double var17 = MovementUtil.a_double_int(11);
         double var19 = var15 * var17;
         double var21 = var5 - var19;
         double var23 = (float)(var3 * var21);
         var2.setX(var23);
      }

      if (var1.playerKnockback().isPresent()) {
         Vec3dAccessor var25 = (Vec3dAccessor)var1.playerKnockback().get();
         double var26 = ((Vec3)var1.playerKnockback().get()).y;
         double var28 = 1.0;
         double var30 = 1.0;
         double var32 = this.vertical.getValue().doubleValue();
         double var34 = 100.0;
         double var36 = var32 / var34;
         double var38 = var30 - var36;
         double var40 = MovementUtil.a_double_int(11);
         double var42 = var38 * var40;
         double var44 = var28 - var42;
         double var46 = (float)(var26 * var44);
         var25.setY(var46);
      }

      if (var1.playerKnockback().isPresent()) {
         Vec3dAccessor var48 = (Vec3dAccessor)var1.playerKnockback().get();
         double var49 = ((Vec3)var1.playerKnockback().get()).z;
         double var51 = 1.0;
         double var53 = 1.0;
         double var55 = this.horizontal.getValue().doubleValue();
         double var57 = 100.0;
         double var59 = var55 / var57;
         double var61 = var53 - var59;
         double var63 = MovementUtil.a_double_int(11);
         double var65 = var61 * var63;
         double var67 = var51 - var65;
         double var69 = (float)(var49 * var67);
         var48.setZ(var69);
      }
   }

   private boolean a_bool_Entity(Entity var1) {
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

   @Override
   public String getDisplayInfo() {
      if (this.mode.getValue().equalsIgnoreCase("Cancel")) {
         return "0%, 0%";
      }

      if (this.mode.getValue().equalsIgnoreCase("Grim")) {
         return "Grim";
      }

      int var1 = this.horizontal.getValue().intValue();
      int var2 = this.vertical.getValue().intValue();
      return var1 + "%, " + var2 + "%";
   }
}
