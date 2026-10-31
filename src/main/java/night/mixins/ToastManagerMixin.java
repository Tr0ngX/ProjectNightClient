package night.mixins;

import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
   @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
   private void night$noAdvancementToast(Toast var1, CallbackInfo var2) {
      if (var1 instanceof AdvancementToast && Night.moduleManager != null) {
         NoRender var3 = Night.moduleManager.getModule(NoRender.class);
         if (var3.isEnabled() && var3.advancements.getValue()) {
            var2.cancel();
         }
      }
   }
}
