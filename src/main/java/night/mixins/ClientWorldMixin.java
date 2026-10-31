package night.mixins;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Objects;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import night.Night;
import night.events.impl.BlockBreakProgressEvent;
import night.events.impl.EntitySpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientWorldMixin {
   @Inject(method = "addEntity", at = @At("HEAD"))
   private void addEntity(Entity var1, CallbackInfo var2) {
      Night.EVENT_BUS.post(new EntitySpawnEvent(var1));
   }

   @ModifyReturnValue(method = "entitiesForRendering", at = @At("RETURN"))
   private Iterable<Entity> night$skipNullEntities(Iterable<Entity> var1) {
      return Iterables.filter(var1, Objects::nonNull);
   }

   @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
   private void destroyBlockProgress(int var1, BlockPos var2, int var3, CallbackInfo var4) {
      if (var3 >= 0 && var3 <= 9) {
         Night.EVENT_BUS.post(new BlockBreakProgressEvent(var1, var2));
      }
   }
}
