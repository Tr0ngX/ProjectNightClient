package night.util.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;

public final class KeyCodeParser {
   public static String a_String_int(int var0) {
      if (var0 != 0 && var0 != -1) {
         Key var2;
         if (var0 < 0) {
            int var1 = -var0 - 1;
            var2 = Type.MOUSE.getOrCreate(var1);
         } else {
            var2 = Type.KEYSYM.getOrCreate(var0);
         }

         return var2.getName();
      } else {
         return Integer.toString(var0);
      }
   }

   public static int a_int_String(String var0) {
      String var1 = var0.trim();
      if (var1.matches("-?\\d+")) {
         return b(Integer.parseInt(var1));
      }

      try {
         Key var2 = InputConstants.getKey(var1);
         int var3;
         if (var2.getType() == Type.MOUSE) {
            var3 = -var2.getValue() - 1;
         } else {
            var3 = var2.getValue();
         }

         return var3;
      } catch (RuntimeException var5) {
         return 0;
      }
   }

   private static int b(int var0) {
      return var0;
   }
}
