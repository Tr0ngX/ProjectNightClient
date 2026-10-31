package night.manager;

import java.util.LinkedHashMap;
import java.util.Map;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.PositionSetting;

public class HudElementManager {
   private static final Map<String, HudElementManager.HudEntry> a = new LinkedHashMap<>();
   private static final Map<String, float[]> b = new LinkedHashMap<>();

   public static void a(String var0, BooleanSetting var1, PositionSetting var2) {
      a(var0, var1, var2, null);
   }

   public static void a(String var0, BooleanSetting var1, PositionSetting var2, CategorySetting var3) {
      a.put(var0, new HudElementManager.HudEntry(var0, var1, var2, var3));
   }

   public static void a(String var0, float var1, float var2, float var3, float var4) {
      b.put(var0, new float[]{var1, var2, var3, var4});
   }

   public static Map<String, HudElementManager.HudEntry> a() {
      return a;
   }

   public static float[] a(String var0) {
      float[] var1 = b.get(var0);
      HudElementManager.HudEntry var2 = a.get(var0);
      if (var1 != null && var2 != null) {
         float var3 = var2.positionSetting().getX();
         float var4 = var2.positionSetting().getY();
         return new float[]{var1[0] + var3, var1[1] + var4, var1[2] + var3, var1[3] + var4};
      } else {
         return var1;
      }
   }

   public static void a(String var0, int var1, int var2) {
      HudElementManager.HudEntry var3 = a.get(var0);
      if (var3 != null) {
         float[] var4 = b.get(var0);
         if (var4 == null) {
            var4 = new float[]{0.0F, 0.0F, 60.0F, 15.0F};
         }

         float var5 = Math.min(var4[0], var4[2]);
         float var6 = Math.max(var4[0], var4[2]);
         float var7 = Math.min(var4[1], var4[3]);
         float var8 = Math.max(var4[1], var4[3]);
         float var9 = 0.0F - var5;
         float var10 = var1 - var6;
         if (var10 < var9) {
            var10 = var9;
         }

         float var11 = 0.0F - var7;
         float var12 = var2 - var8;
         if (var12 < var11) {
            var12 = var11;
         }

         float var13 = var3.positionSetting().getX();
         float var14 = var3.positionSetting().getY();
         float var15 = Math.max(var9, Math.min(var13, var10));
         float var16 = Math.max(var11, Math.min(var14, var12));
         var3.positionSetting().setPosition(var15, var16);
      }
   }

   public record HudEntry(String text, BooleanSetting enabledSetting, PositionSetting positionSetting, CategorySetting categorySetting) {
   }
}
