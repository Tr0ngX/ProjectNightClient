package night.module.impl.movement;

import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PlayerMoveEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;
import night.util.movement.MovementUtil;
import org.joml.Vector2d;

@ModuleInfo(name = "FastSwim", description = "Modifies your swim speed in water and lava.", category = Module.Category.MOVEMENT)
public class FastSwim extends Module {
   private static final double i = 0.04;
   public NumberSetting hWater;
   public NumberSetting vWater;
   public NumberSetting hLava;
   public NumberSetting vLava;

   public FastSwim() {
      Float var1 = 1.0F;
      Float var2 = 0.1F;
      Float var3 = 20.0F;
      this.hWater = new NumberSetting("H-Water", "Horizontal swim speed multiplier in water.", var1, var2, var3);
      Float var4 = 1.0F;
      Float var5 = 0.1F;
      Float var6 = 20.0F;
      this.vWater = new NumberSetting("V-Water", "Vertical (up/down) swim speed multiplier in water.", var4, var5, var6);
      Float var7 = 1.0F;
      Float var8 = 0.1F;
      Float var9 = 20.0F;
      this.hLava = new NumberSetting("H-Lava", "Horizontal swim speed multiplier in lava.", var7, var8, var9);
      Float var10 = 1.0F;
      Float var11 = 0.1F;
      Float var12 = 20.0F;
      this.vLava = new NumberSetting("V-Lava", "Vertical (up/down) swim speed multiplier in lava.", var10, var11, var12);
   }

   @EventTarget
   public void a(PlayerMoveEvent var1) {
      if (mc.player != null) {
         if (!mc.player.onGround()) {
            ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
            if (!var2.isEnabled() || !var2.mode.getValue().equalsIgnoreCase("Control") || !mc.player.isFallFlying()) {
               float var3;
               float var4;
               if (mc.player.isInLava()) {
                  var3 = this.hLava.getValue().floatValue();
                  var4 = this.vLava.getValue().floatValue();
               } else {
                  if (!mc.player.isInWater()) {
                     return;
                  }

                  var3 = this.hWater.getValue().floatValue();
                  var4 = this.vWater.getValue().floatValue();
               }

               double var5 = MovementUtil.a;
               double var7 = var3;
               Vector2d var9 = MovementUtil.a_Vector2d_double(var5 * var7);
               double var10 = var1.getMovement().y;
               if (mc.options.keyJump.isDown()) {
                  double var12 = 0.04;
                  double var14 = var4;
                  var10 = var12 * var14;
               } else if (mc.options.keyShift.isDown()) {
                  double var16 = -0.04;
                  double var18 = var4;
                  var10 = var16 * var18;
               }

               double var20 = var9.x;
               double var22 = var10;
               double var24 = var9.y;
               var1.setMovement(new Vec3(var20, var22, var24));
               var1.setCancelled(true);
            }
         }
      }
   }
}
