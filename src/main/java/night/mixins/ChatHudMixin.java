package night.mixins;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.ChatGraphicsAccess;
import net.minecraft.client.gui.components.ChatComponent.DisplayMode;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import night.Night;
import night.manager.ChatManager;
import night.module.impl.core.CommandsModule;
import night.module.impl.misc.BetterChat;
import night.util.color.TextFormattingUtil;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
   @Shadow
   @Final
   private List<Line> trimmedMessages;

   @ModifyConstant(method = {"addMessageToDisplayQueue", "addMessageToQueue", "addRecentChat"}, constant = @Constant(intValue = 100))
   private int night$infiniteChat(int var1) {
      BetterChat var2 = Night.moduleManager != null ? Night.moduleManager.getModule(BetterChat.class) : null;
      return var2 != null && var2.isEnabled() && var2.infinite.getValue() ? Integer.MAX_VALUE : var1;
   }

   @Inject(method = "addMessageToDisplayQueue", at = @At("TAIL"))
   private void night$trackAnimation(GuiMessage var1, CallbackInfo var2) {
      BetterChat var3 = Night.moduleManager.getModule(BetterChat.class);
      if (var3.isEnabled() && var3.animation2.getValue() && !this.trimmedMessages.isEmpty()) {
         var3.v().put(this.trimmedMessages.getFirst(), System.currentTimeMillis());
      }
   }

   @Inject(
      method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void night$preventChatOverGui(GuiGraphicsExtractor var1, Font var2, int var3, int var4, int var5, DisplayMode var6, boolean var7, CallbackInfo var8) {
      Minecraft var9 = Minecraft.getInstance();
      if (var9.gui != null && var9.gui.screen() != null && !(var9.gui.screen() instanceof ChatScreen) && !ChatManager.suppressSend) {
         var8.cancel();
      }
   }

   @Inject(
      method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
      at = @At("TAIL")
   )
   private void night$cleanupAnimations(CallbackInfo var1) {
      BetterChat var2 = Night.moduleManager.getModule(BetterChat.class);
      if (var2.isEnabled() && var2.animation2.getValue()) {
         var2.v().entrySet().removeIf(var1x -> System.currentTimeMillis() - var1x.getValue() > var2.delay.getValue().intValue());
      }
   }

   @Redirect(
      method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;updatePose(Ljava/util/function/Consumer;)V")
   )
   private void night$applyOffset(ChatGraphicsAccess var1, Consumer<Matrix3x2f> var2) {
      BetterChat var3 = Night.moduleManager.getModule(BetterChat.class);
      int var4 = var3.isEnabled() ? var3.offset.getValue().intValue() : 0;
      var1.updatePose(var2x -> {
         var2.accept(var2x);
         if (var4 != 0) {
            var2x.translate(var4, 0.0F);
         }
      });
   }

   @Redirect(
      method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0)
   )
   private Object night$textOpacity(OptionInstance<?> var1) {
      BetterChat var2 = Night.moduleManager.getModule(BetterChat.class);
      return var2.isEnabled() ? var2.textAlpha.getValue().doubleValue() / 255.0 : var1.get();
   }

   @Redirect(
      method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1)
   )
   private Object night$backgroundOpacity(OptionInstance<?> var1) {
      BetterChat var2 = Night.moduleManager.getModule(BetterChat.class);
      if (var2.isEnabled()) {
         return switch (var2.background.getValue()) {
            case "Clear" -> 0.0;
            case "Custom" -> var2.color.getColor().getAlpha() / 255.0;
            default -> var1.get();
         };
      } else {
         return var1.get();
      }
   }

   @Redirect(
      method = "lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;black(F)I")
   )
   private static int night$backgroundColor(float var0) {
      BetterChat var1 = Night.moduleManager.getModule(BetterChat.class);
      return var1.isEnabled() && var1.background.getValue().equalsIgnoreCase("Custom")
         ? ARGB.color(Math.round(var0 * 255.0F), var1.color.getColor().getRGB() & 16777215)
         : ARGB.black(var0);
   }

   @ModifyArgs(
      method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"
      )
   )
   private void addMessage(Args var1, Component var2, MessageSignature var3, GuiMessageSource var4, GuiMessageTag var5) {
      BetterChat var6 = Night.moduleManager.getModule(BetterChat.class);
      if (var6.isEnabled()) {
         Object var7 = var2;
         Minecraft var8 = Minecraft.getInstance();
         if (var6.highlight.getValue() && var8.player != null) {
            String var9 = var8.player.getGameProfile().name();
            if (var9 != null && !var9.isEmpty()) {
               String var10 = var2.getString();
               if (!night$isSender(var10, var9) && night$containsWord(var10.toLowerCase(), var9.toLowerCase())) {
                  if (var6.highlightSound.getValue()) {
                     var8.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                  }

                  var7 = night$highlightMessage((Component)var7, var9, var6.highlightColor.getColor());
               }
            }
         }

         if (var6.timestamps.getValue()) {
            var7 = Component.literal("")
               .append(
                  Component.literal(
                     TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())
                        + var6.opening.getValue()
                        + TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).primaryWatermarkColor.getValue())
                        + new SimpleDateFormat("HH:mm").format(new Date())
                        + TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())
                        + var6.closing.getValue()
                        + ChatFormatting.RESET
                        + " "
                  )
               )
               .append((Component)var7);
         }

         var1.set(1, var7);
      }
   }

   private static boolean night$isSender(String var0, String var1) {
      if (var0 != null && var1 != null && !var1.isEmpty()) {
         String var2 = var0.toLowerCase();
         String var3 = var1.toLowerCase();
         if (!var2.startsWith("you whisper to") && !var2.startsWith("to ")) {
            int var4 = -1;
            if (var0.startsWith("<")) {
               int var5 = var0.indexOf(62);
               if (var5 != -1) {
                  var4 = var5;
               }
            }

            if (var4 == -1) {
               String[] var11 = new String[]{": ", " \u00bb ", " > ", " -> ", ":"};

               for (String var9 : var11) {
                  int var10 = var0.indexOf(var9);
                  if (var10 != -1 && (var4 == -1 || var10 < var4)) {
                     var4 = var10;
                  }
               }
            }

            if (var4 != -1) {
               String var12 = var0.substring(0, var4);
               if (night$containsWord(var12.toLowerCase(), var3)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean night$containsWord(String var0, String var1) {
      if (var0 != null && var1 != null) {
         int var2 = 0;

         while ((var2 = var0.indexOf(var1, var2)) != -1) {
            boolean var3 = var2 == 0 || !night$isWordChar(var0.charAt(var2 - 1));
            boolean var4 = var2 + var1.length() == var0.length() || !night$isWordChar(var0.charAt(var2 + var1.length()));
            if (var3 && var4) {
               return true;
            }

            var2 += var1.length();
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean night$isWordChar(char var0) {
      return Character.isLetterOrDigit(var0) || var0 == '_';
   }

   private static Component night$highlightMessage(Component var0, String var1, Color var2) {
      TextColor var3 = TextColor.fromRgb(var2.getRGB() & 16777215);
      Style var4 = Style.EMPTY.withColor(var3);
      MutableComponent var5 = Component.empty();
      var0.visit((var3x, var4x) -> {
         if (var4x != null && !var4x.isEmpty()) {
            String var5x = var4x.toLowerCase();
            String var6 = var1.toLowerCase();
            int var7 = 0;

            while (var7 < var4x.length()) {
               int var8 = var5x.indexOf(var6, var7);
               if (var8 == -1) {
                  var5.append(Component.literal(var4x.substring(var7)).withStyle(var3x));
                  break;
               }

               boolean var9 = var8 == 0 || !night$isWordChar(var4x.charAt(var8 - 1));
               boolean var10 = var8 + var6.length() == var4x.length() || !night$isWordChar(var4x.charAt(var8 + var6.length()));
               if (var9 && var10) {
                  if (var8 > var7) {
                     var5.append(Component.literal(var4x.substring(var7, var8)).withStyle(var3x));
                  }

                  var5.append(Component.literal(var4x.substring(var8, var8 + var6.length())).withStyle(var4));
                  var7 = var8 + var6.length();
               } else {
                  var5.append(Component.literal(var4x.substring(var7, var8 + 1)).withStyle(var3x));
                  var7 = var8 + 1;
               }
            }

            return Optional.empty();
         } else {
            return Optional.empty();
         }
      }, Style.EMPTY);
      return var5;
   }
}
