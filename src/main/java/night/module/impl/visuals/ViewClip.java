package night.module.impl.visuals;

import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.ViewClipAccess;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(
   name = "ViewClip",
   description = "Makes your camera clip through walls and allows you to change the camera's distance from yourself.",
   category = Module.Category.VISUALS
)
public class ViewClip extends Module implements ViewClipAccess {
   public BooleanSetting extend = new BooleanSetting("Extend", "Changes the distance of the third person camera from yourself.", false);
   public NumberSetting distance = new NumberSetting(
      "Distance", "The distance of the third person camera from your character.", new BooleanSetting.BooleanCondition(this.extend, true), 4.0F, -50.0F, 50.0F
   );

   @Override
   public String getDisplayInfo() {
      return this.extend.getValue() ? String.valueOf(this.distance.getValue().floatValue()) : "Vanilla";
   }

   @Override
   public boolean isExtendEnabled() {
      return this.extend.getValue();
   }

   @Override
   public float getDistance() {
      return this.distance.getValue().floatValue();
   }
}
