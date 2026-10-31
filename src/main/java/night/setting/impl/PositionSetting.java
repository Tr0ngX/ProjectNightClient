package night.setting.impl;

import lombok.Generated;
import night.setting.Setting;

public class PositionSetting extends Setting {
   private float x = 0.0F;
   private float y = 0.0F;

   public PositionSetting(String var1, String var2) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
   }

   public void setPosition(float var1, float var2) {
      this.x = var1;
      this.y = var2;
   }

   public void reset() {
      this.x = 0.0F;
      this.y = 0.0F;
   }

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   public float getY() {
      return this.y;
   }

   @Generated
   public void setX(float var1) {
      this.x = var1;
   }

   @Generated
   public void setY(float var1) {
      this.y = var1;
   }
}
