package night.mixins;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import night.Night;
import night.mixins.accessors.GuiTextRenderStateAccessor;
import night.module.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {
   @Shadow
   private int firstStratumAfterBlur;

   @Inject(method = "blurBeforeThisStratum", at = @At("HEAD"), cancellable = true)
   private void night$noopIfAlreadyBlurredThisFrame(CallbackInfo var1) {
      if (this.firstStratumAfterBlur != Integer.MAX_VALUE) {
         var1.cancel();
      }
   }

   @Inject(method = "addText", at = @At("HEAD"), cancellable = true)
   private void night$globalFont(GuiTextRenderState var1, CallbackInfo var2) {
      if (Night.moduleManager != null) {
         FontModule var3 = Night.moduleManager.getModule(FontModule.class);
         if (var3 != null && var3.isEnabled() && var3.customFont.getValue() && var3.global.getValue()) {
            if (Night.fontManager != null && Night.fontManager.getCustomFont() != null) {
               GuiTextRenderStateAccessor var4 = (GuiTextRenderStateAccessor)(Object)var1;
               GuiRenderState var5 = (GuiRenderState)(Object)this;
               if (var4.isDropShadow()) {
                  Night.fontManager
                     .getCustomFont()
                     .a(var5, var4.getPose(), var4.getScissor(), var4.getText(), var4.getX() + 1.0F, var4.getY() + 1.0F, var4.getColor(), true);
               }

               Night.fontManager.getCustomFont().a(var5, var4.getPose(), var4.getScissor(), var4.getText(), var4.getX(), var4.getY(), var4.getColor(), false);
               var2.cancel();
            }
         }
      }
   }
}
