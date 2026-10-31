package night.util;

public class MathLerpUtil {
   public static double a(double var0, double var2, double var4) {
      if (var4 >= 1.0) {
         return var2;
      }

      if (var4 <= 0.0) {
         return var0;
      }

      boolean var6 = var2 > var0;
      double var7 = Math.abs(var2 - var0);
      if (var7 <= 0.001) {
         return var2;
      }

      double var9 = var7 * var4;
      return var0 + (var6 ? var9 : -var9);
   }
}
