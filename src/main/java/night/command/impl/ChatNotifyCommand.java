package night.command.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.Module;
import night.setting.impl.BooleanSetting;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "chatnotify",
   displayName = "ChatNotify",
   description = "Manages the toggle notification status of the client's modules.",
   syntax = "<true|false|list|reset> | <[module]> <true|false|reset>"
)
public class ChatNotifyCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         List var2 = this.getModuleNames();
         ArrayList var3 = new ArrayList(var2);
         var3.add("true");
         var3.add("false");
         var3.add("list");
         var3.add("reset");
         return var3;
      }

      if (var1.length == 1) {
         String var4 = var1[0];
         if (Night.moduleManager.getModuleByName(var4) != null) {
            return List.of("true", "false", "reset");
         }
      }

      return List.of();
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 2) {
         String var2 = var1[0];
         Module var3 = Night.moduleManager.getModuleByName(var2);
         if (var3 == null) {
            String var4 = this.getDisplayName();
            String var5 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Could not find the module specified.", var4, var5);
            return;
         }

         switch (var1[1]) {
            case "true":
               var3.chatNotify.setValue(true);
               String var8 = String.valueOf(ChatColorUtil.a());
               String var9 = String.valueOf(ChatColorUtil.b());
               String var10 = this.getDisplayName();
               String var11 = this.getName();
               Night.chatManager
                  .sendReplaceableTaggedMessage("Successfully set the module's notification status to " + var8 + "true" + var9 + ".", var10, var11);
               break;
            case "false":
               var3.chatNotify.setValue(false);
               String var12 = String.valueOf(ChatColorUtil.a());
               String var13 = String.valueOf(ChatColorUtil.b());
               String var14 = this.getDisplayName();
               String var15 = this.getName();
               Night.chatManager
                  .sendReplaceableTaggedMessage("Successfully set the module's notification status to " + var12 + "false" + var13 + ".", var14, var15);
               break;
            case "reset":
               BooleanSetting var16 = var3.chatNotify;
               boolean var17 = var3.chatNotify.getDefaultValue();
               var16.setValue(var17);
               String var18 = this.getDisplayName();
               String var19 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set the module's notification status to it's default value.", var18, var19);
               break;
            default:
               this.sendSyntax();
         }
      } else if (var1.length == 1) {
         switch (var1[0]) {
            case "true":
               Iterator var57 = Night.moduleManager.getModules().iterator();

               while (var57.hasNext()) {
                  ((Module)var57.next()).chatNotify.setValue(true);
               }

               String var58 = String.valueOf(ChatColorUtil.a());
               String var59 = String.valueOf(ChatColorUtil.b());
               String var60 = this.getDisplayName();
               String var61 = this.getName();
               Night.chatManager
                  .sendReplaceableTaggedMessage("Successfully set every module's notification status to " + var58 + "true" + var59 + ".", var60, var61);
               break;
            case "false":
               Iterator var28 = Night.moduleManager.getModules().iterator();

               while (var28.hasNext()) {
                  ((Module)var28.next()).chatNotify.setValue(false);
               }

               String var29 = String.valueOf(ChatColorUtil.a());
               String var30 = String.valueOf(ChatColorUtil.b());
               String var31 = this.getDisplayName();
               String var32 = this.getName();
               Night.chatManager
                  .sendReplaceableTaggedMessage("Successfully set every module's notification status to " + var29 + "false" + var30 + ".", var31, var32);
               break;
            case "list":
               List var33 = Night.moduleManager.getModules().stream().filter(var0 -> var0.chatNotify.getValue()).toList();
               ArrayList var34 = new ArrayList(var33);
               if (var34.isEmpty()) {
                  String var35 = String.valueOf(ChatColorUtil.a());
                  String var36 = String.valueOf(ChatColorUtil.b());
                  String var37 = this.getDisplayName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "There are currently no modules with their notification status set to " + var35 + "true" + var36 + ".", var37, this.getName() + "-list"
                     );
               } else {
                  StringBuilder var38 = new StringBuilder();
                  int var39 = 0;

                  for (Module var41 : (Iterable<Module>)(Object)var34) {
                     StringBuilder var42 = var38;
                     Object var43 = ChatColorUtil.b();
                     StringBuilder var44 = var42.append(var43);
                     String var45 = var41.getName();
                     StringBuilder var46 = var44.append(var45);
                     int var47 = var39 + 1;
                     int var48 = var34.size();
                     String var49;
                     if (var47 == var48) {
                        var49 = "";
                     } else {
                        var49 = ", ";
                     }

                     var46.append(var49);
                     var39++;
                  }

                  String var50 = String.valueOf(ChatColorUtil.b());
                  String var51 = String.valueOf(ChatColorUtil.a());
                  String var52 = String.valueOf(ChatColorUtil.b());
                  int var53 = var34.size();
                  String var54 = String.valueOf(ChatColorUtil.a());
                  String var55 = String.valueOf(ChatColorUtil.b());
                  String var56 = String.valueOf(var38);
                  Night.chatManager
                     .sendReplaceableMessage(
                        var50 + "Notifiable Modules " + var51 + "[" + var52 + var53 + var54 + "]: " + var55 + var56, this.getName() + "-list"
                     );
               }
               break;
            case "reset":
               for (Module var23 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
                  BooleanSetting var24 = var23.chatNotify;
                  boolean var25 = var23.chatNotify.getDefaultValue();
                  var24.setValue(var25);
               }

               String var26 = this.getDisplayName();
               String var27 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set every module's notification status to it's default value.", var26, var27);
               break;
            default:
               this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }
}
