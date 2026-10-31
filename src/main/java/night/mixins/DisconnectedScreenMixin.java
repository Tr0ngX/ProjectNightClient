package night.mixins;

import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import night.Night;
import night.gui.screen.AccountsButtonHelper;
import night.module.impl.misc.AutoReconnect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen {
   @Shadow
   @Final
   private LinearLayout layout;
   @Unique
   private Button toggleButton;
   @Unique
   private Button button;
   @Unique
   private double time = 100.0;

   protected DisconnectedScreenMixin(Component var1) {
      super(var1);
   }

   @Unique
   private static AutoReconnect autoReconnect() {
      return Night.moduleManager.getModule(AutoReconnect.class);
   }

   @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift = Shift.BEFORE))
   private void init(CallbackInfo var1) {
      if (Night.serverManager.getLastServerConnection() != null) {
         this.button = new Builder(Component.literal(this.getText()), var1x -> this.tryConnecting()).width(200).build();
         this.toggleButton = new Builder(
               Component.literal(
                  "Toggle " + (autoReconnect() != null && autoReconnect().isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED) + "AutoReconnect"
               ),
               var1x -> {
                  AutoReconnect var2 = autoReconnect();
                  if (var2 != null) {
                     var2.setEnabled(!var2.isEnabled(), false);
                     this.toggleButton
                        .setMessage(Component.literal("Toggle " + (var2.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED) + "AutoReconnect"));
                     this.button.setMessage(Component.literal(this.getText()));
                     this.time = var2.delay.getValue().intValue() * 20;
                  }
               }
            )
            .width(200)
            .build();
         this.layout.addChild(this.button);
         this.layout.addChild(this.toggleButton);
      }

      this.addRenderableWidget(AccountsButtonHelper.a(8, 8, 75, 20, this));
   }

   public void tick() {
      AutoReconnect var1 = autoReconnect();
      if (var1 != null && var1.isEnabled() && Night.serverManager.getLastServerConnection() != null) {
         if (this.time <= 0.0) {
            this.tryConnecting();
         } else {
            this.time--;
            if (this.button != null) {
               this.button.setMessage(Component.literal(this.getText()));
            }
         }
      }
   }

   @Unique
   private String getText() {
      String var1 = "Reconnect";
      AutoReconnect var2 = autoReconnect();
      if (var2 != null && var2.isEnabled()) {
         var1 = var1 + " " + String.format("(" + ChatFormatting.GREEN + "%.1fs" + ChatFormatting.RESET + ")", this.time / 20.0);
      }

      return var1;
   }

   @Unique
   private void tryConnecting() {
      ConnectScreen.startConnecting(
         new TitleScreen(),
         Minecraft.getInstance(),
         (ServerAddress)Night.serverManager.getLastServerConnection().left(),
         (ServerData)Night.serverManager.getLastServerConnection().right(),
         false,
         new TransferState(Map.of(), Map.of(), false)
      );
   }
}
