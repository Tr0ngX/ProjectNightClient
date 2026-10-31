package night.mixins;

import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
   @Shadow
   private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;

   @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
   private <T extends Entity> void night$safeShouldRender(T var1, Frustum var2, double var3, double var5, double var7, CallbackInfoReturnable<Boolean> var9) {
      if (var1 != null && var1.getType() != null) {
         if (!(var1 instanceof AbstractClientPlayer) && (this.renderers == null || !this.renderers.containsKey(var1.getType()))) {
            var9.setReturnValue(false);
         }
      } else {
         var9.setReturnValue(false);
      }
   }
}
