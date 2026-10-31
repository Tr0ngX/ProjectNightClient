package night.util.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.RenderPipelinesAccessor;
import org.joml.Matrix4f;

public class GlintRenderLayer {
   private static final Identifier d = Identifier.fromNamespaceAndPath("night", "textures/glint.png");
   private static final RenderType e = net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
      "night_no_depth_glint",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()})
               .withLocation("night/no_depth_glint")
               .withCull(false)
               .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
               .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE)))
               .build()
         )
         .withTexture("Sampler0", d)
         .sortOnUpload()
         .createRenderSetup()
   );
   public static List<GlintRenderLayer.GlintQuad> a = new ArrayList<>();
   public static final GlintRenderLayer.GlintParams b = new GlintRenderLayer.GlintParams();
   public static final GlintRenderLayer.GlintParams c = new GlintRenderLayer.GlintParams();
   private static long f = 0L;

   public static void a() {
      long var0 = System.currentTimeMillis();
      if (f == 0L) {
         f = var0;
      }

      float var2 = (float)(var0 - f) / 1000.0F;
      f = var0;
      b.a(var2);
      c.a(var2);
   }

   public static void b() {
      if (!a.isEmpty()) {
         int var0 = 0;

         for (GlintRenderLayer.GlintQuad var2 : a) {
            var0 += var2.value().length;
         }

         ByteBufferBuilder var6 = ByteBufferBuilder.exactlySized(var0 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize());

         try {
            BufferBuilder var7 = new BufferBuilder(var6, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            Iterator var3 = a.iterator();

            while (var3.hasNext()) {
               ((GlintRenderLayer.GlintQuad)var3.next()).a(var7);
            }

            MeshData var8 = var7.build();
            Render3DUtil.a_void_RenderType_MeshData(e, var8);
         } catch (Throwable var5) {
            if (var6 != null) {
               try {
                  var6.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (var6 != null) {
            var6.close();
         }

         a.clear();
      }
   }

   public static class GlintParams {
      public float a = 0.25F;
      public float b = 0.1F;
      public float c = 1.0F;
      public String d = "Diagonal";
      public float e = 0.0F;
      public float f = 0.0F;

      public void a(float var1) {
         this.e = (this.e + this.a * var1) % 1.0F;
         this.f = (this.f + this.b * var1) % 1.0F;
         if (this.e < 0.0F) {
            this.e++;
         }

         if (this.f < 0.0F) {
            this.f++;
         }
      }

      public float[] a(float var1, float var2, float var3) {
         float var4 = this.c * 0.06F;
         float var7;
         float var8;
         switch (this.d) {
            case "Horizontal":
               var7 = (var1 + var3 * 0.5F) * var4;
               var8 = var2 * var4;
               break;
            case "Vertical":
               var7 = var2 * var4;
               var8 = (var1 + var3 * 0.5F) * var4;
               break;
            default:
               var7 = var1 * var4 + var3 * var4 * 0.5F;
               var8 = var2 * var4 + var3 * var4 * 0.5F;
         }

         var7 += this.e;
         var8 += this.f;
         return new float[]{var7, var8};
      }
   }

   public record GlintQuad(GlintRenderLayer.GlintVertex... value) {
      public void a(BufferBuilder var1) {
         for (GlintRenderLayer.GlintVertex var5 : this.value) {
            var1.addVertex(var5.matrix, var5.value, var5.value2, var5.value3).setUv(var5.value4, var5.value5).setColor(var5.count);
         }
      }
   }

   public record GlintVertex(Matrix4f matrix, float value, float value2, float value3, float value4, float value5, int count) {
   }
}
