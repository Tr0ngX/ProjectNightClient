package night.util.render;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.module.impl.core.ColorModule;
import night.module.impl.visuals.ShadersModule;
import night.util.MinecraftInstance;
import night.util.world.PacketAndRenderUtil;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class EspShaderRenderer implements MinecraftInstance {
   public static final String[] a = new String[]{"None", "Gradient", "Rainbow", "Garmadon", "Nebula", "Blue", "Purple", "Glowing", "Pink", "Neekeri", "Image"};
   private static final Identifier c = Identifier.fromNamespaceAndPath("night", "core/esp");
   private static final BindGroupLayout d = BindGroupLayout.builder().withUniform("EspSettings", UniformType.UNIFORM_BUFFER).build();
   private static final RenderPipeline e = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()})
      .withLocation("night/esp_shader_quads")
      .withFragmentShader(c)
      .withBindGroupLayout(d)
      .withCull(false)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final RenderPipeline f = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getLinesSnippet()})
      .withLocation("night/esp_shader_lines")
      .withFragmentShader(c)
      .withShaderDefine("NIGHT_LINES")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withBindGroupLayout(d)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final int g = new Std140SizeCalculator()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec2()
      .putFloat()
      .putFloat()
      .putFloat()
      .putFloat()
      .putInt()
      .putVec2()
      .align(16)
      .get();
   private static final int h = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().get();
   private static final int i = new Std140SizeCalculator().putVec4().get();
   private static GpuBuffer j;
   private static GpuBuffer k;
   private static final int l = new Std140SizeCalculator().putVec4().putVec4().get();
   private static GpuBuffer m;
   private static GpuBuffer n;
   private static final int o = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().get();
   private static GpuBuffer p;
   private static GpuBuffer q;
   private static EspShaderRenderer.ShaderUniformConfig r;
   public static final String[] b = Arrays.copyOfRange(a, 1, a.length);

   public static void a(PostChain var0, float var1) {
      float var2 = Math.min(4.0F + var1 * 0.05F, 14.0F);
      Iterator var3 = ((PostEffectProcessorAccessor)var0).getPasses().iterator();

      while (var3.hasNext()) {
         Map var4 = ((PostPassAccessor)((PostPass)var3.next())).night$getCustomUniforms();
         if (var4.containsKey("StarGlowConfig")) {
            if (j == null || var4.get("StarGlowConfig") != j) {
               j = RenderSystem.getDevice().createBuffer(() -> "Night star glow H shader UBO", 136, i);
               GpuBuffer var5 = (GpuBuffer)var4.put("StarGlowConfig", j);
               if (var5 != null) {
                  var5.close();
               }
            }

            MemoryStack var12 = MemoryStack.stackPush();

            try {
               ByteBuffer var6 = Std140Builder.onStack(var12, i).putVec4(var1, var2, 1.0F, 0.0F).get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(j.slice(), var6);
            } catch (Throwable var11) {
               if (var12 != null) {
                  try {
                     var12.close();
                  } catch (Throwable var9) {
                     var11.addSuppressed(var9);
                  }
               }

               throw var11;
            }

            if (var12 != null) {
               var12.close();
            }
         }

         if (var4.containsKey("StarGlowConfigV")) {
            if (k == null || var4.get("StarGlowConfigV") != k) {
               k = RenderSystem.getDevice().createBuffer(() -> "Night star glow V shader UBO", 136, i);
               GpuBuffer var13 = (GpuBuffer)var4.put("StarGlowConfigV", k);
               if (var13 != null) {
                  var13.close();
               }
            }

            MemoryStack var14 = MemoryStack.stackPush();

            try {
               ByteBuffer var15 = Std140Builder.onStack(var14, i).putVec4(var1, var2, 0.0F, 1.0F).get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(k.slice(), var15);
            } catch (Throwable var10) {
               if (var14 != null) {
                  try {
                     var14.close();
                  } catch (Throwable var8) {
                     var10.addSuppressed(var8);
                  }
               }

               throw var10;
            }

            if (var14 != null) {
               var14.close();
            }
         }
      }
   }

   public static int a_int_String(String var0) {
      for (int var1 = 0; var1 < a.length; var1++) {
         if (a[var1].equalsIgnoreCase(var0)) {
            return var1;
         }
      }

      return 0;
   }

   public static void a_void_ShaderUniformConfig(EspShaderRenderer.ShaderUniformConfig var0) {
      r = var0;
   }

   public static void a(PostChain var0, EspShaderRenderer.ShaderUniformConfig var1, float var2) {
      float var3 = Math.min(6.0F + var2 * 18.0F, 24.0F);
      Iterator var4 = ((PostEffectProcessorAccessor)var0).getPasses().iterator();

      while (var4.hasNext()) {
         Map var5 = ((PostPassAccessor)((PostPass)var4.next())).night$getCustomUniforms();
         if (var5.containsKey("BloomConfig")) {
            if (p == null || var5.get("BloomConfig") != p) {
               p = RenderSystem.getDevice().createBuffer(() -> "Night outline bloom UBO", 136, o);
               GpuBuffer var6 = (GpuBuffer)var5.put("BloomConfig", p);
               if (var6 != null) {
                  var6.close();
               }
            }

            ShadersModule var28 = Night.moduleManager != null ? Night.moduleManager.getModule(ShadersModule.class) : null;
            float var7 = var28 != null ? var28.bloomWidth.getValue().floatValue() : 3.0F;
            float var8 = var28 != null && var28.bloomGlowInside.getValue() ? 1.0F : 0.0F;
            float var9 = var28 != null ? var28.x.getValue().floatValue() : 1.0F;
            float var10 = var28 != null ? var28.y.getValue().floatValue() : 1.0F;
            float var11 = var28 != null ? var28.z.getValue().floatValue() / 100.0F : 1.0F;
            float var12 = var28 != null ? var28.bloomOutlineAlpha.getValue().floatValue() / 100.0F : 1.0F;
            float var13 = var28 != null ? ShadersModule.b_int_String(var28.bloomFillMode.getValue()) : 0.0F;
            float var14 = var28 != null ? var28.bloomGradientFactor.getValue().floatValue() : 200.0F;
            Color var15 = var28 != null ? var28.bloomGradientColor.getColor() : Color.RED;
            float var16 = (float)(System.currentTimeMillis() % 2000000L);
            MemoryStack var17 = MemoryStack.stackPush();

            try {
               ByteBuffer var18 = Std140Builder.onStack(var17, o)
                  .putVec4(var7, var8, var9, var10)
                  .putVec4(var11, var12, var13, var14)
                  .putVec4(var15.getRed() / 255.0F, var15.getGreen() / 255.0F, var15.getBlue() / 255.0F, var15.getAlpha() / 255.0F)
                  .putVec4(var16, 0.0F, mc.getWindow().getWidth(), mc.getWindow().getHeight())
                  .get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(p.slice(), var18);
            } catch (Throwable var27) {
               if (var17 != null) {
                  try {
                     var17.close();
                  } catch (Throwable var23) {
                     var27.addSuppressed(var23);
                  }
               }

               throw var27;
            }

            if (var17 != null) {
               var17.close();
            }
         }

         if (var5.containsKey("OutlineGlowConfig")) {
            if (m == null || var5.get("OutlineGlowConfig") != m) {
               m = RenderSystem.getDevice().createBuffer(() -> "Night outline glow H UBO", 136, l);
               GpuBuffer var29 = (GpuBuffer)var5.put("OutlineGlowConfig", m);
               if (var29 != null) {
                  var29.close();
               }
            }

            ShadersModule var30 = Night.moduleManager != null ? Night.moduleManager.getModule(ShadersModule.class) : null;
            Color var32 = var30 != null && var30.glowColor != null ? var30.glowColor.getColor() : Color.MAGENTA;
            MemoryStack var34 = MemoryStack.stackPush();

            try {
               ByteBuffer var39 = Std140Builder.onStack(var34, l)
                  .putVec4(var2, var3, 1.0F, 0.0F)
                  .putVec4(var32.getRed() / 255.0F, var32.getGreen() / 255.0F, var32.getBlue() / 255.0F, var32.getAlpha() / 255.0F)
                  .get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(m.slice(), var39);
            } catch (Throwable var26) {
               if (var34 != null) {
                  try {
                     var34.close();
                  } catch (Throwable var22) {
                     var26.addSuppressed(var22);
                  }
               }

               throw var26;
            }

            if (var34 != null) {
               var34.close();
            }
         }

         ShadersModule var31 = Night.moduleManager != null ? Night.moduleManager.getModule(ShadersModule.class) : null;
         ColorModule var33 = Night.moduleManager != null ? Night.moduleManager.getModule(ColorModule.class) : null;
         if (var5.containsKey("OutlineGlowConfigV")) {
            if (n == null || var5.get("OutlineGlowConfigV") != n) {
               n = RenderSystem.getDevice().createBuffer(() -> "Night outline glow V UBO", 136, l);
               GpuBuffer var35 = (GpuBuffer)var5.put("OutlineGlowConfigV", n);
               if (var35 != null) {
                  var35.close();
               }
            }

            Color var36 = var31 != null && var31.glowColor != null ? var31.glowColor.getColor() : Color.MAGENTA;
            boolean var40 = var31 != null
               && var31.glowColor != null
               && (var31.glowColor.isRainbow() || var31.glowColor.isSync() && var33 != null && var33.color.isRainbow());
            MemoryStack var42 = MemoryStack.stackPush();

            try {
               ByteBuffer var44 = Std140Builder.onStack(var42, l)
                  .putVec4(var2, var3, var40 ? 1.0F : 0.0F, 1.0F)
                  .putVec4(var36.getRed() / 255.0F, var36.getGreen() / 255.0F, var36.getBlue() / 255.0F, var36.getAlpha() / 255.0F)
                  .get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(n.slice(), var44);
            } catch (Throwable var25) {
               if (var42 != null) {
                  try {
                     var42.close();
                  } catch (Throwable var21) {
                     var25.addSuppressed(var21);
                  }
               }

               throw var25;
            }

            if (var42 != null) {
               var42.close();
            }
         }

         if (var5.containsKey("EspSettings")) {
            if (q == null || var5.get("EspSettings") != q) {
               q = RenderSystem.getDevice().createBuffer(() -> "Night outline shader UBO", 136, h);
               GpuBuffer var37 = (GpuBuffer)var5.put("EspSettings", q);
               if (var37 != null) {
                  var37.close();
               }
            }

            boolean var38 = var31 != null
               && var31.color != null
               && (var31.color.isRainbow() || var31.color.isSync() && var33 != null && var33.color.isRainbow());
            float var41 = var33 != null ? var33.rainbowSaturation.getValue().floatValue() / 100.0F : 1.0F;
            MemoryStack var43 = MemoryStack.stackPush();

            try {
               ByteBuffer var45 = Std140Builder.onStack(var43, h)
                  .putVec4(var1.f)
                  .putVec4(var1.g)
                  .putVec4(var1.h)
                  .putVec4(var1.i)
                  .putVec4(var1.b, var1.c, var1.e, var1.a)
                  .putVec4(var1.d, var2, var38 ? 1.0F : 0.0F, var41)
                  .get();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(q.slice(), var45);
            } catch (Throwable var24) {
               if (var43 != null) {
                  try {
                     var43.close();
                  } catch (Throwable var20) {
                     var24.addSuppressed(var20);
                  }
               }

               throw var24;
            }

            if (var43 != null) {
               var43.close();
            }
         }
      }
   }

   public static EspShaderRenderer.ShaderUniformConfig a(
      String var0, float var1, float var2, float var3, Color var4, Color var5, Color var6, Color var7, Color var8, Vec3 var9
   ) {
      return a(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, new Vector2f(0.0F, 0.0F));
   }

   public static EspShaderRenderer.ShaderUniformConfig a(
      String var0, float var1, float var2, float var3, Color var4, Color var5, Color var6, Color var7, Color var8, Vec3 var9, Vector2f var10
   ) {
      int var11 = a_int_String(var0);
      if (var11 == 0) {
         return null;
      }

      float var12 = (float)(System.currentTimeMillis() % 2000000L) / 1000.0F * var2 * (var11 == 1 ? var3 : 1.0F);
      float var13 = var9 != null && mc.player != null ? (float)Math.sqrt(mc.player.distanceToSqr(var9)) : 1.0F;
      return new EspShaderRenderer.ShaderUniformConfig(var11, var12, var3, var1 / 100.0F, var13, var11 == 7 ? var8 : var4, var5, var6, var7, var10);
   }

   public static void a(List<Render3DUtil.BufferedPrimitive> var0, List<Render3DUtil.BufferedPrimitive> var1) {
      a(var0, var1, r);
   }

   public static void a(List<Render3DUtil.BufferedPrimitive> var0, List<Render3DUtil.BufferedPrimitive> var1, EspShaderRenderer.ShaderUniformConfig var2) {
      if (var2 != null && (!var0.isEmpty() || !var1.isEmpty())) {
         GpuBuffer var3 = RenderSystem.getDevice().createBuffer(() -> "Night ESP shader UBO", 136, g);
         MemoryStack var4 = MemoryStack.stackPush();

         try {
            ByteBuffer var5 = Std140Builder.onStack(var4, g)
               .putVec4(var2.f)
               .putVec4(var2.g)
               .putVec4(var2.h)
               .putVec4(var2.i)
               .putVec2(mc.getWindow().getWidth(), mc.getWindow().getHeight())
               .putFloat(var2.b)
               .putFloat(var2.c)
               .putFloat(var2.d)
               .putFloat(var2.e)
               .putInt(var2.a)
               .putVec2(var2.j.x, var2.j.y)
               .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(var3.slice(), var5);
         } catch (Throwable var12) {
            if (var4 != null) {
               try {
                  var4.close();
               } catch (Throwable var10) {
                  var12.addSuppressed(var10);
               }
            }

            throw var12;
         }

         if (var4 != null) {
            var4.close();
         }

         if (!var0.isEmpty()) {
            int var14 = Render3DUtil.a_int_List(var0);
            ByteBufferBuilder var16 = ByteBufferBuilder.exactlySized(var14 * DefaultVertexFormat.POSITION_COLOR.getVertexSize());

            try {
               BufferBuilder var6 = new BufferBuilder(var16, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
               Iterator var7 = var0.iterator();

               while (var7.hasNext()) {
                  ((Render3DUtil.BufferedPrimitive)var7.next()).a(var6);
               }

               a(e, var6.build(), var3);
            } catch (Throwable var13) {
               if (var16 != null) {
                  try {
                     var16.close();
                  } catch (Throwable var9) {
                     var13.addSuppressed(var9);
                  }
               }

               throw var13;
            }

            if (var16 != null) {
               var16.close();
            }
         }

         if (!var1.isEmpty()) {
            int var15 = Render3DUtil.a_int_List(var1) * 2;
            ByteBufferBuilder var17 = ByteBufferBuilder.exactlySized(var15 * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize());

            try {
               BufferBuilder var18 = new BufferBuilder(var17, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
               Render3DUtil.a_void_BufferBuilder_List(var18, var1);
               a(f, var18.build(), var3);
            } catch (Throwable var11) {
               if (var17 != null) {
                  try {
                     var17.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (var17 != null) {
               var17.close();
            }
         }

         var3.close();
         var0.clear();
         var1.clear();
      } else {
         var0.clear();
         var1.clear();
      }
   }

   private static void a(RenderPipeline var0, MeshData var1, GpuBuffer var2) {
      if (var1 != null) {
         GpuBufferSlice var3 = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

         try {
            GpuBuffer var4 = RenderSystem.getDevice().createBuffer(() -> "Night ESP shader vertices", 32, var1.vertexBuffer());
            GpuBuffer var5 = null;
            GpuBuffer var7;
            IndexType var8;
            if (var1.indexBuffer() == null) {
               AutoStorageIndexBuffer var6 = RenderSystem.getSequentialBuffer(var1.drawState().primitiveTopology());
               var7 = var6.getBuffer(var1.drawState().indexCount());
               var8 = var6.type();
            } else {
               var7 = var5 = RenderSystem.getDevice().createBuffer(() -> "Night ESP shader indices", 64, var1.indexBuffer());
               var8 = var1.drawState().indexType();
            }

            RenderTarget var18 = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTextureView var9 = PacketAndRenderUtil.a_GpuTextureView_RenderTarget(var18);
            GpuTextureView var10 = PacketAndRenderUtil.b_GpuTextureView_RenderTarget(var18);
            RenderPass var11 = RenderSystem.getDevice()
               .createCommandEncoder()
               .createRenderPass(() -> "Night ESP shader draw", var9, Optional.empty(), var10, OptionalDouble.empty());

            try {
               PacketAndRenderUtil.a(var11, var0);
               RenderSystem.bindDefaultUniforms(var11);
               var11.setUniform("DynamicTransforms", var3);
               var11.setUniform("EspSettings", var2);
               var11.setVertexBuffer(0, var4.slice());
               var11.setIndexBuffer(var7, var8);
               var11.drawIndexed(var1.drawState().indexCount(), 1, 0, 0, 0);
            } catch (Throwable var16) {
               if (var11 != null) {
                  try {
                     var11.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (var11 != null) {
               var11.close();
            }

            var4.close();
            if (var5 != null) {
               var5.close();
            }
         } catch (Throwable var17) {
            if (var1 != null) {
               try {
                  var1.close();
               } catch (Throwable var14) {
                  var17.addSuppressed(var14);
               }
            }

            throw var17;
         }

         if (var1 != null) {
            var1.close();
         }
      }
   }

   public static class ShaderUniformConfig {
      public final int a;
      public final float b;
      public final float c;
      public final float d;
      public final float e;
      public final Vector4f f;
      public final Vector4f g;
      public final Vector4f h;
      public final Vector4f i;
      public final Vector2f j;

      public ShaderUniformConfig(int var1, float var2, float var3, float var4, float var5, Color var6, Color var7, Color var8, Color var9) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, new Vector2f(0.0F, 0.0F));
      }

      public ShaderUniformConfig(int var1, float var2, float var3, float var4, float var5, Color var6, Color var7, Color var8, Color var9, Vector2f var10) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
         this.d = var4;
         this.e = var5;
         this.f = a(var6);
         this.g = a(var7);
         this.h = a(var8);
         this.i = a(var9);
         this.j = var10 != null ? var10 : new Vector2f(0.0F, 0.0F);
      }

      private static Vector4f a(Color var0) {
         return new Vector4f(var0.getRed() / 255.0F, var0.getGreen() / 255.0F, var0.getBlue() / 255.0F, var0.getAlpha() / 255.0F);
      }
   }
}
