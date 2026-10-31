package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.impl.Render3DEvent;
import night.mixins.accessors.LevelRendererAccessor;
import night.module.impl.visuals.NoRender;
import night.module.impl.visuals.ShadersModule;
import night.util.render.EspShaderRenderer;
import night.util.render.GlintRenderLayer;
import night.util.render.Render2DUtil;
import night.util.render.Render3DUtil;
import night.util.render.TextGlowRenderer;
import night.util.world.WorldLoadState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
   private static final String NIGHT_LEVEL_RENDER = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V";
   private static ProjectionMatrixBuffer night$noBobProjectionBuffer;
   private static final List<Render3DUtil.BufferedPrimitive> night$emptyQuads = new ArrayList<>();

   @Inject(method = "renderLevel", at = @At("HEAD"))
   private void renderWorld$HEAD(DeltaTracker var1, CallbackInfo var2) {
      Render3DUtil.a();
   }

   @Inject(
      method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V")
   )
   private void night$resolveOutline(DeltaTracker var1, boolean var2, CallbackInfo var3) {
      boolean var4 = WorldLoadState.b;
      WorldLoadState.b = false;
      Identifier var5 = ShadersModule.x();
      if (var5 != null) {
         Minecraft var6 = Minecraft.getInstance();
         if (var6.levelRenderer != null) {
            RenderTarget var7 = ((LevelRendererAccessor)var6.levelRenderer).night$getEntityOutlineTarget();
            if (var7 != null) {
               PostChain var8 = var6.getShaderManager().getPostChain(var5, LevelTargetBundle.MAIN_TARGETS);
               if (var8 != null) {
                  EspShaderRenderer.a(var8, ShadersModule.y(), ShadersModule.z());
                  var8.process(var7, GraphicsResourceAllocator.UNPOOLED);
               }
            }
         }
      }
   }

   @Inject(
      method = "renderLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
         shift = Shift.AFTER
      )
   )
   private void renderWorld$swap(DeltaTracker var1, CallbackInfo var2, @Local(ordinal = 0) Matrix4fc var3, @Local CameraRenderState var4) {
      this.night$renderWorldBody(var1.getGameTimeDeltaPartialTick(false), var3, var4);
   }

   private void night$renderWorldBody(float var1, Matrix4fc var2, CameraRenderState var3) {
      RenderSystem.getModelViewStack().pushMatrix();
      RenderSystem.getModelViewStack().mul(var2);
      PoseStack var4 = new PoseStack();
      Night.EVENT_BUS.post(new Render3DEvent(var4, var1));
      if (!Render3DUtil.b.isEmpty() || !Render3DUtil.c.isEmpty()) {
         Render3DUtil.a_void_List_List_bool(Render3DUtil.b, Render3DUtil.c, false);
      }

      if (!Render3DUtil.d.isEmpty() || !Render3DUtil.e.isEmpty()) {
         Render3DUtil.a_void_List_List_bool(Render3DUtil.d, Render3DUtil.e, true);
      }

      if (!Render3DUtil.f.isEmpty() || !Render3DUtil.g.isEmpty()) {
         EspShaderRenderer.a(Render3DUtil.f, Render3DUtil.g);
      }

      if (!GlintRenderLayer.a.isEmpty()) {
         GlintRenderLayer.b();
      }

      Night.EVENT_BUS.post(new Render3DEvent.Post(var4, var1));
      if (!Render3DUtil.b.isEmpty() || !Render3DUtil.c.isEmpty()) {
         Render3DUtil.a_void_List_List_bool(Render3DUtil.b, Render3DUtil.c, false);
      }

      if (!Render3DUtil.d.isEmpty() || !Render3DUtil.e.isEmpty()) {
         Render3DUtil.a_void_List_List_bool(Render3DUtil.d, Render3DUtil.e, true);
      }

      if (!Render3DUtil.f.isEmpty() || !Render3DUtil.g.isEmpty()) {
         EspShaderRenderer.a(Render3DUtil.f, Render3DUtil.g);
      }

      if (!GlintRenderLayer.a.isEmpty()) {
         GlintRenderLayer.b();
      }

      TextGlowRenderer.a();
      if (!Render3DUtil.h.isEmpty()) {
         if (night$noBobProjectionBuffer == null) {
            night$noBobProjectionBuffer = new ProjectionMatrixBuffer("night_no_bob_tracer");
         }

         RenderSystem.backupProjectionMatrix();
         RenderSystem.setProjectionMatrix(night$noBobProjectionBuffer.getBuffer(var3.projectionMatrix), ProjectionType.PERSPECTIVE);
         Render3DUtil.a_void_List_List_bool(night$emptyQuads, Render3DUtil.h, false);
         RenderSystem.restoreProjectionMatrix();
      }

      RenderSystem.getModelViewStack().popMatrix();
   }

   @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
   private void tiltViewWhenHurt(CameraRenderState var1, PoseStack var2, CallbackInfo var3) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).hurtCamera.getValue()) {
         var3.cancel();
      }
   }

   @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
   private void showFloatingItem(ItemStack var1, CallbackInfo var2) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).totemAnimation.getValue()) {
         var2.cancel();
      }
   }

   @Inject(
      method = "renderLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
         shift = Shift.AFTER
      )
   )
   private void renderWorld(DeltaTracker var1, CallbackInfo var2, @Local(ordinal = 0) Matrix4fc var3, @Local(ordinal = 0) Matrix4f var4) {
      this.night$captureMatrices(var3, var4);
   }

   private void night$captureMatrices(Matrix4fc var1, Matrix4f var2) {
      PoseStack var3 = new PoseStack();
      var3.last().pose().mul(var1);
      Render2DUtil.a.set(var2);
      Render2DUtil.b.set(RenderSystem.getModelViewMatrixCopy());
      Render2DUtil.c.set(var3.last().pose());
   }
}
