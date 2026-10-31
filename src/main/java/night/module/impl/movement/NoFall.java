package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import night.events.EventTarget;
import night.events.impl.PacketSendEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.ModeSetting;

@ModuleInfo(name = "NoFall", description = "Prevents you from taking fall damage.", category = Module.Category.MOVEMENT)
public class NoFall extends Module {
   public ModeSetting mode;

   public NoFall() {
      String[] var1 = new String[]{"Packet", "Grim"};
      this.mode = new ModeSetting("Mode", "The method that will be used in preventing fall damage.", "Packet", var1);
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (this.v()) {
         if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            ClientPacketListener var2 = mc.getConnection();
            double var3 = mc.player.getX();
            double var5 = mc.player.getY();
            double var7 = 1.0E-9;
            double var9 = var5 + var7;
            double var11 = mc.player.getZ();
            float var13 = mc.player.getYRot();
            float var14 = mc.player.getXRot();
            boolean var15 = mc.player.horizontalCollision;
            var2.send(new PosRot(var3, var9, var11, var13, var14, false, var15));
            mc.player.resetFallDistance();
         }
      }
   }

   @EventTarget
   public void a(PacketSendEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equalsIgnoreCase("Packet")) {
            if (var1.getPacket() instanceof ServerboundMovePlayerPacket var3 && this.v()) {
               ((PlayerMoveC2SPacketAccessor)var3).setOnGround(true);
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   private boolean v() {
      double var1 = mc.player.fallDistance;
      double var3 = mc.player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
      boolean var5;
      if (var1 > var3 && !mc.player.onGround() && !mc.player.isFallFlying()) {
         var5 = true;
      } else {
         var5 = false;
      }

      return var5;
   }
}
