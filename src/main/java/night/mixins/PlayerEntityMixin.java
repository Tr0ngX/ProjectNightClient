package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.PushOutOfBlocksEvent;
import night.module.access.PatchAccess;
import night.module.access.ReachAccess;
import night.module.access.SpeedAccess;
import night.module.impl.combat.AutoMace;
import night.module.impl.movement.ElytraFly;
import night.module.impl.movement.Velocity;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements MinecraftInstance {
   protected PlayerEntityMixin(EntityType<? extends LivingEntity> var1, Level var2) {
      super(var1, var2);
   }

   @ModifyReturnValue(method = "isPushedByFluid", at = @At("RETURN"))
   private boolean isPushedByFluids(boolean var1) {
      return (Object)(Object)this == mc.player
            && Night.moduleManager.getModule(Velocity.class).isEnabled()
            && Night.moduleManager.getModule(Velocity.class).antiLiquidPush.getValue()
         ? false
         : var1;
   }

   @Redirect(
      method = "causeExtraKnockback",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V")
   )
   private void grimAttackVelocity$setDeltaMovement(Player var1, Vec3 var2) {
      if ((Object)(Object)this != mc.player || !((PatchAccess)Night.moduleManager.getModuleByName("Patch")).isGrimAttackVelocityEnabled()) {
         var1.setDeltaMovement(var2);
      }
   }

   @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
   private void grimAttackVelocity$setSprinting(Player var1, boolean var2) {
      if ((Object)(Object)this != mc.player || !((PatchAccess)Night.moduleManager.getModuleByName("Patch")).isGrimAttackVelocityEnabled()) {
         var1.setSprinting(var2);
      }
   }

   @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
   private void travel(Vec3 var1, CallbackInfo var2) {
      PushOutOfBlocksEvent var3 = new PushOutOfBlocksEvent(var1);
      Night.EVENT_BUS.post(var3);
      if (var3.isCancelled()) {
         this.move(MoverType.SELF, this.getDeltaMovement());
         var2.cancel();
      }
   }

   @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
   private void getBlockInteractionRange(CallbackInfoReturnable<Double> var1) {
      ReachAccess var2 = (ReachAccess)Night.moduleManager.getModuleByName("Reach");
      if (var2.isEnabled()) {
         var1.setReturnValue(var2.getReachDistance());
      }
   }

   @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
   private void getEntityInteractionRange(CallbackInfoReturnable<Double> var1) {
      ReachAccess var2 = (ReachAccess)Night.moduleManager.getModuleByName("Reach");
      if (var2.isEnabled()) {
         var1.setReturnValue(var2.getReachDistance());
      }
   }

   @ModifyReturnValue(method = "getDesiredPose", at = @At("RETURN"))
   private Pose night$controlRocketStandingHitbox(Pose var1) {
      if (var1 == Pose.FALL_FLYING && (Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         if (var2 != null && var2.K()) {
            return Pose.STANDING;
         }

         AutoMace var3 = Night.moduleManager.getModule(AutoMace.class);
         return var3 != null && var3.z() ? Pose.STANDING : var1;
      } else {
         return var1;
      }
   }

   @Inject(method = "getSpeed", at = @At("HEAD"), cancellable = true)
   private void getMovementSpeed(CallbackInfoReturnable<Float> var1) {
      SpeedAccess var2 = (SpeedAccess)Night.moduleManager.getModuleByName("Speed");
      if (var2.isEnabled() && var2.getMode().equalsIgnoreCase("Vanilla")) {
         var1.setReturnValue(var2.getVanillaSpeed());
      }
   }
}
