package night.util.math;

public class TimerUtil {
   private long lastTimeMs;

   public TimerUtil() {
      long var1 = System.currentTimeMillis();
      this.lastTimeMs = var1;
   }

   public boolean passedMs(Number var1) {
      long var2 = System.currentTimeMillis();
      long var4 = this.lastTimeMs;
      long var6 = var2 - var4;
      long var8 = var1.longValue();
      boolean var10;
      if (var6 >= var8) {
         var10 = true;
      } else {
         var10 = false;
      }

      return var10;
   }

   public long getElapsedMs() {
      long var1 = System.currentTimeMillis();
      long var3 = this.lastTimeMs;
      return var1 - var3;
   }

   public void reset() {
      long var1 = System.currentTimeMillis();
      this.lastTimeMs = var1;
   }
}
