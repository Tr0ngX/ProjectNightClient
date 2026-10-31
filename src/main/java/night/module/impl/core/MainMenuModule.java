package night.module.impl.core;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;

@ModuleInfo(
   name = "Menu",
   description = "Replaces the default title screen with the client's custom main menu screen.",
   category = Module.Category.CORE,
   persistent = true,
   drawn = false
)
public class MainMenuModule extends Module {
   public CategorySetting mainMenuCategory = new CategorySetting("MainMenu", "The category for settings related to the main menu.");
   public BooleanSetting mainMenu;

   public MainMenuModule() {
      CategorySetting var1 = this.mainMenuCategory;
      this.mainMenu = new BooleanSetting(
         "MainMenu", "Enabled", "Replaces Minecraft's default main menu with a customizable one.", new CategorySetting.CategoryCondition(var1), true
      );
   }
}
