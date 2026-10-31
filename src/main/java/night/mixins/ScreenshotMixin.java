package night.mixins;

import java.util.function.Consumer;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import night.Night;
import night.module.impl.misc.ScreenshotModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
   @ModifyVariable(
      method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private static Consumer<Component> night$wrapScreenshotConsumer(Consumer<Component> var0) {
      return var1 -> {
         ScreenshotModule var2 = Night.moduleManager != null ? Night.moduleManager.getModule(ScreenshotModule.class) : null;
         boolean var3 = var2 != null && var2.isEnabled();
         if (!var3) {
            var0.accept(var1);
         }

         try {
            if (var3) {
               var2.a_void_Component(var1);
            }
         } catch (Throwable var5) {
            Night.LOGGER.error("Failed to process screenshot callback in ScreenshotModule", var5);
         }
      };
   }
}
