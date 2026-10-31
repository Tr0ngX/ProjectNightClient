package night.module.impl.core;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Font;
import night.Night;
import night.events.EventTarget;
import night.events.impl.SettingChangeEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.font.CustomFontRenderer;

@ModuleInfo(name = "Font", description = "Manages the client and the game's font rendering.", category = Module.Category.CORE, enabled = true, drawn = false)
public class FontModule extends Module {
   public CategorySetting customFontCategory = new CategorySetting("CustomFont", "The category for settings relating to custom fonts.");
   public BooleanSetting customFont;
   public StringSetting name2;
   public NumberSetting size;
   public ModeSetting style;
   public BooleanSetting global;
   public CategorySetting offsetsCategory;
   public NumberSetting xOffset;
   public NumberSetting yOffset;
   public NumberSetting widthOffset;
   public NumberSetting heightOffset;
   public CategorySetting shadowsCategory;
   public ModeSetting shadowMode;
   public NumberSetting shadowOffset;
   public BooleanSetting glow;
   public NumberSetting glowRadius;
   public NumberSetting glowIntensity;

   public FontModule() {
      CategorySetting var1 = this.customFontCategory;
      this.customFont = new BooleanSetting("CustomFont", "Enabled", "Enables custom font rendering.", new CategorySetting.CategoryCondition(var1), false);
      CategorySetting var3 = this.customFontCategory;
      CategorySetting.CategoryCondition var2 = new CategorySetting.CategoryCondition(var3);
      this.name2 = new StringSetting("Name", "The name of the font that will be rendered.", var2, "Verdana");
      CategorySetting var5 = this.customFontCategory;
      CategorySetting.CategoryCondition var4 = new CategorySetting.CategoryCondition(var5);
      Integer var6 = 18;
      Integer var7 = 8;
      Integer var8 = 48;
      this.size = new NumberSetting("Size", "The size of the custom font that will be rendered.", var4, var6, var7, var8);
      CategorySetting var10 = this.customFontCategory;
      CategorySetting.CategoryCondition var9 = new CategorySetting.CategoryCondition(var10);
      String[] var11 = new String[]{"Plain", "Bold", "Italic", "BoldItalic"};
      this.style = new ModeSetting("Style", "The style that will be used in the font's rendering.", var9, "Plain", var11);
      CategorySetting var13 = this.customFontCategory;
      CategorySetting.CategoryCondition var12 = new CategorySetting.CategoryCondition(var13);
      this.global = new BooleanSetting("Global", "Applies the custom font rendering on every part of the game.", var12, false);
      this.offsetsCategory = new CategorySetting("Offsets", "Allows you to offset the custom font rendering to make it render perfectly.");
      CategorySetting var15 = this.offsetsCategory;
      CategorySetting.CategoryCondition var14 = new CategorySetting.CategoryCondition(var15);
      Integer var16 = 0;
      Integer var17 = -10;
      Integer var18 = 10;
      this.xOffset = new NumberSetting("XOffset", "The offset that will be applied to the font on the X axis.", var14, var16, var17, var18);
      CategorySetting var20 = this.offsetsCategory;
      CategorySetting.CategoryCondition var19 = new CategorySetting.CategoryCondition(var20);
      Integer var21 = 0;
      Integer var22 = -10;
      Integer var23 = 10;
      this.yOffset = new NumberSetting("YOffset", "The offset that will be applied to the font on the Y axis.", var19, var21, var22, var23);
      CategorySetting var25 = this.offsetsCategory;
      CategorySetting.CategoryCondition var24 = new CategorySetting.CategoryCondition(var25);
      Integer var26 = 0;
      Integer var27 = -10;
      Integer var28 = 10;
      this.widthOffset = new NumberSetting("WidthOffset", "The offset that will be applied to the font on the X axis.", var24, var26, var27, var28);
      CategorySetting var30 = this.offsetsCategory;
      CategorySetting.CategoryCondition var29 = new CategorySetting.CategoryCondition(var30);
      Integer var31 = 0;
      Integer var32 = -10;
      Integer var33 = 10;
      this.heightOffset = new NumberSetting("HeightOffset", "The font's offset on the Y axis.", var29, var31, var32, var33);
      this.shadowsCategory = new CategorySetting("Shadows", "The category for settings related to font shadows.");
      CategorySetting var35 = this.shadowsCategory;
      CategorySetting.CategoryCondition var34 = new CategorySetting.CategoryCondition(var35);
      String[] var36 = new String[]{"None", "Default", "Custom"};
      this.shadowMode = new ModeSetting("ShadowMode", "Mode", "The way that the shadow will be rendered.", var34, "Default", var36);
      ModeSetting var38 = this.shadowMode;
      String[] var39 = new String[]{"Custom"};
      ModeSetting.ModeCondition var37 = new ModeSetting.ModeCondition(var38, var39);
      Float var40 = 0.5F;
      Float var41 = -2.0F;
      Float var42 = 2.0F;
      this.shadowOffset = new NumberSetting("ShadowOffset", "Offset", "The distance of the shadow from the text being rendered.", var37, var40, var41, var42);
      this.glow = new BooleanSetting("Glow", "Renders a glow shader effect for HUD text only.", false);
      Integer var43 = 6;
      Integer var44 = 1;
      Integer var45 = 16;
      this.glowRadius = new NumberSetting("GlowRadius", "Blur radius of the text glow in pixels.", var43, var44, var45);
      Float var46 = 1.2F;
      Float var47 = 0.1F;
      Float var48 = 3.0F;
      this.glowIntensity = new NumberSetting("GlowIntensity", "Brightness of the text glow halo.", var46, var47, var48);
   }

