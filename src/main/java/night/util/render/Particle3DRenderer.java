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
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.util.MinecraftInstance;
import night.util.world.PacketAndRenderUtil;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Particle3DRenderer implements MinecraftInstance {
   private static final float[] a = new float[17];
   private static final float[] b = new float[17];
   private static final RenderType c;
   private final List<Particle3DRenderer.Particle> d = new CopyOnWriteArrayList<>();
   private static final RandomSource e;
   private final Quaternionf f = new Quaternionf();
   private final Quaternionf g = new Quaternionf();

   public void a(Vec3 var1, Vec3 var2, double var3, int var5, int var6, boolean var7) {
      if (this.d.size() < 600) {
         this.d.add(new Particle3DRenderer.LinearParticle(var1, var2, var3, var5, var6, var7));
      }
   }

   public void a(Vec3 var1, Vec3 var2, double var3, double var5, int var7, int var8, boolean var9) {
      if (this.d.size() < 600) {
         this.d.add(new Particle3DRenderer.OrbitalParticle(var1, var2, var3, var5, var7, var8, var9));
      }
   }

   public void a() {
      this.d.removeIf(Particle3DRenderer.Particle::b);
   }

   public void a_void_PoseStack_float(PoseStack var1, float var2) {
      if (!this.d.isEmpty() && mc.gameRenderer != null) {
         Camera var3 = mc.gameRenderer.mainCamera();
         if (var3 != null) {
            Vec3 var4 = var3.position();
            long var5 = System.currentTimeMillis();
            int var7 = 0;

            for (Particle3DRenderer.Particle var9 : (Iterable<Particle3DRenderer.Particle>)(Object)this.d) {
               if (var9.a(var4, var2, var5)) {
                  var7 += var9.a();
               }
            }

            if (var7 != 0) {
               this.f.identity().rotationY((float)Math.toRadians(-var3.yRot()));
               this.g.identity().rotationX((float)Math.toRadians(var3.xRot()));
               int var15 = var7 * 16 * 3;
               ByteBufferBuilder var16 = ByteBufferBuilder.exactlySized(var15 * DefaultVertexFormat.POSITION_COLOR.getVertexSize());

               try {
                  BufferBuilder var10 = new BufferBuilder(var16, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

                  for (Particle3DRenderer.Particle var12 : (Iterable<Particle3DRenderer.Particle>)(Object)this.d) {
                     if (var12.a(var4, var2, var5)) {
                        var12.a(var1, var10, var4, var2, var5, this.f, this.g);
                     }
                  }

                  MeshData var17 = var10.build();
                  Render3DUtil.a_void_RenderType_MeshData(c, var17);
               } catch (Throwable var14) {
                  if (var16 != null) {
                     try {
                        var16.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }
                  }

                  throw var14;
               }

               if (var16 != null) {
                  var16.close();
               }
            }
         }
      }
   }

   public void b() {
      this.d.clear();
   }

   public boolean c() {
      return this.d.isEmpty();
   }

   static int a_int_int_float(int var0, float var1) {
      return ((int)((var0 >> 24 & 0xFF) * var1) & 0xFF) << 24 | var0 & 16777215;
   }

   static void a(Matrix4f var0, BufferBuilder var1, float var2, float var3, int var4) {
      for (int var5 = 0; var5 < 16; var5++) {
         float var6 = a[var5] * var2;
         float var7 = b[var5] * var2;
         float var8 = a[var5 + 1] * var2;
         float var9 = b[var5 + 1] * var2;
         var1.addVertex(var0, 0.0F, 0.0F, var3).setColor(var4);
         var1.addVertex(var0, var6, var7, var3).setColor(var4);
         var1.addVertex(var0, var8, var9, var3).setColor(var4);
      }
   }

   static AABB a(AABB var0, double var1, double var3, double var5) {
      return new AABB(
         var0.minX + (var1 < 0.0 ? var1 : 0.0),
         var0.minY + (var3 < 0.0 ? var3 : 0.0),
         var0.minZ + (var5 < 0.0 ? var5 : 0.0),
         var0.maxX + (var1 > 0.0 ? var1 : 0.0),
         var0.maxY + (var3 > 0.0 ? var3 : 0.0),
         var0.maxZ + (var5 > 0.0 ? var5 : 0.0)
      );
   }

   static float b(double var0) {
      if (var0 < 0.1) {
         return (float)(var0 * 10.0);
      } else {
         return var0 > 0.5 ? (float)(1.0 - (var0 - 0.5) * 2.0) : 1.0F;
      }
   }

   public static Vec3 a(double var0) {
      double var2 = e.nextDouble() * 2.0 * Math.PI;
      double var4 = e.nextDouble() * 2.0 * Math.PI;
      double var6 = Math.abs(Math.cos(var4));
      return new Vec3(var0 * Math.cos(var2) * var6, var0 * -Math.sin(var4), var0 * Math.sin(var2) * var6);
   }

   static {
      for (int var0 = 0; var0 <= 16; var0++) {
         double var1 = (Math.PI * 2) * var0 / 16.0;
         a[var0] = (float)Math.cos(var1);
         b[var0] = (float)Math.sin(var1);
      }

      c = net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
         "night_no_depth_particles",
         RenderSetup.builder(
               RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()})
                  .withLocation("night/no_depth_particles")
                  .withCull(false)
                  .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                  .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)))
                  .build()
            )
            .createRenderSetup()
      );
      e = RandomSource.create();
   }

   abstract static class BaseParticle implements Particle3DRenderer.Particle {
      protected final int a;
      protected final int b;
      protected final boolean c;
      protected final long d;
      protected final double e;
      protected final long f;
      protected Vec3 g;
      protected Vec3 h;

      protected BaseParticle(Vec3 var1, double var2, int var4, int var5, boolean var6) {
         this.g = var1;
         this.h = var1;
         this.a = var4;
         this.b = var5;
         this.c = var6;
         this.d = System.currentTimeMillis();
         this.e = var2;
         this.f = (long)(var2 * 1000.0);
      }

      @Override
      public int a() {
         return this.c ? 4 : 2;
      }

      @Override
      public boolean a(Vec3 var1, float var2, long var3) {
         long var5 = var3 - this.d;
         if (var5 > this.f) {
            return false;
         }

         if (Particle3DRenderer.b(Mth.clamp(var5 / 1000.0 / this.e, 0.0, 1.0)) <= 0.0F) {
            return false;
         }

         double var7 = Mth.lerp(var2, this.h.x, this.g.x) - var1.x;
         double var9 = Mth.lerp(var2, this.h.y, this.g.y) - var1.y;
         double var11 = Mth.lerp(var2, this.h.z, this.g.z) - var1.z;
         return var7 * var7 + var9 * var9 + var11 * var11 <= 4096.0;
      }

      @Override
      public void a(PoseStack var1, BufferBuilder var2, Vec3 var3, float var4, long var5, Quaternionf var7, Quaternionf var8) {
         float var9 = Particle3DRenderer.b(Mth.clamp((var5 - this.d) / 1000.0 / this.e, 0.0, 1.0));
         double var10 = Mth.lerp(var4, this.h.x, this.g.x) - var3.x;
         double var12 = Mth.lerp(var4, this.h.y, this.g.y) - var3.y;
         double var14 = Mth.lerp(var4, this.h.z, this.g.z) - var3.z;
         var1.pushPose();
         var1.translate(var10, var12, var14);
         var1.scale(0.02F, 0.02F, 0.02F);
         PacketAndRenderUtil.a(var1, var7);
         PacketAndRenderUtil.a(var1, var8);
         Matrix4f var16 = var1.last().pose();
         int var17 = Particle3DRenderer.a_int_int_float(this.b, var9);
         int var18 = Particle3DRenderer.a_int_int_float(this.a, var9);
         if (this.c) {
            int var19 = Particle3DRenderer.a_int_int_float(this.a, var9 * 0.4F);
            Particle3DRenderer.a(var16, var2, 4.0F, -0.02F, var19);
            Particle3DRenderer.a(var16, var2, 2.5F, -0.015F, var19);
         }

         Particle3DRenderer.a(var16, var2, 2.0F, -0.01F, var17);
         Particle3DRenderer.a(var16, var2, 1.0F, 0.0F, var18);
         var1.popPose();
      }
   }

   static class LinearParticle extends Particle3DRenderer.BaseParticle {
      private double i;
      private double j;
      private double k;

      LinearParticle(Vec3 var1, Vec3 var2, double var3, int var5, int var6, boolean var7) {
         super(var1, var3, var5, var6, var7);
         this.i = var2.x;
         this.j = var2.y;
         this.k = var2.z;
         this.b();
      }

      @Override
      public boolean b() {
         if (System.currentTimeMillis() - this.d > this.f) {
            return true;
         }

         this.h = this.g;
         if (MinecraftInstance.mc.level != null && MinecraftInstance.mc.player != null) {
            AABB var1 = new AABB(this.g.x - 0.025, this.g.y - 0.025, this.g.z - 0.025, this.g.x + 0.025, this.g.y + 0.025, this.g.z + 0.025);
            if (MinecraftInstance.mc
               .level
               .getBlockCollisions(MinecraftInstance.mc.player, Particle3DRenderer.a(var1, this.i, this.j, this.k))
               .iterator()
               .hasNext()) {
               if (this.i != 0.0
                  && MinecraftInstance.mc
                     .level
                     .getBlockCollisions(MinecraftInstance.mc.player, Particle3DRenderer.a(var1, this.i, 0.0, 0.0))
                     .iterator()
                     .hasNext()) {
                  this.i *= -0.7;
               }

               if (this.j != 0.0
                  && MinecraftInstance.mc
                     .level
                     .getBlockCollisions(MinecraftInstance.mc.player, Particle3DRenderer.a(var1, 0.0, this.j, 0.0))
                     .iterator()
                     .hasNext()) {
                  this.j *= -0.7;
               }

               if (this.k != 0.0
                  && MinecraftInstance.mc
                     .level
                     .getBlockCollisions(MinecraftInstance.mc.player, Particle3DRenderer.a(var1, 0.0, 0.0, this.k))
                     .iterator()
                     .hasNext()) {
                  this.k *= -0.7;
               }
            }
         }

         this.g = this.g.add(this.i, this.j, this.k);
         this.i *= 0.98;
         this.k *= 0.98;
         this.j = (this.j - 0.08) * 0.98;
         return false;
      }
   }

   static class OrbitalParticle extends Particle3DRenderer.BaseParticle {
      private final double i;
      private Vec3 j;

      OrbitalParticle(Vec3 var1, Vec3 var2, double var3, double var5, int var7, int var8, boolean var9) {
         super(var1, var5, var7, var8, var9);
         this.j = var2;
         this.i = var3;
         this.b();
      }

      @Override
      public boolean b() {
         if (System.currentTimeMillis() - this.d > this.f) {
            return true;
         }

         this.h = this.g;
         this.j = this.j.scale(this.i);
         this.g = this.g.add(this.j);
         return false;
      }
   }

   interface Particle {
      boolean b();

      boolean a(Vec3 var1, float var2, long var3);

      int a();

      void a(PoseStack var1, BufferBuilder var2, Vec3 var3, float var4, long var5, Quaternionf var7, Quaternionf var8);
   }
}
