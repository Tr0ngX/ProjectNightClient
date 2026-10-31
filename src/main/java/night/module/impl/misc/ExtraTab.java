package night.module.impl.misc;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "ExtraTab", description = "Extends the size of the player list.", category = Module.Category.MISCELLANEOUS)
public class ExtraTab extends Module {
   public NumberSetting limit;
   public BooleanSetting friends;

   public ExtraTab() {
      Integer var1 = 1000;
      Integer var2 = 1;
      Integer var3 = 1000;
      this.limit = new NumberSetting("Limit", "The maximum amount of players that will be listed.", var1, var2, var3);
      this.friends = new BooleanSetting("Friends", "Highlights your friends on the player list.", true);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.limit.getValue().intValue());
   }
}
