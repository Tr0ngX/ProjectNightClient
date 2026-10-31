package night.module.impl.core;

import java.awt.Color;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;

@ModuleInfo(name = "Renders", description = "Manages the client world renders.", category = Module.Category.CORE, persistent = true, drawn = false)
public class RendersModule extends Module {
   public ModeSetting mode;
   public NumberSetting duration;
   public ModeSetting renderMode;
   public ColorSetting fillColor;
   public ColorSetting outlineColor;

   public RendersModule() {
      String[] var1 = new String[]{"Fade", "Shrink"};
      this.mode = new ModeSetting("Mode", "The mode for the place render.", "Fade", var1);
      Integer var2 = 300;
      Integer var3 = 0;
      Integer var4 = 1000;
      this.duration = new NumberSetting("Duration", "The duration for the place render.", var2, var3, var4);
      String[] var5 = new String[]{"Fill", "Outline", "Both"};
      this.renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the blocks highlighted.", "Both", var5);
      ModeSetting var7 = this.renderMode;
      String[] var8 = new String[]{"Fill", "Both"};
      ModeSetting.ModeCondition var6 = new ModeSetting.ModeCondition(var7, var8);
      ColorSetting.ColorValue var9 = ColorUtil.b();
      this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", var6, var9);
      ModeSetting var11 = this.renderMode;
      String[] var12 = new String[]{"Outline", "Both"};
      ModeSetting.ModeCondition var10 = new ModeSetting.ModeCondition(var11, var12);
      ColorSetting.ColorValue var13 = ColorUtil.c();
      this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", var10, var13);
   }

   public Color a(String var1, Color var2, float var3) {
      if (var1.equalsIgnoreCase("Fade")) {
         int var4 = (int)(var2.getAlpha() * var3);
         return ColorUtil.a(var2, var4);
      } else {
         return var2;
      }
   }
}
