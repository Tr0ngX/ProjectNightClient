package night.module.impl.core;

import java.awt.Color;
import java.util.Arrays;
import night.Night;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.render.EspShaderRenderer;

@ModuleInfo(
   name = "ClickGui",
   description = "Allows you to change and interact with the client's modules and settings through a GUI.",
   category = Module.Category.CORE,
   drawn = false,
   bind = 344
)
public class ClickGuiModule extends Module {
   public BooleanSetting sounds = new BooleanSetting("Sounds", "Plays Minecraft UI sounds when interacting with the client's GUI.", true);
   public BooleanSetting blur = new BooleanSetting("Blur", "Whether or not to blur the background behind the GUI.", true);
   public NumberSetting scrollSpeed;
   public NumberSetting scale;
   public ColorSetting color;
   private static final String[] u = w();
   public ModeSetting fillMode;
   private static final String[] v;
   public NumberSetting neekeriSpeed;
   public NumberSetting neekeriOpacity;
   public static final String[] m;
   public ModeSetting background;
   private static final String[] w;
   public NumberSetting bgSpeed;
   public NumberSetting bgOpacity;
   public BooleanSetting glowAccent;
   public BooleanSetting categoryIcons;
   public BooleanSetting triangles;
   public BooleanSetting switches;

   public ClickGuiModule() {
      Integer var1 = 1;
      Integer var2 = 1;
      Integer var3 = 10;
      this.scrollSpeed = new NumberSetting("ScrollSpeed", "The speed at which the scrolling of the frames will be at.", var1, var2, var3);
      Setting.VisibilityCondition var4 = new Setting.VisibilityCondition();
      Float var5 = 1.0F;
      Float var6 = 0.5F;
      Float var7 = 2.0F;
      Float var8 = 0.1F;
      this.scale = new NumberSetting("Scale", "Scale", "The overall size of the ClickGUI. Applies next time you open the GUI.", var4, var5, var6, var7, var8);
      Color var9 = new Color(160, 120, 255);
      this.color = new ColorSetting("Color", "The color that will be used in the GUI.", new ColorSetting.ColorValue(var9, false, false));
      this.fillMode = new ModeSetting("FillMode", "How a toggled module's row background fills.", "Default", u);
      ModeSetting var11 = this.fillMode;
      ModeSetting.ModeCondition var10 = new ModeSetting.ModeCondition(var11, v);
      Float var12 = 1.0F;
      Float var13 = 0.1F;
      Float var14 = 10.0F;
      this.neekeriSpeed = new NumberSetting("NeekeriSpeed", "Speed", "The speed at which the fill animates.", var10, var12, var13, var14);
      ModeSetting var16 = this.fillMode;
      ModeSetting.ModeCondition var15 = new ModeSetting.ModeCondition(var16, v);
      Integer var17 = 90;
      Integer var18 = 0;
      Integer var19 = 100;
      this.neekeriOpacity = new NumberSetting("NeekeriOpacity", "Opacity", "The opacity of the fill.", var15, var17, var18, var19);
      this.background = new ModeSetting("Background", "Shader effect rendered behind the GUI.", "Aurora", m);
      ModeSetting var21 = this.background;
      ModeSetting.ModeCondition var20 = new ModeSetting.ModeCondition(var21, w);
      Float var22 = 1.0F;
      Float var23 = 0.1F;
      Float var24 = 5.0F;
      this.bgSpeed = new NumberSetting("BgSpeed", "Speed", "The speed at which the background shader animates.", var20, var22, var23, var24);
      ModeSetting var26 = this.background;
      ModeSetting.ModeCondition var25 = new ModeSetting.ModeCondition(var26, w);
      Integer var27 = 65;
      Integer var28 = 0;
      Integer var29 = 100;
      this.bgOpacity = new NumberSetting("BgOpacity", "Opacity", "The opacity of the background shader.", var25, var27, var28, var29);
      this.glowAccent = new BooleanSetting("GlowAccent", "Renders glowing accent lines and header highlights.", true);
      this.categoryIcons = new BooleanSetting("CategoryIcons", "Displays modern icons in frame headers.", true);
      this.triangles = new BooleanSetting("Triangles", "Displays triangle collapse/expand indicators for modules and categories.", false);
      this.switches = new BooleanSetting("Switches", "Displays toggle switch buttons for modules and settings.", false);
   }

   public boolean v() {
      return this.color.isRainbow();
   }

   private static String[] w() {
      String[] var0 = EspShaderRenderer.a;
      String[] var1 = new String[var0.length];
      var1[0] = "Default";
      String[] var2 = var0;
      String[] var3 = var1;
      int var4 = var0.length - 1;
      System.arraycopy(var2, 1, var3, 1, var4);
      return var1;
   }

   @Override
   public void onEnable() {
      if (mc.player == null) {
         this.setEnabled(false);
      } else {
         float var1 = this.scale.getValue().floatValue();
         Night.clickGui.a_void_float(var1);
         Night.clickGui.c();
         mc.gui.setScreen(Night.clickGui);
      }
   }

   @Override
   public void onDisable() {
      if (mc.gui.screen() == Night.clickGui) {
         Night.clickGui.b();
      } else {
         mc.gui.setScreen(null);
      }
   }

   static {
      int var0 = u.length;
      v = Arrays.copyOfRange(u, 1, var0);
      String[] var1 = new String[]{"Aurora", "CyberGrid", "Constellations", "MeshGlow", "DarkFrost", "None"};
      m = var1;
      String[] var2 = new String[]{"Aurora", "CyberGrid", "Constellations", "MeshGlow", "DarkFrost"};
      w = var2;
   }
}
