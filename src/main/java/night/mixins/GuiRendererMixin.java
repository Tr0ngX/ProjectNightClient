package night.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.render.GuiRenderer;
import night.manager.RenderPlacementManager;
import night.mixins.accessors.GuiRendererDrawAccessor;
import night.util.render.TextGlowRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
   @Shadow
   @Final
   private List<Object> draws;

   @Shadow
   private void executeDrawRange(Supplier<String> var1, RenderTarget var2, GpuBufferSlice var3, int var4, int var5) {
   }

   @Inject(method = "draw", at = @At("RETURN"))
   private void night$postGuiRender(CallbackInfo var1) {
      Iterator var2 = RenderPlacementManager.RENDER_QUEUE.iterator();

      while (var2.hasNext()) {
         ((Runnable)var2.next()).run();
      }
   }

   @Inject(method = "executeDrawRange", at = @At("HEAD"), cancellable = true)
   private void night$splitTextGlow(Supplier<String> var1, RenderTarget var2, GpuBufferSlice var3, int var4, int var5, CallbackInfo var6) {
      if (TextGlowRenderer.a == TextGlowRenderer.RenderPassMode.NONE) {
         int var7 = -1;

         for (int var8 = var4; var8 < var5; var8++) {
            if (TextGlowRenderer.a_bool_TextureSetup(((GuiRendererDrawAccessor)(Object)this.draws.get(var8)).night$getTextureSetup())) {
               var7 = var8;
            }
         }

         if (var7 >= 0) {
            var6.cancel();

            try {
               TextGlowRenderer.a = TextGlowRenderer.RenderPassMode.MAIN;
               this.executeDrawRange(var1, var2, var3, var4, var7 + 1);
               RenderTarget var12 = TextGlowRenderer.b();
               TextGlowRenderer.a = TextGlowRenderer.RenderPassMode.MASK;
               this.executeDrawRange(() -> "Night text glow mask", var12, var3, var4, var7 + 1);
            } finally {
               TextGlowRenderer.a = TextGlowRenderer.RenderPassMode.NONE;
            }

            TextGlowRenderer.c();
            if (var7 + 1 < var5) {
               this.executeDrawRange(var1, var2, var3, var7 + 1, var5);
            }
         }
      }
   }

   @Inject(method = "executeDraw", at = @At("HEAD"), cancellable = true)
   private void night$filterTextGlow(@Coerce Object var1, RenderPass var2, CallbackInfo var3) {
      if (TextGlowRenderer.a != TextGlowRenderer.RenderPassMode.NONE) {
         boolean var10000 = TextGlowRenderer.a_bool_TextureSetup(((GuiRendererDrawAccessor)var1).night$getTextureSetup());
         if (TextGlowRenderer.a == TextGlowRenderer.RenderPassMode.MASK) {
            boolean var10001 = true;
         }

         if (var10000) {
            var3.cancel();
         }
      }
   }
}
