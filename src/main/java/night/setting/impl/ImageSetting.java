package night.setting.impl;

import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.setting.Setting;

public class ImageSetting extends Setting {
   private String value;
   private final String defaultValue;

   public ImageSetting(String var1, String var2, String var3) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.value = var3;
      this.defaultValue = var3;
   }

   public ImageSetting(String var1, String var2, String var3, String var4) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.value = var4;
      this.defaultValue = var4;
   }

   public ImageSetting(String var1, String var2, Setting.VisibilityCondition var3, String var4) {
      super(var1, var1, var2, var3);
      this.value = var4;
      this.defaultValue = var4;
   }

   public ImageSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, String var5) {
      super(var1, var2, var3, var4);
      this.value = var5;
      this.defaultValue = var5;
   }

   public void reset() {
      String var1 = this.defaultValue;
      this.setValue(var1);
   }

   public void setValue(String var1) {
      this.value = var1;
      if (Night.customImageManager != null) {
         Night.customImageManager.setActiveImage(var1);
      }

      Night.EVENT_BUS.post(new SettingChangeEvent(this));
   }

   @Generated
   public String getValue() {
      return this.value;
   }

   @Generated
   public String getDefaultValue() {
      return this.defaultValue;
   }
}
