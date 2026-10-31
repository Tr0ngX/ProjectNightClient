package night.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DarknessFogEnvironment.class)
public class DarknessFogEnvironmentMixin {
   @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
   private void night$setupFog(FogData var1, Camera var2, ClientLevel var3, float var4, DeltaTracker var5, CallbackInfo var6) {
      NoRender var7 = Night.moduleManager.getModule(NoRender.class);
      if (var7.isEnabled() && var7.blindness.getValue()) {
         var6.cancel();
      }
   }
}
