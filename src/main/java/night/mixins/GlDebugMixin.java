package night.mixins;

import com.mojang.blaze3d.opengl.GlDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlDebug.class)
public class GlDebugMixin {
   @Inject(method = "printDebugLog", at = @At("HEAD"), cancellable = true)
   private void night$silenceFramebufferSpam(int var1, int var2, int var3, int var4, int var5, long var6, long var8, CallbackInfo var10) {
      if (var3 == 1286) {
         var10.cancel();
      }
   }
}
