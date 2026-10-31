package night.setting.impl;

import night.Night;
import night.events.impl.SettingChangeEvent;
import night.setting.Setting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;

public class BooleanSetting extends Setting {
   private boolean value;
   private final boolean defaultValue;
   private final Animation animation;

   public float getAnimationProgress() {
      Animation var1 = this.animation;
      float var2;
      if (this.value) {
         var2 = 1.0F;
      } else {
         var2 = 0.0F;
      }

      return var1.animateTo(var2);
   }

   public BooleanSetting(String var1, String var2, boolean var3) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.value = var3;
      this.defaultValue = var3;
      Animation var4 = b(var3);
      this.animation = var4;
   }

   public BooleanSetting(String var1, String var2, String var3, boolean var4) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.value = var4;
      this.defaultValue = var4;
      Animation var5 = b(var4);
      this.animation = var5;
   }

   public BooleanSetting(String var1, String var2, Setting.VisibilityCondition var3, boolean var4) {
      super(var1, var1, var2, var3);
      this.value = var4;
      this.defaultValue = var4;
      Animation var5 = b(var4);
      this.animation = var5;
   }

   public BooleanSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, boolean var5) {
      super(var1, var2, var3, var4);
      this.value = var5;
      this.defaultValue = var5;
      Animation var6 = b(var5);
      this.animation = var6;
   }

   private static Animation b(boolean var0) {
      float var1;
      if (var0) {
         var1 = 1.0F;
      } else {
         var1 = 0.0F;
      }

      float var2 = var1;
      int var3 = Animation.a_int_int(180);
      return new Animation(var2, var2, var3, EasingUtil.Easing.DECELERATE);
   }

   public boolean getValue() {
      return this.value;
   }

   public void setValue(boolean var1) {
      this.value = var1;
      Night.EVENT_BUS.post(new SettingChangeEvent(this));
   }

   public boolean getDefaultValue() {
      return this.defaultValue;
   }

   public void reset() {
      boolean var1 = this.defaultValue;
      this.value = var1;
   }

   public static class BooleanCondition extends Setting.VisibilityCondition {
      private final BooleanSetting a;
      private final boolean visible;

      public BooleanCondition(BooleanSetting var1, boolean var2) {
         super(var1);
         this.a = var1;
         this.visible = var2;
      }

      @Override
      public void update() {
         if (this.a.getVisibility() != null) {
            this.a.getVisibility().update();
            if (!this.a.getVisibility().isVisible()) {
               this.setVisible(false);
               return;
            }
         }

         boolean var1;
         if (this.d() > 0.001F) {
            var1 = true;
         } else {
            var1 = false;
         }

         this.setVisible(var1);
      }

      public float d() {
         float var1 = this.a.getAnimationProgress();
         float var2;
         if (this.visible) {
            var2 = var1;
         } else {
            float var3 = var1;
            var2 = 1.0F - var3;
         }

         return var2;
      }
   }
}
