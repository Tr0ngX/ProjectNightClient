package night.mixins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {
   @Redirect(
      method = "<clinit>",
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/serialization/codecs/UnboundedMapCodec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
      )
   )
   private static Codec<ItemEnchantments> night$promotePartialEnchantments(
      UnboundedMapCodec<Holder<Enchantment>, Integer> var0,
      Function<Map<Holder<Enchantment>, Integer>, ItemEnchantments> var1,
      Function<ItemEnchantments, Map<Holder<Enchantment>, Integer>> var2
   ) {
      return ((Codec)var0).promotePartial(var0x -> {}).xmap(var1, var2);
   }
}
