package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.WalkAnimationState;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WalkAnimationState.class)
public class LimbAnimatorMixin {
   @ModifyReturnValue(method = "position()F", at = @At("RETURN"))
   private float getPos(float var1) {
      return Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).limbSwing.getValue() ? 0.0F : var1;
   }

   @ModifyReturnValue(method = "position(F)F", at = @At("RETURN"))
   private float getPos2(float var1) {
      return Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).limbSwing.getValue() ? 0.0F : var1;
   }

   @ModifyReturnValue(method = "speed()F", at = @At("RETURN"))
   private float getSpeed(float var1) {
      return Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).limbSwing.getValue() ? 0.0F : var1;
   }

   @ModifyReturnValue(method = "speed(F)F", at = @At("RETURN"))
   private float getSpeed2(float var1) {
      return Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).limbSwing.getValue() ? 0.0F : var1;
   }
}
