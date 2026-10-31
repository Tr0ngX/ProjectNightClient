package night.util.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import org.joml.Matrix4f;

public class ChamsVertexConsumer implements VertexConsumer {
   private static final Matrix4f a = new Matrix4f();
   private final VertexConsumer b;
   private final boolean c;
   private final int d;
   private final boolean e;
   private final int f;
   private final boolean g;
   private final boolean h;
   private final int i;
   private final GlintRenderLayer.GlintParams j;
   private final boolean k;
   private final float l;
   private final float[] m = new float[4];
   private final float[] n = new float[4];
   private final float[] o = new float[4];
   private int p = 0;

   public ChamsVertexConsumer(
      VertexConsumer var1,
      boolean var2,
      int var3,
      boolean var4,
      int var5,
      boolean var6,
      boolean var7,
      int var8,
      GlintRenderLayer.GlintParams var9,
      boolean var10,
      float var11
   ) {
      this.b = var1;
      this.c = var2;
      this.d = var3;
      this.e = var4;
      this.f = var5;
      this.g = var6;
      this.h = var7;
      this.i = var8;
      this.j = var9;
      this.k = var10;
      this.l = var11;
   }

   public VertexConsumer addVertex(float var1, float var2, float var3) {
      if (!this.k && this.b != null) {
         this.b.addVertex(var1, var2, var3);
      }

      this.m[this.p] = var1;
      this.n[this.p] = var2;
      this.o[this.p] = var3;
      this.p++;
      if (this.p == 4) {
         this.a();
         this.p = 0;
      }

      return this;
   }

   private void a() {
      List var1 = this.g ? Render3DUtil.d : Render3DUtil.b;
      List var2 = this.g ? Render3DUtil.e : Render3DUtil.c;
      if (this.c) {
         var1.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(a, this.m[0], this.n[0], this.o[0], this.d),
               new Render3DUtil.BufferedVertex(a, this.m[1], this.n[1], this.o[1], this.d),
               new Render3DUtil.BufferedVertex(a, this.m[2], this.n[2], this.o[2], this.d),
               new Render3DUtil.BufferedVertex(a, this.m[3], this.n[3], this.o[3], this.d)
            )
         );
      }

      if (this.e) {
         var2.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(a, this.m[0], this.n[0], this.o[0], this.f),
               new Render3DUtil.BufferedVertex(a, this.m[1], this.n[1], this.o[1], this.f)
            )
         );
         var2.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(a, this.m[1], this.n[1], this.o[1], this.f),
               new Render3DUtil.BufferedVertex(a, this.m[2], this.n[2], this.o[2], this.f)
            )
         );
         var2.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(a, this.m[2], this.n[2], this.o[2], this.f),
               new Render3DUtil.BufferedVertex(a, this.m[3], this.n[3], this.o[3], this.f)
            )
         );
         var2.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(a, this.m[3], this.n[3], this.o[3], this.f),
               new Render3DUtil.BufferedVertex(a, this.m[0], this.n[0], this.o[0], this.f)
            )
         );
      }

      if (this.h) {
         GlintRenderLayer.GlintParams var3 = this.j != null ? this.j : GlintRenderLayer.b;
         float[] var4 = new float[4];
         float[] var5 = new float[4];

         for (int var6 = 0; var6 < 4; var6++) {
            float[] var7 = var3.a(this.m[var6], this.n[var6], this.o[var6]);
            var4[var6] = var7[0];
            var5[var6] = var7[1];
         }

         GlintRenderLayer.a
            .add(
               new GlintRenderLayer.GlintQuad(
                  new GlintRenderLayer.GlintVertex(a, this.m[0], this.n[0], this.o[0], var4[0], var5[0], this.i),
                  new GlintRenderLayer.GlintVertex(a, this.m[1], this.n[1], this.o[1], var4[1], var5[1], this.i),
                  new GlintRenderLayer.GlintVertex(a, this.m[2], this.n[2], this.o[2], var4[2], var5[2], this.i),
                  new GlintRenderLayer.GlintVertex(a, this.m[3], this.n[3], this.o[3], var4[3], var5[3], this.i)
               )
            );
      }
   }

   public VertexConsumer setColor(int var1, int var2, int var3, int var4) {
      if (!this.k && this.b != null) {
         int var5 = this.l >= 1.0F ? var4 : Math.round(var4 * this.l);
         this.b.setColor(var1, var2, var3, var5);
      }

      return this;
   }

   public VertexConsumer setColor(int var1) {
      if (!this.k && this.b != null) {
         if (this.l >= 1.0F) {
            this.b.setColor(var1);
         } else {
            int var2 = Math.round((var1 >>> 24 & 0xFF) * this.l);
            this.b.setColor(var1 & 16777215 | var2 << 24);
         }
      }

      return this;
   }

   public VertexConsumer setUv(float var1, float var2) {
      if (!this.k && this.b != null) {
         this.b.setUv(var1, var2);
      }

      return this;
   }

   public VertexConsumer setUv1(int var1, int var2) {
      if (!this.k && this.b != null) {
         this.b.setUv1(var1, var2);
      }

      return this;
   }

   public VertexConsumer setUv2(int var1, int var2) {
      if (!this.k && this.b != null) {
         this.b.setUv2(var1, var2);
      }

      return this;
   }

   public VertexConsumer setNormal(float var1, float var2, float var3) {
      if (!this.k && this.b != null) {
         this.b.setNormal(var1, var2, var3);
      }

      return this;
   }

   public VertexConsumer setLineWidth(float var1) {
      if (!this.k && this.b != null) {
         this.b.setLineWidth(var1);
      }

      return this;
   }
}
