package night.util;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RenderQueue {
   private static final List<Runnable> a = new CopyOnWriteArrayList<>();

   public static void a(Runnable var0) {
      a.add(var0);
   }

   public static void b(Runnable var0) {
      a.remove(var0);
   }

   public static void a() {
      Iterator var0 = a.iterator();

      while (var0.hasNext()) {
         ((Runnable)var0.next()).run();
      }
   }
}
