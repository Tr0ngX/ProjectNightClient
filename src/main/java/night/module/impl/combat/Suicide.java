package night.module.impl.combat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;

@ModuleInfo(name = "Suicide", description = "Makes all of the combat modules target you so that you die.", category = Module.Category.COMBAT)
public class Suicide extends Module {
   public BooleanSetting offhandOverride = new BooleanSetting("OffhandOverride", "Changes the target offhand item in order to make it easier to die.", true);
   public BooleanSetting deathDisable = new BooleanSetting("DeathDisable", "Disables the module once you have died.", true);
   public BooleanSetting loginDisable = new BooleanSetting("LoginDisable", "Disables the module when you logged in.", true);

   @EventTarget
   public void a(PlayerDeathEvent var1) {
      if (this.deathDisable.getValue()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 == var3) {
            this.setEnabled(false);
         }
      }
   }

   @EventTarget
   public void a(ClientConnectEvent var1) {
      if (this.loginDisable.getValue()) {
         this.setEnabled(false);
      }
   }

   @Override
   public void onEnable() {
      if (mc.player == null || mc.level == null) {
         this.setEnabled(false);
      }
   }
}
