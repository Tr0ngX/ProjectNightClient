package night.module.impl.movement;

import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.AutoWalkAccess;
import night.setting.impl.BooleanSetting;

@ModuleInfo(
   name = "AutoWalk",
   description = "Holds forward (or backward) movement every tick. Auto-pauses while Baritone is actively driving movement (e.g. EBounce+'s ObstaclePassing) so the two don't fight over input.",
   category = Module.Category.MOVEMENT
)
public class AutoWalk extends Module implements AutoWalkAccess {
   public final BooleanSetting backward = new BooleanSetting("Backward", "Walk backward instead of forward.", false);

   @Override
   public boolean isBackward() {
      return this.backward.getValue();
   }
}
