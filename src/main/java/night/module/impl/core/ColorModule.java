package night.module.impl.core;

import java.awt.Color;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "Color", description = "Manages the client's global color system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class ColorModule extends Module {
   public ColorSetting color;
   public CategorySetting rainbowCategory;
   public NumberSetting rainbowSpeed;
   public NumberSetting rainbowSaturation;
   public NumberSetting rainbowBrightness;
   public NumberSetting rainbowScale;

   public ColorModule() {
      ColorSetting.ColorValue var1 = new ColorSetting.ColorValue(new Color(160, 120, 255), false, false);
      this.color = new ColorSetting("Color", "The global color that is used in sync and in most of the elements.", var1);
      this.rainbowCategory = new CategorySetting("Rainbow", "The category containing all settings related to rainbow coloring.");
      CategorySetting var3 = this.rainbowCategory;
      CategorySetting.CategoryCondition var2 = new CategorySetting.CategoryCondition(var3);
      Long var4 = 6L;
      Long var5 = 1L;
      Long var6 = 20L;
      this.rainbowSpeed = new NumberSetting("RainbowSpeed", "Speed", "The speed that rainbow colors will be cycling at.", var2, var4, var5, var6);
      CategorySetting var8 = this.rainbowCategory;
      CategorySetting.CategoryCondition var7 = new CategorySetting.CategoryCondition(var8);
      Float var9 = 100.0F;
      Float var10 = 0.0F;
      Float var11 = 100.0F;
      this.rainbowSaturation = new NumberSetting("RainbowSaturation", "Saturation", "The saturation value of the rainbow color.", var7, var9, var10, var11);
      CategorySetting var13 = this.rainbowCategory;
      CategorySetting.CategoryCondition var12 = new CategorySetting.CategoryCondition(var13);
      Float var14 = 100.0F;
      Float var15 = 0.0F;
      Float var16 = 100.0F;
      this.rainbowBrightness = new NumberSetting("RainbowBrightness", "Brightness", "The brightness value of the rainbow color.", var12, var14, var15, var16);
      CategorySetting var18 = this.rainbowCategory;
      CategorySetting.CategoryCondition var17 = new CategorySetting.CategoryCondition(var18);
      Float var19 = 100.0F;
      Float var20 = 10.0F;
      Float var21 = 500.0F;
      this.rainbowScale = new NumberSetting("RainbowScale", "Scale", "The scale / density of the rainbow bands.", var17, var19, var20, var21);
   }
}
