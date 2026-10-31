package night.module.impl.player;

import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "Timer", description = "Makes your game run at a faster tick speed.", category = Module.Category.PLAYER)
public class TimerModule extends Module {
   public NumberSetting multiplier;

   public TimerModule() {
      Float var1 = 1.0F;
      Float var2 = 0.0F;
      Float var3 = 20.0F;
      this.multiplier = new NumberSetting("Multiplier", "The multiplier that will be added to the game's speed.", var1, var2, var3);
   }

   @EventTarget(priority = Integer.MIN_VALUE)
   public void a(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         float var2 = this.multiplier.getValue().floatValue();
         Night.worldStateManager.setTimerMultiplier(var2);
      }
   }

   @Override
   public void onEnable() {
      float var1 = this.multiplier.getValue().floatValue();
      Night.worldStateManager.setTimerMultiplier(var1);
   }

   @Override
   public void onDisable() {
      Night.worldStateManager.setTimerMultiplier(1.0F);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.multiplier.getValue().floatValue());
   }
}
