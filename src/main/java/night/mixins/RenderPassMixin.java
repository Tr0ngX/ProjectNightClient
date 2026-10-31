package night.mixins;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.RenderPass.RenderArea;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPass.class)
public abstract class RenderPassMixin {
   @Shadow
   @Final
   private RenderArea renderArea;
   @Shadow
   @Final
   private RenderPassBackend backend;

   @Shadow
   public abstract void disableScissor();

   @Inject(method = "enableScissor", at = @At("HEAD"), cancellable = true)
   private void night$clampScissorToBounds(int var1, int var2, int var3, int var4, CallbackInfo var5) {
      if (this.renderArea != null) {
         int var6 = this.renderArea.x();
         int var7 = this.renderArea.y();
         int var8 = this.renderArea.width();
         int var9 = this.renderArea.height();
         int var10 = var1;
         int var11 = var2;
         int var12 = var1 + var3;
         int var13 = var2 + var4;
         if (var10 < var6) {
            var10 = var6;
         }

         if (var11 < var7) {
            var11 = var7;
         }

         if (var12 > var6 + var8) {
            var12 = var6 + var8;
         }

         if (var13 > var7 + var9) {
            var13 = var7 + var9;
         }

         int var14 = var12 - var10;
         int var15 = var13 - var11;
         if (var14 > 0 && var15 > 0) {
            if (var10 != var1 || var11 != var2 || var14 != var3 || var15 != var4) {
               this.backend.enableScissor(var10, var11, var14, var15);
               var5.cancel();
            }
         } else {
            this.disableScissor();
            var5.cancel();
         }
      }
   }
}
