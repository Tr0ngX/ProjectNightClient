package night.mixins;

import net.minecraft.server.level.BlockDestructionProgress;
import night.Night;
import night.events.impl.BlockBreakProgressEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockDestructionProgress.class)
public class BlockBreakingInfoMixin {
   @Inject(method = "setProgress", at = @At("HEAD"))
   private void setProgress(int var1, CallbackInfo var2) {
      BlockDestructionProgress var3 = (BlockDestructionProgress)(Object)this;
      if (var1 >= 0 && var1 <= 9) {
         Night.EVENT_BUS.post(new BlockBreakProgressEvent(var3.getId(), var3.getPos()));
      }
   }
}
