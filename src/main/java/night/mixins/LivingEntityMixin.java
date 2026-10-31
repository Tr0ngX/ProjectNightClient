package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.ConsumeItemEvent;
import night.events.impl.PlayerJumpEvent;
import night.manager.RotationManager;
import night.mixins.accessors.EntityAccessor;
import night.module.access.FastClimbAccess;
import night.module.access.HoleSnapAccess;
import night.module.access.NoJumpDelayAccess;
import night.module.access.SprintAccess;
import night.module.impl.combat.AutoMace;
import night.module.impl.movement.ElytraFly;
import night.module.impl.movement.Step;
import night.module.impl.player.Swing;
import night.util.MinecraftInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements MinecraftInstance {
   @Shadow
   @Final
   private static AttributeModifier SPEED_MODIFIER_SPRINTING;
   @Shadow
   private int noJumpDelay;
   @Shadow
   protected ItemStack useItem;
   @Unique
   private float night$grimYaw0;
   @Unique
   private boolean night$grimSwapped;

   @Shadow
   @Nullable
   public abstract AttributeInstance getAttribute(Holder<Attribute> var1);

   public LivingEntityMixin(EntityType<?> var1, Level var2) {
      super(var1, var2);
   }

   @WrapMethod(method = "maxUpStep")
   private float getStepHeight(Operation<Float> var1) {
      HoleSnapAccess var2 = Night.moduleManager != null ? (HoleSnapAccess)Night.moduleManager.getModuleByName("HoleSnap") : null;
      return (Object)(Object)this != mc.player
            || Night.moduleManager == null
            || (!Night.moduleManager.getModule(Step.class).isEnabled() || !mc.player.onGround())
               && (var2 == null || !var2.isEnabled() || !var2.isStepEnabled())
         ? (Float)var1.call(new Object[0])
         : Night.moduleManager.getModule(Step.class).height.getValue().floatValue();
   }

   @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 2, shift = Shift.BEFORE))
   private void doItemUse(CallbackInfo var1) {
      NoJumpDelayAccess var2 = Night.moduleManager != null ? (NoJumpDelayAccess)Night.moduleManager.getModuleByName("NoJumpDelay") : null;
      if (var2 != null && var2.isEnabled() && this.noJumpDelay == 10) {
         this.noJumpDelay = var2.getDelayTicks();
      }
   }

   @Inject(method = "aiStep", at = @At("RETURN"))
   private void night$bounceNoJumpDelay(CallbackInfo var1) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         if (var2 != null && var2.A()) {
            this.noJumpDelay = 0;
         }
      }
   }

   @WrapOperation(method = "getCurrentSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/SwingAnimation;duration()I"))
   private int getHandSwingDuration(SwingAnimation var1, Operation<Integer> var2) {
      int var3 = (Integer)var2.call(new Object[]{var1});
      if ((Object)(Object)this != mc.player) {
         return var3;
      } else {
         return Night.moduleManager.getModule(Swing.class).isEnabled()
               && Night.moduleManager.getModule(Swing.class).modifySpeed.getValue()
               && mc.options.getCameraType().isFirstPerson()
            ? 21 - Night.moduleManager.getModule(Swing.class).speed.getValue().intValue()
            : var3;
      }
   }

   @Inject(
      method = "completeUsingItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
         shift = Shift.AFTER
      )
   )
   private void consumeItem(CallbackInfo var1) {
      if ((Object)(Object)this == mc.player) {
         Night.EVENT_BUS.post(new ConsumeItemEvent(this.useItem));
      }
   }

   @Inject(
      method = "setSprinting",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSprinting(Z)V", shift = Shift.AFTER),
      cancellable = true
   )
   private void setSprinting$setSprinting(boolean var1, CallbackInfo var2) {
      if ((Object)(Object)this == mc.player) {
         ElytraFly var3 = Night.moduleManager != null ? Night.moduleManager.getModule(ElytraFly.class) : null;
         if (var3 != null && var3.isEnabled() && var3.mode.getValue().equalsIgnoreCase("Bounce")) {
            return;
         }

         SprintAccess var4 = Night.moduleManager == null ? null : (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
         if (var4 != null && var4.isEnabled()) {
            AttributeInstance var5 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (var5 != null) {
               var5.removeModifier(SPEED_MODIFIER_SPRINTING.id());
               if (var4.canSprint()) {
                  this.setSharedFlag(3, true);
                  var5.addTransientModifier(SPEED_MODIFIER_SPRINTING);
               } else {
                  this.setSharedFlag(3, false);
               }
            }

            var2.cancel();
         }
      }
   }

   @Inject(method = "jumpFromGround", at = @At("HEAD"))
   private void jump$HEAD(CallbackInfo var1) {
      if ((Object)(Object)this == mc.player) {
         Night.EVENT_BUS.post(new PlayerJumpEvent());
      }
   }

   @Inject(method = "jumpFromGround", at = @At("RETURN"))
   private void jump$RETURN(CallbackInfo var1) {
      if ((Object)(Object)this == mc.player) {
         Night.EVENT_BUS.post(new PlayerJumpEvent.Post());
      }
   }

   @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
   private void night$fastClimbIgnore(CallbackInfoReturnable<Boolean> var1) {
      if ((Object)(Object)this == mc.player) {
         FastClimbAccess var2 = (FastClimbAccess)Night.moduleManager.getModuleByName("FastClimb");
         if (var2.isEnabled() && var2.getClimbSpeed() == 0.0F) {
            var1.setReturnValue(false);
         }
      }
   }

   @ModifyConstant(method = "handleOnClimbable", constant = @Constant(floatValue = 0.15F), require = 0)
   private float night$fastClimbScale(float var1) {
      return this.night$fastClimbFactor(var1);
   }

   @Unique
   private float night$fastClimbFactor(float var1) {
      if ((Object)(Object)this != mc.player) {
         return var1;
      }

      FastClimbAccess var2 = (FastClimbAccess)Night.moduleManager.getModuleByName("FastClimb");
      if (!var2.isEnabled()) {
         return var1;
      }

      float var3 = var2.getClimbSpeed();
      return !(var3 <= 0.0F) && !(var3 >= 1.0F) ? var1 * var3 : var1;
   }

   @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
   private void night$hideElytraSwap(EquipmentSlot var1, CallbackInfoReturnable<ItemStack> var2) {
      if (var1 == EquipmentSlot.CHEST && (Object)(Object)this == mc.player && Night.moduleManager != null) {
         ItemStack var3 = Night.moduleManager.getModule(ElytraFly.class).N();
         if (var3 != null) {
            var2.setReturnValue(var3);
         }
      }
   }

   @Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
   private void night$pinBounceAndControlRocketGlide(CallbackInfoReturnable<Boolean> var1) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var2 = Night.moduleManager.getModule(ElytraFly.class);
         if (var2 != null && var2.isEnabled()) {
            if (var2.A() && var2.P()) {
               var1.setReturnValue(true);
               return;
            }

            if (var2.K()) {
               var1.setReturnValue(true);
               return;
            }
         }

         AutoMace var3 = Night.moduleManager.getModule(AutoMace.class);
         if (var3 != null && var3.z()) {
            var1.setReturnValue(true);
         }
      }
   }

   @WrapOperation(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F"))
   private float wrapFallFlyingPitch(LivingEntity var1, Operation<Float> var2) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var3 = Night.moduleManager.getModule(ElytraFly.class);
         if (var3 != null && var3.B()) {
            return var3.C();
         }
      }

      return (Float)var2.call(new Object[]{var1});
   }

   @WrapOperation(
      method = "updateFallFlyingMovement",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;")
   )
   private Vec3 wrapFallFlyingLookAngle(LivingEntity var1, Operation<Vec3> var2) {
      if ((Object)(Object)this == mc.player && Night.moduleManager != null) {
         ElytraFly var3 = Night.moduleManager.getModule(ElytraFly.class);
         if (var3 != null && var3.B()) {
            return Vec3.directionFromRotation(var3.C(), var1.getYRot());
         }
      }

      return (Vec3)var2.call(new Object[]{var1});
   }

   @Inject(method = "travel", at = @At("HEAD"))
   private void travel$grimHead(Vec3 var1, CallbackInfo var2) {
      this.night$grimSwapped = false;
      if ((Object)(Object)this == mc.player && this.night$shouldSpoofRotation()) {
         this.night$grimYaw0 = ((EntityAccessor)(Object)this).getRawYRot();
         ((EntityAccessor)(Object)this).setRawYRot(this.night$spoofYaw());
         RotationManager.spoofingRotation = true;
         this.night$grimSwapped = true;
      }
   }

   @Inject(method = "travel", at = @At("RETURN"))
   private void travel$grimReturn(Vec3 var1, CallbackInfo var2) {
      if (this.night$grimSwapped) {
         this.night$grimSwapped = false;
         RotationManager.spoofingRotation = false;
         ((EntityAccessor)(Object)this).setRawYRot(this.night$grimYaw0);
      }
   }

   @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
   private float jumpFromGround$grimYaw(float var1) {
      return (Object)(Object)this == mc.player && this.night$shouldSpoofRotation() ? this.night$spoofYaw() : var1;
   }

   @Unique
   private boolean night$grimCompensating() {
      if (Night.moduleManager == null) {
         return false;
      }

      SprintAccess var1 = (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
      return var1 != null && var1.isGrimSprintActive();
   }

   @Unique
   private boolean night$shouldSpoofRotation() {
      return this.night$grimCompensating();
   }

   @Unique
   private float night$spoofYaw() {
      return ((SprintAccess)Night.moduleManager.getModuleByName("Sprint")).getSprintYaw();
   }
}
