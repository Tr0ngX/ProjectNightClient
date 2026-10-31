package night.pingbypass;

public class PingBypassTimer {
   private static volatile long a = 0L;

   public static void a() {
      a = System.currentTimeMillis();
   }

   public static long b() {
      return a;
   }
}
