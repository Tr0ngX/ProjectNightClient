package night.module.impl.player;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "NoBreakDelay", description = "Allows you to customize the delay between breaking blocks.", category = Module.Category.PLAYER)
public class NoBreakDelay extends Module {
   public NumberSetting delay;

   public NoBreakDelay() {
      Integer var1 = 0;
      Integer var2 = 0;
      Integer var3 = 5;
      this.delay = new NumberSetting("Delay", "The delay between block breaks in ticks (0 is no delay, 5 is vanilla).", var1, var2, var3);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.delay.getValue().intValue());
   }
}
