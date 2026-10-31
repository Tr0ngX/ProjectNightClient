package night.mixins;

import java.awt.Color;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import night.Night;
import night.module.access.AmbienceAccess;
import night.module.access.FullBrightAccess;
import night.module.impl.visuals.NoRender;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public class LightmapTextureManagerMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void night$render(LightmapRenderState var1, CallbackInfo var2) {
      boolean var3 = var1.needsUpdate;
      FullBrightAccess var4 = (FullBrightAccess)Night.moduleManager.getModuleByName("FullBright");
      if (var4.isEnabled() && var4.getMode().equalsIgnoreCase("Gamma")) {
         var1.blockFactor = 1.0F;
         var1.skyFactor = 1.0F;
         var1.brightness = 1.0F;
         var1.nightVisionEffectIntensity = 1.0F;
         var1.darknessEffectScale = 0.0F;
         var1.bossOverlayWorldDarkening = 0.0F;
         var1.needsUpdate = true;
      }

      NoRender var5 = Night.moduleManager.getModule(NoRender.class);
      if (var5.isEnabled() && var5.blindness.getValue()) {
         var1.darknessEffectScale = 0.0F;
         var1.needsUpdate = true;
      }

      AmbienceAccess var6 = Night.moduleManager != null ? (AmbienceAccess)Night.moduleManager.getModuleByName("Ambience") : null;
      if (var6 != null && var6.isEnabled()) {
         Color var7 = var6.getAmbienceColor();
         float var8 = var7.getRed() / 255.0F;
         float var9 = var7.getGreen() / 255.0F;
         float var10 = var7.getBlue() / 255.0F;
         float var11 = var7.getAlpha() / 255.0F;
         if (var6.isLightmapEnabled() && var3) {
            if (var6.isSkyEnabled()) {
               Vector3fc var12 = var1.skyLightColor;
               var1.skyLightColor = new Vector3f(
                  var12.x() * (1.0F - var11) + var8 * var11, var12.y() * (1.0F - var11) + var9 * var11, var12.z() * (1.0F - var11) + var10 * var11
               );
               Vector3fc var13 = var1.ambientColor;
               var1.ambientColor = new Vector3f(
                  var13.x() * (1.0F - var11) + var8 * var11, var13.y() * (1.0F - var11) + var9 * var11, var13.z() * (1.0F - var11) + var10 * var11
               );
            }

            if (var6.isBlocksEnabled()) {
               Vector3fc var14 = var1.blockLightTint;
               var1.blockLightTint = new Vector3f(
                  var14.x() * (1.0F - var11) + var8 * var11, var14.y() * (1.0F - var11) + var9 * var11, var14.z() * (1.0F - var11) + var10 * var11
               );
            }
         }
      }
   }
}
