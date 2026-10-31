package night.module.impl.misc;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;

@ModuleInfo(name = "MouseFix", description = "Fixes multiple mouse issues.", category = Module.Category.MISCELLANEOUS)
public class MouseFix extends Module {
   public BooleanSetting customDebounce = new BooleanSetting("CustomDebounce", "Implements a custom debounce timer on mouse inputs.", true);
}
