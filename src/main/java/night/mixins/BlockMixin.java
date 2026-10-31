package night.mixins;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import night.Night;
import night.module.impl.movement.NoSlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
   @Inject(method = "getFriction", at = @At("HEAD"), cancellable = true)
   private void getSlipperiness(CallbackInfoReturnable<Float> var1) {
      if ((Object)(Object)this == Blocks.SLIME_BLOCK
         && Night.moduleManager.getModule(NoSlow.class).isEnabled()
         && Night.moduleManager.getModule(NoSlow.class).slimeBlocks.getValue()) {
         var1.setReturnValue(0.6F);
      }
   }
}
