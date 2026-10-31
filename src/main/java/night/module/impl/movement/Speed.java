package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.TickEvent;
import night.manager.SpeedManager;
import night.manager.WorldStateManager;
import night.mixins.accessors.Vec3dAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.SpeedAccess;
import night.module.access.SprintAccess;
import night.module.impl.misc.FakePlayer;
import night.security.LicenseService;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.movement.MovementUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;
import org.joml.Vector2d;

@ModuleInfo(name = "Speed", description = "Makes it so that you move faster than normal.", category = Module.Category.MOVEMENT)
public class Speed extends Module implements SpeedAccess {
   public ModeSetting mode;
   public NumberSetting vanillaSpeed;
   public BooleanSetting vanillaOnGround;
   public BooleanSetting useTimer;
   public BooleanSetting bypass;
   public NumberSetting threshold;
   public NumberSetting timerMultiplier;
   public BooleanSetting speedInLiquid;
   public BooleanSetting boost;
   public NumberSetting boostMultiplier;
   public NumberSetting boostCap;
   public BooleanSetting autoJump;
   private double q;
   private double r;
   private double s;
   private int t;
   private int u;
   private boolean v;

   public Speed() {
      String[] var1 = new String[]{"Vanilla", "Strafe", "StrafeStrict", "Grim"};
      this.mode = new ModeSetting("Mode", "The method that will be used to increase your speed.", "Strafe", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Vanilla"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      Double var5 = 10.0;
      Double var6 = 0.0;
      Double var7 = 20.0;
      this.vanillaSpeed = new NumberSetting("VanillaSpeed", "Speed", "The speed that will be applied to your movement.", var2, var5, var6, var7);
      ModeSetting var9 = this.mode;
      String[] var10 = new String[]{"Vanilla"};
      ModeSetting.ModeCondition var8 = new ModeSetting.ModeCondition(var9, var10);
      this.vanillaOnGround = new BooleanSetting("VanillaOnGround", "OnGround", "Only applies the speed when you are on ground.", var8, false);
      ModeSetting var12 = this.mode;
      String[] var13 = new String[]{"Strafe", "StrafeStrict"};
      ModeSetting.ModeCondition var11 = new ModeSetting.ModeCondition(var12, var13);
      this.useTimer = new BooleanSetting("UseTimer", "Adds a timer multiplier when strafing.", var11, false);
      BooleanSetting var15 = this.useTimer;
      BooleanSetting.BooleanCondition var14 = new BooleanSetting.BooleanCondition(var15, true);
      this.bypass = new BooleanSetting("Bypass", "Allows you to use timer on certain servers.", var14, true);
      BooleanSetting var17 = this.bypass;
      BooleanSetting.BooleanCondition var16 = new BooleanSetting.BooleanCondition(var17, true);
      Integer var18 = 25;
      Integer var19 = 15;
      Integer var20 = 30;
      this.threshold = new NumberSetting("Threshold", "The threshold value for the timer bypass.", var16, var18, var19, var20);
      BooleanSetting var22 = this.useTimer;
      BooleanSetting.BooleanCondition var21 = new BooleanSetting.BooleanCondition(var22, true);
      Float var23 = 1.08F;
      Float var24 = 1.0F;
      Float var25 = 1.2F;
      this.timerMultiplier = new NumberSetting(
         "TimerMultiplier", "Multiplier", "The timer multiplier that will be applied to the timer.", var21, var23, var24, var25
      );
      ModeSetting var26 = this.mode;
      String[] var27 = new String[]{"Strafe", "StrafeStrict"};
      this.speedInLiquid = new BooleanSetting(
         "SpeedInLiquid", "Increases your speed while in water or lava.", new ModeSetting.ModeCondition(var26, var27), false
      );
      ModeSetting var28 = this.mode;
      String[] var29 = new String[]{"Strafe", "StrafeStrict"};
      this.boost = new BooleanSetting("Boost", "Uses explosion knockback to boost your speed.", new ModeSetting.ModeCondition(var28, var29), false);
      BooleanSetting var31 = this.boost;
      BooleanSetting.BooleanCondition var30 = new BooleanSetting.BooleanCondition(var31, true);
      Double var32 = 1.0;
      Double var33 = 0.01;
      Double var34 = 5.0;
      this.boostMultiplier = new NumberSetting("BoostMultiplier", "Multiplier", "Scales the explosion knockback boost.", var30, var32, var33, var34);
      BooleanSetting var36 = this.boost;
      BooleanSetting.BooleanCondition var35 = new BooleanSetting.BooleanCondition(var36, true);
      Double var37 = 5.0;
      Double var38 = 0.0;
      Double var39 = 10.0;
      this.boostCap = new NumberSetting("BoostCap", "Cap", "Maximum boost value that can be applied.", var35, var37, var38, var39);
      ModeSetting var40 = this.mode;
      String[] var41 = new String[]{"Grim"};
      this.autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you when on ground.", new ModeSetting.ModeCondition(var40, var41), false);
      this.v = false;
   }

   @Override
   public void onEnable() {
      this.t = 1;
      this.u = 0;
      this.v = false;
   }

   @Override
   public void onDisable() {
      Night.worldStateManager.setTimerMultiplier(1.0F);
      if (this.v) {
         mc.options.keyJump.setDown(false);
         this.v = false;
      }
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equalsIgnoreCase("Strafe") || this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
            double var2 = mc.player.getX();
            double var4 = mc.player.xo;
            double var6 = Mth.square(var2 - var4);
            double var8 = mc.player.getZ();
            double var10 = mc.player.zo;
            double var12 = Mth.square(var8 - var10);
            double var14 = Math.sqrt(var6 + var12);
            this.q = var14;
            WorldStateManager var16 = Night.worldStateManager;
            float var17;
            if (this.v()) {
               var17 = this.timerMultiplier.getValue().floatValue();
            } else {
               var17 = 1.0F;
            }

            var16.setTimerMultiplier(var17);
         }

         if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            boolean var24;
            label109: {
               if (!mc.player.isVisuallyCrawling() && mc.player.getPose() != Pose.SWIMMING && !(mc.player.getEyeHeight() < 1.0F)) {
                  double var18 = mc.player.getX();
                  double var20 = mc.player.getY();
                  double var22 = mc.player.getZ();
                  if (!this.a(var18, var20, var22)) {
                     var24 = false;
                     break label109;
                  }
               }

               var24 = true;
            }

            boolean var25 = var24;
            boolean var26 = true;
            if (var25) {
               double var27 = mc.player.getX();
               double var29 = mc.player.getY();
               double var31 = mc.player.getZ();
               boolean var33 = this.a(var27, var29, var31);
               boolean var34 = this.x();
               if (!var33 || !var34) {
                  var26 = false;
               }
            }

            if ((!var26 || !this.autoJump.getValue() || !MovementUtil.a()) && this.v) {
               mc.options.keyJump.setDown(false);
               this.v = false;
            }

            if (this.autoJump.getValue() && MovementUtil.a() && mc.player.onGround() && var26) {
               mc.options.keyJump.setDown(true);
               this.v = true;
               SprintAccess var35 = (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
               boolean var36;
               if (var35 != null && var35.isGrimSprintActive()) {
                  var36 = true;
               } else {
                  var36 = false;
               }

               boolean var37 = var36;
               if (mc.player.isSprinting() && !var37) {
                  float var38 = mc.player.getYRot() * (float) (Math.PI / 180.0);
                  double var39 = -Math.sin(var38);
                  double var41 = 0.2;
                  double var43 = var39 * var41;
                  double var45 = Math.cos(var38);
                  double var47 = 0.2;
                  double var49 = var45 * var47;
                  Vector2d var51 = MovementUtil.a_Vector2d_double(0.2);
                  LocalPlayer var52 = mc.player;
                  double var53 = mc.player.getDeltaMovement().x;
                  double var55 = var43;
                  double var57 = var53 - var55;
                  double var59 = var51.x;
                  double var61 = var57 + var59;
                  double var63 = mc.player.getDeltaMovement().y;
                  double var65 = mc.player.getDeltaMovement().z;
                  double var67 = var49;
                  double var69 = var65 - var67;
                  double var71 = var51.y;
                  double var73 = var69 + var71;
                  var52.setDeltaMovement(var61, var63, var73);
               }
            } else if (this.v) {
               mc.options.keyJump.setDown(false);
               this.v = false;
            }

            int var75 = 0;

            for (Entity var77 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
               if (var77 != null) {
                  Entity var78 = var77;
                  LocalPlayer var79 = mc.player;
                  if (var78 != var79 && var77 instanceof LivingEntity && !EntityUtil.c_bool_Entity(var77)) {
                     if (Night.moduleManager.getModule(FakePlayer.class).isEnabled()) {
                        RemotePlayer var80 = Night.moduleManager.getModule(FakePlayer.class).z();
                        Entity var81 = var77;
                        if (var80 == var81) {
                           continue;
                        }
                     }

                     if (!(var77 instanceof ArmorStand)) {
                        LocalPlayer var82 = mc.player;
                        Entity var83 = var77;
                        double var84 = Mth.sqrt((float)var82.distanceToSqr(var83));
                        double var86 = 1.5;
                        if (var84 <= var86) {
                           var75++;
                        }
                     }
                  }
               }
            }

            if (var75 > 0) {
               double var88 = 0.08;
               double var90 = var75;
               Vector2d var92 = MovementUtil.a_Vector2d_double(var88 * var90);
               LocalPlayer var93 = mc.player;
               double var94 = mc.player.getDeltaMovement().x;
               double var96 = var92.x;
               double var98 = var94 + var96;
               double var100 = mc.player.getDeltaMovement().y;
               double var102 = mc.player.getDeltaMovement().z;
               double var104 = var92.y;
               double var106 = var102 + var104;
               var93.setDeltaMovement(var98, var100, var106);
            }
         }
      }
   }

   @EventTarget
   public void a(PlayerMoveEvent var1) {
      if (this.mode.getValue().equalsIgnoreCase("Strafe") || this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
         if (Night.moduleManager.getModule(HoleSnap.class).isEnabled() && Night.moduleManager.getModule(HoleSnap.class).r != null) {
            return;
         }

         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         if (var2.isEnabled() && var2.mode.getValue().equalsIgnoreCase("Control") && mc.player.isFallFlying()) {
            return;
         }

         double var3 = mc.player.fallDistance;
         double var5 = 5.0;
         if (var3 >= var5 || mc.player.isShiftKeyDown() || mc.player.onClimbable()) {
            return;
         }

         ClientLevel var7 = mc.level;
         BlockPos var8 = mc.player.blockPosition();
         if (var7.getBlockState(var8).getBlock() == Blocks.COBWEB
            || mc.player.getAbilities().flying
            || (mc.player.isInWater() || mc.player.isInLava()) && !this.speedInLiquid.getValue()) {
            return;
         }

         double var9;
         double var15;
         label106: {
            var9 = MovementUtil.b(MovementUtil.a);
            if (mc.player.input.getMoveVector().y <= 0.0F) {
               double var11 = this.s;
               double var13 = 0.0;
               if (var11 > var13) {
                  var15 = 0.66;
                  break label106;
               }
            }

            var15 = 1.0;
         }

         double var17 = var9 * var15;
         this.r = var17;
         if (this.t == 1 && MovementUtil.a() && mc.player.verticalCollision) {
            Vec3dAccessor var19 = (Vec3dAccessor)mc.player.getDeltaMovement();
            double var20 = MovementUtil.c(0.39999995F);
            var19.setY(var20);
            double var22 = var1.getMovement().x;
            double var24 = mc.player.getDeltaMovement().y;
            double var26 = var1.getMovement().z;
            var1.setMovement(new Vec3(var22, var24, var26));
            double var28 = this.r;
            double var30 = 2.149;
            double var32 = var28 * var30;
            this.r = var32;
            if (this.boost.getValue()) {
               double var34 = this.r;
               double var36 = this.w();
               double var38 = var34 + var36;
               this.r = var38;
            }

            this.t = 2;
         } else if (this.t == 2) {
            double var40 = this.q;
            double var42 = 0.66;
            double var44 = this.q;
            double var46 = MovementUtil.b(MovementUtil.a);
            double var48 = var44 - var46;
            double var50 = var42 * var48;
            double var52 = var40 - var50;
            this.r = var52;
            if (this.boost.getValue()) {
               double var54 = this.r;
               double var56 = this.w();
               double var58 = var54 + var56;
               this.r = var58;
            }

            this.t = 3;
         } else {
            ClientLevel var60 = mc.level;
            LocalPlayer var61 = mc.player;
            AABB var62 = mc.player.getBoundingBox();
            double var63 = 0.0;
            double var65 = mc.player.getDeltaMovement().y;
            double var67 = 0.0;
            AABB var69 = var62.move(var63, var65, var67);
            if (!var60.getEntityCollisions(var61, var69).isEmpty() || mc.player.verticalCollision) {
               this.t = 1;
            }

            double var70 = this.q;
            double var72 = this.q;
            double var74 = 159.0;
            double var76 = var72 / var74;
            double var78 = var70 - var76;
            this.r = var78;
         }

         double var80 = this.r;
         double var82 = MovementUtil.b(MovementUtil.a);
         double var84 = Math.max(var80, var82);
         this.r = var84;
         boolean var86;
         if (mc.player.input.keyPresses.forward()
            && !mc.player.input.keyPresses.backward()
            && !mc.player.input.keyPresses.left()
            && !mc.player.input.keyPresses.right()) {
            var86 = true;
         } else {
            var86 = false;
         }

         boolean var87 = var86;
         double var88;
         if (!this.mode.getValue().equalsIgnoreCase("StrafeStrict") && var87) {
            var88 = 0.576;
         } else {
            var88 = 0.465;
         }

         double var90 = MovementUtil.b(var88);
         double var92;
         if (!this.mode.getValue().equalsIgnoreCase("StrafeStrict") && var87) {
            var92 = 0.57;
         } else {
            var92 = 0.44;
         }

         double var94 = MovementUtil.b(var92);
         double var96 = this.r;
         double var98;
         if (this.u > 25) {
            var98 = var90;
         } else {
            var98 = var94;
         }

         double var100 = Math.min(var96, var98);
         this.r = var100;
         int var102 = this.u;
         int var103 = var102 + 1;
         this.u = var103;
         if (var102 > 50) {
            this.u = 0;
         }

         Vector2d var104 = MovementUtil.a_Vector2d_double(this.r);
         double var105 = var104.x;
         double var107 = var1.getMovement().y;
         double var109 = var1.getMovement().z;
         var1.setMovement(new Vec3(var105, var107, var109));
         double var111 = var1.getMovement().x;
         double var113 = var1.getMovement().y;
         double var115 = var104.y;
         var1.setMovement(new Vec3(var111, var113, var115));
         double var117 = mc.player.input.getMoveVector().y;
         this.s = var117;
         var1.setCancelled(true);
      }
   }

   private double w() {
      if (SpeedManager.INSTANCE == null) {
         return 0.0;
      }

      double var1 = SpeedManager.INSTANCE.getExplosionBoost(false);
      double var3 = 0.0;
      if (var1 == var3) {
         return 0.0;
      }

      double var5 = var1;
      double var7 = this.boostMultiplier.getValue().doubleValue();
      double var9 = var5 * var7;
      double var11 = this.boostCap.getValue().doubleValue();
      return Math.min(var9, var11);
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   @Override
   public String getMode() {
      return this.mode.getValue();
   }

   @Override
   public float getVanillaSpeed() {
      double var1 = this.vanillaSpeed.getValue().floatValue();
      double var3 = MovementUtil.a_double_int(12);
      float var5 = (float)(var1 * var3);
      LicenseService var6 = Night.licenseService;
      long var7;
      if (var6 != null && var6.getRole() != null) {
         var7 = var6.getHeartbeatAgeMs();
      } else {
         var7 = Long.MAX_VALUE;
      }

      long var9 = var7;
      long var11 = 300000L;
      long var13 = var9 - var11;
      long var15 = -1L;
      int var17 = (int)((var13 ^ var15) >>> 63);
      float var18 = var5;
      float var19 = var17 * 0.6F;
      float var20 = 1.0F - var19;
      return var18 * var20;
   }

   public boolean v() {
      if (mc.player == null) {
         return false;
      }

      if (!this.mode.getValue().equalsIgnoreCase("Strafe") && !this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
         return false;
      }

      if (!this.useTimer.getValue()) {
         return false;
      }

      boolean var5;
      label40: {
         if (MovementUtil.a() && !mc.player.isShiftKeyDown() && !mc.player.isInWater()) {
            double var1 = mc.player.fallDistance;
            double var3 = 5.0;
            if (var1 < var3) {
               var5 = true;
               break label40;
            }
         }

         var5 = false;
      }

      if (var5) {
         int var6 = this.u;
         int var7 = this.threshold.getValue().intValue();
         if (var6 > var7 || !this.bypass.getValue()) {
            return true;
         }
      }

      return false;
   }

   private boolean a(double var1, double var3, double var5) {
      if (mc.level == null) {
         return false;
      }

      double var7 = var1;
      double var9 = var3;
      double var11 = 1.0;
      double var13 = var9 + var11;
      double var15 = var5;
      BlockPos var17 = BlockPos.containing(var7, var13, var15);
      ClientLevel var18 = mc.level;
      BlockPos var19 = var17;
      BlockState var20 = var18.getBlockState(var19);
      if (var20.isAir() || !var20.isSolid() && !PacketAndRenderUtil.a_bool_BlockState(var20) && var20.canBeReplaced()) {
         double var21 = var1;
         double var23 = var3;
         double var25 = 1.2;
         double var27 = var23 + var25;
         double var29 = var5;
         BlockPos var31 = BlockPos.containing(var21, var27, var29);
         BlockPos var32 = var17;
         if (var31.equals(var32)) {
            return false;
         }

         ClientLevel var33 = mc.level;
         BlockPos var34 = var31;
         BlockState var35 = var33.getBlockState(var34);
         boolean var36;
         if (var35.isAir() || !var35.isSolid() && !PacketAndRenderUtil.a_bool_BlockState(var35) && var35.canBeReplaced()) {
            var36 = false;
         } else {
            var36 = true;
         }

         return var36;
      } else {
         return true;
      }
   }

   private boolean x() {
      if (mc.player != null && mc.level != null) {
         double var1 = mc.player.getDeltaMovement().x;
         double var3 = mc.player.getDeltaMovement().z;
         double var5 = var1;
         double var7 = var3;
         double var9 = Math.hypot(var5, var7);
         double var11 = 0.01;
         if (var9 > var11) {
            double var13 = var9;
            double var15 = 0.35;
            double var17 = Math.min(var13, var15);
            double var19 = mc.player.getX();
            double var21 = var1;
            double var23 = var9;
            double var25 = var21 / var23;
            double var27 = var17;
            double var29 = var25 * var27;
            double var31 = var19 + var29;
            double var33 = mc.player.getZ();
            double var35 = var3;
            double var37 = var9;
            double var39 = var35 / var37;
            double var41 = var17;
            double var43 = var39 * var41;
            double var45 = var33 + var43;
            double var47 = var31;
            double var49 = mc.player.getY();
            double var51 = var45;
            if (!this.a(var47, var49, var51)) {
               return false;
            }
         }

         if (MovementUtil.a()) {
            Vector2d var53 = MovementUtil.a_Vector2d_double(0.3);
            double var54 = mc.player.getX();
            double var56 = var53.x;
            double var58 = var54 + var56;
            double var60 = mc.player.getZ();
            double var62 = var53.y;
            double var64 = var60 + var62;
            double var66 = var58;
            double var68 = mc.player.getY();
            double var70 = var64;
            if (!this.a(var66, var68, var70)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
