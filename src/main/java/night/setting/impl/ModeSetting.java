package night.setting.impl;

import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.setting.Setting;

public class ModeSetting extends Setting {
   private String value;
   private final String defaultValue;
   private final List<String> modes;

   public ModeSetting(String var1, String var2, String var3, String[] var4) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.value = var3;
      this.defaultValue = var3;
      List var5 = Arrays.asList(var4);
      this.modes = var5;
   }

   public ModeSetting(String var1, String var2, String var3, String var4, String[] var5) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.value = var4;
      this.defaultValue = var4;
      List var6 = Arrays.asList(var5);
      this.modes = var6;
   }

   public ModeSetting(String var1, String var2, Setting.VisibilityCondition var3, String var4, String[] var5) {
      super(var1, var1, var2, var3);
      this.value = var4;
      this.defaultValue = var4;
      List var6 = Arrays.asList(var5);
      this.modes = var6;
   }

   public ModeSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, String var5, String[] var6) {
      super(var1, var2, var3, var4);
      this.value = var5;
      this.defaultValue = var5;
      List var7 = Arrays.asList(var6);
      this.modes = var7;
   }

   public void setValue(String var1) {
      if (this.modes.contains(var1)) {
         this.value = var1;
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   public void reset() {
      String var1 = this.defaultValue;
      this.value = var1;
   }

   @Generated
   public String getValue() {
      return this.value;
   }

   @Generated
   public String getDefaultValue() {
      return this.defaultValue;
   }

   @Generated
   public List<String> getModes() {
      return this.modes;
   }

   public static class ModeCondition extends Setting.VisibilityCondition {
      private final ModeSetting a;
      private final List<String> b;

      public ModeCondition(ModeSetting var1, String... var2) {
         super(var1);
         this.a = var1;
         List var3 = Arrays.asList(var2);
         this.b = var3;
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

         boolean var1 = false;

         for (String var3 : (Iterable<String>)(Object)this.b) {
            String var4 = this.a.getValue();
            String var5 = var3;
            if (var4.equals(var5)) {
               var1 = true;
               break;
            }
         }

         boolean var6 = var1;
         this.setVisible(var6);
      }
   }
}
