package night.mixins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.network.chat.Style$Serializer")
public abstract class StyleSerializerMixin {
   @Redirect(
      method = "lambda$static$0",
      at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;optionalFieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;")
   )
   private static <A> MapCodec<Optional<A>> night$lenientOptionalField(Codec<A> var0, String var1) {
      return !"hover_event".equals(var1) && !"click_event".equals(var1) ? var0.optionalFieldOf(var1) : var0.lenientOptionalFieldOf(var1);
   }
}
