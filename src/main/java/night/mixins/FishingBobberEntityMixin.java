package night.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import night.Night;
import night.module.impl.movement.Velocity;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public class FishingBobberEntityMixin implements MinecraftInstance {
   @WrapOperation(
      method = "handleEntityEvent",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V")
   )
   private void pushOutOfBlocks(FishingHook var1, Entity var2, Operation<Void> var3) {
      if (var2 != mc.player
         || !Night.moduleManager.getModule(Velocity.class).isEnabled()
         || !Night.moduleManager.getModule(Velocity.class).antiFishingRod.getValue()) {
         var3.call(new Object[]{var1, var2});
      }
   }
}
