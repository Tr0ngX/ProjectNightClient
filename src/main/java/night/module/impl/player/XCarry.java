package night.module.impl.player;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import night.events.EventTarget;
import night.events.impl.PacketSendEvent;
import night.module.Module;
import night.module.ModuleInfo;

@ModuleInfo(name = "XCarry", description = "Allows you to carry items in your crafting slots.", category = Module.Category.PLAYER)
public class XCarry extends Module {
   @EventTarget
   public void a(PacketSendEvent var1) {
      if (mc.player != null) {
         if (var1.getPacket() instanceof ServerboundContainerClosePacket) {
            var1.setCancelled(true);
         }
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         ClientPacketListener var1 = mc.getConnection();
         int var2 = mc.player.inventoryMenu.containerId;
         var1.send(new ServerboundContainerClosePacket(var2));
      }
   }
}
