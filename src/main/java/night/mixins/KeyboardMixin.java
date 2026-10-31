package night.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import night.Night;
import night.events.impl.KeyInputEvent;
import night.events.impl.WindowResizeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
   @Shadow
   @Final
   private Minecraft minecraft;

   @Inject(method = "keyPress", at = @At("HEAD"))
   private void keyPress(long var1, int var3, KeyEvent var4, CallbackInfo var5) {
      Night.EVENT_BUS.post(new WindowResizeEvent(var4.key(), var4.scancode(), var3, var4.modifiers()));
      if (var1 == this.minecraft.getWindow().handle() && var3 == 1 && this.minecraft.gui.screen() == null) {
         Night.EVENT_BUS.post(new KeyInputEvent(var4.key(), var4.modifiers()));
      }
   }
}
