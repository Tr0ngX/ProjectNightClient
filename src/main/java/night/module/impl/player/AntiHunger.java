package night.module.impl.player;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import night.events.EventTarget;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;

@ModuleInfo(name = "AntiHunger", description = "Reduces the amount of hunger consumption.", category = Module.Category.PLAYER)
public class AntiHunger extends Module {
   public BooleanSetting ground = new BooleanSetting("Ground", "Modifies movement packets to decrease hunger consumption.", true);
   public BooleanSetting sprint = new BooleanSetting("Sprint", "Spoofs sprinting packets to decrease hunger consumption.", true);
   private boolean g = false;
   private boolean persistent = false;

   @EventTarget
   public void a(PacketSendEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.persistent && var1.getPacket() instanceof ServerboundMovePlayerPacket) {
            this.persistent = false;
         } else if (!mc.player.isPassenger() && !mc.player.isInWater() && !mc.player.isUnderWater()) {
            if (var1.getPacket() instanceof ServerboundMovePlayerPacket var3 && this.ground.getValue() && mc.player.onGround()) {
               double var4 = mc.player.fallDistance;
               double var6 = 0.0;
               if (var4 <= var6 && !mc.gameMode.isDestroying()) {
                  ((PlayerMoveC2SPacketAccessor)var3).setOnGround(false);
               }
            }

            if (var1.getPacket() instanceof ServerboundPlayerCommandPacket var9 && this.sprint.getValue() && var9.getAction() == Action.START_SPRINTING) {
               var1.setCancelled(true);
            }
         }
      }
   }

   @EventTarget
   public void a(MotionSyncEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (mc.player.onGround() && !this.g && this.ground.getValue()) {
            this.persistent = true;
         }

         boolean var2 = mc.player.onGround();
         this.g = var2;
      }
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         boolean var1 = mc.player.onGround();
         this.g = var1;
      }
   }
}
