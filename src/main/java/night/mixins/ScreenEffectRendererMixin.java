package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
   @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
   private static void night$renderFire(PoseStack var0, SubmitNodeCollector var1, TextureAtlasSprite var2, CallbackInfo var3) {
      NoRender var4 = Night.moduleManager.getModule(NoRender.class);
      if (var4.isEnabled() && var4.fireOverlay.getValue()) {
         var3.cancel();
      }
   }

   @Inject(method = "submitWater", at = @At("HEAD"), cancellable = true)
   private static void night$renderWater(CallbackInfo var0) {
      NoRender var1 = Night.moduleManager.getModule(NoRender.class);
      if (var1.isEnabled() && var1.liquidOverlay.getValue()) {
         var0.cancel();
      }
   }

   @Inject(method = "submitBlockSprite", at = @At("HEAD"), cancellable = true)
   private static void night$renderTex(CallbackInfo var0) {
      NoRender var1 = Night.moduleManager.getModule(NoRender.class);
      if (var1.isEnabled() && var1.blockOverlay.getValue()) {
         var0.cancel();
      }
   }
}
