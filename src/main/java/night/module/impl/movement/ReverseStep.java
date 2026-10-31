package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;
import night.util.math.TimerUtil;

@ModuleInfo(name = "ReverseStep", description = "Makes it so that you fall down instantly at a specified height.", category = Module.Category.MOVEMENT)
public class ReverseStep extends Module {
   public NumberSetting height;

   public ReverseStep() {
      Double var1 = 3.0;
      Double var2 = 0.0;
      Double var3 = 12.0;
      this.height = new NumberSetting("Height", "The maximum height at which instant falling will be applied to.", var1, var2, var3);
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      TimerUtil var2 = Night.serverManager.getSetbackTimer();
      Long var3 = 300L;
      if (var2.passedMs(var3)) {
         if (!mc.player.isPassenger()
            && !mc.player.isFallFlying()
            && !mc.player.onClimbable()
            && !mc.player.isInLava()
            && !mc.player.isInWater()
            && !mc.player.input.keyPresses.jump()
            && !mc.player.input.keyPresses.shift()) {
            if (mc.player.onGround()) {
               double var4 = this.height.getValue().doubleValue();
               if (this.a_bool_double(var4)) {
                  LocalPlayer var6 = mc.player;
                  double var7 = mc.player.getDeltaMovement().x;
                  double var9 = -this.height.getValue().doubleValue();
                  double var11 = mc.player.getDeltaMovement().z;
                  var6.setDeltaMovement(var7, var9, var11);
               }
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.height.getValue().floatValue());
   }

   private boolean a_bool_double(double var1) {
      double var3 = 0.0;

      while (true) {
         double var5 = var3;
         double var7 = var1;
         double var9 = 0.5;
         double var11 = var7 + var9;
         if (!(var5 < var11)) {
            return false;
         }

         ClientLevel var13 = mc.level;
         LocalPlayer var14 = mc.player;
         AABB var15 = mc.player.getBoundingBox();
         double var16 = 0.0;
         double var18 = -var3;
         double var20 = 0.0;
         AABB var22 = var15.move(var16, var18, var20);
         if (!var13.noCollision(var14, var22)) {
            return true;
         }

         double var23 = var3;
         double var25 = 0.01;
         var3 = var23 + var25;
      }
   }
}
