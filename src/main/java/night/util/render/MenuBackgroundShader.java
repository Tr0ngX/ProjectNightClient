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
import night.util.MinecraftInstance;
import org.lwjgl.system.MemoryStack;

public class MenuBackgroundShader implements MinecraftInstance {
   private static final Identifier a = Identifier.fromNamespaceAndPath("night", "menu_bg");
   private static final Identifier b = Identifier.withDefaultNamespace("menu_bg_target");
   private static final Identifier c = Identifier.fromNamespaceAndPath("night", "menu_bg_texture");
   private static final int d = new Std140SizeCalculator().putVec4().align(16).get();
   private static PostChain e;
   private static GpuBuffer f;
   private static long g = -1L;
   private static MenuBackgroundShader.RenderTargetTexture h;
   private static final long i = System.currentTimeMillis();
   private static final Identifier j = Identifier.fromNamespaceAndPath("night", "textures/gui/moonlight_bg.png");

   private static void a(float var0, float var1, boolean var2) {
      Minecraft var3 = Minecraft.getInstance();
      if (var3 != null && var3.getShaderManager() != null && var3.gameRenderer != null && var3.gameRenderer.mainRenderTarget() != null) {
         if (var2) {
            try {
               PostChain var4 = var3.getShaderManager().getPostChain(a, LevelTargetBundle.MAIN_TARGETS);
               if (var4 != e) {
                  e = var4;
                  f = null;
               }

               if (e == null) {
                  return;
               }

               a(var0, var1);
               e.process(var3.gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
               RenderTarget var5 = ((PostEffectProcessorAccessor)e).getPersistentTargets().get(b);
               if (var5 == null || var5.getColorTextureView() == null) {
                  return;
               }

               if (h == null || !h.a(var5.getColorTextureView())) {
                  if (h != null) {
                     var3.getTextureManager().release(c);
                  }

                  h = new MenuBackgroundShader.RenderTargetTexture(var5.getColorTexture(), var5.getColorTextureView());
                  var3.getTextureManager().register(c, h);
               }
            } catch (Throwable var6) {
            }
         }
      }
   }

   private static void a(float var0, float var1) {
      if (e != null) {
         Iterator var2 = ((PostEffectProcessorAccessor)e).getPasses().iterator();

         while (var2.hasNext()) {
            Map var3 = ((PostPassAccessor)((PostPass)var2.next())).night$getCustomUniforms();
            if (var3.containsKey("MenuBgConfig")) {
               if (f == null || var3.get("MenuBgConfig") != f) {
                  f = RenderSystem.getDevice().createBuffer(() -> "Night Menu BG UBO", 136, d);
                  GpuBuffer var4 = (GpuBuffer)var3.put("MenuBgConfig", f);
                  if (var4 != null) {
                     var4.close();
                  }
               }

               float var10 = (float)(System.currentTimeMillis() - i) / 1000.0F;
               MemoryStack var5 = MemoryStack.stackPush();

               try {
                  ByteBuffer var6 = Std140Builder.onStack(var5, d).putVec4(var10, var0, var1, 0.0F).get();
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(f.slice(), var6);
               } catch (Throwable var9) {
                  if (var5 != null) {
                     try {
                        var5.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (var5 != null) {
                  var5.close();
               }
            }
         }
      }
   }

   public static void a(GuiGraphicsExtractor var0, int var1, int var2, float var3, float var4) {
      a(var0, var1, var2, var3, var4, true);
   }

   public static void a(GuiGraphicsExtractor var0, int var1, int var2, float var3, float var4, boolean var5) {
      try {
         a(var3, var4, var5);
         int var6 = Math.min(255, Math.max(0, Math.round(var3 * 255.0F)));
         int var7 = var6 << 24 | 16777215;
         if (h != null) {
            var0.blit(RenderPipelines.GUI_TEXTURED, c, 0, 0, 0.0F, 0.0F, var1, var2, var1, var2, var1, var2, var7);
         } else {
            var0.blit(RenderPipelines.GUI_TEXTURED, j, 0, 0, 0.0F, 0.0F, var1, var2, var1, var2, var1, var2, var7);
         }
      } catch (Throwable var8) {
         Render2DUtil.a(var0, 0.0F, 0.0F, var1, var2, new Color(8, 8, 12, (int)(255.0F * var3)));
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
