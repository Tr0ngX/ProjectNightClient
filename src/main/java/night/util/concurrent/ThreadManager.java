package night.util.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadManager {
   private static final Map<Class<?>, Thread> a = new ConcurrentHashMap<>();

   public static void a(Runnable var0) {
      Class var1 = var0.getClass().getEnclosingClass();
      if (var1 == null) {
         var1 = var0.getClass();
      }

      Class var2 = var1;
      Thread var3 = a.get(var2);
      if (var3 == null || !var3.isAlive()) {
         Class var4 = var1;
         ThreadManager.WorkerThread var5 = new ThreadManager.WorkerThread(() -> {
            try {
               var0.run();
            } catch (Throwable var3x) {
               a.remove(var4);
               throw var3x;
            }

            a.remove(var4);
         });
         Class var6 = var4;
         ThreadManager.WorkerThread var7 = var5;
         a.put(var6, var7);
         var5.start();
      }
   }

   static class WorkerThread extends Thread {
      private final Runnable a;

      public WorkerThread(Runnable var1) {
         this.a = var1;
      }

      @Override
      public void run() {
         this.a.run();
      }
   }
}
