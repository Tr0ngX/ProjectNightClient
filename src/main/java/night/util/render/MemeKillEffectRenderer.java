package night.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.util.MinecraftInstance;
import org.joml.Vector3f;

public class MemeKillEffectRenderer implements MinecraftInstance {
   public static final MemeKillEffectRenderer a = new MemeKillEffectRenderer();
   private static final Identifier b = Identifier.fromNamespaceAndPath("night", "textures/effect/meme_circle.png");
   private static final Identifier c = Identifier.fromNamespaceAndPath("night", "textures/effect/meme_arrow.png");
   private static RenderType d;
   private static RenderType e;
   private static final RenderPipeline f = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getEntitySnippet()})
      .withLocation("night/no_depth_meme")
      .withShaderDefine("ALPHA_CUTOUT", 0.1F)
      .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2)
      .withCull(false)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .build();
   private final List<MemeKillEffectRenderer.MemeParticle> g = new ArrayList<>();
   private long h = 0L;

   private static RenderType a_RenderType_String_Identifier(String var0, Identifier var1) {
      return net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
         "night_" + var0,
         RenderSetup.builder(f)
            .withTexture("Sampler0", var1)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(OutlineProperty.NONE)
            .createRenderSetup()
      );
   }

   private static RenderType a() {
      if (d != null) {
         return d;
      }

      d = a_RenderType_String_Identifier("kill_effect_meme_circle", b);
      return d;
   }

   private static RenderType b() {
      if (e != null) {
         return e;
      }

      e = a_RenderType_String_Identifier("kill_effect_meme_arrow", c);
      return e;
   }

   public void a_void_Vector3f_float(Vector3f var1, float var2) {
      MemeKillEffectRenderer.MemeParticle var3 = new MemeKillEffectRenderer.MemeParticle();
      var3.a.set(var1);
      var3.c = var2;
      this.g.add(var3);
   }

   public void a(PoseStack var1) {
      if (this.g.isEmpty()) {
         this.h = 0L;
      } else if (mc.player != null && mc.level != null) {
         long var2 = System.nanoTime();
         float var4 = this.h == 0L ? 0.0F : Math.min((float)(var2 - this.h) / 1.0E9F, 0.1F);
         this.h = var2;
         Iterator var5 = this.g.iterator();

         while (var5.hasNext()) {
            MemeKillEffectRenderer.MemeParticle var6 = (MemeKillEffectRenderer.MemeParticle)var5.next();
            var6.b += var4;
            if (var6.b >= 0.90000004F) {
               var5.remove();
            }
         }

         if (!this.g.isEmpty()) {
            Pose var24 = var1.last();
            Camera var7 = mc.gameRenderer.mainCamera();
            Vector3f var8 = new Vector3f(var7.upVector());
            Vector3f var9 = new Vector3f(var7.leftVector());
            Vec3 var10 = var7.position();
            Vector3f var11 = new Vector3f((float)var10.x, (float)var10.y, (float)var10.z);
            Vector3f var12 = new Vector3f();
            RenderType var13 = a();
            ByteBufferBuilder var14 = new ByteBufferBuilder(1024);

            try {
               BufferBuilder var15 = new BufferBuilder(var14, var13.primitiveTopology(), var13.format());

               for (MemeKillEffectRenderer.MemeParticle var17 : (Iterable<MemeKillEffectRenderer.MemeParticle>)(Object)this.g) {
                  if (!(var17.b % 0.3F >= 0.18F)) {
                     var12.set(var17.a).sub(var11);
                     a(var15, var24, var12, var8, var9, var17.c, 255);
                  }
               }

               MeshData var27 = var15.build();
               if (var27 != null) {
                  Render3DUtil.a_void_RenderType_MeshData(var13, var27);
               }
            } catch (Throwable var23) {
               try {
                  var14.close();
               } catch (Throwable var21) {
                  var23.addSuppressed(var21);
               }

               throw var23;
            }

            var14.close();
            Vector3f var25 = new Vector3f();
            RenderType var26 = b();
            ByteBufferBuilder var28 = new ByteBufferBuilder(1024);

            try {
               BufferBuilder var29 = new BufferBuilder(var28, var26.primitiveTopology(), var26.format());

               for (MemeKillEffectRenderer.MemeParticle var19 : (Iterable<MemeKillEffectRenderer.MemeParticle>)(Object)this.g) {
                  if (!(var19.b % 0.3F >= 0.18F)) {
                     var12.set(var19.a).sub(var11);
                     var25.set(var9).mul(-var19.c * 1.1F).add(var8.x * var19.c * 1.1F, var8.y * var19.c * 1.1F, var8.z * var19.c * 1.1F).add(var12);
                     a(var29, var24, var25, var8, var9, var19.c * 0.8F, 255);
                  }
               }

               MeshData var30 = var29.build();
               if (var30 != null) {
                  Render3DUtil.a_void_RenderType_MeshData(var26, var30);
               }
            } catch (Throwable var22) {
               try {
                  var28.close();
               } catch (Throwable var20) {
                  var22.addSuppressed(var20);
               }

               throw var22;
            }

            var28.close();
         }
      }
   }

   private static void a(BufferBuilder var0, Pose var1, Vector3f var2, Vector3f var3, Vector3f var4, float var5, int var6) {
      Vector3f var7 = new Vector3f(var3).mul(var5);
      Vector3f var8 = new Vector3f(var4).mul(var5);
      Vector3f var9 = new Vector3f(var2).sub(var7).sub(var8);
      Vector3f var10 = new Vector3f(var2).sub(var7).add(var8);
      Vector3f var11 = new Vector3f(var2).add(var7).add(var8);
      Vector3f var12 = new Vector3f(var2).add(var7).sub(var8);
      int var13 = var6 << 24 | 16777215;
      var0.addVertex(var1, var9.x, var9.y, var9.z)
         .setColor(var13)
         .setUv(1.0F, 1.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(var1, 0.0F, 1.0F, 0.0F);
      var0.addVertex(var1, var10.x, var10.y, var10.z)
         .setColor(var13)
         .setUv(0.0F, 1.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(var1, 0.0F, 1.0F, 0.0F);
      var0.addVertex(var1, var11.x, var11.y, var11.z)
         .setColor(var13)
         .setUv(0.0F, 0.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(var1, 0.0F, 1.0F, 0.0F);
      var0.addVertex(var1, var12.x, var12.y, var12.z)
         .setColor(var13)
         .setUv(1.0F, 0.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(var1, 0.0F, 1.0F, 0.0F);
   }

   static final class MemeParticle {
      final Vector3f a = new Vector3f();
      float b;
      float c;
   }
}
