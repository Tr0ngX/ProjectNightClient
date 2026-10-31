package night.util.font;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.awt.Font;
import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import night.module.impl.core.FontModule;
import night.util.MinecraftInstance;
import night.util.render.GuiQuadRenderState;
import night.util.render.Render2DUtil;
import night.util.render.Render3DUtil;
import night.util.render.TextGlowRenderer;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;

public class CustomFontRenderer implements Closeable, MinecraftInstance {
   private final ObjectList<GlyphPage> a = new ObjectArrayList();
   private static final Map<Identifier, RenderType> b = new HashMap<>();
   private final Font[] c;
   private Font[] d;
   private final float e;
   private final int f;
   private final int g;
   private int h = 0;
   private int i = -1;
   private boolean j;
   private final ReentrantReadWriteLock k = new ReentrantReadWriteLock();

   public CustomFontRenderer(Font[] var1, float var2) {
      this(var1, var2, 256, 48);
   }

   public CustomFontRenderer(Font[] var1, float var2, int var3, int var4) {
      Preconditions.checkArgument(var2 > 0.0F, "size <= 0");
      Preconditions.checkArgument(var1.length > 0, "fonts.length <= 0");
      Preconditions.checkArgument(var3 > 4, "Unreasonable charactersPerPage count");
      Preconditions.checkArgument(var4 > 0, "paddingBetweenCharacters <= 0");
      this.c = (Font[])var1.clone();
      this.e = var2;
      this.f = var3;
      this.g = var4;
      this.a(this.c, var2);
   }

   public void a(GuiGraphicsExtractor var1, String var2, float var3, float var4, int var5, boolean var6) {
      this.a(var1, var2, var3, var4, var5, var6, false, true);
   }

   public void a(GuiGraphicsExtractor var1, String var2, float var3, float var4, int var5, boolean var6, boolean var7, boolean var8) {
      this.a(var1, Component.literal(var2).getVisualOrderText(), var3, var4, var5, var6, var7, var8);
   }

   public void a(GuiGraphicsExtractor var1, FormattedCharSequence var2, float var3, float var4, int var5, boolean var6, boolean var7, boolean var8) {
      GuiRenderState var9 = ((GuiGraphicsExtractorAccessor)var1).night$getGuiRenderState();
      this.a_void_GuiRenderState_Matrix3x2fc_ScreenRectangle_FormattedCharSequence_float_float_int_bool_bool_bool(
         var9, new Matrix3x2f(var1.pose()), Render2DUtil.a_ScreenRectangle_GuiGraphicsExtractor(var1), var2, var3, var4, var5, var6, var7, var8
      );
   }

   public void a(GuiRenderState var1, Matrix3x2fc var2, ScreenRectangle var3, FormattedCharSequence var4, float var5, float var6, int var7, boolean var8) {
      this.a_void_GuiRenderState_Matrix3x2fc_ScreenRectangle_FormattedCharSequence_float_float_int_bool_bool_bool(
         var1, var2, var3, var4, var5, var6, var7, var8, false, true
      );
   }

