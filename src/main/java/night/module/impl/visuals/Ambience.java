package night.module.impl.visuals;

import java.awt.Color;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.AmbienceAccess;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "Ambience", description = "Modifies the world ambient lighting and atmosphere color.", category = Module.Category.VISUALS)
public class Ambience extends Module implements AmbienceAccess {
   public ColorSetting color = new ColorSetting(
      "Color", "The ambience color applied to the world.", new ColorSetting.ColorValue(new Color(255, 128, 128, 255), true, false)
   );
   public BooleanSetting useSaturation = new BooleanSetting("UseSaturation", "Overrides the saturation of the ambience color.", false);
   public NumberSetting saturation = new NumberSetting(
      "Saturation", "The custom saturation level.", new BooleanSetting.BooleanCondition(this.useSaturation, true), 0.5F, 0.0F, 1.0F
   );
   public BooleanSetting lightmap = new BooleanSetting("Lightmap", "Modifies the world lightmap lighting.", true);
   public BooleanSetting sky = new BooleanSetting("Sky", "Tints sky lighting with ambience.", true);
   public BooleanSetting blocks = new BooleanSetting("Blocks", "Tints block lighting with ambience.", true);

   @Override
   public Color getAmbienceColor() {
      Color var1 = this.color.getColor();
      if (this.useSaturation.getValue()) {
         float[] var2 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
         Color var3 = Color.getHSBColor(var2[0], this.saturation.getValue().floatValue(), var2[2]);
         return new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), var1.getAlpha());
      } else {
         return var1;
      }
   }

   public boolean v() {
      return this.useSaturation.getValue();
   }

   public float w() {
      return this.saturation.getValue().floatValue();
   }

   @Override
   public boolean isLightmapEnabled() {
      return this.lightmap.getValue();
   }

   @Override
   public boolean isSkyEnabled() {
      return this.sky.getValue();
   }

   @Override
   public boolean isBlocksEnabled() {
      return this.blocks.getValue();
   }
}
