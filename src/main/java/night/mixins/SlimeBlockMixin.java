package night.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import night.Night;
import night.module.impl.movement.NoSlow;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeBlock.class)
public class SlimeBlockMixin implements MinecraftInstance {
   @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
   private void onSteppedOn(Level var1, BlockPos var2, BlockState var3, Entity var4, CallbackInfo var5) {
      if (var4 == mc.player && Night.moduleManager.getModule(NoSlow.class).isEnabled() && Night.moduleManager.getModule(NoSlow.class).slimeBlocks.getValue()) {
         var5.cancel();
      }
   }
}