   public void a_void_GuiRenderState_Matrix3x2fc_ScreenRectangle_FormattedCharSequence_float_float_int_bool_bool_bool(
      GuiRenderState var1,
      Matrix3x2fc var2,
      ScreenRectangle var3,
      FormattedCharSequence var4,
      float var5,
      float var6,
      int var7,
      boolean var8,
      boolean var9,
      boolean var10
   ) {
      this.b();
      if (Night.moduleManager != null) {
         FontModule var11 = Night.moduleManager.getModule(FontModule.class);
         if (var11 != null && var11.isEnabled()) {
            var5 += var11.xOffset.getValue().floatValue();
            var6 += var11.yOffset.getValue().floatValue();
         }
      }

      Map var37 = this.a_Map_FormattedCharSequence_int_bool_floatArr(var4, var7, var8, new float[1]);
      if (!var37.isEmpty()) {
         float var12 = 1.0F / this.h;

         for (Entry var14 : (Iterable<Entry>)(Object)var37.entrySet()) {
            GlyphPage var15 = (GlyphPage)var14.getKey();
            List var16 = (List)var14.getValue();
            int var17 = var16.size();
            TextureSetup var18 = TextureSetup.singleTexture(var15.c().getTextureView(), var15.c().getSampler());
            if (var9 || var10) {
               float[] var19 = new float[var17 * 4];
               float[] var20 = new float[var17 * 4];
               float[] var21 = new float[var17 * 4];
               float[] var22 = new float[var17 * 4];
               int[] var23 = new int[var17 * 4];
               int var24 = 0;

               for (CustomFontRenderer.GlyphPlacement var26 : (Iterable<CustomFontRenderer.GlyphPlacement>)(Object)var16) {
                  GlyphMetrics var27 = var26.glyphMetrics();
                  float var28 = (float)var27.count() / var15.e();
                  float var29 = (float)var27.count2() / var15.f();
                  float var30 = (float)(var27.count() + var27.count3()) / var15.e();
                  float var31 = (float)(var27.count2() + var27.count4()) / var15.f();
                  float var32 = var5 + var26.value() * var12;
                  float var33 = var6 + var26.value2() * var12;
                  float var34 = var5 + (var26.value() + var27.count3()) * var12;
                  float var35 = var6 + (var26.value2() + var27.count4()) * var12;
                  int var36 = var24 * 4;
                  var19[var36] = var32;
                  var20[var36] = var33;
                  var21[var36] = var28;
                  var22[var36] = var29;
                  var19[var36 + 1] = var32;
                  var20[var36 + 1] = var35;
                  var21[var36 + 1] = var28;
                  var22[var36 + 1] = var31;
                  var19[var36 + 2] = var34;
                  var20[var36 + 2] = var35;
                  var21[var36 + 2] = var30;
                  var22[var36 + 2] = var31;
                  var19[var36 + 3] = var34;
                  var20[var36 + 3] = var33;
                  var21[var36 + 3] = var30;
                  var22[var36 + 3] = var29;
                  var23[var36] = var23[var36 + 1] = var23[var36 + 2] = var23[var36 + 3] = var26.count();
                  var24++;
               }

               if (var9) {
                  TextureSetup var38 = TextureSetup.singleTexture(var15.c().getTextureView(), TextGlowRenderer.a_GpuSampler_FilterMode(FilterMode.LINEAR));
                  var1.addGuiElement(new GuiQuadRenderState(var38, new Matrix3x2f(var2), var19, var20, var23, var21, var22, var3));
               }

               if (var10) {
                  var1.addGuiElement(new GuiQuadRenderState(var18, new Matrix3x2f(var2), var19, var20, var23, var21, var22, var3));
               }
            }
         }
      }
   }

   public void a(PoseStack var1, String var2, float var3, float var4, int var5, boolean var6, boolean var7, boolean var8) {
      this.a(var1, Component.literal(var2).getVisualOrderText(), var3, var4, var5, var6, var7, var8);
   }

