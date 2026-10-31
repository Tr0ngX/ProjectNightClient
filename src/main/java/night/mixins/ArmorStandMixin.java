package night.mixins;

import net.minecraft.world.entity.decoration.ArmorStand;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
   @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
   private void night$hideFromRaytrace(CallbackInfoReturnable<Boolean> var1) {
      NoRender var2 = Night.moduleManager != null ? Night.moduleManager.getModule(NoRender.class) : null;
      if (var2 != null && var2.isEnabled() && var2.armorStand.getValue()) {
         var1.setReturnValue(false);
      }
   }
}
