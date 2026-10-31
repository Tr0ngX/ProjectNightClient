package night.mixins;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import night.gui.screen.AccountsButtonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JoinMultiplayerScreen.class, priority = 10000)
public abstract class JoinMultiplayerScreenMixin extends Screen {
   protected JoinMultiplayerScreenMixin(Component var1) {
      super(var1);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void init(CallbackInfo var1) {
      byte var2 = 20;
      AbstractWidget var3 = null;

      for (GuiEventListener var5 : (Iterable<GuiEventListener>)(Object)this.children()) {
         if (var5 instanceof AbstractWidget var6) {
            String var7 = var6.getClass().getName().toLowerCase();
            String var8 = var6.getMessage() != null ? var6.getMessage().getString().toLowerCase() : "";
            if (var7.contains("viafabric") || var8.contains("viafabric") || var8.contains("viaversion")) {
               var3 = var6;
               break;
            }
         }
      }

      int var9;
      int var10;
      if (var3 != null) {
         var9 = var3.getX() - 75 - 4;
         var10 = var3.getY();
         var2 = (byte)(var3.getHeight() > 0 ? var3.getHeight() : 20);
      } else {
         var9 = this.width - 75 - 6;
         var10 = 5;
      }

      this.addRenderableWidget(AccountsButtonHelper.a(var9, var10, 75, var2, this));
   }
}
