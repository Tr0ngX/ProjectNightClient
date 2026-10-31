package night.mixins.accessors;

import javax.annotation.Nullable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Style.class)
public interface StyleAccessor {
   @Invoker("<init>")
   static Style create(
      @Nullable TextColor var0,
      @Nullable Integer var1,
      @Nullable Boolean var2,
      @Nullable Boolean var3,
      @Nullable Boolean var4,
      @Nullable Boolean var5,
      @Nullable Boolean var6,
      @Nullable ClickEvent var7,
      @Nullable HoverEvent var8,
      @Nullable String var9,
      @Nullable FontDescription var10
   ) {
      return null;
   }
}
