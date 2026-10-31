package night.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.events.impl.ChatSendEvent;
import night.events.impl.CommandExecuteEvent;
import night.module.impl.core.HudModule;
import night.module.impl.core.IRCModule;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
   @Shadow
   protected EditBox input;

   protected ChatScreenMixin(Component var1) {
      super(var1);
   }

   @Inject(
      method = "handleChatInput",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"),
      cancellable = true
   )
   private void sendMessage(String var1, boolean var2, CallbackInfo var3) {
      ChatSendEvent var4 = new ChatSendEvent(var1);
      Night.EVENT_BUS.post(var4);
      if (var4.isCancelled()) {
         var3.cancel();
      }
   }

   @Inject(
      method = "handleChatInput",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V"),
      cancellable = true
   )
   private void sendCommand(String var1, boolean var2, CallbackInfo var3) {
      CommandExecuteEvent var4 = new CommandExecuteEvent(var1);
      Night.EVENT_BUS.post(var4);
      if (var4.isCancelled()) {
         var3.cancel();
      }
   }

   @WrapWithCondition(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
   private boolean render(GuiGraphicsExtractor var1, int var2, int var3, int var4, int var5, int var6) {
      return !Night.moduleManager.getModule(HudModule.class).isEnabled();
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void night$renderIrcOutline(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (this.input != null) {
         String var6 = this.input.getValue();
         IRCModule var7 = Night.moduleManager.getModule(IRCModule.class);
         if (var7 != null && var7.isEnabled()) {
            String var8 = var7.prefix.getValue();
            if (var8 == null || var8.isEmpty()) {
               var8 = "$";
            }

            if (var6 != null && var6.startsWith(var8)) {
               int var9 = this.input.getX() - 2;
               int var10 = this.input.getY() - 2;
               int var11 = this.input.getX() + this.input.getWidth() + 2;
               int var12 = this.input.getY() + this.input.getHeight() + 2;
               Color var13 = ColorUtil.d();
               Render2DUtil.b(var1, var9 - 1.0F, var10 - 1.0F, var11 + 1.0F, var12 + 1.0F, new Color(var13.getRed(), var13.getGreen(), var13.getBlue(), 90));
               Render2DUtil.b(var1, var9, var10, var11, var12, var13);
               Render2DUtil.a(var1, var9, var10, var11, var12, new Color(var13.getRed(), var13.getGreen(), var13.getBlue(), 35));
            }
         }
      }
   }
}
