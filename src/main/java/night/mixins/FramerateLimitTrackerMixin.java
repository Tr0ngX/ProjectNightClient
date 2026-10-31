package night.mixins;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private int framerateLimit;

   @Inject(method = "getThrottleReason", at = @At("HEAD"), cancellable = true)
   private void night$preventThrottleInGui(CallbackInfoReturnable<FramerateThrottleReason> var1) {
      if (this.minecraft != null && this.minecraft.getWindow() != null) {
         if (this.minecraft.getWindow().isIconified()) {
            var1.setReturnValue(FramerateThrottleReason.WINDOW_ICONIFIED);
         } else {
            var1.setReturnValue(FramerateThrottleReason.NONE);
         }
      }
   }
}
