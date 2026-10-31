package night.manager;

import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.util.MinecraftInstance;
import night.util.math.TimerUtil;
import night.util.movement.MovementUtil;

public class SpeedManager implements MinecraftInstance {
   public static SpeedManager INSTANCE;
   private final TimerUtil boostTimer = new TimerUtil();
   private final TimerUtil activeTimer = new TimerUtil();
   double explosionSpeed;
   boolean boostActive = false;

   public SpeedManager() {
      INSTANCE = this;
      Night.EVENT_BUS.register(this);
      double var1 = 0.0;
      this.explosionSpeed = var1;
   }

   @EventTarget(priority = 100)
   public void onPacketReceive(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (var1.getPacket() instanceof ClientboundExplodePacket var3) {
            Vec3 var4 = mc.player.position();
            Vec3 var5 = var3.center();
            double var6 = var4.distanceTo(var5);
            double var8 = 6.0;
            if (var6 <= var8) {
               var3.playerKnockback().ifPresent(var1x -> {
                  double var2 = var1x.x;
                  double var4x = 0.0;
                  if (var2 == var4x) {
                     double var6x = var1x.z;
                     double var8x = 0.0;
                     if (var6x == var8x) {
                        return;
                     }
                  }

                  this.boostTimer.reset();
                  double var10 = var1x.x;
                  double var12 = var1x.z;
                  double var14 = Math.hypot(var10, var12);
                  this.explosionSpeed = var14;
                  this.boostActive = true;
                  this.activeTimer.reset();
               });
            }
         }
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!MovementUtil.a()) {
            double var2 = 0.0;
            this.explosionSpeed = var2;
            this.boostActive = false;
         }

         long var4 = this.boostTimer.getElapsedMs();
         long var6 = 400L;
         if (var4 >= var6) {
            double var8 = 0.0;
            this.explosionSpeed = var8;
         }

         long var10 = this.activeTimer.getElapsedMs();
         long var12 = 500L;
         if (var10 >= var12) {
            this.boostActive = false;
         }
      }
   }

   public double getExplosionBoost(boolean var1) {
      return this.explosionSpeed;
   }
}
