package night.util.inventory;

import java.util.List;

public class ShulkerSearchUtil {
   public static int a(List<String> var0, String var1) {
      int var2 = 0;

      while (true) {
         int var3 = var2;
         int var4 = var0.size();
         if (var3 >= var4) {
            return -1;
         }

         int var5 = var2;
         if (((String)var0.get(var5)).equalsIgnoreCase(var1)) {
            return var2;
         }

         var2++;
      }
   }
}
