package night.pingbypass;

import night.Night;

public class PingBypassState {
   public static volatile boolean a = false;
   public static volatile boolean b = false;
   public static volatile boolean c = false;
   public static volatile boolean d = false;
   public static volatile boolean e = true;
   public static volatile boolean f = false;

   public static boolean a() {
      boolean var0;
      if (d && Night.pingBypassConfig != null && !Night.pingBypassConfig.isServerMode()) {
         var0 = true;
      } else {
         var0 = false;
      }

      return var0;
   }
}
