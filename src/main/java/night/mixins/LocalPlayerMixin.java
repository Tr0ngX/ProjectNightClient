package night.mixins;

import net.minecraft.client.player.LocalPlayer;
import night.Night;
import night.mixins.accessors.EntityAccessor;
import night.module.impl.player.RotationLock;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements MinecraftInstance {
   @Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
   private void night$unlockViewYaw(float var1, CallbackInfoReturnable<Float> var2) {
      if (!((LocalPlayer)(Object)this).isPassenger()) {
         RotationLock var3 = Night.moduleManager.getModule(RotationLock.class);
         if (var3 != null && var3.isEnabled() && var3.custom.getValue() && (var3.mode.getValue().equals("Yaw") || var3.mode.getValue().equals("Both"))) {
            var2.setReturnValue(((EntityAccessor)(Object)this).getRawYRot());
         }
      }
   }

   @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
   private void night$unlockViewPitch(float var1, CallbackInfoReturnable<Float> var2) {
      RotationLock var3 = Night.moduleManager.getModule(RotationLock.class);
      if (var3 != null && var3.isEnabled() && var3.custom.getValue() && (var3.mode.getValue().equals("Pitch") || var3.mode.getValue().equals("Both"))) {
         var2.setReturnValue(((EntityAccessor)(Object)this).getRawXRot());
      }
   }
}
