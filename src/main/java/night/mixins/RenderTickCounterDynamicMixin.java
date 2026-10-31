package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker.Timer;
import night.Night;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Timer.class)
public class RenderTickCounterDynamicMixin {
   @ModifyExpressionValue(method = "advanceGameTime", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F"))
   private float night$scaleMspt(float var1) {
      return var1 / Night.worldStateManager.getTimerMultiplier();
   }
}
