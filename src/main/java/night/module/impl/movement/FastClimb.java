package night.module.impl.movement;

import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FastClimbAccess;
import night.setting.Setting;
import night.setting.impl.NumberSetting;

@ModuleInfo(
   name = "FastClimb",
   description = "Modifies your climbing speed on ladders, scaffolding, vines and other climbables.",
   category = Module.Category.MOVEMENT
)
public class FastClimb extends Module implements FastClimbAccess {
   public NumberSetting speed;

   public FastClimb() {
      Setting.VisibilityCondition var1 = new Setting.VisibilityCondition();
      Float var2 = 1.0F;
      Float var3 = 0.0F;
      Float var4 = 1.0F;
      Float var5 = 0.1F;
      this.speed = new NumberSetting(
         "Speed",
         "Speed",
         "How much of vanilla's climb speed to use. Ignore walks past climbables like normal blocks, 1 is vanilla's normal speed.",
         var1,
         var2,
         var3,
         var4,
         var5,
         true
      );
   }

   @Override
   public float getClimbSpeed() {
      return this.speed.getValue().floatValue();
   }
}
