package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import night.Night;
import night.module.impl.visuals.NoRender;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin implements MinecraftInstance {
   @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
   private void night$submit(BlockEntityRenderState var1, PoseStack var2, SubmitNodeCollector var3, CameraRenderState var4, CallbackInfo var5) {
      NoRender var6 = Night.moduleManager.getModule(NoRender.class);
      if (var6.isEnabled()
         && !var6.z.getValue().equals("Never")
         && (
            var6.z.getValue().equals("Always")
               || var6.z.getValue().equals("Distance")
                  && Math.sqrt(mc.player.distanceToSqr(var1.blockPos.getX(), var1.blockPos.getY(), var1.blockPos.getZ()))
                     > var6.tileDistance.getValue().floatValue()
         )) {
         var5.cancel();
      }
   }
}
