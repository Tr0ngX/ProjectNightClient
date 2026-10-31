package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import night.Night;
import night.events.impl.AttackBlockEvent;
import night.events.impl.AttackEntityEvent;
import night.events.impl.BreakBlockEvent;
import night.gui.PeekContainerScreen;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.impl.core.NoMiddleClick;
import night.module.impl.movement.InventoryControl;
import night.module.impl.player.NoBreakDelay;
import night.module.impl.player.NoInteract;
import night.pingbypass.PingBypassState;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private int destroyDelay;
   @Unique
   private boolean night$stoppedSprintForClick = false;

   @Inject(method = "handleContainerInput", at = @At("HEAD"), cancellable = true)
   private void beforeContainerClick(int var1, int var2, int var3, ContainerInput var4, Player var5, CallbackInfo var6) {
      this.night$stoppedSprintForClick = false;
      if (this.minecraft.gui.screen() instanceof PeekContainerScreen) {
         var6.cancel();
      } else if (InventoryUtil.g && !InventoryUtil.h && var5.containerMenu != var5.inventoryMenu && var1 == var5.containerMenu.containerId) {
         var6.cancel();
      } else {
         InventoryControl var7 = Night.moduleManager.getModule(InventoryControl.class);
         if (var7.isEnabled() && var7.grimV2.getValue()) {
            if (this.minecraft.player != null && this.minecraft.getConnection() != null) {
               Input var8 = this.minecraft.player.input.keyPresses;
               if (var8.forward() || var8.backward() || var8.left() || var8.right() || var8.jump()) {
                  Input var9 = new Input(false, false, false, false, false, var8.shift(), var8.sprint());
                  this.minecraft.getConnection().send(new ServerboundPlayerInputPacket(var9));
                  ((ClientPlayerEntityAccessor)(Object)this.minecraft.player).setLastSentInput(var9);
               }

               if (this.minecraft.player.isSprinting()) {
                  this.minecraft.getConnection().send(new ServerboundPlayerCommandPacket(this.minecraft.player, Action.STOP_SPRINTING));
                  this.night$stoppedSprintForClick = true;
               }
            }
         }
      }
   }

   @Inject(method = "handleContainerInput", at = @At("TAIL"))
   private void afterContainerClick(int var1, int var2, int var3, ContainerInput var4, Player var5, CallbackInfo var6) {
      if (this.night$stoppedSprintForClick) {
         this.night$stoppedSprintForClick = false;
         if (this.minecraft.player != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new ServerboundPlayerCommandPacket(this.minecraft.player, Action.START_SPRINTING));
         }
      }
   }

   @Inject(method = "ensureHasSentCarriedItem", at = @At("HEAD"), cancellable = true)
   private void syncSelectedSlot(CallbackInfo var1) {
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         var1.cancel();
      }
   }

   @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
   private void stopUsingItem(CallbackInfo var1) {
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         var1.cancel();
      }
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void onTick(CallbackInfo var1) {
      if (Night.moduleManager != null) {
         NoBreakDelay var2 = Night.moduleManager.getModule(NoBreakDelay.class);
         if (var2 != null && var2.isEnabled() && this.destroyDelay > var2.delay.getValue().intValue()) {
            this.destroyDelay = var2.delay.getValue().intValue();
         }
      }
   }

   @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
   private void onContinueDestroyBlock(BlockPos var1, Direction var2, CallbackInfoReturnable<Boolean> var3) {
      if (Night.moduleManager != null) {
         NoBreakDelay var4 = Night.moduleManager.getModule(NoBreakDelay.class);
         if (var4 != null && var4.isEnabled() && this.destroyDelay > var4.delay.getValue().intValue()) {
            this.destroyDelay = var4.delay.getValue().intValue();
         }
      }
   }

   @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
   private void attackBlock(BlockPos var1, Direction var2, CallbackInfoReturnable<Boolean> var3) {
      if (Night.moduleManager != null) {
         NoBreakDelay var4 = Night.moduleManager.getModule(NoBreakDelay.class);
         if (var4 != null && var4.isEnabled() && this.destroyDelay > var4.delay.getValue().intValue()) {
            this.destroyDelay = var4.delay.getValue().intValue();
         }
      }

      AttackBlockEvent var5 = new AttackBlockEvent(var1, var2);
      Night.EVENT_BUS.post(var5);
      if (var5.isCancelled()) {
         var3.setReturnValue(false);
      }
   }

   @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
   private void interactBlock(LocalPlayer var1, InteractionHand var2, BlockHitResult var3, CallbackInfoReturnable<InteractionResult> var4) {
      NoInteract var5 = Night.moduleManager.getModule(NoInteract.class);
      if (var5.isEnabled()
         && var5.v()
         && var5.mode.getValue().equalsIgnoreCase("Disable")
         && BlockInteractionUtil.a.contains(this.minecraft.level.getBlockState(var3.getBlockPos()).getBlock())) {
         var4.setReturnValue(InteractionResult.FAIL);
      }
   }

   @Inject(
      method = "useItemOn",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
      )
   )
   private void interactBlock$BEFORE(LocalPlayer var1, InteractionHand var2, BlockHitResult var3, CallbackInfoReturnable<InteractionResult> var4) {
      NoInteract var5 = Night.moduleManager.getModule(NoInteract.class);
      if (!this.minecraft.player.isShiftKeyDown()
         && var5.isEnabled()
         && var5.v()
         && var5.mode.getValue().equalsIgnoreCase("Sneak")
         && BlockInteractionUtil.a.contains(this.minecraft.level.getBlockState(var3.getBlockPos()).getBlock())) {
         this.minecraft.player.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
      }
   }

   @Inject(
      method = "useItemOn",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
         shift = Shift.AFTER
      )
   )
   private void interactBlock$AFTER(LocalPlayer var1, InteractionHand var2, BlockHitResult var3, CallbackInfoReturnable<InteractionResult> var4) {
      NoInteract var5 = Night.moduleManager.getModule(NoInteract.class);
      if (!this.minecraft.player.isShiftKeyDown()
         && var5.isEnabled()
         && var5.v()
         && var5.mode.getValue().equalsIgnoreCase("Sneak")
         && BlockInteractionUtil.a.contains(this.minecraft.level.getBlockState(var3.getBlockPos()).getBlock())) {
         this.minecraft.player.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));
      }
   }

   @Inject(method = "attack", at = @At("HEAD"))
   private void attackEntity(Player var1, Entity var2, CallbackInfo var3) {
      Night.EVENT_BUS.post(new AttackEntityEvent(var1, var2));
   }

   @Inject(method = "destroyBlock", at = @At("HEAD"))
   private void breakBlock(BlockPos var1, CallbackInfoReturnable<Boolean> var2) {
      Night.EVENT_BUS.post(new BreakBlockEvent(var1));
   }

   @Inject(method = "destroyBlock", at = @At("TAIL"))
   private void afterBreakBlock(BlockPos var1, CallbackInfoReturnable<Boolean> var2) {
      if (Night.moduleManager != null) {
         NoBreakDelay var3 = Night.moduleManager.getModule(NoBreakDelay.class);
         if (var3 != null && var3.isEnabled() && this.destroyDelay > var3.delay.getValue().intValue()) {
            this.destroyDelay = var3.delay.getValue().intValue();
         }
      }
   }

   @Inject(method = "handlePickItemFromBlock", at = @At("HEAD"), cancellable = true)
   private void onPickBlock(BlockPos var1, boolean var2, CallbackInfo var3) {
      if (Night.moduleManager != null) {
         NoMiddleClick var4 = Night.moduleManager.getModule(NoMiddleClick.class);
         if (var4 != null && var4.isEnabled()) {
            var3.cancel();
         }
      }
   }

   @Inject(method = "handlePickItemFromEntity", at = @At("HEAD"), cancellable = true)
   private void onPickEntity(Entity var1, boolean var2, CallbackInfo var3) {
      if (Night.moduleManager != null) {
         NoMiddleClick var4 = Night.moduleManager.getModule(NoMiddleClick.class);
         if (var4 != null && var4.isEnabled()) {
            var3.cancel();
         }
      }
   }
}
