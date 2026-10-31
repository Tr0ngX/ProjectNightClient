package night.mixins;

import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import night.Night;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerJoinEvent;
import night.events.impl.PlayerLeaveEvent;
import night.module.impl.movement.ElytraFly;
import night.pingbypass.PingBypassState;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin implements MinecraftInstance {
   private static final String ENSURE_MAIN_THREAD = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V";
   @Unique
   private float handleMovePlayer$prevYaw;
   @Unique
   private float handleMovePlayer$prevPitch;
   @Unique
   private boolean handleMovePlayer$guard;

   @Inject(method = "handleLogin", at = @At("TAIL"))
   private void onGameJoin(ClientboundLoginPacket var1, CallbackInfo var2) {
      Night.EVENT_BUS.post(new ClientConnectEvent());
   }

   @Inject(
      method = "handlePlayerInfoRemove",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
         shift = Shift.AFTER
      )
   )
   private void night$onPlayerInfoRemove(ClientboundPlayerInfoRemovePacket var1, CallbackInfo var2) {
      for (UUID var4 : (Iterable<UUID>)(Object)var1.profileIds()) {
         if (var4 != null) {
            PlayerInfo var5 = ((ClientPacketListener)(Object)this).getPlayerInfo(var4);
            String var6 = var5 != null ? var5.getProfile().name() : null;
            Night.EVENT_BUS.post(new PlayerLeaveEvent(var4, var6));
         }
      }
   }

   @Inject(
      method = "handlePlayerInfoUpdate",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
         shift = Shift.AFTER
      )
   )
   private void night$onPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket var1, CallbackInfo var2) {
      if (var1.actions().contains(Action.ADD_PLAYER)) {
         for (Entry var4 : (Iterable<Entry>)(Object)var1.newEntries()) {
            Night.EVENT_BUS.post(new PlayerJoinEvent(var4.profileId(), var4.profile() != null ? var4.profile().name() : null));
         }
      }
   }

   @Inject(method = "handleMovePlayer", at = @At("HEAD"))
   private void handleMovePlayer$HEAD(ClientboundPlayerPositionPacket var1, CallbackInfo var2) {
      this.handleMovePlayer$guard = false;
      if (mc.player != null) {
         if (!PingBypassState.a()) {
            ElytraFly var3 = Night.moduleManager != null ? Night.moduleManager.getModule(ElytraFly.class) : null;
            boolean var4 = var3 != null && var3.isEnabled() && var3.mode.getValue().equalsIgnoreCase("Bounce");
            if (Night.rotationManager != null && Night.rotationManager.isRecentlyRotated() || var4) {
               this.handleMovePlayer$prevYaw = mc.player.getYRot();
               this.handleMovePlayer$prevPitch = var4 && var3.F() ? var3.G() : mc.player.getXRot();
               this.handleMovePlayer$guard = true;
            }
         }
      }
   }

   @Inject(method = "handleMovePlayer", at = @At("TAIL"))
   private void handleMovePlayer$TAIL(ClientboundPlayerPositionPacket var1, CallbackInfo var2) {
      if (this.handleMovePlayer$guard) {
         this.handleMovePlayer$guard = false;
         mc.player.setYRot(this.handleMovePlayer$prevYaw);
         mc.player.setXRot(this.handleMovePlayer$prevPitch);
         mc.player.xRotO = this.handleMovePlayer$prevPitch;
      }
   }
}
