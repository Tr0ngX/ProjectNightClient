package night.mixins;

import net.minecraft.world.level.Level;
import night.Night;
import night.module.impl.visuals.Atmosphere;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class WeatherMixin {
   @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
   private void night$isRaining(CallbackInfoReturnable<Boolean> var1) {
      Atmosphere var2 = Night.moduleManager.getModule(Atmosphere.class);
      if (var2 != null && var2.isEnabled() && !var2.weather.getValue().equalsIgnoreCase("Unchanged")) {
         String var3 = var2.weather.getValue();
         var1.setReturnValue(var3.equalsIgnoreCase("Rain") || var3.equalsIgnoreCase("Thunder"));
      }
   }

   @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
   private void night$isThundering(CallbackInfoReturnable<Boolean> var1) {
      Atmosphere var2 = Night.moduleManager.getModule(Atmosphere.class);
      if (var2 != null && var2.isEnabled() && !var2.weather.getValue().equalsIgnoreCase("Unchanged")) {
         var1.setReturnValue(var2.weather.getValue().equalsIgnoreCase("Thunder"));
      }
   }

   @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
   private void night$getRainLevel(float var1, CallbackInfoReturnable<Float> var2) {
      Atmosphere var3 = Night.moduleManager.getModule(Atmosphere.class);
      if (var3 != null && var3.isEnabled() && !var3.weather.getValue().equalsIgnoreCase("Unchanged")) {
         String var4 = var3.weather.getValue();
         var2.setReturnValue(!var4.equalsIgnoreCase("Rain") && !var4.equalsIgnoreCase("Thunder") ? 0.0F : 1.0F);
      }
   }

   @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
   private void night$getThunderLevel(float var1, CallbackInfoReturnable<Float> var2) {
      Atmosphere var3 = Night.moduleManager.getModule(Atmosphere.class);
      if (var3 != null && var3.isEnabled() && !var3.weather.getValue().equalsIgnoreCase("Unchanged")) {
         var2.setReturnValue(var3.weather.getValue().equalsIgnoreCase("Thunder") ? 1.0F : 0.0F);
      }
   }
}
