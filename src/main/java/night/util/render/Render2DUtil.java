package night.util.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import night.Night;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import night.util.MinecraftInstance;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

public class Render2DUtil implements MinecraftInstance {
   public static final Matrix4f a = new Matrix4f();
   public static final Matrix4f b = new Matrix4f();
   public static final Matrix4f c = new Matrix4f();
   private static final Map<Identifier, RenderType> d = new HashMap<>();
   private static MethodHandle e;
   private static MethodHandle f;
   private static boolean g = false;

   public static void a(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, Color var5) {
      int var6 = var5.getRGB();
      a(var0, new float[]{var1, var1, var3, var3}, new float[]{var2, var4, var4, var2}, new int[]{var6, var6, var6, var6});
   }

   public static void a(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, Color var5, Color var6) {
      int var7 = var5.getRGB();
      int var8 = var6.getRGB();
      a(var0, new float[]{var1, var1, var3, var3}, new float[]{var2, var4, var4, var2}, new int[]{var7, var8, var8, var7});
   }

   public static void b(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, Color var5, Color var6) {
      int var7 = var5.getRGB();
      int var8 = var6.getRGB();
      a(var0, new float[]{var1, var1, var3, var3}, new float[]{var2, var4, var4, var2}, new int[]{var7, var7, var8, var8});
   }

   public static void a(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, float var5, Color var6, Color var7) {
      int var8 = var6.getRGB();
      int var9 = var7.getRGB();
      int var10 = (int)Math.ceil((var3 - var1) / var5);
      int var11 = (int)Math.ceil((var4 - var2) / var5);
      int var12 = var10 * var11;
      if (var12 > 0) {
         float[] var13 = new float[var12 * 4];
         float[] var14 = new float[var12 * 4];
         int[] var15 = new int[var12 * 4];
         int var16 = 0;

         for (int var17 = 0; var17 < var11; var17++) {
            float var18 = var2 + var17 * var5;
            float var19 = Math.min(var2 + (var17 + 1) * var5, var4);

            for (int var20 = 0; var20 < var10; var20++) {
               float var21 = var1 + var20 * var5;
               float var22 = Math.min(var1 + (var20 + 1) * var5, var3);
               int var23 = (var17 + var20) % 2 == 0 ? var8 : var9;
               a(var13, var14, var15, var16++, var21, var18, var22, var19, var23);
            }
         }

         a(var0, var13, var14, var15);
      }
   }

   public static void b(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, Color var5) {
      int var6 = var5.getRGB();
      float[] var7 = new float[16];
      float[] var8 = new float[16];
      int[] var9 = new int[16];
      a(var7, var8, var9, 0, var1, var2, var1 + 0.5F, var4, var6);
      a(var7, var8, var9, 1, var3 - 0.5F, var2, var3, var4, var6);
      a(var7, var8, var9, 2, var1, var4 - 0.5F, var3, var4, var6);
      a(var7, var8, var9, 3, var1, var2, var3, var2 + 0.5F, var6);
      a(var0, var7, var8, var9);
   }

   public static ScreenRectangle a_ScreenRectangle_GuiGraphicsExtractor(GuiGraphicsExtractor var0) {
      if (!g) {
         g = true;

         try {
            Field var1 = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
            var1.setAccessible(true);
            e = MethodHandles.lookup().unreflectGetter(var1);
            Method var2 = var1.getType().getDeclaredMethod("peek");
            var2.setAccessible(true);
            f = MethodHandles.lookup().unreflect(var2);
         } catch (Throwable var4) {
            Night.LOGGER.debug("Could not bind scissorStack handle: {}", var4.getMessage());
         }
      }

      if (e != null && f != null) {
         try {
            Object var5 = (Object)e.invoke((GuiGraphicsExtractor)var0);
            if (var5 != null) {
               return (ScreenRectangle)f.invoke((Object)var5);
            }
         } catch (Throwable var3) {
         }
      }

      return null;
   }

   private static void a(GuiGraphicsExtractor var0, float[] var1, float[] var2, int[] var3) {
      ((GuiGraphicsExtractorAccessor)var0)
         .night$getGuiRenderState()
         .addGuiElement(new GuiQuadRenderState(new Matrix3x2f(var0.pose()), var1, var2, var3, a_ScreenRectangle_GuiGraphicsExtractor(var0)));
   }

   private static void a(float[] var0, float[] var1, int[] var2, int var3, float var4, float var5, float var6, float var7, int var8) {
      int var9 = var3 * 4;
      var0[var9] = var4;
      var1[var9] = var5;
      var0[var9 + 1] = var4;
      var1[var9 + 1] = var7;
      var0[var9 + 2] = var6;
      var1[var9 + 2] = var7;
      var0[var9 + 3] = var6;
      var1[var9 + 3] = var5;
      var2[var9] = var2[var9 + 1] = var2[var9 + 2] = var2[var9 + 3] = var8;
   }

