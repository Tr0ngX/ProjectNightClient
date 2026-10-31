package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.module.impl.visuals.Chams;
import night.module.impl.visuals.ShadersModule;
import night.util.duck.ChamsRenderStateDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
   @Inject(
      method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
      at = @At("TAIL")
   )
   private void night$forceChamsOverlayLayers(Avatar var1, AvatarRenderState var2, float var3, CallbackInfo var4) {
      if (var1 instanceof Entity) {
         Chams var5 = Night.moduleManager.getModule(Chams.class);
         if (var5 != null && var5.isEnabled() && var5.players.getValue() && var5.b_bool_Entity(var1)) {
            var2.showHat = true;
            var2.showJacket = true;
            var2.showLeftSleeve = true;
            var2.showRightSleeve = true;
            var2.showLeftPants = true;
            var2.showRightPants = true;
         }
      }
   }

   @Redirect(
      method = "renderHand",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"
      ),
      require = 0
   )
   private void night$handOutline(SubmitNodeCollector var1, ModelPart var2, PoseStack var3, RenderType var4, int var5, int var6, TextureAtlasSprite var7) {
      var1.submitModelPart(var2, var3, var4, var5, var6, var7, -1, null, night$outlineColor());
   }

   private static int night$outlineColor() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0.player == null) {
         return 0;
      }

      ShadersModule var1 = Night.moduleManager.getModule(ShadersModule.class);
      return var1.isEnabled() && var1.b_bool_Entity(var0.player) ? var1.d(var0.player).getRGB() | 0xFF000000 : 0;
   }

   @Inject(
      method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;",
      at = @At("RETURN"),
      cancellable = true
   )
   private void night$applyPopChamYOffset(AvatarRenderState var1, CallbackInfoReturnable<Vec3> var2) {
      float var3 = ((ChamsRenderStateDuck)var1).night$chamsYOffset();
      if (var3 != 0.0F) {
         Vec3 var4 = (Vec3)var2.getReturnValue();
         var2.setReturnValue(var4 == null ? new Vec3(0.0, var3, 0.0) : var4.add(0.0, var3, 0.0));
      }
   }
}
