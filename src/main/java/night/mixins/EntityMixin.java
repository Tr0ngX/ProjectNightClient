package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.CameraBobEvent;
import night.events.impl.ReachDistanceEvent;
import night.events.impl.UpdateVelocityEvent;
import night.manager.RotationManager;
import night.module.access.FreecamAccess;
import night.module.impl.combat.AutoMace;
import night.module.impl.misc.FakePlayer;
import night.module.impl.movement.ElytraFly;
import night.module.impl.movement.NoSlow;
import night.module.impl.movement.Velocity;
import night.module.impl.player.RotationLock;
import night.module.impl.visuals.LogoutSpot;
import night.module.impl.visuals.PopChams;
import night.util.MinecraftInstance;
import night.util.world.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements MinecraftInstance {
   @Shadow
   private float yRot;
   @Shadow
   private float xRot;

   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void night$cancelGhostTick(CallbackInfo var1) {
      if ((Object)(Object)this instanceof RemotePlayer var3) {
         PopChams var5 = Night.moduleManager != null ? Night.moduleManager.getModule(PopChams.class) : null;
         if (var5 != null && var5.a_bool_Entity(var3)) {
            var1.cancel();
            return;
         }

         LogoutSpot var4 = Night.moduleManager != null ? Night.moduleManager.getModule(LogoutSpot.class) : null;
         if (var4 != null && var4.a_bool_Entity(var3)) {
            var1.cancel();
         }
      }
   }

   @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
   public void onTurn(double var1, double var3, CallbackInfo var5) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         FreecamAccess var6 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
         if (var6 != null && var6.isEnabled() && !var6.isRotateEnabled()) {
            var6.changeLookDirection(var1, var3);
            var5.cancel();
            return;
         }

         ElytraFly var7 = Night.moduleManager.getModule(ElytraFly.class);
         if (var7 != null && var7.x()) {
            var7.a_void_double_double(var1, var3);
            var5.cancel();
            return;
         }

         AutoMace var8 = Night.moduleManager.getModule(AutoMace.class);
         if (var8 != null && var8.w()) {
            var8.a_void_double_double(var1, var3);
            var5.cancel();
         }
      }
   }

   @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void pushAwayFrom(Entity var1, CallbackInfo var2) {
      if (((Object)(Object)this != mc.player || !this.isPhantomEntity(var1)) && (var1 != mc.player || !this.isPhantomEntity((Entity)(Object)(Object)this))) {
         if ((Object)(Object)this == mc.player
            && Night.moduleManager.getModule(Velocity.class).isEnabled()
            && Night.moduleManager.getModule(Velocity.class).antiPush.getValue()) {
            var2.cancel();
         }
      } else {
         var2.cancel();
      }
   }

   private boolean isPhantomEntity(Entity var1) {
      if (EntityUtil.c_bool_Entity(var1)) {
         return true;
      }

      FakePlayer var2 = Night.moduleManager.getModule(FakePlayer.class);
      return var2 != null && var1 == var2.z();
   }

   @WrapOperation(
      method = "updateFluidInteraction",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V"
      )
   )
   private void updateMovementInFluid(EntityFluidInteraction var1, TagKey<Fluid> var2, Entity var3, double var4, Operation<Void> var6) {
      if ((Object)(Object)this != mc.player
         || !Night.moduleManager.getModule(Velocity.class).isEnabled()
         || !Night.moduleManager.getModule(Velocity.class).antiLiquidPush.getValue()) {
         var6.call(new Object[]{var1, var2, var3, var4});
      }
   }

   @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
   private void updateVelocity(float var1, Vec3 var2, CallbackInfo var3) {
      if ((Object)(Object)this == mc.player) {
         UpdateVelocityEvent var4 = new UpdateVelocityEvent(var2, var1);
         Night.EVENT_BUS.post(var4);
         if (var4.isCancelled()) {
            var3.cancel();
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(var4.getVelocity()));
         }
      }
   }

   @Inject(method = "getYRot()F", at = @At("HEAD"), cancellable = true)
   private void getYaw(CallbackInfoReturnable<Float> var1) {
      if (!RotationManager.spoofingRotation || (Object)(Object)this != mc.player) {
         RotationLock var2 = Night.moduleManager.getModule(RotationLock.class);
         if (var2 != null && var2.isEnabled() && (var2.mode.getValue().equals("Yaw") || var2.mode.getValue().equals("Both")) && (Object)(Object)this == mc.player) {
            var1.setReturnValue(var2.a_float_float(this.yRot));
         }
      }
   }

   @Inject(method = "getXRot()F", at = @At("HEAD"), cancellable = true)
   private void getPitchRaw(CallbackInfoReturnable<Float> var1) {
      if (!RotationManager.spoofingRotation || (Object)(Object)this != mc.player) {
         RotationLock var2 = Night.moduleManager.getModule(RotationLock.class);
         if (var2 != null && var2.isEnabled() && (var2.mode.getValue().equals("Pitch") || var2.mode.getValue().equals("Both")) && (Object)(Object)this == mc.player) {
            var1.setReturnValue(var2.b(this.xRot));
         }
      }
   }

   @Inject(method = "setYRot", at = @At("HEAD"), cancellable = true)
   private void setYaw(float var1, CallbackInfo var2) {
      if ((Object)(Object)this == mc.player) {
         Night.EVENT_BUS.post(new ReachDistanceEvent(var1));
      }
   }

   @Inject(method = "setXRot", at = @At("HEAD"), cancellable = true)
   private void setPitch(float var1, CallbackInfo var2) {
      if ((Object)(Object)this == mc.player) {
         Night.EVENT_BUS.post(new CameraBobEvent(var1));
      }
   }

   @ModifyExpressionValue(
      method = "getBlockSpeedFactor",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")
   )
   private Block getVelocityMultiplier(Block var1) {
      return !Night.moduleManager.getModule(NoSlow.class).isEnabled()
            || (var1 != Blocks.SOUL_SAND || !Night.moduleManager.getModule(NoSlow.class).soulSand.getValue())
               && (var1 != Blocks.HONEY_BLOCK || !Night.moduleManager.getModule(NoSlow.class).honeyBlocks.getValue())
         ? var1
         : Blocks.STONE;
   }

   @ModifyReturnValue(method = "isSprinting()Z", at = @At("RETURN"))
   private boolean injectIsSprinting(boolean var1) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         return var2 != null && var2.A() && var2.P() ? true : var1;
      } else {
         return var1;
      }
   }

   @ModifyReturnValue(method = "getPose", at = @At("RETURN"))
   private Pose injectGetPose(Pose var1) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         return var2 != null && var2.A() && var2.P() ? Pose.FALL_FLYING : var1;
      } else {
         return var1;
      }
   }
}
