package night.module.impl.movement;

import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.NoJumpDelayAccess;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "NoJumpDelay", description = "Removes the delay that slows down your jumping.", category = Module.Category.MOVEMENT)
public class NoJumpDelay extends Module implements NoJumpDelayAccess {
   public NumberSetting ticks;

   public NoJumpDelay() {
      Integer var1 = 1;
      Integer var2 = 0;
      Integer var3 = 20;
      this.ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before jumping again.", var1, var2, var3);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.ticks.getValue());
   }

   @Override
   public int getDelayTicks() {
      return this.ticks.getValue().intValue();
   }
}
