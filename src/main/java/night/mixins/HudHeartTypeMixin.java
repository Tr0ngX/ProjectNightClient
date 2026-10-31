package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.Hud$HeartType")
public class HudHeartTypeMixin {
   @ModifyExpressionValue(
      method = "forPlayer",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1)
   )
   private static boolean night$hideWitherHeart(boolean var0) {
      if (var0 && Night.moduleManager != null) {
         NoRender var1 = Night.moduleManager.getModule(NoRender.class);
         if (var1.isEnabled() && var1.wither.getValue()) {
            return false;
         }
      }

      return var0;
   }
}
