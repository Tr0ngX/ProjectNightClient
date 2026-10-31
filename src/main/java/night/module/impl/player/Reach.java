package night.module.impl.player;

import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.ReachAccess;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "Reach", description = "Allows you to modify the distance at which you can interact with blocks.", category = Module.Category.PLAYER)
public class Reach extends Module implements ReachAccess {
   public NumberSetting amount;

   public Reach() {
      Double var1 = 6.0;
      Double var2 = 0.0;
      Double var3 = 8.0;
      this.amount = new NumberSetting("Amount", "The maximum distance at which you will be able to interact with blocks.", var1, var2, var3);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.amount.getValue().doubleValue());
   }

   @Override
   public double getReachDistance() {
      return this.amount.getValue().doubleValue();
   }
}
