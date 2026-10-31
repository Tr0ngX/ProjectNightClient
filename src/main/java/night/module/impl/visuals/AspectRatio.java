package night.module.impl.visuals;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "AspectRatio", description = "Modifies the game's aspect ratio.", category = Module.Category.VISUALS)
public class AspectRatio extends Module {
   public NumberSetting ratio = new NumberSetting("Ratio", "The aspect ratio that will be applied to the game's rendering.", 1.78F, 0.0F, 5.0F);

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.ratio.getValue().floatValue());
   }
}