   public static void a(PoseStack var0, float var1, float var2, float var3, Color var4) {
      Matrix4f var5 = var0.last().pose();
      int var6 = var4.getRGB();
      ByteBufferBuilder var7 = ByteBufferBuilder.exactlySized(361 * DefaultVertexFormat.POSITION_COLOR.getVertexSize());

      try {
         BufferBuilder var8 = new BufferBuilder(var7, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

         for (int var9 = 0; var9 <= 360; var9++) {
            var8.addVertex(var5, (float)(var1 + Math.sin(var9 * Math.PI / 180.0) * var3), (float)(var2 + Math.cos(var9 * Math.PI / 180.0) * var3), 0.0F)
               .setColor(var6);
         }

         MeshData var12 = var8.build();
         Render3DUtil.a_void_RenderType_MeshData(RenderTypes.debugTriangleFan(), var12);
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

   public static void a(PoseStack var0, float var1, float var2, float var3, float var4, Identifier var5, Color var6) {
      Matrix4f var7 = var0.last().pose();
      int var8 = var6.getRGB();
      ByteBufferBuilder var9 = ByteBufferBuilder.exactlySized(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize());

      try {
         BufferBuilder var10 = new BufferBuilder(var9, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         var10.addVertex(var7, var1, var2, 0.0F).setUv(0.0F, 0.0F).setColor(var8);
         var10.addVertex(var7, var1, var4, 0.0F).setUv(0.0F, 1.0F).setColor(var8);
         var10.addVertex(var7, var3, var4, 0.0F).setUv(1.0F, 1.0F).setColor(var8);
         var10.addVertex(var7, var3, var2, 0.0F).setUv(1.0F, 0.0F).setColor(var8);
         MeshData var11 = var10.build();
         Render3DUtil.a_void_RenderType_MeshData(a_RenderType_Identifier(var5), var11);
      } catch (Throwable var13) {
         if (var9 != null) {
            try {
               var9.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (var9 != null) {
         var9.close();
      }
   }

   public static void c(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, Color var5) {
      int var6 = var5.getRGB();
      float var7 = var2 + var4 * 0.75F;
      float[] var8 = new float[]{var1, var1 - var3, var1, var1, var1, var1, var1 + var3, var1};
      float[] var9 = new float[]{var2, var2 + var4, var7, var2, var2, var7, var2 + var4, var2};
      int[] var10 = new int[]{var6, var6, var6, var6, var6, var6, var6, var6};
      a(var0, var8, var9, var10);
   }

   public static void d(GuiGraphicsExtractor var0, float var1, float var2, float var3, float var4, Color var5) {
      int var6 = var5.getRGB();
      float var7 = var2 + var4 * 0.75F;
      float[] var8 = new float[16];
      float[] var9 = new float[16];
      int[] var10 = new int[16];
      a(var8, var9, var10, 0, var1, var2, var1 - var3, var2 + var4, 0.5F, var6);
      a(var8, var9, var10, 1, var1 - var3, var2 + var4, var1, var7, 0.5F, var6);
      a(var8, var9, var10, 2, var1, var7, var1 + var3, var2 + var4, 0.5F, var6);
      a(var8, var9, var10, 3, var1 + var3, var2 + var4, var1, var2, 0.5F, var6);
      a(var0, var8, var9, var10);
   }

   private static void a(float[] var0, float[] var1, int[] var2, int var3, float var4, float var5, float var6, float var7, float var8, int var9) {
      float var10 = var6 - var4;
      float var11 = var7 - var5;
      float var12 = (float)Math.sqrt(var10 * var10 + var11 * var11);
      if (!(var12 < 1.0E-4F)) {
         float var13 = -var11 / var12 * var8;
         float var14 = var10 / var12 * var8;
         int var15 = var3 * 4;
         var0[var15] = var4 + var13;
         var1[var15] = var5 + var14;
         var0[var15 + 1] = var4 - var13;
         var1[var15 + 1] = var5 - var14;
         var0[var15 + 2] = var6 - var13;
         var1[var15 + 2] = var7 - var14;
         var0[var15 + 3] = var6 + var13;
         var1[var15 + 3] = var7 + var14;
         var2[var15] = var2[var15 + 1] = var2[var15 + 2] = var2[var15 + 3] = var9;
      }
   }

   private static RenderType a_RenderType_Identifier(Identifier var0) {
      return d.computeIfAbsent(
         var0,
         var0x -> net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
            "night_textured_" + var0x, RenderSetup.builder(RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", var0x).createRenderSetup()
         )
      );
   }
}
