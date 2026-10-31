package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.Submit;
import night.util.duck.ChamsRenderStateDuck;
import night.util.render.ChamsVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererMixin {
   @Redirect(
      method = "prepareModel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
      )
   )
   private <S> void night$captureChams(Model<S> var1, PoseStack var2, VertexConsumer var3, int var4, int var5, int var6, Submit<S> var7) {
      if (var7.state() instanceof ChamsRenderStateDuck var9
         && (var9.night$chamsFill() || var9.night$chamsOutline() || var9.night$chamsGlint() || var9.night$chamsRealAlpha() < 1.0F)) {
         int var12 = var6;
         float var10 = var9.night$chamsRealAlpha();
         if (var10 < 1.0F) {
            int var11 = Math.round((var6 >>> 24 & 0xFF) * var10);
            var12 = var6 & 16777215 | var11 << 24;
         }

         var1.renderToBuffer(
            var2,
            new ChamsVertexConsumer(
               var3,
               var9.night$chamsFill(),
               var9.night$chamsFillColor(),
               var9.night$chamsOutline(),
               var9.night$chamsOutlineColor(),
               var9.night$chamsShine(),
               var9.night$chamsGlint(),
               var9.night$chamsGlintColor(),
               var9.night$chamsGlintParams(),
               var9.night$chamsSuppressReal(),
               var10
            ),
            var4,
            var5,
            var12
         );
      } else {
         var1.renderToBuffer(var2, var3, var4, var5, var6);
      }
   }
}
