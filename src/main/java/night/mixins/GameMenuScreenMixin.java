package night.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.module.impl.core.PingBypassModule;
import night.pingbypass.PingBypassState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
   protected GameMenuScreenMixin(Component var1) {
      super(var1);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void init(CallbackInfo var1) {
      if (PingBypassState.d) {
         this.addRenderableWidget(Button.builder(Component.literal("\u00a7cDisconnect Proxy"), var0 -> {
            PingBypassModule var1x = Night.moduleManager.getModule(PingBypassModule.class);
            if (var1x != null && var1x.isEnabled()) {
               var1x.setEnabled(false);
            }
         }).bounds(this.width / 2 - 102, this.height - 40, 204, 20).build());
      }
   }
}
