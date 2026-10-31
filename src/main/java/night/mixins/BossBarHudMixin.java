package night.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public class BossBarHudMixin {
   @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
   private void render(GuiGraphicsExtractor var1, CallbackInfo var2) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).bossBar.getValue()) {
         var2.cancel();
      }
   }
}
