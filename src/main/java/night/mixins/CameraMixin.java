package night.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.level.material.FogType;
import night.Night;
import night.module.access.FOVModifierAccess;
import night.module.access.FreecamAccess;
import night.module.access.MotionCameraAccess;
import night.module.access.ViewClipAccess;
import night.module.impl.combat.AutoMace;
import night.module.impl.movement.ElytraFly;
import night.module.impl.player.RotationLock;
import night.module.impl.visuals.NoRender;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin implements MinecraftInstance {
   @Shadow
   private boolean detached;

   @Shadow
   protected abstract void move(float var1, float var2, float var3);

   @Shadow
   protected abstract float getMaxZoom(float var1);

   @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
   private void update(Args var1) {
      MotionCameraAccess var2 = Night.moduleManager != null ? (MotionCameraAccess)Night.moduleManager.getModuleByName("MotionCamera") : null;
      if (var2 != null && var2.isEnabled() && var2.isSmoothPerspective()) {
         var1.set(0, (float)var2.getInterpolatedDistance());
      } else {
         ViewClipAccess var3 = Night.moduleManager != null ? (ViewClipAccess)Night.moduleManager.getModuleByName("ViewClip") : null;
         if (var3 != null && var3.isEnabled() && var3.isExtendEnabled()) {
            var1.set(0, var3.getDistance());
         }
      }
   }

   @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
   private void clipToSpace(float var1, CallbackInfoReturnable<Float> var2) {
      ViewClipAccess var3 = Night.moduleManager != null ? (ViewClipAccess)Night.moduleManager.getModuleByName("ViewClip") : null;
      if (var3 != null && var3.isEnabled()) {
         var2.setReturnValue(var1);
      }
   }

   @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
   private void getSubmersionType(CallbackInfoReturnable<FogType> var1) {
      if (Night.moduleManager != null
         && Night.moduleManager.getModule(NoRender.class).isEnabled()
         && Night.moduleManager.getModule(NoRender.class).liquidOverlay.getValue()) {
         var1.setReturnValue(FogType.NONE);
      }
   }

   @Inject(method = "update", at = @At("TAIL"))
   private void update$TAIL(DeltaTracker var1, CallbackInfo var2) {
      if (Night.moduleManager != null) {
         if (((FreecamAccess)Night.moduleManager.getModuleByName("Freecam")).isEnabled()) {
            this.detached = true;
         } else {
            MotionCameraAccess var3 = (MotionCameraAccess)Night.moduleManager.getModuleByName("MotionCamera");
            if (var3 != null && var3.isEnabled() && var3.isSmoothPerspective() && var3.shouldRenderThirdPerson()) {
               this.detached = true;
               if (mc.options != null && mc.options.getCameraType().isFirstPerson()) {
                  float var4 = (float)var3.getInterpolatedDistance();
                  if (var4 > 0.001F) {
                     this.move(-this.getMaxZoom(var4), 0.0F, 0.0F);
                  }
               }
            }
         }
      }
   }

   @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
   private void update$setRotation(Args var1) {
      if (Night.moduleManager != null) {
         FreecamAccess var2 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
         if (var2 != null && var2.isEnabled()) {
            var1.setAll(new Object[]{var2.getCameraYaw(), var2.getCameraPitch()});
            return;
         }

         ElytraFly var3 = Night.moduleManager.getModule(ElytraFly.class);
         if (var3 != null && var3.x()) {
            boolean var8 = mc.options != null && mc.options.getCameraType().isMirrored();
            float var10 = var3.v();
            float var12 = var3.w();
            if (var8) {
               var10 += 180.0F;
               var12 = -var12;
            }

            var1.setAll(new Object[]{var10, var12});
            return;
         }

         AutoMace var4 = Night.moduleManager.getModule(AutoMace.class);
         if (var4 != null && var4.w()) {
            boolean var9 = mc.options != null && mc.options.getCameraType().isMirrored();
            float var11 = var4.x();
            float var13 = var4.y();
            if (var9) {
               var11 += 180.0F;
               var13 = -var13;
            }

            var1.setAll(new Object[]{var11, var13});
            return;
         }

         RotationLock var5 = Night.moduleManager.getModule(RotationLock.class);
         if (var5 != null && var5.isEnabled() && var5.custom.getValue()) {
            float var6 = var5.v();
            float var7 = var5.w();
            if (var6 != 0.0F || var7 != 0.0F) {
               var1.setAll(new Object[]{(Float)var1.get(0) + var6, (Float)var1.get(1) + var7});
            }
         }
      }
   }

   @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
   private void update$setPos(Args var1) {
      if (Night.moduleManager != null) {
         FreecamAccess var2 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
         if (var2.isEnabled()) {
            var1.setAll(new Object[]{var2.getCameraX(), var2.getCameraY(), var2.getCameraZ()});
         } else {
            MotionCameraAccess var3 = (MotionCameraAccess)Night.moduleManager.getModuleByName("MotionCamera");
            if (var3 != null && var3.isEnabled() && var3.shouldSmoothCamera()) {
               var1.setAll(new Object[]{var3.getCameraX(), var3.getCameraY(), var3.getCameraZ()});
            }
         }
      }
   }

   @Inject(method = "calculateFov", at = @At("TAIL"), cancellable = true)
   private void getFOV(float var1, CallbackInfoReturnable<Float> var2) {
      if (Night.moduleManager != null) {
         FOVModifierAccess var3 = (FOVModifierAccess)Night.moduleManager.getModuleByName("FOVModifier");
         if (var3.isEnabled()) {
            var2.setReturnValue(var3.getFov());
         }
      }
   }
}
