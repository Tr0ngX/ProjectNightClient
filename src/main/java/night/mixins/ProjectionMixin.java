package night.mixins;

import net.minecraft.client.renderer.Projection;
import night.Night;
import night.module.impl.visuals.AspectRatio;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Projection.class)
public class ProjectionMixin {
   @Shadow
   private boolean isMatrixDirty;

   @Inject(method = "getMatrix", at = @At("HEAD"))
   private void getMatrix$forceDirty(Matrix4f var1, CallbackInfoReturnable<Matrix4f> var2) {
      this.isMatrixDirty = true;
   }

   @ModifyArgs(method = "getMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setPerspective(FFFFZ)Lorg/joml/Matrix4f;"))
   private void getMatrix$aspectRatio(Args var1) {
      if (Night.moduleManager != null) {
         AspectRatio var2 = Night.moduleManager.getModule(AspectRatio.class);
         if (var2 != null && var2.isEnabled()) {
            var1.set(1, var2.ratio.getValue().floatValue());
         }
      }
   }
}
