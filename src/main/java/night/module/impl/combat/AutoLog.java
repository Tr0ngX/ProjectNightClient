package night.module.impl.combat;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "AutoLog", description = "Logs out automatically to avoid dying.", category = Module.Category.COMBAT)
public class AutoLog extends Module {
   public BooleanSetting healthCheck = new BooleanSetting("HealthCheck", "Checks if you are at a specific health to log out.", false);
   public NumberSetting health;
   public BooleanSetting totemCheck;
   public NumberSetting totems;
   public BooleanSetting selfDisable;

   public AutoLog() {
      BooleanSetting var2 = this.healthCheck;
      BooleanSetting.BooleanCondition var1 = new BooleanSetting.BooleanCondition(var2, true);
      Integer var3 = 10;
      Integer var4 = 0;
      Integer var5 = 20;
      this.health = new NumberSetting("Health", "The health the player must be at to log out.", var1, var3, var4, var5);
      this.totemCheck = new BooleanSetting("TotemCheck", "Checks if you ran out of totems to be able to log out.", true);
      BooleanSetting var7 = this.totemCheck;
      BooleanSetting.BooleanCondition var6 = new BooleanSetting.BooleanCondition(var7, true);
      Integer var8 = 2;
      Integer var9 = 0;
      Integer var10 = 9;
      this.totems = new NumberSetting("Totems", "The amount of totems to have in your inventory to log out.", var6, var8, var9, var10);
      this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module after logging out.", true);
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck()) {
         int var2 = mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
         if (this.healthCheck.getValue()) {
            float var3 = mc.player.getHealth();
            float var4 = this.health.getValue().intValue();
            if (var3 <= var4) {
               ClientPacketListener var5 = mc.getConnection();
               MutableComponent var6 = Component.literal("Health was lower than or equal to " + this.health.getValue().intValue() + ".");
               var5.onDisconnect(new DisconnectionDetails(var6));
               if (this.selfDisable.getValue()) {
                  this.setEnabled(false);
               }
            }
         }

         if (this.totemCheck.getValue()) {
            int var7 = var2;
            int var8 = this.totems.getValue().intValue();
            if (var7 <= var8) {
               ClientPacketListener var9 = mc.getConnection();
               MutableComponent var10 = Component.literal("Couldn't find totems in your inventory.");
               var9.onDisconnect(new DisconnectionDetails(var10));
               if (this.selfDisable.getValue()) {
                  this.setEnabled(false);
               }
            }
         }
      }
   }
}
