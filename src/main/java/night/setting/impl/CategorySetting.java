package night.setting.impl;

import lombok.Generated;
import night.setting.Setting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;

public class CategorySetting extends Setting {
   private boolean open = false;
   private final Animation animation;

   public CategorySetting(String var1, String var2) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      int var3 = Animation.a_int_int(200);
      this.animation = new Animation(var3, EasingUtil.Easing.DECELERATE);
   }

   public CategorySetting(String var1, String var2, String var3) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      int var4 = Animation.a_int_int(200);
      this.animation = new Animation(var4, EasingUtil.Easing.DECELERATE);
   }

   public CategorySetting(String var1, String var2, Setting.VisibilityCondition var3) {
      super(var1, var1, var2, var3);
      int var4 = Animation.a_int_int(200);
      this.animation = new Animation(var4, EasingUtil.Easing.DECELERATE);
   }

   public CategorySetting(String var1, String var2, String var3, Setting.VisibilityCondition var4) {
      super(var1, var2, var3, var4);
      int var5 = Animation.a_int_int(200);
      this.animation = new Animation(var5, EasingUtil.Easing.DECELERATE);
   }

   public float getAnimationProgress() {
      Animation var1 = this.animation;
      float var2;
      if (this.open) {
         var2 = 1.0F;
      } else {
         var2 = 0.0F;
      }

      return var1.animateTo(var2);
   }

   @Generated
   public boolean isOpen() {
      return this.open;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public void setOpen(boolean var1) {
      this.open = var1;
   }

   public static class CategoryCondition extends Setting.VisibilityCondition {
      private final CategorySetting a;

      public CategoryCondition(CategorySetting var1) {
         super(var1);
         this.a = var1;
      }

      public CategorySetting d() {
         return this.a;
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
         if (this.a.getAnimationProgress() > 0.001F) {
            var1 = true;
         } else {
            var1 = false;
         }

         this.setVisible(var1);
      }
   }
}
