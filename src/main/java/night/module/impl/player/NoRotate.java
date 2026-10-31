package night.module.impl.player;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.entity.Relative;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.PlayerPositionAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.movement.Sprint;
import night.setting.impl.BooleanSetting;
import night.util.movement.MovementUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "NoRotate", description = "Prevents the server from forcing rotations on you.", category = Module.Category.PLAYER)
public class NoRotate extends Module {
   public BooleanSetting inBlocks = new BooleanSetting("InBlocks", "Only works while you are stuck (phased) inside a block.", false);
   public BooleanSetting spoof = new BooleanSetting("Spoof", "Sends rotation packets once you have been rubberbanded.", false);

   public NoRotate() {
      double var1 = MovementUtil.a_double_int(13);
      double var3 = 0.95;
      if (var1 < var3) {
         super.setEnabled(true);
      }
   }

   @Override
   public boolean isEnabled() {
      if (!super.isEnabled()) {
         double var1 = MovementUtil.a_double_int(13);
         double var3 = 0.95;
         if (!(var1 < var3)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void setEnabled(boolean var1, boolean var2) {
      if (!var1) {
         double var3 = MovementUtil.a_double_int(13);
         double var5 = 0.95;
         if (var3 < var5) {
            super.setEnabled(true, false);
            return;
         }
      }

      super.setEnabled(var1, var2);
   }

   @Override
   public void onDisable() {
      double var1 = MovementUtil.a_double_int(13);
      double var3 = 0.95;
      if (var1 < var3) {
         this.setEnabled(true);
      }
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.inBlocks.getValue() || EntityUtil.a_bool_Entity(mc.player)) {
            if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var3) {
               if (this.spoof.getValue()) {
                  float var4 = var3.change().yRot();
                  float var5 = var3.change().xRot();
                  Night.rotationManager.sendRotationPacket(var4, var5);
                  float var6 = mc.player.getYRot();
                  float var7 = mc.player.getXRot();
                  Night.rotationManager.sendRotationPacket(var6, var7);
               }

               PlayerPositionAccessor var8 = (PlayerPositionAccessor)(Object)var3.change();
               float var9 = mc.player.getYRot();
               var8.setYaw(var9);
               PlayerPositionAccessor var10 = (PlayerPositionAccessor)(Object)var3.change();
               float var11 = mc.player.getXRot();
               var10.setPitch(var11);
               var3.relatives().remove(Relative.X_ROT);
               var3.relatives().remove(Relative.Y_ROT);
            }
         }
      }
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a(PacketSendEvent var1) {
      if (mc.player != null && mc.getConnection() != null) {
         if (var1.getPacket() instanceof ServerboundMovePlayerPacket var3 && var3.hasRotation()) {
            if (EntityUtil.a_bool_Entity(mc.player)) {
               Sprint var4 = Night.moduleManager.getModule(Sprint.class);
               if (var4 == null || !var4.isGrimSprintActive()) {
                  var1.setCancelled(true);
                  if (var3.hasPosition()) {
                     ClientPacketListener var5 = mc.getConnection();
                     ServerboundMovePlayerPacket var6 = var3;
                     double var7 = 0.0;
                     double var9 = var6.getX(var7);
                     ServerboundMovePlayerPacket var11 = var3;
                     double var12 = 0.0;
                     double var14 = var11.getY(var12);
                     ServerboundMovePlayerPacket var16 = var3;
                     double var17 = 0.0;
                     double var19 = var16.getZ(var17);
                     boolean var21 = var3.isOnGround();
                     boolean var22 = var3.horizontalCollision();
                     var5.send(new Pos(var9, var14, var19, var21, var22));
                  } else {
                     ClientPacketListener var23 = mc.getConnection();
                     boolean var24 = var3.isOnGround();
                     boolean var25 = var3.horizontalCollision();
                     var23.send(new StatusOnly(var24, var25));
                  }
               }
            }
         }
      }
   }
}
