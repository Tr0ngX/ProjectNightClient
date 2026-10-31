package night.mixins.accessors;

import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextColor.class)
public interface TextColorAccessor {
   @Invoker("<init>")
   static TextColor create(int var0, String var1) {
      throw new AssertionError();
   }
}
