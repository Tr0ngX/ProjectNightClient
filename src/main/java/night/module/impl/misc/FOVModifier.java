package night.module.impl.misc;

import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FOVModifierAccess;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "FOVModifier", description = "Gives you more customizability for the games FOV.", category = Module.Category.MISCELLANEOUS)
public class FOVModifier extends Module implements FOVModifierAccess {
   public NumberSetting fOV;

   public FOVModifier() {
      Integer var1 = 120;
      Integer var2 = 50;
      Integer var3 = 150;
      this.fOV = new NumberSetting("FOV", "The FOV you want to use.", var1, var2, var3);
   }

   @Override
   public String getDisplayInfo() {
      return this.fOV.getValue().intValue() + "";
   }

   @Override
   public float getFov() {
      return this.fOV.getValue().floatValue();
   }
}
