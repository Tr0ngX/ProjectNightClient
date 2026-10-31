package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.module.impl.misc.BetterChat;
import night.util.animation.EasingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess")
public class ChatComponentDrawingBackgroundMixin {
   @Unique
   private int night$xOffset = 0;

   @Inject(method = "handleMessage", at = @At("HEAD"))
   private void night$computeOffset(int var1, float var2, FormattedCharSequence var3, CallbackInfoReturnable<Boolean> var4) {
      this.night$xOffset = 0;
      BetterChat var5 = Night.moduleManager.getModule(BetterChat.class);
      if (var5.isEnabled() && var5.animation2.getValue()) {
         Long var6 = var5.a(var3);
         if (var6 != null) {
            int var7 = var5.delay.getValue().intValue();
            float var8 = var7 <= 0 ? 1.0F : EasingUtil.a(var6, var7);
            if (!(var8 >= 1.0F)) {
               float var9 = Minecraft.getInstance().font.width(var3);
               this.night$xOffset = Math.round(-var9 * (1.0F - var8));
            }
         }
      }
   }

   @ModifyArg(
      method = "handleMessage",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V"
      ),
      index = 1
   )
   private int night$slideInX(int var1) {
      return var1 + this.night$xOffset;
   }
}
