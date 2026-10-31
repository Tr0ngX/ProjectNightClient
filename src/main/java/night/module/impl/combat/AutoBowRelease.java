package night.module.impl.combat;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.NumberSetting;
import night.util.network.PacketPredictionUtil;

@ModuleInfo(
   name = "AutoBowRelease",
   description = "Automatically releases your bow after a certain amount of time has passed.",
   category = Module.Category.COMBAT
)
public class AutoBowRelease extends Module {
   public NumberSetting ticks;

   public AutoBowRelease() {
      Integer var1 = 3;
      Integer var2 = 0;
      Integer var3 = 20;
      this.ticks = new NumberSetting("Ticks", "The number of ticks that have to be waited for before releasing the bow.", var1, var2, var3);
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (!Night.moduleManager.getModule(SelfBow.class).isEnabled()) {
         if ((mc.player.getOffhandItem().getItem() == Items.BOW || mc.player.getMainHandItem().getItem() == Items.BOW) && mc.player.isUsingItem()) {
            int var2 = mc.player.getTicksUsingItem();
            int var3 = this.ticks.getValue().intValue();
            if (var2 >= var3) {
               ClientPacketListener var4 = mc.getConnection();
               Direction var5 = mc.player.getDirection();
               var4.send(new ServerboundPlayerActionPacket(Action.RELEASE_USE_ITEM, BlockPos.ZERO, var5));
               PacketPredictionUtil.a_void_SequencedPacketCreator(var0 -> {
                  InteractionHand var1x;
                  if (mc.player.getOffhandItem().getItem() == Items.BOW) {
                     var1x = InteractionHand.OFF_HAND;
                  } else {
                     var1x = InteractionHand.MAIN_HAND;
                  }

                  float var2x = mc.player.getYRot();
                  float var3x = mc.player.getXRot();
                  return new ServerboundUseItemPacket(var1x, var0, var2x, var3x);
               });
               mc.player.stopUsingItem();
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.ticks.getValue().intValue());
   }
}
