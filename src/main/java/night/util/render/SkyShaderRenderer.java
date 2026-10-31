package night.util.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import night.Night;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.module.impl.visuals.Atmosphere;
import night.util.MinecraftInstance;
import night.util.world.PacketAndRenderUtil;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class SkyShaderRenderer implements MinecraftInstance {
   private static final Identifier a = Identifier.fromNamespaceAndPath("night", "core/sky");
   private static final BindGroupLayout b = BindGroupLayout.builder().withUniform("SkySettings", UniformType.UNIFORM_BUFFER).build();
   private static final RenderPipeline c = RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getGlobalsSnippet()})
      .withLocation("night/sky_shader")
      .withVertexShader("core/screenquad")
      .withFragmentShader(a)
      .withBindGroupLayout(b)
      .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
      .withColorTargetState(ColorTargetState.DEFAULT)
      .withCull(false)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final int d = new Std140SizeCalculator()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec2()
      .putFloat()
      .putFloat()
      .putFloat()
      .putInt()
      .align(16)
      .get();
   private static float e = 0.0F;

   public static void a() {
      GpuBuffer var0 = b();
      if (var0 != null) {
         RenderTarget var1 = Minecraft.getInstance().gameRenderer.mainRenderTarget();
         GpuTextureView var2 = PacketAndRenderUtil.a_GpuTextureView_RenderTarget(var1);
         GpuTextureView var3 = PacketAndRenderUtil.b_GpuTextureView_RenderTarget(var1);
         RenderPass var4 = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "Night Sky shader draw", var2, Optional.empty(), var3, OptionalDouble.empty());

         try {
            a(var4, var0);
         } catch (Throwable var8) {
            if (var4 != null) {
               try {
                  var4.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (var4 != null) {
            var4.close();
         }

         var0.close();
      }
   }

   private static void a(RenderPass var0, GpuBuffer var1) {
      PacketAndRenderUtil.a(var0, c);
      var0.setUniform("SkySettings", var1);
      var0.draw(3, 1, 0, 0);
   }

   private static GpuBuffer b() {
      if (mc.level != null && mc.player != null) {
         Atmosphere var0 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
         if (var0 != null && var0.isEnabled() && var0.modifySky.getValue()) {
            if (!var0.skyMode.getValue().equalsIgnoreCase("Custom")) {
               return null;
            }

            int var1 = mc.getWindow().getWidth();
            int var2 = mc.getWindow().getHeight();
            if (var1 > 0 && var2 > 0) {
               float var3 = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
               e = e + var3 * 0.05F * var0.speed.getValue().floatValue();
               int var4 = var0.customSky.getValue().equalsIgnoreCase("Smoke") ? 1 : 0;
               Color var5 = var0.skyTint.getColor();
               float var6 = var5.getRed() / 255.0F;
               float var7 = var5.getGreen() / 255.0F;
               float var8 = var5.getBlue() / 255.0F;
               float var9 = var5.getAlpha() / 255.0F;
               Vector4f var10 = new Vector4f(var6, var7, var8, var9);
               Vector4f var11 = new Vector4f(var6 * 0.1F, var7 * 0.1F, var8 * 0.1F, 1.0F);
               Vector4f var12 = new Vector4f(var6 * 0.8F, var7 * 0.8F, var8 * 0.8F, 1.0F);
               float var13 = 0.0F;
               float var14 = 0.0F;
               float var15 = (float)Math.toRadians(70.0);
               if (mc.gameRenderer != null && mc.gameRenderer.mainCamera() != null) {
                  Camera var16 = mc.gameRenderer.mainCamera();
                  var13 = (float)Math.toRadians(var16.yRot());
                  var14 = (float)Math.toRadians(var16.xRot());
               }

               if (mc.options != null) {
                  var15 = (float)Math.toRadians(((Integer)mc.options.fov().get()).intValue());
               }

               Vector4f var22 = new Vector4f(var13, var14, var15, 0.0F);
               MemoryStack var17 = MemoryStack.stackPush();

               GpuBuffer var19;
               try {
                  ByteBuffer var18 = Std140Builder.onStack(var17, d)
                     .putVec4(var10)
                     .putVec4(var11)
                     .putVec4(var12)
                     .putVec4(var22)
                     .putVec2(var1, var2)
                     .putFloat(e)
                     .putFloat(var0.intensity.getValue().floatValue())
                     .putFloat(var0.speed.getValue().floatValue())
                     .putInt(var4)
                     .get();
                  var19 = RenderSystem.getDevice().createBuffer(() -> "Night Sky shader UBO", 136, var18);
               } catch (Throwable var21) {
                  if (var17 != null) {
                     try {
                        var17.close();
                     } catch (Throwable var20) {
                        var21.addSuppressed(var20);
                     }
                  }

                  throw var21;
               }

               if (var17 != null) {
                  var17.close();
               }

               return var19;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