   @EventTarget
   public void a(SettingChangeEvent var1) {
      Setting var2 = var1.getSetting();
      BooleanSetting var3 = this.glow;
      if (var2 == var3) {
         HudModule var4 = HudModule.bf;
         if (var4 != null) {
            boolean var5 = var4.textGlow.getValue();
            boolean var6 = this.glow.getValue();
            if (var5 != var6) {
               BooleanSetting var7 = var4.textGlow;
               boolean var8 = this.glow.getValue();
               var7.setValue(var8);
            }
         }
      }

      Setting var9 = var1.getSetting();
      StringSetting var10 = this.name2;
      if (var9 != var10) {
         Setting var11 = var1.getSetting();
         NumberSetting var12 = this.size;
         if (var11 != var12) {
            Setting var13 = var1.getSetting();
            ModeSetting var14 = this.style;
            if (var13 != var14) {
               Setting var15 = var1.getSetting();
               BooleanSetting var16 = this.customFont;
               if (var15 != var16) {
                  Setting var17 = var1.getSetting();
                  BooleanSetting var18 = this.glow;
                  if (var17 != var18) {
                     return;
                  }
               }
            }
         }
      }

      this.v();
   }

   @Override
   public void onEnable() {
      this.v();
   }

   public void v() {
      try {
         if (RenderSystem.getDevice() == null) {
            return;
         }
      } catch (Throwable var31) {
         return;
      }

      byte var2;
      if (this.style.getValue().equalsIgnoreCase("BoldItalic")) {
         var2 = 3;
      } else if (this.style.getValue().equalsIgnoreCase("Bold")) {
         var2 = 1;
      } else if (this.style.getValue().equalsIgnoreCase("Italic")) {
         var2 = 2;
      } else {
         var2 = 0;
      }

      byte var3 = var2;
      int var4 = this.size.getValue().intValue();
      String var5 = this.name2.getValue();
      byte var6 = var3;
      int var7 = var4;
      Font var8 = new Font(var5, var6, var7);
      byte var9 = var3;
      int var10 = var4;
      Font var11 = new Font("Segoe UI Symbol", var9, var10);
      int var12 = var4;
      Font var13 = new Font("Segoe UI Emoji", 0, var12);
      byte var14 = var3;
      int var15 = var4;
      Font var16 = new Font("Arial Unicode MS", var14, var15);
      byte var17 = var3;
      int var18 = var4;
      Font var19 = new Font("SansSerif", var17, var18);
      byte var20 = var3;
      int var21 = var4;
      Font var22 = new Font("Dialog", var20, var21);
      Font[] var23 = new Font[6];
      Font var24 = var8;
      var23[0] = var24;
      Font var25 = var11;
      var23[1] = var25;
      Font var26 = var13;
      var23[2] = var26;
      Font var27 = var16;
      var23[3] = var27;
      Font var28 = var19;
      var23[4] = var28;
      Font var29 = var22;
      var23[5] = var29;
      float var30 = this.size.getValue().floatValue() / 2.0F;
      Night.fontManager.setCustomFont(new CustomFontRenderer(var23, var30));
   }
}
