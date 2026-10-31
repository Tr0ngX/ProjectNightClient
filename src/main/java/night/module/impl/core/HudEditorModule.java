package night.module.impl.core;

import night.gui.HudEditorScreen;
import night.manager.HudElementManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.PositionSetting;

@ModuleInfo(
   name = "HUDEditor",
   description = "Lets you drag-and-drop and toggle individual HUD elements directly on screen.",
   category = Module.Category.CORE,
   drawn = false
)
public class HudEditorModule extends Module {
   public BooleanSetting lowArmor = new BooleanSetting("LowArmor", "Displays a demo low armor toast to preview and align its appearance/retraction.", false);
   public PositionSetting lowArmorPosition = new PositionSetting("LowArmorPosition", "Drag offset for the LowArmor toast notification.");

   public HudEditorModule() {
      HudElementManager.a("LowArmor", this.lowArmor, this.lowArmorPosition);
   }

   @Override
   public void onEnable() {
      mc.gui.setScreen(new HudEditorScreen());
   }

   @Override
   public void onDisable() {
      if (mc.gui.screen() instanceof HudEditorScreen) {
         mc.gui.setScreen(null);
      }
   }
}
