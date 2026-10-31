package night.module.impl.visuals;

import lombok.Generated;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.KeyboardTickEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FreecamAccess;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.math.MathUtil;

@ModuleInfo(name = "Freecam", description = "Allows you to move your camera anywhere you want without restriction.", category = Module.Category.VISUALS)
public class Freecam extends Module implements FreecamAccess {
   public NumberSetting horizontalSpeed = new NumberSetting("HorizontalSpeed", "The speed at which your camera will move horizontally.", 1.0F, 0.1F, 3.0F);
   public NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", "The speed at which your camera will move vertically.", 0.5F, 0.1F, 3.0F);
   public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates your real player model along with the camera.", false);
   public BooleanSetting shift = new BooleanSetting("Shift", "Allows moving down with shift even when a GUI is open.", false);
   private float i;
   private float j;
   private float k;
   private float l;
   private double m;
   private double n;
   private double o;
   private double p;
   private double q;
   private double r;
   private CameraType s;

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null) {
         this.k = this.i;
         this.l = this.j;
         if (this.rotate.getValue()) {
            this.i = mc.player.getYRot();
            this.j = mc.player.getXRot();
         }
      }
   }

   @Override
   public void changeLookDirection(double var1, double var3) {
      float var5 = (float)var3 * 0.15F;
      float var6 = (float)var1 * 0.15F;
      this.j = Mth.clamp(this.j + var5, -90.0F, 90.0F);
      this.i += var6;
   }

   @EventTarget
   public void a(KeyboardTickEvent var1) {
      if (mc.player != null) {
         this.p = this.m;
         this.q = this.n;
         this.r = this.o;
         float var2 = var1.getMovementForward();
         float var3 = var1.getMovementSideways();
         double var4 = this.horizontalSpeed.getValue().doubleValue();
         if (var2 != 0.0F || var3 != 0.0F) {
            float var6 = (float)Math.sqrt(var2 * var2 + var3 * var3);
            if (var6 > 1.0E-4F && Math.abs(var6 - 1.0F) > 1.0E-4F) {
               var2 /= var6;
               var3 /= var6;
            }

            double var7 = Math.cos(Math.toRadians(this.i + 90.0F));
            double var9 = Math.sin(Math.toRadians(this.i + 90.0F));
            double var11 = var2 * var4 * var7 + var3 * var4 * var9;
            double var13 = var2 * var4 * var9 - var3 * var4 * var7;
            this.m += var11;
            this.o += var13;
         }

         if (mc.options.keyJump.isDown()) {
            this.n = this.n + this.verticalSpeed.getValue().doubleValue();
         }

         if (mc.options.keyShift.isDown() && (this.shift.getValue() || mc.gui.screen() == null)) {
            this.n = this.n - this.verticalSpeed.getValue().doubleValue();
         }

         var1.setMovementForward(0.0F);
         var1.setMovementSideways(0.0F);
         var1.setCancelled(true);
      }
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.level != null) {
         mc.smartCull = false;
         this.s = mc.options.getCameraType();
         mc.options.setCameraType(CameraType.FIRST_PERSON);
         this.i = this.k = mc.player.getYRot();
         this.j = this.l = mc.player.getXRot();
         this.m = this.p = mc.player.getX();
         this.n = this.q = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
         this.o = this.r = mc.player.getZ();
      } else {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void a(PlayerDeathEvent var1) {
      if (var1.getPlayer() != null && mc.player != null) {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void a(ClientConnectEvent var1) {
      this.setEnabled(false);
   }

   @Override
   public void onDisable() {
      if (mc.player != null && mc.level != null) {
         mc.smartCull = true;
         if (this.s != null) {
            mc.options.setCameraType(this.s);
         }
      }
   }

   @Override
   public boolean isRotateEnabled() {
      return this.rotate.getValue();
   }

   @Override
   public float getCameraYaw() {
      return (float)MathUtil.a(this.k, this.i, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
   }

   @Override
   public float getCameraPitch() {
      return (float)MathUtil.a(this.l, this.j, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
   }

   @Override
   public double getCameraX() {
      return MathUtil.a(this.p, this.m, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
   }

   @Override
   public double getCameraY() {
      return MathUtil.a(this.q, this.n, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
   }

   @Override
   public double getCameraZ() {
      return MathUtil.a(this.r, this.o, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
   }

   @Generated
   public NumberSetting v() {
      return this.horizontalSpeed;
   }

   @Generated
   public NumberSetting w() {
      return this.verticalSpeed;
   }

   @Generated
   public BooleanSetting x() {
      return this.rotate;
   }

   @Generated
   public BooleanSetting y() {
      return this.shift;
   }

   @Generated
   public float z() {
      return this.k;
   }

   @Generated
   public float A() {
      return this.l;
   }

   @Generated
   public double B() {
      return this.p;
   }

   @Generated
   public double C() {
      return this.q;
   }

   @Generated
   public double D() {
      return this.r;
   }

   @Generated
   public CameraType E() {
      return this.s;
   }

   @Generated
   public void a(NumberSetting var1) {
      this.horizontalSpeed = var1;
   }

   @Generated
   public void b(NumberSetting var1) {
      this.verticalSpeed = var1;
   }

   @Generated
   public void a(BooleanSetting var1) {
      this.rotate = var1;
   }

   @Generated
   public void b(BooleanSetting var1) {
      this.shift = var1;
   }

   @Generated
   public void a(float var1) {
      this.i = var1;
   }

   @Generated
   public void b(float var1) {
      this.j = var1;
   }

   @Generated
   public void c(float var1) {
      this.k = var1;
   }

   @Generated
   public void d(float var1) {
      this.l = var1;
   }

   @Generated
   public void a(double var1) {
      this.m = var1;
   }

   @Generated
   public void b(double var1) {
      this.n = var1;
   }

   @Generated
   public void c(double var1) {
      this.o = var1;
   }

   @Generated
   public void d(double var1) {
      this.p = var1;
   }

   @Generated
   public void e(double var1) {
      this.q = var1;
   }

   @Generated
   public void f(double var1) {
      this.r = var1;
   }

   @Generated
   public void a(CameraType var1) {
      this.s = var1;
   }
}
