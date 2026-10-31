package night.module.impl.core;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;

@ModuleInfo(name = "Rotations", description = "Manages the client's rotation system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class RotationsModule extends Module {
   public BooleanSetting movementFix = new BooleanSetting("MovementFix", "Makes your movement in accordance with your yaw.", false);
   public BooleanSetting snapBack = new BooleanSetting("SnapBack", "Reverts rotations to previous values after rotating.", false);
   public ModeSetting jitter;

   public RotationsModule() {
      String[] var1 = new String[]{"None", "Grim", "Normal"};
      this.jitter = new ModeSetting(
         "Jitter",
         "Adds random noise to sent rotations so they're never bit-for-bit identical, defeating aim-consistency checks like Grim's AimModulo360.",
         "None",
         var1
      );
   }
}
