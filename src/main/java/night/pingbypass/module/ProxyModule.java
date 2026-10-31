package night.pingbypass.module;

import java.util.List;
import night.setting.Setting;

public abstract class ProxyModule {
   private final String name;
   private boolean enabled = false;

   protected ProxyModule(String var1) {
      this.name = var1;
   }

   public String getName() {
      return this.name;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
      if (var1) {
         this.onEnable();
      } else {
         this.onDisable();
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public abstract void onTick();

   public abstract List<Setting> getSettings();

   public Setting getSetting(String var1) {
      return this.getSettings().stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst().orElse(null);
   }
}
