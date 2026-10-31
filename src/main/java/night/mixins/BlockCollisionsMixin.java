package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.block.state.BlockState;
import night.util.world.BlockInteractionUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin {
   @Shadow
   @Final
   private MutableBlockPos pos;

   @ModifyExpressionValue(
      method = "computeNext",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
      )
   )
   private BlockState night$predictPlacedCollision(BlockState var1) {
      if (!var1.isAir()) {
         return var1;
      }

      BlockState var2 = BlockInteractionUtil.b_BlockState_BlockPos(this.pos);
      return var2 != null ? var2 : var1;
   }
}
