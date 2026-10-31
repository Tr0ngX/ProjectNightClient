package night.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.module.impl.movement.FastWeb;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public class CobwebBlockMixin implements MinecraftInstance {
   @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
   private void onEntityCollision(BlockState var1, Level var2, BlockPos var3, Entity var4, InsideBlockEffectApplier var5, boolean var6, CallbackInfo var7) {
      if (Night.moduleManager.getModule(FastWeb.class).isEnabled()) {
         if (Night.moduleManager.getModule(FastWeb.class).sneak.getValue() && !mc.player.isShiftKeyDown()) {
            return;
         }

         if (Night.moduleManager.getModule(FastWeb.class).mode.getValue().equalsIgnoreCase("Ignore")) {
            var4.resetFallDistance();
            var7.cancel();
         }

         if (Night.moduleManager.getModule(FastWeb.class).mode.getValue().equalsIgnoreCase("Strong")) {
            var4.makeStuckInBlock(
               var1,
               new Vec3(
                  Night.moduleManager.getModule(FastWeb.class).horizontal.getValue().doubleValue(),
                  Night.moduleManager.getModule(FastWeb.class).vertical.getValue().doubleValue(),
                  Night.moduleManager.getModule(FastWeb.class).horizontal.getValue().doubleValue()
               )
            );
            var7.cancel();
         }
      }
   }
}
