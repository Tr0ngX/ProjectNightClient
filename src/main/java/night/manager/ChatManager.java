package night.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.mixins.accessors.ChatHudAccessor;
import night.module.impl.core.CommandsModule;
import night.module.impl.misc.BetterChat;
import night.util.MinecraftInstance;
import night.util.color.ChatColorUtil;
import night.util.color.TextFormattingUtil;
import night.util.duck.GuiMessageDuck;
import night.util.duck.GuiMessageLineDuck;

public class ChatManager implements MinecraftInstance {
   public static boolean suppressSend = false;
   private final List<String> queuedMessages = new ArrayList<>();

   public ChatManager() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (mc.player != null && mc.gui != null) {
         if (!this.queuedMessages.isEmpty()) {
            List var2 = this.queuedMessages;

            for (String var4 : (Iterable<String>)(Object)new ArrayList(var2)) {
               this.addRawClientMessage(var4);
               List var5 = this.queuedMessages;
               String var6 = var4;
               var5.remove(var6);
            }
         }
      }
   }

   public void sendMessage(String var1) {
      if (mc.player != null && mc.gui != null) {
         String var2 = this.a();
         String var3 = String.valueOf(ChatColorUtil.b());
         this.addRawClientMessage(var2 + " " + var3 + var1);
      }
   }

   public void sendReplaceableMessage(String var1, String var2) {
      if (mc.player != null && mc.gui != null) {
         removeMessagesByIdentifier(var2);
         String var3 = this.a();
         String var4 = String.valueOf(ChatColorUtil.b());
         this.addIdentifiedClientMessage(var3 + " " + var4 + var1, var2);
      }
   }

   public void sendTaggedMessage(String var1, String var2) {
      if (mc.player != null && mc.gui != null) {
         String var3 = this.a();
         String var4 = String.valueOf(ChatFormatting.DARK_AQUA);
         String var5 = String.valueOf(ChatFormatting.AQUA);
         String var6 = String.valueOf(ChatFormatting.DARK_AQUA);
         String var7 = String.valueOf(ChatColorUtil.b());
         this.addRawClientMessage(var3 + var4 + " [" + var5 + var2 + var6 + "]: " + var7 + var1);
      }
   }

   public void sendReplaceableTaggedMessage(String var1, String var2, String var3) {
      if (mc.player != null && mc.gui != null) {
         removeMessagesByIdentifier(var3);
         String var4 = this.a();
         String var5 = String.valueOf(ChatFormatting.DARK_AQUA);
         String var6 = String.valueOf(ChatFormatting.AQUA);
         String var7 = String.valueOf(ChatFormatting.DARK_AQUA);
         String var8 = String.valueOf(ChatColorUtil.b());
         this.addIdentifiedClientMessage(var4 + var5 + " [" + var6 + var2 + var7 + "]: " + var8 + var1, var3);
      }
   }

   public void sendInfo(String var1) {
      if (mc.player != null && mc.gui != null) {
         String var2 = this.a();
         String var3 = String.valueOf(ChatFormatting.DARK_BLUE);
         String var4 = String.valueOf(ChatFormatting.BLUE);
         String var5 = String.valueOf(ChatFormatting.DARK_BLUE);
         String var6 = String.valueOf(ChatColorUtil.b());
         this.addRawClientMessage(var2 + var3 + " [" + var4 + "?" + var5 + "] " + var6 + var1);
      }
   }

   public void sendWarning(String var1) {
      if (mc.player != null && mc.gui != null) {
         String var2 = this.a();
         String var3 = String.valueOf(ChatFormatting.GOLD);
         String var4 = String.valueOf(ChatFormatting.YELLOW);
         String var5 = String.valueOf(ChatFormatting.GOLD);
         String var6 = String.valueOf(ChatColorUtil.b());
         this.addRawClientMessage(var2 + var3 + " [" + var4 + "!" + var5 + "] " + var6 + var1);
      }
   }

   public void sendError(String var1) {
      if (mc.player != null && mc.gui != null) {
         String var2 = this.a();
         String var3 = String.valueOf(ChatFormatting.DARK_RED);
         String var4 = String.valueOf(ChatFormatting.RED);
         String var5 = String.valueOf(ChatFormatting.DARK_RED);
         String var6 = String.valueOf(ChatColorUtil.b());
         this.addRawClientMessage(var2 + var3 + " [" + var4 + "!!" + var5 + "] " + var6 + var1);
      }
   }

   public void queueMessage(String var1) {
      List var2 = this.queuedMessages;
      String var3 = this.a();
      String var4 = String.valueOf(ChatColorUtil.b());
      var2.add(var3 + " " + var4 + var1);
   }

   public void addRawClientMessage(String var1) {
      MutableComponent var2 = Component.literal(var1);
      this.a(var2, "");
   }

   public void addIdentifiedClientMessage(String var1, String var2) {
      MutableComponent var3 = Component.literal(var1);
      this.a(var3, var2);
   }

   public void addComponentMessage(Component var1) {
      this.a(var1, "");
   }

   private void a(Component var1, String var2) {
      if (!mc.isSameThread()) {
         mc.execute(() -> this.a(var1, var2));
      } else {
         int var3 = mc.gui.hud.getGuiTicks();
         GuiMessageTag var4 = GuiMessageTag.system();
         GuiMessage var5 = new GuiMessage(var3, var1, null, GuiMessageSource.SYSTEM_CLIENT, var4);
         ((GuiMessageDuck)(Object)var5).night$setClientMessage(true);
         ((GuiMessageDuck)(Object)var5).night$setClientIdentifier(var2);
         ChatHudAccessor var6 = (ChatHudAccessor)mc.gui.hud.getChat();
         GuiMessage var7 = var5;
         var6.invokeLogChatMessage(var7);
         ChatHudAccessor var8 = (ChatHudAccessor)mc.gui.hud.getChat();
         GuiMessage var9 = var5;
         var8.invokeAddMessage(var9);
         Component var10 = var5.content();
         double var11 = ((ChatHudAccessor)mc.gui.hud.getChat()).invokeGetWidth();
         double var13 = ((ChatHudAccessor)mc.gui.hud.getChat()).invokeGetScale();
         int var15 = Mth.floor(var11 / var13);
         Font var16 = mc.font;
         List var17 = ComponentRenderUtils.wrapComponents(var10, var15, var16);
         int var18 = 0;

         while (true) {
            int var19 = var18;
            int var20 = var17.size();
            if (var19 >= var20) {
               BetterChat var39;
               if (Night.moduleManager != null) {
                  var39 = Night.moduleManager.getModule(BetterChat.class);
               } else {
                  var39 = null;
               }

               BetterChat var40 = var39;
               boolean var41;
               if (var40 != null && var40.isEnabled() && var40.infinite.getValue()) {
                  var41 = true;
               } else {
                  var41 = false;
               }

               if (!var41) {
                  List var42 = ((ChatHudAccessor)mc.gui.hud.getChat()).getVisibleMessages();

                  while (var42.size() > 100 && !var42.isEmpty()) {
                     List var43 = var42;
                     int var44 = var42.size() - 1;
                     var43.remove(var44);
                  }
               }

               return;
            }

            List var21 = var17;
            int var22 = var18;
            FormattedCharSequence var23 = (FormattedCharSequence)var21.get(var22);
            if (mc.gui.hud.getChat().isChatFocused() && ((ChatHudAccessor)mc.gui.hud.getChat()).getScrolledLines() > 0) {
               ((ChatHudAccessor)mc.gui.hud.getChat()).setHasUnreadNewMessages(true);
               mc.gui.hud.getChat().scrollChat(1);
            }

            int var24 = var18;
            int var25 = var17.size() - 1;
            boolean var26;
            if (var24 == var25) {
               var26 = true;
            } else {
               var26 = false;
            }

            boolean var27 = var26;
            GuiMessage var28 = var5;
            FormattedCharSequence var29 = var23;
            boolean var30 = var27;
            Line var31 = new Line(var28, var29, var30);
            ((GuiMessageLineDuck)(Object)var31).night$setClientMessage(true);
            ((GuiMessageLineDuck)(Object)var31).night$setClientIdentifier(var2);
            List var32 = ((ChatHudAccessor)mc.gui.hud.getChat()).getVisibleMessages();
            Line var33 = var31;
            var32.addFirst(var33);
            BetterChat var34;
            if (Night.moduleManager != null) {
               var34 = Night.moduleManager.getModule(BetterChat.class);
            } else {
               var34 = null;
            }

            BetterChat var35 = var34;
            if (var35 != null && var35.isEnabled() && var35.animation2.getValue()) {
               Map var36 = var35.v();
               Line var37 = var31;
               Long var38 = System.currentTimeMillis();
               var36.put(var37, var38);
            }

            var18++;
         }
      }
   }

   public static void removeMessagesByIdentifier(String var0) {
      if (!mc.isSameThread()) {
         mc.execute(() -> removeMessagesByIdentifier(var0));
      } else {
         try {
            ArrayList var1 = new ArrayList();

            for (GuiMessage var3 : (Iterable<GuiMessage>)(Object)((ChatHudAccessor)mc.gui.hud.getChat()).getMessages()) {
               if (((GuiMessageDuck)(Object)var3).night$isClientMessage()
                  && !((GuiMessageDuck)(Object)var3).night$getClientIdentifier().isEmpty()
                  && ((GuiMessageDuck)(Object)var3).night$getClientIdentifier().equals(var0)) {
                  ArrayList var4 = var1;
                  GuiMessage var5 = var3;
                  var4.add(var5);
               }
            }

            ArrayList var6 = new ArrayList();

            for (Line var8 : (Iterable<Line>)(Object)((ChatHudAccessor)mc.gui.hud.getChat()).getVisibleMessages()) {
               if (((GuiMessageLineDuck)(Object)var8).night$isClientMessage()
                  && !((GuiMessageLineDuck)(Object)var8).night$getClientIdentifier().isEmpty()
                  && ((GuiMessageLineDuck)(Object)var8).night$getClientIdentifier().equals(var0)) {
                  ArrayList var9 = var6;
                  Line var10 = var8;
                  var9.add(var10);
               }
            }

            List var11 = ((ChatHudAccessor)mc.gui.hud.getChat()).getMessages();
            ArrayList var12 = var1;
            var11.removeAll(var12);
            List var13 = ((ChatHudAccessor)mc.gui.hud.getChat()).getVisibleMessages();
            ArrayList var14 = var6;
            var13.removeAll(var14);
         } catch (Exception var15) {
            var15.printStackTrace();
         }
      }
   }

   private String a() {
      return this.h(null);
   }

   private String h(String var1) {
      if (!Night.moduleManager.getModule(CommandsModule.class).watermark.getValue()) {
         return "";
      }

      String var2 = String.valueOf(TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).secondaryWatermarkColor.getValue()));
      String var3 = Night.moduleManager.getModule(CommandsModule.class).opening.getValue();
      String var4 = String.valueOf(TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).primaryWatermarkColor.getValue()));
      String var5;
      if (var1 == null) {
         var5 = Night.moduleManager.getModule(CommandsModule.class).watermarkText.getValue();
      } else {
         var5 = var1;
      }

      String var6 = String.valueOf(TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).secondaryWatermarkColor.getValue()));
      String var7 = Night.moduleManager.getModule(CommandsModule.class).closing.getValue();
      return var2 + var3 + var4 + var5 + var6 + var7;
   }
}
