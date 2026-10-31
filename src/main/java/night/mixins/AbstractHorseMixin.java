package night.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import night.Night;
import night.module.impl.movement.EntityControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
   @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
   private void night$onGetControllingPassenger(CallbackInfoReturnable<LivingEntity> var1) {
      if (Night.moduleManager != null) {
         EntityControl var2 = Night.moduleManager.getModule(EntityControl.class);
         if (var2 != null && var2.isEnabled() && var2.control.getValue() && ((AbstractHorse)(Object)this).getFirstPassenger() instanceof LivingEntity var4) {
            var1.setReturnValue(var4);
         }
      }
   }

   @Inject(method = "isTamed", at = @At("HEAD"), cancellable = true)
   private void night$onIsTamed(CallbackInfoReturnable<Boolean> var1) {
      if (Night.moduleManager != null) {
         EntityControl var2 = Night.moduleManager.getModule(EntityControl.class);
         if (var2 != null && var2.isEnabled() && var2.control.getValue()) {
            var1.setReturnValue(true);
         }
      }
   }
}
