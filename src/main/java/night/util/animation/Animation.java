package night.util.animation;

import lombok.Generated;
import net.minecraft.util.Mth;

public class Animation {
   private float value;
   private float target;
   private long lastUpdateTime;
   private EasingUtil.Easing easing;
   private final int duration;
   private int f;

   public static int a_int_int(int var0) {
      return Math.round(var0 / 0.8F);
   }

   public Animation(int var1, EasingUtil.Easing var2) {
      this(0.0F, 0.0F, var1, var2);
   }

   public Animation(float var1, float var2, int var3, EasingUtil.Easing var4) {
      this.target = var1;
      this.value = var2;
      this.duration = var3;
      this.f = var3;
      this.easing = var4;
      this.lastUpdateTime = System.currentTimeMillis();
   }

   public float getValue() {
      return this.f <= 0 ? this.value : Mth.lerp(EasingUtil.a(EasingUtil.a(this.lastUpdateTime, this.f), this.easing), this.target, this.value);
   }

   public float getTarget() {
      return this.getValue();
   }

   public float animateTo(float var1) {
      float var2 = this.getValue();
      if (this.value != var1) {
         float var3 = Math.abs(var1 - var2);
         float var4 = Math.max(Math.abs(this.value - this.target), var3);
         int var5 = var4 <= 1.0E-4F ? this.duration : Math.round(this.duration * Mth.clamp(var3 / var4, 0.0F, 1.0F));
         this.target = var2;
         this.value = var1;
         this.f = Math.max(40, var5);
         this.lastUpdateTime = System.currentTimeMillis();
      }

      return var2;
   }

   public void setEasing(EasingUtil.Easing var1) {
      if (this.easing != var1) {
         float var2 = this.getValue();
         int var3 = (int)Math.min(EasingUtil.a_long_long(this.lastUpdateTime), 2147483647L);
         int var4 = Math.max(0, this.f - var3);
         this.target = var2;
         this.f = var4;
         this.lastUpdateTime = System.currentTimeMillis();
         this.easing = var1;
      }
   }

   @Generated
   public long getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   @Generated
   public void setValue(float var1) {
      this.value = var1;
   }

   @Generated
   public void setTarget(float var1) {
      this.target = var1;
   }

   @Generated
   public void a_void_long(long var1) {
      this.lastUpdateTime = var1;
   }

   @Generated
   public void b(int var1) {
      this.f = var1;
   }
}
