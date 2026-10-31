package night.util.math;

public class NanoTimer {
   private long timeNs;

   public NanoTimer() {
      long var1 = System.currentTimeMillis();
      this.timeNs = var1;
   }

   public boolean passedNs(long var1) {
      long var3 = this.timeNs;
      long var5 = 0L;
      if (var3 == var5) {
         return true;
      }

      long var7 = System.currentTimeMillis();
      long var9 = this.timeNs;
      long var11 = var7 - var9;
      long var13 = var1;
      boolean var15;
      if (var11 >= var13) {
         var15 = true;
      } else {
         var15 = false;
      }

      return var15;
   }

   public void reset() {
      long var1 = 0L;
      this.timeNs = var1;
   }

   public void clear() {
      long var1 = System.currentTimeMillis();
      this.timeNs = var1;
   }
}
