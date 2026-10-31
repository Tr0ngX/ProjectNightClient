package night.command.impl;

import java.util.ArrayList;
import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.Module;
import night.util.color.ChatColorUtil;
import night.util.input.KeyNameUtil;

@CommandInfo(
   name = "bind",
   displayName = "Bind",
   description = "Changes the toggle keybind of a module.",
   syntax = "<[module]> <[key]|reset> | <reset|list> ",
   aliases = {"b", "key", "keybind"}
)
public class BindCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         List var2 = this.getModuleNames();
         ArrayList var3 = new ArrayList(var2);
         var3.add("reset");
         var3.add("list");
         return var3;
      }

      if (var1.length == 1) {
         String var4 = var1[0];
         if (Night.moduleManager.getModuleByName(var4) != null) {
            return List.of("<bind>");
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

         if (var1[1].equalsIgnoreCase("reset")) {
            var3.setKeybind(0);
            String var38 = String.valueOf(ChatColorUtil.a());
            String var39 = var3.getName();
            String var40 = String.valueOf(ChatColorUtil.b());
            String var41 = this.getDisplayName();
            String var42 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Successfully reset the toggle keybind of the " + var38 + var39 + var40 + " module.", var41, var42);
         } else {
            Module var43 = var3;
            int var44 = KeyNameUtil.a_int_String(var1[1]);
            var43.setKeybind(var44);
            String var45 = String.valueOf(ChatColorUtil.a());
            String var46 = var3.getName();
            String var47 = String.valueOf(ChatColorUtil.b());
            String var48 = String.valueOf(ChatColorUtil.a());
            String var49 = KeyNameUtil.a_String_int(var3.getKeybind());
            String var50 = String.valueOf(ChatColorUtil.b());
            String var51 = this.getDisplayName();
            String var52 = this.getName();
            Night.chatManager
               .sendReplaceableTaggedMessage(
                  "Successfully bound the " + var45 + var46 + var47 + " module to the " + var48 + var49 + var50 + " key.", var51, var52
               );
         }
      } else if (var1.length == 1) {
         switch (var1[0].toLowerCase()) {
            case "reset":
               Night.moduleManager.getModules().forEach(var0 -> var0.bind.reset());
               String var8 = this.getDisplayName();
               String var9 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully reset every module's toggle keybind.", var8, var9);
               break;
            case "list":
               List var10 = Night.moduleManager.getModules().stream().filter(var0 -> {
                  boolean var1x;
                  if (var0.getKeybind() != 0) {
                     var1x = true;
                  } else {
                     var1x = false;
                  }

                  return var1x;
               }).toList();
               if (var10.isEmpty()) {
                  String var11 = this.getDisplayName();
                  Night.chatManager.sendReplaceableTaggedMessage("There are currently no bound modules.", var11, this.getName() + "-list");
               } else {
                  StringBuilder var12 = new StringBuilder();
                  int var13 = 0;

                  for (Module var15 : (Iterable<Module>)(Object)var10) {
                     var13++;
                     StringBuilder var16 = var12;
                     Object var17 = ChatColorUtil.b();
                     StringBuilder var18 = var16.append(var17);
                     String var19 = var15.getName();
                     StringBuilder var20 = var18.append(var19);
                     Object var21 = ChatColorUtil.a();
                     StringBuilder var22 = var20.append(var21).append(" [");
                     Object var23 = ChatColorUtil.b();
                     StringBuilder var24 = var22.append(var23);
                     String var25 = KeyNameUtil.a_String_int(var15.getKeybind()).toUpperCase();
                     StringBuilder var26 = var24.append(var25);
                     Object var27 = ChatColorUtil.a();
                     StringBuilder var28 = var26.append(var27).append("]");
                     int var29 = var13;
                     int var30 = var10.size();
                     String var31;
                     if (var29 == var30) {
                        var31 = "";
                     } else {
                        var31 = ", ";
                     }

                     var28.append(var31);
                  }

                  String var32 = String.valueOf(ChatColorUtil.a());
                  String var33 = String.valueOf(ChatColorUtil.b());
                  int var34 = var10.size();
                  String var35 = String.valueOf(ChatColorUtil.a());
                  String var36 = String.valueOf(ChatColorUtil.b());
                  String var37 = String.valueOf(var12);
                  Night.chatManager
                     .sendReplaceableMessage("Bound Modules " + var32 + "[" + var33 + var34 + var35 + "]: " + var36 + var37, this.getName() + "-list");
               }
               break;
            default:
               this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }
}
