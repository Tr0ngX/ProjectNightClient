package night.util.color;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import night.Night;
import night.module.impl.core.ColorModule;
import night.setting.impl.ColorSetting;

public class ColorUtil {
   public static ColorSetting.ColorValue a() {
      return new ColorSetting.ColorValue(new Color(160, 120, 255), true, false);
   }

   public static ColorSetting.ColorValue b() {
      return new ColorSetting.ColorValue(new Color(160, 120, 255, 40), true, false);
   }

   public static ColorSetting.ColorValue c() {
      return new ColorSetting.ColorValue(new Color(160, 120, 255, 120), true, false);
   }

   public static Color d() {
      return a(255, 0L);
   }

   public static Color a_Color_int(int var0) {
      return a(var0, 0L);
   }

   public static Color a(int var0, long var1) {
      ColorModule var3 = Night.moduleManager != null ? Night.moduleManager.getModule(ColorModule.class) : null;
      if (var3 == null) {
         return new Color(160, 120, 255, var0);
      } else {
         return var3.color.isRainbow()
            ? a(
               var3.rainbowSpeed.getValue().longValue(),
               var3.rainbowSaturation.getValue().floatValue() / 100.0F,
               var3.rainbowBrightness.getValue().floatValue() / 100.0F,
               var0,
               var1
            )
            : a(var3.color.getColorValue().getColor(), var0);
      }
   }

   public static Color a(Color var0, int var1) {
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), var1);
   }

   public static Color a_Color_long(long var0) {
      return b(255, var0);
   }

   public static Color b(int var0, long var1) {
      ColorModule var3 = Night.moduleManager != null ? Night.moduleManager.getModule(ColorModule.class) : null;
      long var4 = var3 != null ? var3.rainbowSpeed.getValue().longValue() : 6L;
      float var6 = var3 != null ? var3.rainbowSaturation.getValue().floatValue() / 100.0F : 1.0F;
      float var7 = var3 != null ? var3.rainbowBrightness.getValue().floatValue() / 100.0F : 1.0F;
      float var8 = var3 != null && var3.rainbowScale != null ? var3.rainbowScale.getValue().floatValue() / 100.0F : 1.0F;
      return a(var4, var6, var7, var0, (long)((float)var1 * var8));
   }

   public static Color a(long var0, float var2, float var3, int var4, long var5) {
      var0 = Math.clamp(var0, 1, 20);
      double var7 = 10500.0 - 500.0 * var0;
      double var9 = System.currentTimeMillis() % (long)var7 / var7;
      double var11 = var5 * 0.0035;
      float var13 = (float)((var9 + var11) % 1.0);
      if (var13 < 0.0F) {
         var13++;
      }

      Color var14 = new Color(Color.HSBtoRGB(Math.clamp(var13, 0.0F, 1.0F), Math.clamp(var2, 0.0F, 1.0F), Math.clamp(var3, 0.0F, 1.0F)));
      return new Color(var14.getRed(), var14.getGreen(), var14.getBlue(), var4);
   }

   public static Color a(Color var0, long var1) {
      ColorModule var3 = Night.moduleManager != null ? Night.moduleManager.getModule(ColorModule.class) : null;
      long var4 = var3 != null ? var3.rainbowSpeed.getValue().longValue() : 6L;
      return a(var0, var4, 255, var1);
   }

   public static Color a(Color var0, long var1, int var3, long var4) {
      var1 = Math.max(1L, Math.min(var1, 20L));
      float[] var6 = new float[3];
      Color.RGBtoHSB(var0.getRed(), var0.getGreen(), var0.getBlue(), var6);
      double var7 = 10500.0 - 500.0 * var1;
      double var9 = System.currentTimeMillis() % (long)var7 / var7;
      double var11 = var4 * 0.0035;
      double var13 = (var9 + var11) % 1.0;
      if (var13 < 0.0) {
         var13++;
      }

      float var15 = (float)Math.abs(var13 * 2.0 % 2.0 - 1.0);
      var6[2] = 0.5F + 0.5F * var15;
      Color var16 = new Color(Color.HSBtoRGB(var6[0], var6[1], var6[2]));
      return new Color(var16.getRed(), var16.getGreen(), var16.getBlue(), var3);
   }

   public static Color a_Color_Color(Color var0) {
      return b(var0, 15L);
   }

   public static Color b(Color var0, long var1) {
      var1 = Math.max(1L, Math.min(var1, 20L));
      double var3 = (Math.sin((Math.PI * 2) * ((float)var1 / 20.0F) * ((float)(System.currentTimeMillis() - Night.START_TIME) / 1000.0F)) + 1.0) / 2.0;
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), (int)(var0.getAlpha() * var3));
   }

   public static Color a_Color_String(String var0) {
      int var1 = var0.hashCode();
      return new Color((var1 & 0xFF0000) >> 16, (var1 & 0xFF00) >> 8, var1 & 0xFF);
   }

   public static boolean b_bool_String(String var0) {
      if (var0.startsWith("#")) {
         var0 = var0.substring(1);
      }

      if (var0.length() != 3 && var0.length() != 6) {
         return false;
      }

      for (int var1 = 0; var1 < var0.length(); var1++) {
         if ((var0.charAt(var1) < '0' || var0.charAt(var1) > '\t')
            && (var0.charAt(var1) < 'a' || var0.charAt(var1) > 'f')
            && var0.charAt(var1) < 'A'
            && var0.charAt(var1) > 'F') {
            return false;
         }
      }

      return true;
   }

   public static ChatFormatting a_ChatFormatting_double(double var0) {
      if (var0 > 18.0) {
         return ChatFormatting.GREEN;
      } else if (var0 > 16.0) {
         return ChatFormatting.DARK_GREEN;
      } else if (var0 > 12.0) {
         return ChatFormatting.YELLOW;
      } else if (var0 > 8.0) {
         return ChatFormatting.GOLD;
      } else {
         return var0 > 5.0 ? ChatFormatting.RED : ChatFormatting.DARK_RED;
      }
   }

   public static ChatFormatting b_ChatFormatting_int(int var0) {
      if (var0 == 1) {
         return ChatFormatting.GREEN;
      } else if (var0 == 2) {
         return ChatFormatting.DARK_GREEN;
      } else if (var0 == 3) {
         return ChatFormatting.YELLOW;
      } else if (var0 == 4) {
         return ChatFormatting.GOLD;
      } else {
         return var0 == 5 ? ChatFormatting.RED : ChatFormatting.DARK_RED;
      }
   }
}
