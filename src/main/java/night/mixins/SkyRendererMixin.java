package night.mixins;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType.Skybox;
import night.Night;
import night.module.impl.visuals.Atmosphere;
import night.util.render.EspShaderRenderer;
import night.util.render.SkyShaderRenderer;
import night.util.render.StarCaptureTarget;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
   @Redirect(
      method = "renderStars",
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;"
      )
   )
   private RenderPass night$redirectStars(
      CommandEncoder var1, Supplier<String> var2, GpuTextureView var3, Optional<Vector4fc> var4, GpuTextureView var5, OptionalDouble var6
   ) {
      Atmosphere var7 = Night.moduleManager.getModule(Atmosphere.class);
      if (var7.isEnabled() && var7.starGlow.getValue()) {
         TextureTarget var8 = StarCaptureTarget.a();
         return var1.createRenderPass(var2, var8.getColorTextureView(), Optional.of(new Vector4f(0.0F, 0.0F, 0.0F, 0.0F)), var5, var6);
      } else {
         return var1.createRenderPass(var2, var3, var4, var5, var6);
      }
   }

   @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
   private void night$resolveStarGlow(PoseStack var1, float var2, float var3, float var4, MoonPhase var5, float var6, float var7, CallbackInfo var8) {
      if (!(var7 <= 0.0F)) {
         this.night$compositeStarGlow();
      }
   }

   private void night$compositeStarGlow() {
      Atmosphere var1 = Night.moduleManager.getModule(Atmosphere.class);
      if (var1.isEnabled() && var1.starGlow.getValue()) {
         TextureTarget var2 = StarCaptureTarget.b();
         if (var2 != null) {
            Minecraft var3 = Minecraft.getInstance();
            PostChain var4 = var3.getShaderManager().getPostChain(Identifier.fromNamespaceAndPath("night", "star_glow"), LevelTargetBundle.MAIN_TARGETS);
            if (var4 != null) {
               EspShaderRenderer.a(var4, var1.starGlowIntensity.getValue().floatValue());
               var4.process(var2, GraphicsResourceAllocator.UNPOOLED);
               var2.blitAndBlendToTexture(
                  var3.gameRenderer.mainRenderTarget().getColorTextureView(), var3.gameRenderer.mainRenderTarget().getDepthTextureView()
               );
            }
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("RETURN"))
   private void night$forceSkybox(ClientLevel var1, float var2, Camera var3, SkyRenderState var4, CallbackInfo var5) {
      Atmosphere var6 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var6 != null && var6.isEnabled() && var6.modifySky.getValue()) {
         var4.skybox = Skybox.OVERWORLD;
      }
   }

   @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
   private void night$renderCustomSkyDisc(int var1, CallbackInfo var2) {
      Atmosphere var3 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var3 != null && var3.isEnabled() && var3.modifySky.getValue() && var3.skyMode.getValue().equalsIgnoreCase("Custom")) {
         SkyShaderRenderer.a();
         var2.cancel();
      }
   }

   @ModifyVariable(method = "renderSkyDisc", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private int night$modifySkyColor(int var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      return var2 != null && var2.isEnabled() && var2.modifySky.getValue() && var2.skyMode.getValue().equalsIgnoreCase("Vanilla")
         ? var2.skyColor.getColor().getRGB()
         : var1;
   }

   @Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
   private void night$cancelDarkDisc(CallbackInfo var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var2 != null && var2.isEnabled() && var2.modifySky.getValue()) {
         var1.cancel();
      }
   }

   @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
   private void night$cancelSunriseAndSunset(CallbackInfo var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var2 != null && var2.isEnabled() && var2.modifySky.getValue()) {
         var1.cancel();
      }
   }

   @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true)
   private void night$cancelSunMoonAndStars(CallbackInfo var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var2 != null && var2.isEnabled() && var2.modifySky.getValue()) {
         Minecraft var3 = Minecraft.getInstance();
         if (var3.level != null && var3.level.dimensionType().skybox() == Skybox.NONE
            || var2.skyMode.getValue().equalsIgnoreCase("Custom") && !var2.starGlow.getValue()) {
            var1.cancel();
         }
      }
   }

   @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
   private void night$renderCustomEndSky(CallbackInfo var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      if (var2 != null && var2.isEnabled() && var2.modifySky.getValue() && var2.skyMode.getValue().equalsIgnoreCase("Custom")) {
         SkyShaderRenderer.a();
         var1.cancel();
      }
   }
}
