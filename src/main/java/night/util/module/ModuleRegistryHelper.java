package night.util.module;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import night.Night;
import night.module.Module;

public final class ModuleRegistryHelper {
   private static final Set<Module> a = ConcurrentHashMap.newKeySet();

   public static void a(Module var0) {
      a.add(var0);
      Night.worldStateManager.setTimerMultiplier(1.08F);
   }

   public static void b(Module var0) {
      if (a.remove(var0) && a.isEmpty()) {
         Night.worldStateManager.setTimerMultiplier(1.0F);
      }
   }
}
