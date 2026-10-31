package night.setting.impl;

import java.awt.Color;
import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.module.impl.core.ColorModule;
import night.setting.Setting;
import night.util.color.ColorUtil;

public class ColorSetting extends Setting {
   private ColorSetting.ColorValue value;
   private final ColorSetting.ColorValue defaultValue;

   public ColorSetting(String var1, String var2, ColorSetting.ColorValue var3) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.value = var3;
      Color var4 = var3.getColor();
      boolean var5 = var3.isSync();
      boolean var6 = var3.isRainbow();
      this.defaultValue = new ColorSetting.ColorValue(var4, var5, var6);
   }

   public ColorSetting(String var1, String var2, String var3, ColorSetting.ColorValue var4) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.value = var4;
      Color var5 = var4.getColor();
      boolean var6 = var4.isSync();
      boolean var7 = var4.isRainbow();
      this.defaultValue = new ColorSetting.ColorValue(var5, var6, var7);
   }

   public ColorSetting(String var1, String var2, Setting.VisibilityCondition var3, ColorSetting.ColorValue var4) {
      super(var1, var1, var2, var3);
      this.value = var4;
      Color var5 = var4.getColor();
      boolean var6 = var4.isSync();
      boolean var7 = var4.isRainbow();
      this.defaultValue = new ColorSetting.ColorValue(var5, var6, var7);
   }

   public ColorSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, ColorSetting.ColorValue var5) {
      super(var1, var2, var3, var4);
      this.value = var5;
      Color var6 = var5.getColor();
      boolean var7 = var5.isSync();
      boolean var8 = var5.isRainbow();
      this.defaultValue = new ColorSetting.ColorValue(var6, var7, var8);
   }

   public Color getColor() {
      long var1 = 0L;
      return this.getColor(var1);
   }

   public Color getColor(long var1) {
      if (this.isSync()) {
         int var3 = this.getAlpha();
         long var4 = var1;
         return ColorUtil.a(var3, var4);
      }

      if (this.isRainbow()) {
         ColorModule var6;
         if (Night.moduleManager != null) {
            var6 = Night.moduleManager.getModule(ColorModule.class);
         } else {
            var6 = null;
         }

         ColorModule var8 = var6;
         long var9;
         if (var8 != null) {
            var9 = var8.rainbowSpeed.getValue().longValue();
         } else {
            var9 = 6L;
         }

         long var11 = var9;
         float var13;
         if (var8 != null) {
            var13 = var8.rainbowSaturation.getValue().floatValue() / 100.0F;
         } else {
            var13 = 1.0F;
         }

         float var14 = var13;
         float var15;
         if (var8 != null) {
            var15 = var8.rainbowBrightness.getValue().floatValue() / 100.0F;
         } else {
            var15 = 1.0F;
         }

         float var16 = var15;
         float var17;
         if (var8 != null && var8.rainbowScale != null) {
            var17 = var8.rainbowScale.getValue().floatValue() / 100.0F;
         } else {
            var17 = 1.0F;
         }

         float var18 = var17;
         long var19 = var11;
         float var21 = var14;
         float var22 = var16;
         int var23 = this.getAlpha();
         float var24 = (float)var1;
         float var25 = var18;
         long var26 = (long)(var24 * var25);
         return ColorUtil.a(var19, var21, var22, var23, var26);
      } else {
         if (Night.moduleManager != null) {
            ColorSetting var7 = Night.moduleManager.getModule(ColorModule.class).color;
            if (this == var7) {
               return ColorUtil.a(this.value.getColor(), 255);
            }
         }

         return this.value.getColor();
      }
   }

   public void setColorValue(ColorSetting.ColorValue var1) {
      this.value = var1;
      if (Night.EVENT_BUS != null) {
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   public void setColor(Color var1) {
      this.value.setColor(var1);
      if (Night.EVENT_BUS != null) {
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   public int getAlpha() {
      return this.getColorValue().getColor().getAlpha();
   }

   public boolean isSync() {
      return this.value.isSync();
   }

   public void setSync(boolean var1) {
      this.value.setSync(var1);
      if (Night.EVENT_BUS != null) {
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   public boolean isRainbow() {
      return this.value.isRainbow();
   }

   public void setRainbow(boolean var1) {
      this.value.setRainbow(var1);
      if (Night.EVENT_BUS != null) {
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   public void reset() {
      int var2 = this.defaultValue.getColor().getRGB();
      Color var1 = new Color(var2, true);
      boolean var3 = this.defaultValue.isSync();
      boolean var4 = this.defaultValue.isRainbow();
      this.value = new ColorSetting.ColorValue(var1, var3, var4);
      if (Night.EVENT_BUS != null) {
         Night.EVENT_BUS.post(new SettingChangeEvent(this));
      }
   }

   @Generated
   public ColorSetting.ColorValue getColorValue() {
      return this.value;
   }

   @Generated
   public ColorSetting.ColorValue getDefaultColorValue() {
      return this.defaultValue;
   }

   public static class ColorCondition extends Setting.VisibilityCondition {
      private final ColorSetting a;
      private final boolean visible;
      private final ColorSetting.ColorCondition.PropertyType c;

      public ColorCondition(ColorSetting var1, boolean var2, ColorSetting.ColorCondition.PropertyType var3) {
         super(var1);
         this.a = var1;
         this.visible = var2;
         this.c = var3;
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

         boolean var3;
         if (this.c == ColorSetting.ColorCondition.PropertyType.RAINBOW) {
            boolean var1 = this.a.isRainbow();
            boolean var2 = this.visible;
            if (var1 == var2) {
               var3 = true;
            } else {
               var3 = false;
            }
         } else {
            boolean var4 = this.a.isSync();
            boolean var5 = this.visible;
            if (var4 == var5) {
               var3 = true;
            } else {
               var3 = false;
            }
         }

         this.setVisible(var3);
      }

      public enum PropertyType {
         RAINBOW,
         SYNC;
      }
   }

   public static class ColorValue {
      private Color color;
      private boolean sync;
      private boolean rainbow;

      @Generated
      public Color getColor() {
         return this.color;
      }

      @Generated
      public boolean isSync() {
         return this.sync;
      }

      @Generated
      public boolean isRainbow() {
         return this.rainbow;
      }

      @Generated
      public void setColor(Color var1) {
         this.color = var1;
      }

      @Generated
      public void setSync(boolean var1) {
         this.sync = var1;
      }

      @Generated
      public void setRainbow(boolean var1) {
         this.rainbow = var1;
      }

      @Generated
      public ColorValue(Color var1, boolean var2, boolean var3) {
         this.color = var1;
         this.sync = var2;
         this.rainbow = var3;
      }
   }
}
