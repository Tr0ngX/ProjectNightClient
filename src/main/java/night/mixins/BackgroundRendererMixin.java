package night.mixins;

import java.awt.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import night.Night;
import night.module.impl.visuals.Atmosphere;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AtmosphericFogEnvironment.class)
public class BackgroundRendererMixin {
   @Inject(method = "setupFog", at = @At("TAIL"))
   private void night$setupFog(FogData var1, Camera var2, ClientLevel var3, float var4, DeltaTracker var5, CallbackInfo var6) {
      NoRender var7 = Night.moduleManager.getModule(NoRender.class);
      Atmosphere var8 = Night.moduleManager.getModule(Atmosphere.class);
      if (var7.isEnabled() && var7.fog.getValue()) {
         var1.environmentalStart = var4 * 4.0F;
         var1.environmentalEnd = var4 * 4.25F;
      } else if (var8 != null && var8.isEnabled() && var8.modifyFog.getValue()) {
         var1.environmentalStart = var8.fogStart.getValue().floatValue();
         var1.environmentalEnd = var8.fogEnd.getValue().floatValue();
      }
   }

   @Inject(method = "getBaseColor", at = @At("TAIL"), cancellable = true)
   private void night$getBaseColor(ClientLevel var1, Camera var2, int var3, float var4, CallbackInfoReturnable<Integer> var5) {
      Atmosphere var6 = Night.moduleManager.getModule(Atmosphere.class);
      if (var6 != null && var6.isEnabled() && var6.modifyFog.getValue()) {
         Color var7 = var6.fogColor.getColor();
         var5.setReturnValue(ARGB.color(255, var7.getRed(), var7.getGreen(), var7.getBlue()));
      }
   }
}
