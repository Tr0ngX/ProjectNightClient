package night.utils.minecraft;

import java.util.Optional;

public final class BaritoneUtils {
   private BaritoneUtils() {
   }

   public static Object getPrimaryBaritone() {
      try {
         Class var0 = Class.forName("baritone.api.BaritoneAPI");
         Object var1 = var0.getMethod("getProvider").invoke(null);
         return var1.getClass().getMethod("getPrimaryBaritone").invoke(var1);
      } catch (Throwable var2) {
         return null;
      }
   }

   public static boolean isAvailable() {
      return getPrimaryBaritone() != null;
   }

   public static boolean isActive() {
      Object var0 = getPrimaryBaritone();
      if (var0 == null) {
         return false;
      }

      try {
         Object var1 = var0.getClass().getMethod("getPathingBehavior").invoke(var0);
         if ((Boolean)var1.getClass().getMethod("isPathing").invoke(var1)) {
            return true;
         }
      } catch (Throwable var7) {
      }

      try {
         Object var8 = var0.getClass().getMethod("getCustomGoalProcess").invoke(var0);
         if ((Boolean)var8.getClass().getMethod("isActive").invoke(var8)) {
            return true;
         }
      } catch (Throwable var6) {
      }

      try {
         Object var9 = var0.getClass().getMethod("getElytraProcess").invoke(var0);
         if (var9 != null && (Boolean)var9.getClass().getMethod("isActive").invoke(var9)) {
            return true;
         }
      } catch (Throwable var5) {
      }

      try {
         Object var10 = var0.getClass().getMethod("getPathingControlManager").invoke(var0);
         Optional var2 = (Optional)var10.getClass().getMethod("mostRecentInControl").invoke(var10);
         if (var2.isPresent()) {
            Object var3 = var2.get();
            if ((Boolean)var3.getClass().getMethod("isActive").invoke(var3)) {
               return true;
            }
         }
      } catch (Throwable var4) {
      }

      return false;
   }
}
