package night.mixins;

import net.minecraft.client.renderer.LevelRenderer;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void night$resetItemCounter(CallbackInfo var1) {
      NoRender var2 = Night.moduleManager.getModule(NoRender.class);
      if (var2 != null) {
         var2.v();
      }
   }
}