   public void a(PoseStack var1, FormattedCharSequence var2, float var3, float var4, int var5, boolean var6, boolean var7, boolean var8) {
      this.b();
      if (Night.moduleManager != null) {
         FontModule var9 = Night.moduleManager.getModule(FontModule.class);
         if (var9 != null && var9.isEnabled()) {
            var3 += var9.xOffset.getValue().floatValue();
            var4 += var9.yOffset.getValue().floatValue();
         }
      }

      Map var15 = this.a_Map_FormattedCharSequence_int_bool_floatArr(var2, var5, var6, new float[1]);
      if (!var15.isEmpty()) {
         var1.pushPose();
         var1.translate(var3, var4, 0.0F);
         var1.scale(1.0F / this.h, 1.0F / this.h, 1.0F);
         Matrix4f var10 = var1.last().pose();

         for (Entry var12 : (Iterable<Entry>)(Object)var15.entrySet()) {
            GlyphPage var13 = (GlyphPage)var12.getKey();
            Runnable var14 = () -> {
               ByteBufferBuilder var3x = ByteBufferBuilder.exactlySized(
                  ((ObjectList)var12.getValue()).size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()
               );

               try {
                  BufferBuilder var4x = new BufferBuilder(var3x, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                  ObjectListIterator var5x = ((ObjectList)var12.getValue()).iterator();

                  while (var5x.hasNext()) {
                     CustomFontRenderer.GlyphPlacement var6x = (CustomFontRenderer.GlyphPlacement)var5x.next();
                     GlyphMetrics var7x = var6x.glyphMetrics();
                     float var8x = (float)var7x.count() / var13.e();
                     float var9x = (float)var7x.count2() / var13.f();
                     float var10x = (float)(var7x.count() + var7x.count3()) / var13.e();
                     float var11 = (float)(var7x.count2() + var7x.count4()) / var13.f();
                     var4x.addVertex(var10, var6x.value(), var6x.value2() + var7x.count4(), 0.0F).setUv(var8x, var11).setColor(var6x.count());
                     var4x.addVertex(var10, var6x.value() + var7x.count3(), var6x.value2() + var7x.count4(), 0.0F).setUv(var10x, var11).setColor(var6x.count());
                     var4x.addVertex(var10, var6x.value() + var7x.count3(), var6x.value2(), 0.0F).setUv(var10x, var9x).setColor(var6x.count());
                     var4x.addVertex(var10, var6x.value(), var6x.value2(), 0.0F).setUv(var8x, var9x).setColor(var6x.count());
                  }

                  MeshData var14x = var4x.build();
                  Render3DUtil.a_void_RenderType_MeshData(a_RenderType_Identifier(var13.d()), var14x);
               } catch (Throwable var13x) {
                  if (var3x != null) {
                     try {
                        var3x.close();
                     } catch (Throwable var12x) {
                        var13x.addSuppressed(var12x);
                     }
                  }

                  throw var13x;
               }

               if (var3x != null) {
                  var3x.close();
               }
            };
            if (var7) {
               TextGlowRenderer.a_void_Runnable(var14);
            }

            if (var8) {
               var14.run();
            }
         }

         var1.popPose();
      }
   }

   private Map<GlyphPage, ObjectList<CustomFontRenderer.GlyphPlacement>> a_Map_FormattedCharSequence_int_bool_floatArr(
      FormattedCharSequence var1, int var2, boolean var3, float[] var4
   ) {
      if ((var2 & -67108864) == 0) {
         var2 |= -16777216;
      }

      if (var3) {
         var2 = (var2 & 16579836) >> 2 | var2 & 0xFF000000;
      }

      int var5 = var2 >> 24 & 0xFF;
      int var6 = var2 >> 16 & 0xFF;
      int var7 = var2 >> 8 & 0xFF;
      int var8 = var2 & 0xFF;
      LinkedHashMap<GlyphPage, ObjectList<CustomFontRenderer.GlyphPlacement>> var9 = new LinkedHashMap<>();
      float[] var10 = new float[]{0.0F};
      var1.accept(
         (var8x, var9x, var10x) -> {
            char[] var11 = Character.toChars(var10x);
            int var12 = var6;
            int var13 = var7;
            int var14 = var8;
            if (var9x.getColor() != null) {
               int var15 = var9x.getColor().getValue();
               if ((var15 & -67108864) == 0) {
                  var15 |= -16777216;
               }

               if (var3) {
                  var15 = (var15 & 16579836) >> 2 | var15 & 0xFF000000;
               }

               var12 = var15 >> 16 & 0xFF;
               var13 = var15 >> 8 & 0xFF;
               var14 = var15 & 0xFF;
            }

            int var21 = var5 << 24 | var12 << 16 | var13 << 8 | var14;

            for (char var19 : var11) {
               GlyphMetrics var20 = this.a_GlyphMetrics_char(var19);
               if (var20 != null) {
                  if (var20.value() != ' ') {
                     var9.computeIfAbsent(var20.glyphPage(), var0 -> new ObjectArrayList())
                        .add(new CustomFontRenderer.GlyphPlacement(var10[0], 0.0F, var21, var20));
                  }

                  var10[0] += var20.count3();
               }
            }

            return true;
         }
      );
      var4[0] = var10[0];
      return var9;
   }

   private static RenderType a_RenderType_Identifier(Identifier var0) {
      return b.computeIfAbsent(
         var0,
         var0x -> net.minecraft.client.renderer.rendertype.RenderTypeHelper.create("night_font_" + var0x, RenderSetup.builder(RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", var0x).createRenderSetup())
      );
   }

   private void b() {
      if (mc.getWindow().getGuiScale() != this.i) {
         this.close();
         this.a(this.c, this.e);
      }
   }

   public float a_float_String(String var1) {
      char[] var2 = b_String_String(var1).toCharArray();
      float var3 = 0.0F;

      for (char var7 : var2) {
         GlyphMetrics var8 = this.a_GlyphMetrics_char(var7);
         if (var8 != null) {
            var3 += (float)var8.count3() / this.h;
         }
      }

      return Math.max(var3, 0.0F);
   }

   public float a_float_FormattedCharSequence(FormattedCharSequence var1) {
      float[] var2 = new float[2];
      var1.accept((var2x, var3, var4) -> {
         char var5 = (char)var4;
         GlyphMetrics var6 = this.a_GlyphMetrics_char(var5);
         if (var6 != null) {
            var2[0] += (float)var6.count3() / this.h;
         }

         return true;
      });
      return Math.max(var2[0], var2[1]);
   }

   public float a() {
      GlyphMetrics var1 = this.a_GlyphMetrics_char('A');
      return var1 != null ? (float)var1.count4() / this.h : 0.0F;
   }

   public static String b_String_String(String var0) {
      char[] var1 = var0.toCharArray();
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var1.length; var3++) {
         char var4 = var1[var3];
         if (var4 == 167) {
            var3++;
         } else {
            var2.append(var4);
         }
      }

      return var2.toString();
   }

   private void a(Font[] var1, float var2) {
      if (this.j) {
         throw new IllegalStateException("Double call to init()");
      }

      this.k.writeLock().lock();

      try {
         int var3 = mc != null && mc.getWindow() != null ? mc.getWindow().getGuiScale() : 2;
         this.i = var3 <= 0 ? 2 : var3;
         this.h = this.i * 2;
         this.d = new Font[var1.length];

         for (int var4 = 0; var4 < var1.length; var4++) {
            this.d[var4] = var1[var4].deriveFont(var2 * this.h);
         }

         this.j = true;
      } finally {
         this.k.writeLock().unlock();
      }

      this.a_GlyphMetrics_char('A');
   }

   private GlyphMetrics a_GlyphMetrics_char(char var1) {
      this.k.readLock().lock();

      try {
         ObjectListIterator var2 = this.a.iterator();

         while (var2.hasNext()) {
            GlyphPage var3 = (GlyphPage)var2.next();
            if (var3.b(var1)) {
               return var3.a_GlyphMetrics_char(var1);
            }
         }
      } finally {
         this.k.readLock().unlock();
      }

      int var13 = this.f * (int)Math.floor((double)var1 / this.f);
      GlyphPage var14 = new GlyphPage(this.d, (char)var13, (char)(var13 + this.f), this.g);
      this.k.writeLock().lock();

      try {
         var14.a();
         this.a.add(var14);
      } finally {
         this.k.writeLock().unlock();
      }

      return var14.a_GlyphMetrics_char(var1);
   }

   @Override
   public void close() {
      try {
         this.k.writeLock().lock();

         try {
            ObjectListIterator var1 = this.a.iterator();

            while (var1.hasNext()) {
               ((GlyphPage)var1.next()).b();
            }

            this.a.clear();
            this.j = false;
         } finally {
            this.k.writeLock().unlock();
         }
      } catch (Throwable var6) {
         throw var6;
      }
   }

   public record GlyphPlacement(float value, float value2, int count, GlyphMetrics glyphMetrics) {
   }
}
