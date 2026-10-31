package night.module.impl.visuals;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import night.events.EventTarget;
import night.events.impl.SettingChangeEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FullBrightAccess;
import night.setting.impl.ModeSetting;

@ModuleInfo(name = "FullBright", description = "Gives you the ability to clearly see even when in the dark.", category = Module.Category.VISUALS)
public class FullBright extends Module implements FullBrightAccess {
   public ModeSetting mode = new ModeSetting("Mode", "The way that will be used to change the game's brightness.", "Gamma", new String[]{"Gamma", "Potion"});

   @EventTarget
   public void a(SettingChangeEvent var1) {
      if (var1.getSetting() == this.mode && this.isEnabled() && mc.player != null) {
         if (this.mode.getValue().equalsIgnoreCase("Potion")) {
            if (!mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
               mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
            }
         } else if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
         }
      }
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (this.mode.getValue().equalsIgnoreCase("Potion")) {
         if (!mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
         }
      }
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         if (this.mode.getValue().equalsIgnoreCase("Potion")) {
            if (!mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
               mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         if (this.mode.getValue().equalsIgnoreCase("Potion")) {
            if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
               mc.player.removeEffect(MobEffects.NIGHT_VISION);
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   @Override
   public String getMode() {
      return this.mode.getValue();
   }
}
