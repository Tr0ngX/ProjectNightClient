package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer.Submit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {
   @ModifyArg(method = "prepareMainSubmit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;setColor(I)V"))
   private int night$handsOpacity(int var1, @Local(argsOnly = true) Submit var2) {
      return !var2.displayContext().firstPerson() ? var1 : applyHandsOpacity(var1);
   }

   private static int applyHandsOpacity(int var0) {
      return var0;
   }
}
