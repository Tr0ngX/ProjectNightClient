package night.setting.impl;

import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.setting.Setting;

public class KeybindSetting extends Setting {
   private int key;
   private final int defaultKey;
   public static final String[] MODES;
   private String mode = "Bind";
   private boolean allowHoldMode = true;

   public KeybindSetting disableHoldMode() {
      this.allowHoldMode = false;
      return this;
   }

   public KeybindSetting(String var1, String var2, int var3) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.key = var3;
      this.defaultKey = var3;
   }

   public KeybindSetting(String var1, String var2, String var3, int var4) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.key = var4;
      this.defaultKey = var4;
   }

   public KeybindSetting(String var1, String var2, Setting.VisibilityCondition var3, int var4) {
      super(var1, var1, var2, var3);
      this.key = var4;
      this.defaultKey = var4;
   }

   public KeybindSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, int var5) {
      super(var1, var2, var3, var4);
      this.key = var5;
      this.defaultKey = var5;
   }

   public void reset() {
      int var1 = this.defaultKey;
      this.key = var1;
      this.mode = "Bind";
   }

   public void setKey(int var1) {
      this.key = var1;
      Night.EVENT_BUS.post(new SettingChangeEvent(this));
   }

   public void cycleMode() {
      if (this.allowHoldMode) {
         List var1 = Arrays.asList(MODES);
         String var2 = this.mode;
         int var3 = var1.indexOf(var2) + 1;
         int var4 = MODES.length;
         int var5 = var3 % var4;
         String var6 = MODES[var5];
         this.mode = var6;
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   public void setMode(String var1) {
      if (this.allowHoldMode || var1.equals("Bind")) {
         if (Arrays.asList(MODES).contains(var1)) {
            this.mode = var1;
            Night.EVENT_BUS.post(new SettingChangeEvent(this));
         }
      }
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public int getDefaultKey() {
      return this.defaultKey;
   }

   @Generated
   public String getMode() {
      return this.mode;
   }

   @Generated
   public boolean isAllowHoldMode() {
      return this.allowHoldMode;
   }

   @Generated
   public void setAllowHoldMode(boolean var1) {
      this.allowHoldMode = var1;
   }

   static {
      String[] var0 = new String[]{"Bind", "Hold", "ReverseHold"};
      MODES = var0;
   }

   public static class KeyCondition extends Setting.VisibilityCondition {
      private final KeybindSetting a;
      private final int b;

      public KeyCondition(KeybindSetting var1, int var2) {
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

         int var1 = this.a.getKey();
         int var2 = this.b;
         boolean var3;
         if (var1 == var2) {
            var3 = true;
         } else {
            var3 = false;
         }

         this.setVisible(var3);
      }
   }
}
