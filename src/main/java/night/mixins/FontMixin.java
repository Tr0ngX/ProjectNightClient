package night.mixins;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.module.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontMixin {
   @Inject(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
   private void width$String(String var1, CallbackInfoReturnable<Integer> var2) {
      if (Night.moduleManager != null) {
         FontModule var3 = Night.moduleManager.getModule(FontModule.class);
         if (var3 != null && var3.isEnabled() && var3.customFont.getValue() && var3.global.getValue() && Night.fontManager.getCustomFont() != null) {
            var2.setReturnValue((int)Night.fontManager.getCustomFont().a_float_String(var1));
         }
      }
   }

   @Inject(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), cancellable = true)
   private void width$FormattedText(FormattedText var1, CallbackInfoReturnable<Integer> var2) {
      if (Night.moduleManager != null) {
         FontModule var3 = Night.moduleManager.getModule(FontModule.class);
         if (var3 != null && var3.isEnabled() && var3.customFont.getValue() && var3.global.getValue() && Night.fontManager.getCustomFont() != null) {
            var2.setReturnValue((int)Night.fontManager.getCustomFont().a_float_String(var1.getString()));
         }
      }
   }

   @Inject(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), cancellable = true)
   private void width$FormattedCharSequence(FormattedCharSequence var1, CallbackInfoReturnable<Integer> var2) {
      if (Night.moduleManager != null) {
         FontModule var3 = Night.moduleManager.getModule(FontModule.class);
         if (var3 != null && var3.isEnabled() && var3.customFont.getValue() && var3.global.getValue() && Night.fontManager.getCustomFont() != null) {
            var2.setReturnValue((int)Night.fontManager.getCustomFont().a_float_FormattedCharSequence(var1));
         }
      }
   }
}
