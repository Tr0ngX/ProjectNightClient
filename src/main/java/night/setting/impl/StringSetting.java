package night.setting.impl;

import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.setting.Setting;

public class StringSetting extends Setting {
   private String value;
   private final String defaultValue;

   public StringSetting(String var1, String var2, String var3) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.value = var3;
      this.defaultValue = var3;
   }

   public StringSetting(String var1, String var2, String var3, String var4) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.value = var4;
      this.defaultValue = var4;
   }

   public StringSetting(String var1, String var2, Setting.VisibilityCondition var3, String var4) {
      super(var1, var1, var2, var3);
      this.value = var4;
      this.defaultValue = var4;
   }

   public StringSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, String var5) {
      super(var1, var2, var3, var4);
      this.value = var5;
      this.defaultValue = var5;
   }

   public void reset() {
      String var1 = this.defaultValue;
      this.value = var1;
   }

   public void setValue(String var1) {
      this.value = var1;
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

   public static class StringCondition extends Setting.VisibilityCondition {
      private final StringSetting a;
      private final String b;

      public StringCondition(StringSetting var1, String var2) {
         super(var1);
         this.a = var1;
         this.b = var2;
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

         String var1 = this.a.getValue();
         String var2 = this.b;
         boolean var3 = var1.equals(var2);
         this.setVisible(var3);
      }
   }
}
