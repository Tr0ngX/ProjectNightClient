package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Set;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import night.Night;
import night.module.impl.visuals.Atmosphere;
import night.module.impl.visuals.BlockHighlight;
import night.module.impl.visuals.ShadersModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
   @Shadow
   @Final
   private RenderTarget entityOutlineTarget;

   @ModifyReturnValue(method = "entityOutlineTarget", at = @At("RETURN"))
   private RenderTarget night$outlineTargetOutsideFrameGraph(RenderTarget var1) {
      return var1 != null ? var1 : this.entityOutlineTarget;
   }

   @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private boolean renderOutline(boolean var1) {
      return Night.moduleManager != null && Night.moduleManager.getModule(BlockHighlight.class).isEnabled() ? false : var1;
   }

   @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 1)
   private boolean night$forceShouldRenderSky(boolean var1) {
      Atmosphere var2 = Night.moduleManager != null ? Night.moduleManager.getModule(Atmosphere.class) : null;
      return var2 != null && var2.isEnabled() && var2.modifySky.getValue() ? true : var1;
   }

   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"
      )
   )
   private PostChain night$interceptEntityOutlineChain(ShaderManager var1, Identifier var2, Set<Identifier> var3) {
      return var2.equals(Identifier.withDefaultNamespace("entity_outline")) && ShadersModule.x() != null ? null : var1.getPostChain(var2, var3);
   }
}
