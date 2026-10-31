package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.ChangeHandEvent;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.RenderHandEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.access.SprintAccess;
import night.module.impl.movement.ElytraFly;
import night.module.impl.movement.InventoryControl;
import night.module.impl.movement.NoSlow;
import night.module.impl.movement.Velocity;
import night.module.impl.player.NoEntityTrace;
import night.module.impl.player.Swing;
import night.pingbypass.PingBypassState;
import night.util.RenderQueue;
import night.util.world.PacketAndRenderUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LocalPlayer.class, priority = 500)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
   @Shadow
   @Final
   public ClientPacketListener connection;
   @Shadow
   private float xRotLast;
   @Shadow
   public float xBob;
   @Shadow
   public ClientInput input;

   public ClientPlayerEntityMixin(ClientLevel var1, GameProfile var2) {
      super(var1, var2);
   }

   @Shadow
   protected abstract void updateAutoJump(float var1, float var2);

   @Inject(method = "tick", at = @At("HEAD"))
   private void earlyTick$dispatch(CallbackInfo var1) {
      if (Minecraft.getInstance().player == (Object)(Object)this) {
         RenderQueue.a();
      }
   }

   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = Shift.BEFORE))
   private void tick$BEFORE(CallbackInfo var1) {
      Night.EVENT_BUS.post(new UpdateWalkingPlayerEvent());
   }

   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = Shift.AFTER))
   private void tick$AFTER(CallbackInfo var1) {
      Night.EVENT_BUS.post(new MotionSyncEvent());
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void tick$TAIL(CallbackInfo var1) {
      Night.EVENT_BUS.post(new MotionSyncEvent.Post());
   }

   @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
   private float modifySendPositionPitch(float var1) {
      if (Night.moduleManager != null) {
         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         if (var2 != null && var2.B()) {
            return var2.C();
         }
      }

      return var1;
   }

   @WrapOperation(
      method = "sendPosition",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;yRotLast:F", opcode = 180, ordinal = 0)
   )
   private float sendPosition$yRotLast(LocalPlayer var1, Operation<Float> var2) {
      return Night.rotationManager.getCurrentRotation() != null ? Night.rotationManager.getServerYaw() : (Float)var2.call(new Object[]{var1});
   }

   @WrapOperation(
      method = "sendPosition",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;xRotLast:F", opcode = 180, ordinal = 0)
   )
   private float sendPosition$xRotLast(LocalPlayer var1, Operation<Float> var2) {
      return Night.rotationManager.getCurrentRotation() != null ? Night.rotationManager.getServerPitch() : (Float)var2.call(new Object[]{var1});
   }

   @ModifyExpressionValue(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
   private boolean tickMovement$isUsingItem(boolean var1) {
      return Night.moduleManager.getModule(NoSlow.class).isEnabled()
            && Night.moduleManager.getModule(NoSlow.class).items.getValue()
            && !Night.moduleManager.getModule(NoSlow.class).w()
         ? false
         : var1;
   }

   @ModifyExpressionValue(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z"))
   private boolean tickMovement$isMovingSlowly(boolean var1) {
      if (!var1) {
         return var1;
      } else {
         NoSlow var2 = Night.moduleManager.getModule(NoSlow.class);
         if (var2 == null || !var2.isEnabled()) {
            return var1;
         } else if (this.isVisuallyCrawling() && var2.crawl.getValue()) {
            return false;
         } else {
            return this.isCrouching() && var2.sneak.getValue() ? false : var1;
         }
      }
   }

   @Inject(method = "move", at = @At("HEAD"), cancellable = true)
   private void move(MoverType var1, Vec3 var2, CallbackInfo var3) {
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         var3.cancel();
      } else {
         PlayerMoveEvent var4 = new PlayerMoveEvent(var1, var2);
         Night.EVENT_BUS.post(var4);
         if (var4.isCancelled()) {
            var3.cancel();
            double var5 = this.getX();
            double var7 = this.getZ();
            super.move(var1, var4.getMovement());
            this.updateAutoJump((float)(this.getX() - var5), (float)(this.getZ() - var7));
         }
      }
   }

   @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
   private void sendMovementPackets(CallbackInfo var1) {
      RenderHandEvent var2 = new RenderHandEvent();
      Night.EVENT_BUS.post(var2);
      if (var2.isCancelled()) {
         var1.cancel();
      }
   }

   @ModifyExpressionValue(
      method = "pick",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"
      )
   )
   @Nullable
   private static EntityHitResult pick$getEntityHitResult(@Nullable EntityHitResult var0) {
      NoEntityTrace var1 = Night.moduleManager.getModule(NoEntityTrace.class);
      return var1.isEnabled() && var1.a(var0 != null ? var0.getEntity() : null) ? null : var0;
   }

   @Inject(method = "itemUseSpeedMultiplier", at = @At("HEAD"), cancellable = true)
   private void onItemUseSpeedMultiplier(CallbackInfoReturnable<Float> var1) {
      NoSlow var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoSlow.class) : null;
      if (var2 != null && var2.isEnabled() && var2.items.getValue() && !var2.w()) {
         var1.setReturnValue(1.0F);
      }
   }

   @Inject(method = "shouldStopRunSprinting", at = @At("HEAD"), cancellable = true)
   private void shouldStopRunSprinting$HEAD(CallbackInfoReturnable<Boolean> var1) {
      NoSlow var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoSlow.class) : null;
      if (var2 != null
         && var2.isEnabled()
         && var2.items.getValue()
         && !var2.w()
         && this.isUsingItem()
         && !this.isFallFlying()
         && !this.isMobilityRestricted()
         && !this.isPassenger()) {
         if (this.horizontalCollision && !this.minorHorizontalCollision) {
            var1.setReturnValue(true);
            return;
         }

         if (!(this.getFoodData().getFoodLevel() > 6.0F) && !this.getAbilities().mayfly) {
            var1.setReturnValue(true);
            return;
         }

         SprintAccess var3 = Night.moduleManager == null ? null : (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
         if (var3 != null && var3.isEnabled() ? !var3.canSprint() : this.input == null || !this.input.hasForwardImpulse()) {
            var1.setReturnValue(true);
            return;
         }

         if (this.isInWater() && !this.isUnderWater()) {
            var1.setReturnValue(true);
            return;
         }

         var1.setReturnValue(false);
      }
   }

   @Inject(method = "shouldStopRunSprinting", at = @At("RETURN"), cancellable = true)
   private void shouldStopRunSprinting$RETURN(CallbackInfoReturnable<Boolean> var1) {
      if ((Boolean)var1.getReturnValue()) {
         NoSlow var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoSlow.class) : null;
         if (var2 != null
            && var2.isEnabled()
            && var2.items.getValue()
            && !var2.w()
            && this.isUsingItem()
            && !this.isFallFlying()
            && !this.isMobilityRestricted()
            && !this.isPassenger()
            && (!this.horizontalCollision || this.minorHorizontalCollision)) {
            boolean var3 = this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly;
            SprintAccess var4 = Night.moduleManager == null ? null : (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
            boolean var5 = var4 != null && var4.isEnabled() ? var4.canSprint() : this.input != null && this.input.hasForwardImpulse();
            if (var3 && var5 && (!this.isInWater() || this.isUnderWater())) {
               var1.setReturnValue(false);
            }
         }
      }
   }

   @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
   private void canStartSprinting$HEAD(CallbackInfoReturnable<Boolean> var1) {
      NoSlow var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoSlow.class) : null;
      if (var2 != null
         && var2.isEnabled()
         && var2.items.getValue()
         && !var2.w()
         && this.isUsingItem()
         && !this.isSprinting()
         && !this.isFallFlying()
         && !this.isMobilityRestricted()
         && !this.isPassenger()
         && (this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly)) {
         SprintAccess var3 = Night.moduleManager == null ? null : (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
         if ((var3 != null && var3.isEnabled() ? var3.canSprint() : this.input != null && this.input.hasForwardImpulse())
            && (!this.isInWater() || this.isUnderWater())) {
            var1.setReturnValue(true);
         }
      }
   }

   @ModifyExpressionValue(
      method = "canStartSprinting",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z")
   )
   private boolean canStartSprinting$isUsingItem(boolean var1) {
      NoSlow var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoSlow.class) : null;
      return var2 != null && var2.isEnabled() && var2.items.getValue() && !var2.w() ? false : var1;
   }

   @Inject(method = "canStartSprinting", at = @At("RETURN"), cancellable = true)
   private void canStartSprinting$RETURN(CallbackInfoReturnable<Boolean> var1) {
      if (!(Boolean)var1.getReturnValue()) {
         NoSlow var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoSlow.class) : null;
         if (var2 != null
            && var2.isEnabled()
            && var2.items.getValue()
            && !var2.w()
            && this.isUsingItem()
            && !this.isSprinting()
            && !this.isFallFlying()
            && !this.isMobilityRestricted()
            && !this.isPassenger()
            && (this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly)) {
            SprintAccess var3 = Night.moduleManager == null ? null : (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
            if ((var3 != null && var3.isEnabled() ? var3.canSprint() : this.input != null && this.input.hasForwardImpulse())
               && (!this.isInWater() || this.isUnderWater())) {
               var1.setReturnValue(true);
            }
         }
      }
   }

   @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
   private void pushOutOfBlocks(double var1, double var3, CallbackInfo var5) {
      if (Night.moduleManager.getModule(Velocity.class).isEnabled() && Night.moduleManager.getModule(Velocity.class).antiBlockPush.getValue()) {
         var5.cancel();
      }
   }

   @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
   private void swingHand(InteractionHand var1, CallbackInfo var2) {
      if (Night.moduleManager.getModule(Swing.class).isEnabled()) {
         if (!Night.moduleManager.getModule(Swing.class).hand.getValue().equals("None")) {
            switch (Night.moduleManager.getModule(Swing.class).hand.getValue()) {
               case "Default":
                  super.swing(var1);
                  break;
               case "Mainhand":
                  super.swing(InteractionHand.MAIN_HAND);
                  break;
               case "Offhand":
                  super.swing(InteractionHand.OFF_HAND);
                  break;
               case "Both":
                  super.swing(InteractionHand.MAIN_HAND);
                  super.swing(InteractionHand.OFF_HAND);
            }

            if (Night.moduleManager.getModule(Swing.class).hand.getValue().equalsIgnoreCase("Packet")
               || !Night.moduleManager.getModule(Swing.class).noPacket.getValue()) {
               this.connection.send(PacketAndRenderUtil.a_Packet_InteractionHand(var1));
            }
         }

         var2.cancel();
      }
   }

   @Inject(method = "handlePortalTransitionEffect", at = @At("HEAD"), cancellable = true)
   private void tickNausea(boolean var1, CallbackInfo var2) {
      if (Night.moduleManager.getModule(InventoryControl.class).isEnabled() && Night.moduleManager.getModule(InventoryControl.class).portals.getValue()) {
         var2.cancel();
      }
   }

   @Inject(method = "startUsingItem", at = @At("HEAD"))
   private void setCurrentHand(InteractionHand var1, CallbackInfo var2) {
      Night.EVENT_BUS.post(new ChangeHandEvent());
   }
}
