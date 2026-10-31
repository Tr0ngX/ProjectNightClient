package night.module.impl.player;

import net.minecraft.util.Mth;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.EntityAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "RotationLock", description = "Locks your rotation to a certain yaw and pitch.", category = Module.Category.PLAYER)
public class RotationLock extends Module {
   public ModeSetting mode;
   public BooleanSetting custom;
   public NumberSetting pitch;
   private boolean persistent;
   private float i;
   private float j;
   private static final long k = 180L;
   private static final float l = 3.0F;
   private long m;
   private long n;

   public RotationLock() {
      String[] var1 = new String[]{"Yaw", "Pitch", "Both"};
      this.mode = new ModeSetting("Mode", "Whether the pitch or yaw will be locked.", "Both", var1);
      ModeSetting var2 = this.mode;
      String[] var3 = new String[]{"Yaw", "Pitch", "Both"};
      this.custom = new BooleanSetting(
         "Custom",
         "Holds your locked angle steady (camera free to turn) and only re-locks, snapping straight to the next 45deg, once you've turned far enough past it -- instead of an always-fixed value.",
         new ModeSetting.ModeCondition(var2, var3),
         false
      );
      ModeSetting var5 = this.mode;
      String[] var6 = new String[]{"Pitch", "Both"};
      ModeSetting.ModeCondition var4 = new ModeSetting.ModeCondition(var5, var6);
      Float var7 = 0.0F;
      Float var8 = -90.0F;
      Float var9 = 90.0F;
      Integer var10 = 1;
      this.pitch = new NumberSetting("Pitch", "Pitch", "The fixed degrees pitch locks to when Custom is off.", var4, var7, var8, var9, var10);
      this.persistent = false;
      this.i = 0.0F;
      this.j = 0.0F;
      long var11 = -1L;
      this.m = var11;
      long var13 = -1L;
      this.n = var13;
   }

   @Override
   public void onEnable() {
      this.persistent = false;
   }

   private float a_float_long(long var1) {
      long var3 = var1;
      long var5 = 0L;
      if (var3 < var5) {
         return 0.0F;
      }

      long var7 = System.currentTimeMillis();
      long var9 = var1;
      long var11 = var7 - var9;
      long var13 = 180L;
      if (var11 >= var13) {
         return 0.0F;
      }

      float var15 = (float)var11 / 180.0F;
      double var16 = var15;
      double var18 = Math.PI;
      double var20 = var16 * var18;
      double var22 = 5.0;
      double var24 = Math.sin(var20 * var22);
      double var26 = 3.0;
      double var28 = var24 * var26;
      float var30 = var15;
      double var31 = 1.0F - var30;
      return (float)(var28 * var31);
   }

   public float v() {
      float var3;
      if (this.custom.getValue()) {
         long var1 = this.m;
         var3 = this.a_float_long(var1);
      } else {
         var3 = 0.0F;
      }

      return var3;
   }

   public float w() {
      float var3;
      if (this.custom.getValue()) {
         long var1 = this.n;
         var3 = this.a_float_long(var1);
      } else {
         var3 = 0.0F;
      }

      return var3;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && this.custom.getValue()) {
         float var2 = ((EntityAccessor)mc.player).getRawYRot();
         float var3 = ((EntityAccessor)mc.player).getRawXRot();
         if (!this.persistent) {
            float var4 = c(var2);
            this.i = var4;
            float var5 = c(var3);
            this.j = var5;
            this.persistent = true;
         } else {
            float var6 = var2;
            float var7 = this.i;
            if (Math.abs(Mth.wrapDegrees(var6 - var7)) >= 45.0F) {
               float var8 = c(var2);
               float var9 = this.i;
               if (var8 != var9) {
                  long var10 = System.currentTimeMillis();
                  this.m = var10;
               }

               float var12 = var8;
               this.i = var12;
            }

            float var13 = var3;
            float var14 = this.j;
            if (Math.abs(Mth.wrapDegrees(var13 - var14)) >= 45.0F) {
               float var15 = c(var3);
               float var16 = this.j;
               if (var15 != var16) {
                  long var17 = System.currentTimeMillis();
                  this.n = var17;
               }

               float var19 = var15;
               this.j = var19;
            }
         }
      } else {
         this.persistent = false;
      }
   }

   private static float c(float var0) {
      return Mth.wrapDegrees(Math.round(var0 / 45.0F) * 45.0F);
   }

   public float a_float_float(float var1) {
      return this.custom.getValue() ? this.i : c(var1);
   }

   public float b(float var1) {
      return this.custom.getValue() ? this.j : this.pitch.getValue().floatValue();
   }

   @Override
   public String getDisplayInfo() {
      String var1;
      if (this.custom.getValue()) {
         var1 = String.valueOf(this.i);
      } else {
         var1 = "Auto";
      }

      String var2 = var1;
      String var3;
      if (this.custom.getValue()) {
         var3 = String.valueOf(this.j);
      } else {
         var3 = String.valueOf(this.pitch.getValue().floatValue());
      }

      String var4 = var3;
      String var5 = var2;
      String var6 = var4;
      return var5 + ", " + var6;
   }
}
