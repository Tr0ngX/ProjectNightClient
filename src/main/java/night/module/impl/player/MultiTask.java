package night.module.impl.player;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;

@ModuleInfo(name = "MultiTask", description = "Allows you to interact with blocks while eating or using an item.", category = Module.Category.PLAYER)
public class MultiTask extends Module {
   public BooleanSetting pearl = new BooleanSetting("Pearl", "Don't stop eating when you throw a pearl with Phase / KeyAction.", true);
   public BooleanSetting autoTotem = new BooleanSetting("AutoTotem", "Don't stop/reset your eating when AutoTotem swaps a totem into your offhand.", true);
}
