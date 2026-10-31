package night.mixins;

import net.minecraft.client.multiplayer.ClientExplosionTracker;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.module.impl.visuals.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientExplosionTracker.class)
public class ClientExplosionTrackerMixin {
   @Inject(method = "track", at = @At("HEAD"), cancellable = true)
   private void night$cancelDebris(Vec3 var1, float var2, int var3, WeightedList<ExplosionParticleInfo> var4, CallbackInfo var5) {
      NoRender var6 = Night.moduleManager.getModule(NoRender.class);
      if (var6.isEnabled() && var6.explosions.getValue()) {
         var5.cancel();
      }
   }
}
