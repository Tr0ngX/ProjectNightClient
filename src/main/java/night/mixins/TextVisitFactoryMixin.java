package night.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import night.util.MinecraftInstance;
import night.util.color.SpecialChatFormatting;
import night.util.color.TextFormattingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StringDecomposer.class)
public class TextVisitFactoryMixin implements MinecraftInstance {
   @WrapOperation(
      method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
      at = @At(value = "INVOKE", target = "Ljava/lang/String;charAt(I)C", ordinal = 1)
   )
   private static char visitFormatted(String var0, int var1, Operation<Character> var2, @Local(ordinal = 2) LocalRef<Style> var3) {
      SpecialChatFormatting var4 = SpecialChatFormatting.a(var0.charAt(var1));
      if (var4 != null) {
         var3.set(TextFormattingUtil.a_Style_Style_SpecialChatFormatting((Style)var3.get(), var4));
      }

      return (Character)var2.call(new Object[]{var0, var1});
   }
}
