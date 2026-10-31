package night.mixins;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
   @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
   private void night$limitItems(CallbackInfo var1) {
      NoRender var2 = Night.moduleManager.getModule(NoRender.class);
      if (var2 != null && var2.isEnabled()) {
         if (!var2.w()) {
            var1.cancel();
         }
      }
   }
}
