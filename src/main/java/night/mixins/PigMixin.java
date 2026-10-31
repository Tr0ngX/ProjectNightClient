package night.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import night.Night;
import night.module.impl.movement.EntityControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
public abstract class PigMixin {
   @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
   private void night$onGetControllingPassenger(CallbackInfoReturnable<LivingEntity> var1) {
      if (Night.moduleManager != null) {
         EntityControl var2 = Night.moduleManager.getModule(EntityControl.class);
         if (var2 != null && var2.isEnabled() && var2.control.getValue() && ((Pig)(Object)this).getFirstPassenger() instanceof LivingEntity var4) {
            var1.setReturnValue(var4);
         }
      }
   }
}
