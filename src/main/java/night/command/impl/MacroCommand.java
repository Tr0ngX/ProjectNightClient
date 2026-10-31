package night.command.impl;

import java.util.Arrays;
import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.util.color.ChatColorUtil;
import night.util.input.KeyNameUtil;

@CommandInfo(
   name = "macro",
   displayName = "Macro",
   description = "Allows you to manage the client's macro system.",
   syntax = "add <[key]> <[message]> | remove <[key]> | <clear|list>"
)
public class MacroCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      return var1.length == 0 ? List.of("add", "remove", "clear", "list") : List.of();
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length >= 3) {
         if (var1[0].equalsIgnoreCase("add")) {
            int var2 = KeyNameUtil.a_int_String(var1[1]);
            if (var2 == 0) {
               String var3 = this.getDisplayName();
               Night.chatManager.sendTaggedMessage("The keybind specified is not valid.", var3);
               return;
            }

            StringBuilder var50 = new StringBuilder();
            int var51 = var1.length;
            String[] var52 = Arrays.copyOfRange(var1, 2, var51);
            int var53 = 0;
            String[] var54 = var52;
            int var55 = var54.length;
            int var56 = 0;

            while (true) {
               int var57 = var56;
               int var58 = var55;
               if (var57 >= var58) {
                  String var68 = var50.toString();
                  int var69 = var2;
                  Night.macroManager.addMacro(var68, var69);
                  String var70 = String.valueOf(ChatColorUtil.a());
                  String var71 = String.valueOf(var50);
                  String var72 = String.valueOf(ChatColorUtil.b());
                  String var73 = String.valueOf(ChatColorUtil.a());
                  String var74 = KeyNameUtil.a_String_int(var2);
                  String var75 = String.valueOf(ChatColorUtil.b());
                  String var76 = this.getDisplayName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "Successfully added " + var70 + var71 + var72 + " as a macro, bound to the " + var73 + var74 + var75 + " key.",
                        var76,
                        "command-" + this.getName()
                     );
                  break;
               }

               String[] var59 = var54;
               int var60 = var56;
               String var61 = var59[var60];
               var53++;
               StringBuilder var62 = var50;
               String var63 = var61;
               StringBuilder var64 = var62.append(var63);
               int var65 = var53;
               int var66 = var52.length;
               String var67;
               if (var65 == var66) {
                  var67 = "";
               } else {
                  var67 = " ";
               }

               var64.append(var67);
               var56++;
            }
         } else {
            this.sendSyntax();
         }
      } else if (var1.length == 2) {
         if (var1[0].equalsIgnoreCase("remove")) {
            int var4 = KeyNameUtil.a_int_String(var1[1]);
            if (var4 == 0) {
               String var5 = this.getDisplayName();
               Night.chatManager.sendTaggedMessage("The keybind specified is not valid.", var5);
               return;
            }

            int var38 = var4;
            if (!Night.macroManager.hasMacroForKey(var38)) {
               String var39 = String.valueOf(ChatColorUtil.a());
               String var40 = KeyNameUtil.a_String_int(var4);
               String var41 = String.valueOf(ChatColorUtil.b());
               String var42 = this.getDisplayName();
               Night.chatManager
                  .sendReplaceableTaggedMessage("There is no macro with the " + var39 + var40 + var41 + " key.", var42, "command-" + this.getName());
            } else {
               String var43 = String.valueOf(ChatColorUtil.a());
               int var44 = var4;
               String var45 = Night.macroManager.getMacroByKey(var44);
               String var46 = String.valueOf(ChatColorUtil.b());
               String var47 = this.getDisplayName();
               Night.chatManager
                  .sendReplaceableTaggedMessage("Successfully removed the " + var43 + var45 + var46 + " macro.", var47, "command-" + this.getName());
               int var48 = var4;
               String var49 = Night.macroManager.getMacroByKey(var48);
               Night.macroManager.removeMacro(var49);
            }
         } else {
            this.sendSyntax();
         }
      } else if (var1.length == 1) {
         if (var1[0].equalsIgnoreCase("clear")) {
            Night.macroManager.clearMacros();
            String var6 = this.getDisplayName();
            Night.chatManager.sendReplaceableTaggedMessage("Successfully removed all macros.", var6, "command-" + this.getName());
         } else if (var1[0].equalsIgnoreCase("list")) {
            if (Night.macroManager.getMacros().isEmpty()) {
               String var7 = this.getDisplayName();
               Night.chatManager.sendReplaceableTaggedMessage("There are currently no macros added.", var7, "command-" + this.getName() + "-list");
            } else {
               StringBuilder var8 = new StringBuilder();
               int var9 = 0;

               for (String var11 : (Iterable<String>)(Object)Night.macroManager.getMacros().keySet()) {
                  var9++;
                  String var12 = var11;
                  int var13 = Night.macroManager.getKeyByMacro(var12);
                  StringBuilder var14 = var8;
                  Object var15 = ChatColorUtil.b();
                  StringBuilder var16 = var14.append(var15);
                  String var17 = var11;
                  StringBuilder var18 = var16.append(var17);
                  Object var19 = ChatColorUtil.a();
                  StringBuilder var20 = var18.append(var19).append(" [");
                  Object var21 = ChatColorUtil.b();
                  StringBuilder var22 = var20.append(var21);
                  String var23 = KeyNameUtil.a_String_int(var13);
                  StringBuilder var24 = var22.append(var23);
                  Object var25 = ChatColorUtil.a();
                  StringBuilder var26 = var24.append(var25).append("]");
                  Object var27 = ChatColorUtil.b();
                  StringBuilder var28 = var26.append(var27);
                  int var29 = var9;
                  int var30 = Night.macroManager.getMacros().size();
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
               int var34 = Night.macroManager.getMacros().size();
               String var35 = String.valueOf(ChatColorUtil.a());
               String var36 = String.valueOf(ChatColorUtil.b());
               String var37 = String.valueOf(var8);
               Night.chatManager
                  .sendReplaceableMessage("Macros " + var32 + "[" + var33 + var34 + var35 + "]: " + var36 + var37, "command-" + this.getName() + "-list");
            }
         } else {
            this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }
}
