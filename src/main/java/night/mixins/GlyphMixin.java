package night.mixins;

import com.mojang.blaze3d.font.GlyphInfo;
import night.Night;
import night.manager.FontManager;
import night.module.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphInfo.class)
public interface GlyphMixin {
   @Inject(method = "getShadowOffset", at = @At("HEAD"), cancellable = true)
   private void getShadowOffset(CallbackInfoReturnable<Float> var1) {
      if (FontManager.isInRender()) {
         if (Night.moduleManager != null) {
            FontModule var2 = Night.moduleManager.getModule(FontModule.class);
            if (var2 != null && var2.isEnabled() && !var2.shadowMode.getValue().equalsIgnoreCase("Default")) {
               var1.setReturnValue(Night.fontManager.getShadowOffset());
            }
         }
      }
   }
}
