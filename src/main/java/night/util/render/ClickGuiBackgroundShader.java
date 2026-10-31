package night.util.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.awt.Color;
import java.nio.ByteBuffer;
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
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import org.lwjgl.system.MemoryStack;

public class ClickGuiBackgroundShader {
   private static final Identifier a = Identifier.fromNamespaceAndPath("night", "clickgui_bg");
   private static final Identifier b = Identifier.withDefaultNamespace("clickgui_bg_target");
   private static final Identifier c = Identifier.fromNamespaceAndPath("night", "clickgui_bg_texture");
   private static final int d = new Std140SizeCalculator().putVec4().putVec4().align(16).get();
   private static PostChain e;
   private static GpuBuffer f;
   private static long g = -1L;
   private static ClickGuiBackgroundShader.RenderTargetTexture h;
   private static float i = 0.0F;
   private static long j = -1L;

   private static void a(int var0, float var1, float var2, Color var3, boolean var4) {
      long var5 = System.currentTimeMillis();
      if (var5 - g >= 8L) {
         g = var5;
         PostChain var7 = Minecraft.getInstance().getShaderManager().getPostChain(a, LevelTargetBundle.MAIN_TARGETS);
         if (var7 != e) {
            e = var7;
            f = null;
         }

         if (e != null) {
            b(var0, var1, var2, var3, var4);
            e.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
            RenderTarget var8 = ((PostEffectProcessorAccessor)e).getPersistentTargets().get(b);
            if (var8 != null && var8.getColorTextureView() != null) {
               if (h == null || !h.a(var8.getColorTextureView())) {
                  if (h != null) {
                     Minecraft.getInstance().getTextureManager().release(c);
                  }

                  h = new ClickGuiBackgroundShader.RenderTargetTexture(var8.getColorTexture(), var8.getColorTextureView());
                  Minecraft.getInstance().getTextureManager().register(c, h);
               }
            }
         }
      }
   }

   private static void b(int var0, float var1, float var2, Color var3, boolean var4) {
      Iterator var5 = ((PostEffectProcessorAccessor)e).getPasses().iterator();

      while (var5.hasNext()) {
         Map var6 = ((PostPassAccessor)((PostPass)var5.next())).night$getCustomUniforms();
         if (var6.containsKey("ClickGuiBgConfig")) {
            if (f == null || var6.get("ClickGuiBgConfig") != f) {
               f = RenderSystem.getDevice().createBuffer(() -> "Night ClickGUI BG UBO", 136, d);
               GpuBuffer var7 = (GpuBuffer)var6.put("ClickGuiBgConfig", f);
               if (var7 != null) {
                  var7.close();
               }
            }

            long var17 = System.currentTimeMillis();
            if (j >= 0L) {
               i = i + (float)(var17 - j) / 1000.0F * var1;
            }

            j = var17;
            float var8 = var3.getRed() / 255.0F;
            float var9 = var3.getGreen() / 255.0F;
            float var10 = var3.getBlue() / 255.0F;
            float var11 = var4 ? 1.0F : 0.0F;
            MemoryStack var12 = MemoryStack.stackPush();

            try {
               ByteBuffer var13 = Std140Builder.onStack(var12, d).putVec4(i, var0, var2, var1).putVec4(var8, var9, var10, var11).get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(f.slice(), var13);
            } catch (Throwable var16) {
               if (var12 != null) {
                  try {
                     var12.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (var12 != null) {
               var12.close();
            }
         }
      }
   }

   public static void a(GuiGraphicsExtractor var0, int var1, int var2, int var3, float var4, float var5, Color var6, boolean var7) {
      if (!(var5 <= 0.001F) && var3 >= 0) {
         a(var3, var4, var5, var6, var7);
         if (h != null) {
            int var8 = Math.min(255, Math.max(0, Math.round(var5 * 255.0F))) << 24 | 16777215;
            var0.blit(RenderPipelines.GUI_TEXTURED, c, 0, 0, 0.0F, 0.0F, var1, var2, var1, var2, var1, var2, var8);
         }
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
