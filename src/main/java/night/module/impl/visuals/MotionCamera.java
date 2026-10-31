package night.module.impl.visuals;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import night.Night;
import night.events.EventTarget;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.MotionCameraAccess;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.MathLerpUtil;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;

@ModuleInfo(name = "MotionCamera", description = "Smooths camera movement and adds smooth perspective transitions.", category = Module.Category.VISUALS)
public class MotionCamera extends Module implements MotionCameraAccess {
   public BooleanSetting noFirstPerson = new BooleanSetting("NoFirstPerson", "Disables motion camera in first person.", true);
   public BooleanSetting pearlException = new BooleanSetting(
      "PearlException",
      "Still smooths in 1st person for a moment after throwing an ender pearl (ignores NoFirstPerson).",
      new BooleanSetting.BooleanCondition(this.noFirstPerson, true),
      true
   );
   public BooleanSetting rubberbandException = new BooleanSetting(
      "RubberbandException",
      "Still smooths in 1st person for a moment after a rubberband/teleport correction (ignores NoFirstPerson).",
      new BooleanSetting.BooleanCondition(this.noFirstPerson, true),
      true
   );
   public NumberSetting exceptionDuration = new NumberSetting(
      "ExceptionDuration",
      "How long the pearl/rubberband exception window lasts, in ms.",
      new BooleanSetting.BooleanCondition(this.noFirstPerson, true),
      500,
      100,
      2000
   );
   public NumberSetting firstPersonSpeed = new NumberSetting("FirstPersonSpeed", "Movement smoothness speed in first person.", 0.6, 0.01, 1.0);
   public NumberSetting speed = new NumberSetting("Speed", "Movement smoothness speed in third person.", 0.3, 0.01, 1.0);
   public BooleanSetting smoothPerspective = new BooleanSetting(
      "SmoothPerspective", "Smoothly transitions distance when switching between 1st and 3rd person.", true
   );
   public NumberSetting perspectiveSpeed = new NumberSetting("PerspectiveSpeed", "Speed of perspective switch transition.", 0.5, 0.05, 1.0);
   private double m;
   private double n;
   private double o;
   private double p;
   private double q;
   private double r;
   private double s = 0.0;
   private double t = 0.0;
   private final TimerUtil u = new TimerUtil();
   private final TimerUtil v = new TimerUtil();

   @EventTarget
   public void a(EntitySpawnEvent var1) {
      if (this.pearlException.getValue() && mc.player != null) {
         Entity var2 = var1.getEntity();
         if (var2 instanceof ThrownEnderpearl) {
            var2 = ((ThrownEnderpearl)var2).getOwner();
            if (var2 instanceof Player && (Player)var2 == mc.player) {
               this.u.reset();
            }
         }
      }
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (this.rubberbandException.getValue()) {
         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.v.reset();
         }
      }
   }

   private boolean v() {
      long var1 = this.exceptionDuration.getValue().longValue();
      return this.pearlException.getValue() && !this.u.passedMs(var1) || this.rubberbandException.getValue() && !this.v.passedMs(var1);
   }

   private boolean w() {
      return mc.player != null && (mc.player.isPassenger() || mc.player.isFallFlying());
   }

   @Override
   public boolean isSmoothPerspective() {
      return this.smoothPerspective.getValue();
   }

   @Override
   public boolean shouldSmoothCamera() {
      return mc.options == null
         ? false
         : this.isEnabled() && !this.w() && (!this.noFirstPerson.getValue() || !mc.options.getCameraType().isFirstPerson() || this.v());
   }

   @Override
   public boolean shouldRenderThirdPerson() {
      return mc.options == null
         ? false
         : this.isEnabled() && this.smoothPerspective.getValue() && (this.s > 0.01 || !mc.options.getCameraType().isFirstPerson());
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.m = mc.player.getX();
         this.n = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
         this.o = mc.player.getZ();
         this.p = this.m;
         this.q = this.n;
         this.r = this.o;
         double var1 = mc.options.getCameraType().isFirstPerson() ? 0.0 : this.x();
         this.s = var1;
         this.t = var1;
      }
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null && mc.options != null) {
         this.p = this.m;
         this.q = this.n;
         this.r = this.o;
         double var2 = mc.player.getX();
         double var4 = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
         double var6 = mc.player.getZ();
         if (this.w()) {
            this.m = var2;
            this.n = var4;
            this.o = var6;
         } else {
            double var8 = mc.options.getCameraType().isFirstPerson() ? this.firstPersonSpeed.getValue().doubleValue() : this.speed.getValue().doubleValue();
            this.m = MathLerpUtil.a(this.m, var2, var8);
            this.n = MathLerpUtil.a(this.n, var4, var8);
            this.o = MathLerpUtil.a(this.o, var6, var8);
         }

         this.t = this.s;
         boolean var12 = mc.options.getCameraType().isFirstPerson();
         double var10 = var12 ? 0.0 : this.x();
         if (this.smoothPerspective.getValue() && !var12) {
            this.s = MathLerpUtil.a(this.s, var10, this.perspectiveSpeed.getValue().doubleValue());
         } else {
            this.s = var10;
         }

         if (var12) {
            this.t = 0.0;
         }
      }
   }

   private double x() {
      ViewClip var1 = Night.moduleManager.getModule(ViewClip.class);
      return var1 != null && var1.isEnabled() && var1.extend.getValue() ? var1.distance.getValue().doubleValue() : 4.0;
   }

   @Override
   public double getCameraX() {
      float var1 = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0F;
      return MathUtil.a(this.p, this.m, var1);
   }

   @Override
   public double getCameraY() {
      float var1 = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0F;
      return MathUtil.a(this.q, this.n, var1);
   }

   @Override
   public double getCameraZ() {
      float var1 = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0F;
      return MathUtil.a(this.r, this.o, var1);
   }

   @Override
   public double getInterpolatedDistance() {
      float var1 = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0F;
      return MathUtil.a(this.t, this.s, var1);
   }
}
