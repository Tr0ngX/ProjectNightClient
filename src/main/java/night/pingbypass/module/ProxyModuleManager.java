package night.pingbypass.module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ProxyModuleManager {
   public static final Set<String> SUPPORTED_MODULES = Set.of("AutoCrystal", "AutoTotem", "SpeedMine");
   private final List<ProxyModule> modules = new ArrayList<>();

   public void register(ProxyModule var1) {
      this.modules.add(var1);
      this.modules.sort(Comparator.comparing(ProxyModule::getName));
   }

   public ProxyModule getModule(String var1) {
      return this.modules.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst().orElse(null);
   }

   public void onTick() {
      for (ProxyModule var2 : (Iterable<ProxyModule>)(Object)this.modules) {
         if (var2.isEnabled()) {
            var2.onTick();
         }
      }
   }
}
