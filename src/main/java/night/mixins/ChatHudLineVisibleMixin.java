package night.mixins;

import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import night.util.duck.GuiMessageLineDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Line.class)
public class ChatHudLineVisibleMixin implements GuiMessageLineDuck {
   @Unique
   private boolean clientMessage = false;
   @Unique
   private String clientIdentifier = "";

   @Override
   public boolean night$isClientMessage() {
      return this.clientMessage;
   }

   @Override
   public void night$setClientMessage(boolean var1) {
      this.clientMessage = var1;
   }

   @Override
   public String night$getClientIdentifier() {
      return this.clientIdentifier;
   }

   @Override
   public void night$setClientIdentifier(String var1) {
      this.clientIdentifier = var1;
   }
}
