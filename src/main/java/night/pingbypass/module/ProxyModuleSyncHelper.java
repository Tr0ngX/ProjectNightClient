package night.pingbypass.module;

import java.awt.Color;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;

public class ProxyModuleSyncHelper {
   public static void a(ProxyModule var0, boolean var1) {
      var0.setEnabled(var1);
   }

   public static void a(ProxyModule var0, String var1, String var2) {
      Setting var3 = var0.getSetting(var1);
      if (var3 != null) {
         if (var3 instanceof BooleanSetting var4) {
            boolean var5 = Boolean.parseBoolean(var2);
            var4.setValue(var5);
         } else if (var3 instanceof NumberSetting var6) {
            switch (var6.getNumberType()) {
               case INTEGER:
                  NumberSetting var7 = var6;
                  Integer var8 = Integer.parseInt(var2);
                  var7.setValue(var8);
                  break;
               case LONG:
                  NumberSetting var11 = var6;
                  Long var12 = Long.parseLong(var2);
                  var11.setValue(var12);
                  break;
               case FLOAT:
                  NumberSetting var9 = var6;
                  Float var10 = Float.parseFloat(var2);
                  var9.setValue(var10);
                  break;
               case DOUBLE:
                  NumberSetting var13 = var6;
                  Double var14 = Double.parseDouble(var2);
                  var13.setValue(var14);
            }
         } else if (var3 instanceof ModeSetting) {
            ((ModeSetting)var3).setValue(var2);
         } else if (var3 instanceof StringSetting) {
            ((StringSetting)var3).setValue(var2);
         } else if (var3 instanceof ColorSetting var15) {
            String[] var16 = var2.split(",");
            if (var16.length >= 4) {
               ColorSetting var17 = var15;
               int var18 = Integer.parseInt(var16[0]);
               int var19 = Integer.parseInt(var16[1]);
               int var20 = Integer.parseInt(var16[2]);
               int var21 = Integer.parseInt(var16[3]);
               var17.setColor(new Color(var18, var19, var20, var21));
               if (var16.length >= 5) {
                  ColorSetting var22 = var15;
                  boolean var23 = Boolean.parseBoolean(var16[4]);
                  var22.setSync(var23);
               }

               if (var16.length >= 6) {
                  ColorSetting var24 = var15;
                  boolean var25 = Boolean.parseBoolean(var16[5]);
                  var24.setRainbow(var25);
               }
            }
         }
      }
   }
}
