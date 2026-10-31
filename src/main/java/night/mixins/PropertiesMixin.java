package night.mixins;

import net.minecraft.client.ClientClockManager;
import night.Night;
import night.module.impl.visuals.Atmosphere;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientClockManager.class)
public class PropertiesMixin {
   @Inject(method = "getTotalTicks", at = @At("HEAD"), cancellable = true)
   private void getTotalTicks(CallbackInfoReturnable<Long> var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var2 != null && var2.isEnabled() && var2.modifyTime.getValue()) {
         var1.setReturnValue(var2.time.getValue().longValue() * 100L);
      }
   }
}
