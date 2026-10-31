package night.module.impl.misc;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;

@ModuleInfo(
   name = "AutoReconnect",
   description = "Automatically reconnects you to a server after a specified time period.",
   category = Module.Category.MISCELLANEOUS
)
public class AutoReconnect extends Module {
   public NumberSetting delay;

   public AutoReconnect() {
      Integer var1 = 5;
      Integer var2 = 0;
      Integer var3 = 20;
      this.delay = new NumberSetting("Delay", "The amount of seconds that have to pass before reconnecting.", var1, var2, var3);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.delay.getValue().intValue());
   }
}
