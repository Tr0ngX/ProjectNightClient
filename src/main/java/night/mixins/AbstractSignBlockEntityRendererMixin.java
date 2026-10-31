package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignBlockEntityRendererMixin<S extends SignRenderState> {
   @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
   private void submitSignText(S var1, PoseStack var2, SubmitNodeCollector var3, SignText var4, CallbackInfo var5) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).signText.getValue()) {
         var5.cancel();
      }
   }
}
