package night.util.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font.GlyphVisitor;
import net.minecraft.client.gui.Font.PreparedText;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import night.module.impl.core.FontModule;
import night.util.MinecraftInstance;
import night.util.world.PacketAndRenderUtil;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

public final class TextGlowRenderer implements MinecraftInstance {
   private static final Identifier b = Identifier.fromNamespaceAndPath("night", "text_glow");
   private static final int c = new Std140SizeCalculator().putVec4().get();
   private static final Vector4f d = new Vector4f(0.0F, 0.0F, 0.0F, 0.0F);
   private static TextureTarget e;
   private static TextureTarget f;
   private static boolean g;
   private static GpuBuffer h;
   private static GpuBuffer i;
   private static GpuSampler j;
   private static GpuSampler k;
   public static TextGlowRenderer.RenderPassMode a = TextGlowRenderer.RenderPassMode.NONE;

   public static GpuSampler a_GpuSampler_FilterMode(FilterMode var0) {
      if (j == null) {
         j = RenderSystem.getDevice()
            .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.of(0.0));
         k = RenderSystem.getDevice()
            .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.of(0.0));
      }

      return var0 == FilterMode.LINEAR ? j : k;
   }

   public static boolean a_bool_TextureSetup(TextureSetup var0) {
      GpuSampler var1 = var0.sampler0();
      return var1 != null && (var1 == j || var1 == k);
   }

   public static void a_void_Runnable(Runnable var0) {
      TextureTarget var1 = e = a_TextureTarget_TextureTarget_String(e, "night_text_glow_world");
      if (!g) {
         a_void_TextureTarget(var1);
         g = true;
      }

      GpuTextureView var2 = PacketAndRenderUtil.a();
      GpuTextureView var3 = PacketAndRenderUtil.b();
      PacketAndRenderUtil.a(var1.getColorTextureView(), var1.getDepthTextureView());

      try {
         var0.run();
      } finally {
         PacketAndRenderUtil.a(var2, var3);
      }
   }

   public static void a() {
      if (g) {
         g = false;
         b(e);
      }
   }

   public static RenderTarget b() {
      f = a_TextureTarget_TextureTarget_String(f, "night_text_glow_gui");
      a_void_TextureTarget(f);
      return f;
   }

   public static void c() {
      b(f);
   }

   public static void a(GuiGraphicsExtractor var0, String var1, float var2, float var3, int var4) {
      a_void_GuiGraphicsExtractor_PreparedText(var0, mc.font.prepareText(var1, var2, var3, var4, false, 0));
   }

   public static void a(GuiGraphicsExtractor var0, FormattedCharSequence var1, float var2, float var3, int var4) {
      a_void_GuiGraphicsExtractor_PreparedText(var0, mc.font.prepareText(var1, var2, var3, var4, false, false, 0));
   }

   private static void a_void_GuiGraphicsExtractor_PreparedText(GuiGraphicsExtractor var0, PreparedText var1) {
      GuiRenderState var2 = ((GuiGraphicsExtractorAccessor)var0).night$getGuiRenderState();
      Matrix3x2f var3 = new Matrix3x2f(var0.pose());
      ScreenRectangle var4 = Render2DUtil.a_ScreenRectangle_GuiGraphicsExtractor(var0);
      final LinkedHashMap<Object, List<TextRenderable>> var5 = new LinkedHashMap<>();
      var1.visit(new GlyphVisitor() {
         public void acceptRenderable(TextRenderable var1) {
            ((java.util.List)var5.computeIfAbsent(var1.textureView(), var0x -> new ArrayList<>())).add(var1);
         }
      });

      for (List var7 : (Iterable<List>)(Object)var5.values()) {
         float[] var8 = new float[var7.size() * 2];
         float[] var9 = new float[var7.size() * 2];

         for (int var10 = 0; var10 < var7.size(); var10++) {
            TextRenderable var11 = (TextRenderable)var7.get(var10);
            var8[var10 * 2] = var11.left();
            var8[var10 * 2 + 1] = var11.right();
            var9[var10 * 2] = var11.top();
            var9[var10 * 2 + 1] = var11.bottom();
         }

         TextRenderable var12 = (TextRenderable)var7.getFirst();
         TextureSetup var13 = new TextureSetup(
            var12.textureView(),
            null,
            mc.gameRenderer.lightmap(),
            a_GpuSampler_FilterMode(FilterMode.NEAREST),
            null,
            RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
         );
         var2.addGuiElement(new TextGlowRenderer.GlowGuiRenderState(var3, var7, var12.guiPipeline(), var13, var4, GuiQuadRenderState.a(var8, var9, var3, var4)));
      }
   }

   private static TextureTarget a_TextureTarget_TextureTarget_String(@Nullable TextureTarget var0, String var1) {
      int var2 = mc.getWindow().getWidth();
      int var3 = mc.getWindow().getHeight();
      if (var0 == null || var0.width != var2 || var0.height != var3) {
         if (var0 != null) {
            var0.destroyBuffers();
         }

         var0 = PacketAndRenderUtil.a(var1, var2, var3);
      }

      return var0;
   }

   private static void a_void_TextureTarget(TextureTarget var0) {
      RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(var0.getColorTexture(), d, var0.getDepthTexture(), 0.0);
   }

   private static void b(@Nullable TextureTarget var0) {
      if (var0 != null) {
         PostChain var1 = mc.getShaderManager().getPostChain(b, LevelTargetBundle.MAIN_TARGETS);
         if (var1 != null) {
            a_void_PostChain(var1);
            var1.process(var0, GraphicsResourceAllocator.UNPOOLED);
            RenderTarget var2 = mc.gameRenderer.mainRenderTarget();
            var0.blitAndBlendToTexture(var2.getColorTextureView(), var2.getDepthTextureView());
         }
      }
   }

   private static void a_void_PostChain(PostChain var0) {
      float var1 = 6.0F;
      float var2 = 1.2F;
      FontModule var3 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
      if (var3 != null) {
         var1 = var3.glowRadius.getValue().floatValue();
         var2 = var3.glowIntensity.getValue().floatValue();
      }

      Iterator var4 = ((PostEffectProcessorAccessor)var0).getPasses().iterator();

      while (var4.hasNext()) {
         Map var5 = ((PostPassAccessor)((PostPass)var4.next())).night$getCustomUniforms();
         if (var5.containsKey("TextGlowConfig")) {
            if (h == null || var5.get("TextGlowConfig") != h) {
               h = RenderSystem.getDevice().createBuffer(() -> "Night text glow H UBO", 136, c);
               GpuBuffer var6 = (GpuBuffer)var5.put("TextGlowConfig", h);
               if (var6 != null) {
                  var6.close();
               }
            }

            a(h, var1, var2);
         }

         if (var5.containsKey("TextGlowConfigV")) {
            if (i == null || var5.get("TextGlowConfigV") != i) {
               i = RenderSystem.getDevice().createBuffer(() -> "Night text glow V UBO", 136, c);
               GpuBuffer var7 = (GpuBuffer)var5.put("TextGlowConfigV", i);
               if (var7 != null) {
                  var7.close();
               }
            }

            a(i, var1, var2);
         }
      }
   }

   private static void a(GpuBuffer var0, float var1, float var2) {
      MemoryStack var3 = MemoryStack.stackPush();

      try {
         ByteBuffer var4 = Std140Builder.onStack(var3, c).putVec4(var1, var2, 0.0F, 0.0F).get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(var0.slice(), var4);
      } catch (Throwable var7) {
         if (var3 != null) {
            try {
               var3.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (var3 != null) {
         var3.close();
      }
   }

   record GlowGuiRenderState(
      Matrix3x2fc pose,
      List<TextRenderable> list,
      RenderPipeline renderPipeline,
      TextureSetup textureSetup,
      @Nullable ScreenRectangle screenRectangle,
      @Nullable ScreenRectangle screenRectangle2
   ) implements GuiElementRenderState {
      public void buildVertices(VertexConsumer var1) {
         Matrix4f var2 = new Matrix4f().mul(this.pose);
         Iterator var3 = this.list.iterator();

         while (var3.hasNext()) {
            ((TextRenderable)var3.next()).render(var2, var1, 15728880, true);
         }
      }

      public RenderPipeline pipeline() {
         return this.renderPipeline;
      }

      public @Nullable ScreenRectangle scissorArea() {
         return this.screenRectangle;
      }

      public @Nullable ScreenRectangle bounds() {
         return this.screenRectangle2;
      }
   }

   public enum RenderPassMode {
      NONE,
      MAIN,
      MASK;
   }
}
