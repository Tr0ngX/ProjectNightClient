package night.module.impl.movement;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.movement.MovementUtil;
import org.joml.Vector2d;

@ModuleInfo(name = "EntityControl", description = "Allows you to control and fly with ridden entities.", category = Module.Category.MOVEMENT)
public class EntityControl extends Module {
   public static EntityControl e;
   public BooleanSetting fly = new BooleanSetting("Fly", "Allows you to fly with your vehicle.", true);
   public NumberSetting speed;
   public NumberSetting verticalSpeed;
   public NumberSetting fallSpeed;
   public BooleanSetting control;
   public BooleanSetting noSync;
   private Entity l;

   public EntityControl() {
      Double var1 = 5.0;
      Double var2 = 0.1;
      Double var3 = 50.0;
      this.speed = new NumberSetting("Speed", "Horizontal flying speed.", var1, var2, var3);
      Double var4 = 6.0;
      Double var5 = 0.0;
      Double var6 = 20.0;
      this.verticalSpeed = new NumberSetting("VerticalSpeed", "Vertical flying speed.", var4, var5, var6);
      Double var7 = 0.1;
      Double var8 = 0.0;
      Double var9 = 50.0;
      this.fallSpeed = new NumberSetting("FallSpeed", "Glide down speed when no vertical key is pressed.", var7, var8, var9);
      this.control = new BooleanSetting("Control", "Steer entities even without a saddle.", true);
      this.noSync = new BooleanSetting("NoSync", "Cancels server vehicle sync packets to prevent rubberbanding.", false);
      e = this;
   }

   public static EntityControl v() {
      return e;
   }

   @Override
   public void onDisable() {
      this.w();
   }

   private void w() {
      if (this.l != null) {
         this.l.setNoGravity(false);
         this.l = null;
      }
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (!this.nullCheck() && this.fly.getValue()) {
         Entity var2 = mc.player.getVehicle();
         if (var2 == null) {
            this.w();
         } else {
            if (this.l != null) {
               Entity var3 = this.l;
               Entity var4 = var2;
               if (var3 != var4) {
                  this.w();
               }
            }

            Entity var5 = var2;
            this.l = var5;
            var2.setNoGravity(true);
            Entity var6 = var2;
            float var7 = mc.player.getYRot();
            var6.setYRot(var7);
            Vector2d var8 = MovementUtil.a_Vector2d_double(this.speed.getValue().doubleValue());
            double var9 = var8.x;
            double var11 = var8.y;
            double var17;
            if (!(mc.gui.screen() instanceof ChatScreen) && mc.gui.screen() == null) {
               boolean var19 = mc.options.keyJump.isDown();
               boolean var20;
               if (!mc.options.keySprint.isDown() && !mc.options.keyShift.isDown()) {
                  var20 = false;
               } else {
                  var20 = true;
               }

               boolean var21 = var20;
               if (var19) {
                  if (var21) {
                     double var22 = -this.fallSpeed.getValue().doubleValue();
                     double var24 = 20.0;
                     var17 = var22 / var24;
                  } else {
                     double var30 = this.verticalSpeed.getValue().doubleValue();
                     double var32 = 20.0;
                     var17 = var30 / var32;
                  }
               } else if (var21) {
                  double var26 = -this.verticalSpeed.getValue().doubleValue();
                  double var28 = 20.0;
                  var17 = var26 / var28;
               } else {
                  double var34 = -this.fallSpeed.getValue().doubleValue();
                  double var36 = 20.0;
                  var17 = var34 / var36;
               }
            } else {
               double var13 = -this.fallSpeed.getValue().doubleValue();
               double var15 = 20.0;
               var17 = var13 / var15;
            }

            Entity var38 = var2;
            double var39 = var9;
            double var41 = var17;
            double var43 = var11;
            var38.setDeltaMovement(new Vec3(var39, var41, var43));
         }
      } else {
         this.w();
      }
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (!this.nullCheck()) {
         if (var1.getPacket() instanceof ClientboundMoveVehiclePacket && this.noSync.getValue()) {
            var1.cancel();
         }
      }
   }
}
