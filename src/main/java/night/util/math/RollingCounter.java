package night.util.math;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RollingCounter {
   private final ConcurrentLinkedQueue<Long> timestamps = new ConcurrentLinkedQueue<>();

   public void increment() {
      ConcurrentLinkedQueue var1 = this.timestamps;
      long var2 = System.currentTimeMillis();
      long var4 = 1000L;
      Long var6 = var2 + var4;
      var1.add(var6);
   }

   public int getCount() {
      this.timestamps.removeIf(var0 -> {
         long var1 = var0;
         long var3 = System.currentTimeMillis();
         boolean var5;
         if (var1 < var3) {
            var5 = true;
         } else {
            var5 = false;
         }

         return var5;
      });
      return this.timestamps.size();
   }

   public void reset() {
      this.timestamps.clear();
   }
}
