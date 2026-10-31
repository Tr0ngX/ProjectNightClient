package night.mixins;

import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import night.Night;
import night.module.access.FreecamAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
   @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
   private void night$blockThirdPersonOnFreecam(CameraType var1, CallbackInfo var2) {
      if (Night.moduleManager != null) {
         FreecamAccess var3 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
         if (var3 != null && var3.isEnabled() && !var1.isFirstPerson()) {
            var2.cancel();
         }
      }
   }
}
