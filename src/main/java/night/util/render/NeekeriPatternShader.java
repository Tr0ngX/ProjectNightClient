package night.util.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import night.Night;
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import night.module.impl.core.ClickGuiModule;
import org.lwjgl.system.MemoryStack;

public class NeekeriPatternShader {
   private static final Identifier a = Identifier.fromNamespaceAndPath("night", "neekeri_ui");
   private static final Identifier b = Identifier.withDefaultNamespace("pattern");
   private static final Identifier c = Identifier.fromNamespaceAndPath("night", "neekeri_ui_pattern");
   private static final int d = new Std140SizeCalculator().putVec4().get();
   private static PostChain e;
   private static GpuBuffer f;
   private static long g = -1L;
   private static NeekeriPatternShader.RenderTargetTexture h;
   private static float i = 0.0F;
   private static long j = -1L;

   private static void a() {
      long var0 = System.currentTimeMillis();
      if (var0 - g >= 10L) {
         g = var0;
         PostChain var2 = Minecraft.getInstance().getShaderManager().getPostChain(a, LevelTargetBundle.MAIN_TARGETS);
         if (var2 != e) {
            e = var2;
            f = null;
         }

         if (e != null) {
            try {
               b();
               e.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
            } catch (IllegalStateException var4) {
               return;
            }

            RenderTarget var3 = ((PostEffectProcessorAccessor)e).getPersistentTargets().get(b);
            if (var3 != null && var3.getColorTextureView() != null) {
               if (h == null || !h.a(var3.getColorTextureView())) {
                  if (h != null) {
                     Minecraft.getInstance().getTextureManager().release(c);
                  }

                  h = new NeekeriPatternShader.RenderTargetTexture(var3.getColorTexture(), var3.getColorTextureView());
                  Minecraft.getInstance().getTextureManager().register(c, h);
               }
            }
         }
      }
   }

   private static void b() {
      Iterator var0 = ((PostEffectProcessorAccessor)e).getPasses().iterator();

      while (var0.hasNext()) {
         Map var1 = ((PostPassAccessor)((PostPass)var0.next())).night$getCustomUniforms();
         if (var1.containsKey("NeekeriConfig")) {
            if (f == null || var1.get("NeekeriConfig") != f) {
               f = RenderSystem.getDevice().createBuffer(() -> "Night neekeri fill UBO", 136, d);
               GpuBuffer var2 = (GpuBuffer)var1.put("NeekeriConfig", f);
               if (var2 != null) {
                  var2.close();
               }
            }

            ClickGuiModule var12 = Night.moduleManager.getModule(ClickGuiModule.class);
            long var3 = System.currentTimeMillis();
            float var5 = var12.neekeriSpeed.getValue().floatValue();
            if (j >= 0L) {
               i = i + (float)(var3 - j) / 1000.0F * var5;
            }

            j = var3;
            int var6 = Arrays.asList(EspShaderRenderer.a).indexOf(var12.fillMode.getValue());
            if (var6 <= 0) {
               var6 = EspShaderRenderer.a.length - 1;
            }

            MemoryStack var7 = MemoryStack.stackPush();

            try {
               ByteBuffer var8 = Std140Builder.onStack(var7, d).putVec4(i, var6, 0.0F, 0.0F).get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(f.slice(), var8);
            } catch (Throwable var11) {
               if (var7 != null) {
                  try {
                     var7.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (var7 != null) {
               var7.close();
            }
         }
      }
   }

   public static void a(GuiGraphicsExtractor var0, int var1, int var2, int var3, int var4, int var5) {
      a();
      if (h != null) {
         int var6 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
         int var7 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
         var0.blit(RenderPipelines.GUI_TEXTURED, c, var1, var2, var1, var2, var3, var4, var3, var4, var6, var7, var5 << 24 | 16777215);
      }
   }

   static class RenderTargetTexture extends AbstractTexture {
      RenderTargetTexture(GpuTexture var1, GpuTextureView var2) {
         this.texture = var1;
         this.textureView = var2;
      }

      boolean a(GpuTextureView var1) {
         return this.textureView == var1;
      }

      public void close() {
      }
   }
}
