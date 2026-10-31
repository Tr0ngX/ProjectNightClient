package night.module.impl.movement;

import lombok.Generated;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.RotationUpdateEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FreecamAccess;
import night.module.access.SprintAccess;
import night.security.LicenseService;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.math.TimerUtil;
import night.util.movement.MovementUtil;

@ModuleInfo(name = "Sprint", description = "Makes it so that you are always sprinting when possible.", category = Module.Category.MOVEMENT)
public class Sprint extends Module implements SprintAccess {
   public ModeSetting mode;
   public NumberSetting instantSpeed;
   public BooleanSetting water;
   public BooleanSetting lava;
   public BooleanSetting pauseOnSetback;
   private float j;
   private Vec2 k;
   private boolean l;
   private Float m;
   private float n;
   private int o;

   public Sprint() {
      String[] var1 = new String[]{"Legit", "Rage", "Instant", "Grim"};
      this.mode = new ModeSetting("Mode", "The limits to when you can be sprinting.", "Rage", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Instant"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      Float var5 = (float)MovementUtil.a;
      Float var6 = 0.05F;
      Float var7 = 0.6F;
      this.instantSpeed = new NumberSetting("InstantSpeed", "Per-tick horizontal speed to move at (blocks/tick) when Mode is Instant.", var2, var5, var6, var7);
      ModeSetting var8 = this.mode;
      String[] var9 = new String[]{"Instant"};
      this.water = new BooleanSetting("Water", "Keeps applying the Instant speed override while in water.", new ModeSetting.ModeCondition(var8, var9), true);
      ModeSetting var10 = this.mode;
      String[] var11 = new String[]{"Instant"};
      this.lava = new BooleanSetting("Lava", "Keeps applying the Instant speed override while in lava.", new ModeSetting.ModeCondition(var10, var11), false);
      this.pauseOnSetback = new BooleanSetting(
         "PauseOnSetback", "Briefly pauses sprinting when receiving a setback from the server to allow GrimAC buffer to decay.", true
      );
      double var12 = MovementUtil.a_double_int(10);
      double var14 = 0.95;
      if (var12 < var14) {
         super.setEnabled(true);
      }
   }

   @Override
   public boolean isEnabled() {
      if (!super.isEnabled()) {
         double var1 = MovementUtil.a_double_int(10);
         double var3 = 0.95;
         if (!(var1 < var3)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void setEnabled(boolean var1, boolean var2) {
      if (!var1) {
         double var3 = MovementUtil.a_double_int(10);
         double var5 = 0.95;
         if (var3 < var5) {
            super.setEnabled(true, false);
            return;
         }
      }

      super.setEnabled(var1, var2);
   }

   @Override
   public int getStrafeSide() {
      return this.o;
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null) {
         LicenseService var2 = Night.licenseService;
         long var3;
         if (var2 != null && var2.getRole() != null) {
            var3 = var2.getHeartbeatAgeMs();
         } else {
            var3 = Long.MAX_VALUE;
         }

         long var5 = var3;
         long var7 = 300000L;
         long var9 = var5 - var7;
         long var11 = -1L;
         int var13 = (int)((var9 ^ var11) >>> 63);
         ModeSetting var14 = this.mode;
         String[] var15 = new String[2];
         String var16 = this.mode.getValue();
         var15[0] = var16;
         var15[1] = "Instant";
         int var17 = var13;
         String var18 = var15[var17];
         var14.setValue(var18);
         float var19 = mc.player.getYRot();
         this.j = var19;
         Vec2 var20 = mc.player.input.getMoveVector();
         this.k = var20;
         if (this.w()) {
            this.x();
         } else {
            this.m = null;
         }

         if (this.canSprint()) {
            mc.player.setSprinting(true);
         } else if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
         }
      }
   }

   @EventTarget
   public void a(PlayerMoveEvent var1) {
      if (mc.player != null) {
         if (this.mode.getValue().equalsIgnoreCase("Instant")) {
            ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
            if (!var2.isEnabled() || !var2.mode.getValue().equalsIgnoreCase("Control") || !mc.player.isFallFlying()) {
               HoleSnap var3 = Night.moduleManager.getModule(HoleSnap.class);
               if (!var3.isEnabled() || var3.r == null) {
                  boolean var6;
                  label61: {
                     label75: {
                        if (mc.player.getFoodData().getFoodLevel() > 6
                           && (this.lava.getValue() || !mc.player.isInLava())
                           && (this.water.getValue() || !mc.player.isInWater())) {
                           if (!this.pauseOnSetback.getValue() || Night.serverManager == null) {
                              break label75;
                           }

                           TimerUtil var4 = Night.serverManager.getSetbackTimer();
                           Long var5 = 150L;
                           if (var4.passedMs(var5)) {
                              break label75;
                           }
                        }

                        var6 = false;
                        break label61;
                     }

                     var6 = true;
                  }

                  if (var6) {
                     Vec2 var7 = this.k;
                     if (var7.x == 0.0F && var7.y == 0.0F) {
                        double var82 = 0.0;
                        double var84 = var1.getMovement().y;
                        double var86 = 0.0;
                        var1.setMovement(new Vec3(var82, var84, var86));
                     } else {
                        float var8 = var7.x;
                        float var9 = var7.x;
                        float var10 = var8 * var9;
                        float var11 = var7.y;
                        float var12 = var7.y;
                        float var13 = var11 * var12;
                        double var14 = Math.sqrt(var10 + var13);
                        double var16 = var7.x;
                        double var18 = var14;
                        double var20 = var16 / var18;
                        double var22 = var7.y;
                        double var24 = var14;
                        double var26 = var22 / var24;
                        double var28 = Math.toRadians(this.j);
                        double var30 = Math.sin(var28);
                        double var32 = Math.cos(var28);
                        double var34 = var20;
                        double var36 = var32;
                        double var38 = var34 * var36;
                        double var40 = var26;
                        double var42 = var30;
                        double var44 = var40 * var42;
                        double var46 = var38 - var44;
                        double var48 = var20;
                        double var50 = var30;
                        double var52 = var48 * var50;
                        double var54 = var26;
                        double var56 = var32;
                        double var58 = var54 * var56;
                        double var60 = var52 + var58;
                        double var62 = this.instantSpeed.getValue().doubleValue();
                        double var64 = MovementUtil.a_double_int(10);
                        double var66 = var62 * var64;
                        double var68 = var46;
                        double var70 = var66;
                        double var72 = var68 * var70;
                        double var74 = var1.getMovement().y;
                        double var76 = var60;
                        double var78 = var66;
                        double var80 = var76 * var78;
                        var1.setMovement(new Vec3(var72, var74, var80));
                     }

                     var1.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a(RotationUpdateEvent var1) {
      if (this.m != null && !var1.isCancelled()) {
         float var2 = this.m;
         var1.setYaw(var2);
         float var3 = this.n;
         var1.setPitch(var3);
         var1.setModule(this);
      }
   }

   private boolean w() {
      if (this.mode.getValue().equalsIgnoreCase("Grim")) {
         double var1 = MovementUtil.a_double_int(10);
         double var3 = 0.95;
         if (var1 >= var3) {
            return true;
         }
      }

      return false;
   }

   private void x() {
      this.l = false;
      this.m = null;
      if (!mc.player.isFallFlying() && !mc.player.isPassenger()) {
         if (this.pauseOnSetback.getValue() && Night.serverManager != null) {
            TimerUtil var1 = Night.serverManager.getSetbackTimer();
            Long var2 = 150L;
            if (!var1.passedMs(var2)) {
               return;
            }
         }

         if (!((FreecamAccess)Night.moduleManager.getModuleByName("Freecam")).isEnabled()) {
            byte var3;
            if (mc.options.keyRight.isDown()) {
               var3 = 1;
            } else {
               var3 = 0;
            }

            byte var4;
            if (mc.options.keyLeft.isDown()) {
               var4 = 1;
            } else {
               var4 = 0;
            }

            int var5 = var3 - var4;
            byte var6;
            if (mc.options.keyUp.isDown()) {
               var6 = 1;
            } else {
               var6 = 0;
            }

            byte var7;
            if (mc.options.keyDown.isDown()) {
               var7 = 1;
            } else {
               var7 = 0;
            }

            int var8 = var6 - var7;
            if (var5 != 0 || var8 != 0) {
               double var9 = var5;
               double var11 = var8;
               float var13 = (float)Math.toDegrees(Math.atan2(var9, var11));
               byte var14 = 0;
               float var20;
               if (var5 != 0 && var8 != 0) {
                  byte var15;
                  if (var5 < 0) {
                     var15 = 1;
                  } else {
                     var15 = -1;
                  }

                  var14 = var15;
                  float var16 = mc.player.getYRot();
                  float var17 = var13;
                  float var18 = var16 + var17;
                  float var19;
                  if (var14 > 0) {
                     var19 = 45.0F;
                  } else {
                     var19 = -45.0F;
                  }

                  var20 = Mth.wrapDegrees(var18 + var19);
               } else {
                  float var21 = mc.player.getYRot();
                  float var22 = var13;
                  var20 = Mth.wrapDegrees(var21 + var22);
               }

               Float var23 = var20;
               this.m = var23;
               float var24 = mc.player.getXRot();
               this.n = var24;
               byte var25 = var14;
               this.o = var25;
               this.l = true;
            }
         }
      }
   }

   @Override
   public boolean isGrimSprintActive() {
      if (mc.player != null && this.isEnabled() && this.w() && this.l) {
         if (this.pauseOnSetback.getValue() && Night.serverManager != null) {
            TimerUtil var1 = Night.serverManager.getSetbackTimer();
            Long var2 = 150L;
            if (!var1.passedMs(var2)) {
               return false;
            }
         }

         boolean var3;
         if (Night.rotationManager.getCurrentRotation() != null && Night.rotationManager.getActiveModule() == this) {
            var3 = true;
         } else {
            var3 = false;
         }

         return var3;
      } else {
         return false;
      }
   }

   @Override
   public float getSprintYaw() {
      return Night.rotationManager.getCurrentRotation().getYaw();
   }

   @Override
   public boolean isInstantMode() {
      return this.mode.getValue().equalsIgnoreCase("Instant");
   }

   @Override
   public boolean isGrimMode() {
      boolean var1;
      if (this.isEnabled() && this.w()) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         LocalPlayer var1 = mc.player;
         boolean var2 = this.canSprint();
         var1.setSprinting(var2);
      }
   }

   @Override
   public void onDisable() {
      double var1 = MovementUtil.a_double_int(10);
      double var3 = 0.95;
      if (var1 < var3) {
         this.setEnabled(true);
      } else {
         this.l = false;
         if (mc.player != null) {
            mc.player.setSprinting(false);
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   @Override
   public boolean canSprint() {
      if (this.pauseOnSetback.getValue() && Night.serverManager != null) {
         TimerUtil var1 = Night.serverManager.getSetbackTimer();
         Long var2 = 150L;
         if (!var1.passedMs(var2)) {
            return false;
         }
      }

      if (!((ClientPlayerEntityAccessor)mc.player).invokeCanSprint(true)) {
         return false;
      }

      if (mc.player.isInWater() && !mc.player.isUnderWater()) {
         return false;
      }

      if (mc.player.isSwimming() && !mc.player.onGround() && !mc.player.input.keyPresses.shift() && !mc.player.isInWater()) {
         return false;
      }

      if (mc.player.isInLava()) {
         return false;
      }

      if (mc.player.onClimbable()) {
         return false;
      }

      if (mc.player.isFallFlying()) {
         ElytraFly var3;
         if (Night.moduleManager != null) {
            var3 = Night.moduleManager.getModule(ElytraFly.class);
         } else {
            var3 = null;
         }

         ElytraFly var4 = var3;
         if (var4 == null || !var4.isEnabled() || !var4.mode.getValue().equalsIgnoreCase("Bounce")) {
            return false;
         }
      }

      if (mc.player.getFoodData().getFoodLevel() <= 6) {
         return false;
      }

      if (!mc.player.isUsingItem() || Night.moduleManager.getModule(NoSlow.class).isEnabled() && Night.moduleManager.getModule(NoSlow.class).items.getValue()) {
         double var5 = MovementUtil.a_double_int(10);
         double var7 = 0.95;
         if (!(var5 < var7) && !this.mode.getValue().equalsIgnoreCase("Rage") && !this.mode.getValue().equalsIgnoreCase("Instant") && !this.w()) {
            if (!((ClientPlayerEntityAccessor)mc.player).invokeIsWalking()) {
               return false;
            } else {
               return mc.player.horizontalCollision && !mc.player.minorHorizontalCollision ? false : mc.player.input.hasForwardImpulse();
            }
         } else {
            if (mc.player.horizontalCollision && !mc.player.minorHorizontalCollision) {
               return false;
            }

            if (this.w() && !this.isGrimSprintActive()) {
               return mc.player.input.keyPresses.forward();
            }

            boolean var9;
            if (!mc.player.input.keyPresses.forward()
               && !mc.player.input.keyPresses.backward()
               && !mc.player.input.keyPresses.left()
               && !mc.player.input.keyPresses.right()) {
               var9 = false;
            } else {
               var9 = true;
            }

            return var9;
         }
      } else {
         return false;
      }
   }

   @Generated
   public Float v() {
      return this.m;
   }
}
