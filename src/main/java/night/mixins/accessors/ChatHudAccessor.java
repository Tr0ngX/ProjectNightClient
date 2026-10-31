package night.mixins.accessors;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatHudAccessor {
   @Accessor("allMessages")
   List<GuiMessage> getMessages();

   @Accessor("trimmedMessages")
   List<Line> getVisibleMessages();

   @Accessor("chatScrollbarPos")
   int getScrolledLines();

   @Accessor("newMessageSinceScroll")
   void setHasUnreadNewMessages(boolean var1);

   @Invoker("logChatMessage")
   void invokeLogChatMessage(GuiMessage var1);

   @Invoker("addMessageToDisplayQueue")
   void invokeAddVisibleMessage(GuiMessage var1);

   @Invoker("addMessageToQueue")
   void invokeAddMessage(GuiMessage var1);

   @Invoker("getWidth")
   int invokeGetWidth();

   @Invoker("getScale")
   double invokeGetScale();
}
