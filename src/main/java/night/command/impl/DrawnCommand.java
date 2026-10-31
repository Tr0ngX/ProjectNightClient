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
   name = "drawn",
   displayName = "Drawn",
   description = "Manages the modules that will be shown on the HUD's module list.",
   syntax = "<true|false|list|reset> | <[module]> <true|false|reset>"
)
public class DrawnCommand extends Command {
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
               var3.drawn.setValue(true);
               String var58 = String.valueOf(ChatColorUtil.a());
               String var59 = String.valueOf(ChatColorUtil.b());
               String var60 = this.getDisplayName();
               String var61 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set the module's drawn status to " + var58 + "true" + var59 + ".", var60, var61);
               break;
            case "false":
               var3.drawn.setValue(false);
               String var54 = String.valueOf(ChatColorUtil.a());
               String var55 = String.valueOf(ChatColorUtil.b());
               String var56 = this.getDisplayName();
               String var57 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set the module's drawn status to " + var54 + "false" + var55 + ".", var56, var57);
               break;
            case "reset":
               BooleanSetting var50 = var3.drawn;
               boolean var51 = var3.drawn.getDefaultValue();
               var50.setValue(var51);
               String var52 = this.getDisplayName();
               String var53 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set the module's drawn status to it's default value.", var52, var53);
               break;
            default:
               this.sendSyntax();
         }
      } else if (var1.length == 1) {
         switch (var1[0]) {
            case "true":
               Iterator var13 = Night.moduleManager.getModules().iterator();

               while (var13.hasNext()) {
                  ((Module)var13.next()).drawn.setValue(true);
               }

               String var14 = String.valueOf(ChatColorUtil.a());
               String var15 = String.valueOf(ChatColorUtil.b());
               String var16 = this.getDisplayName();
               String var17 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set every module's drawn status to " + var14 + "true" + var15 + ".", var16, var17);
               break;
            case "false":
               Iterator var8 = Night.moduleManager.getModules().iterator();

               while (var8.hasNext()) {
                  ((Module)var8.next()).drawn.setValue(false);
               }

               String var9 = String.valueOf(ChatColorUtil.a());
               String var10 = String.valueOf(ChatColorUtil.b());
               String var11 = this.getDisplayName();
               String var12 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set every module's drawn status to " + var9 + "false" + var10 + ".", var11, var12);
               break;
            case "list":
               List var18 = Night.moduleManager.getModules().stream().filter(var0 -> var0.drawn.getValue()).toList();
               ArrayList var19 = new ArrayList(var18);
               if (var19.isEmpty()) {
                  String var20 = String.valueOf(ChatColorUtil.a());
                  String var21 = String.valueOf(ChatColorUtil.b());
                  String var22 = this.getDisplayName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "There are currently no modules with their drawn status set to " + var20 + "true" + var21 + ".", var22, this.getName() + "-list"
                     );
               } else {
                  StringBuilder var23 = new StringBuilder();
                  int var24 = 0;

                  for (Module var26 : (Iterable<Module>)(Object)var19) {
                     StringBuilder var27 = var23;
                     Object var28 = ChatColorUtil.b();
                     StringBuilder var29 = var27.append(var28);
                     String var30 = var26.getName();
                     StringBuilder var31 = var29.append(var30);
                     int var32 = var24 + 1;
                     int var33 = var19.size();
                     String var34;
                     if (var32 == var33) {
                        var34 = "";
                     } else {
                        var34 = ", ";
                     }

                     var31.append(var34);
                     var24++;
                  }

                  String var35 = String.valueOf(ChatColorUtil.b());
                  String var36 = String.valueOf(ChatColorUtil.a());
                  String var37 = String.valueOf(ChatColorUtil.b());
                  int var38 = var19.size();
                  String var39 = String.valueOf(ChatColorUtil.a());
                  String var40 = String.valueOf(ChatColorUtil.b());
                  String var41 = String.valueOf(var23);
                  Night.chatManager
                     .sendReplaceableMessage(var35 + "Drawn Modules " + var36 + "[" + var37 + var38 + var39 + "]: " + var40 + var41, this.getName() + "-list");
               }
               break;
            case "reset":
               for (Module var43 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
                  BooleanSetting var44 = var43.drawn;
                  boolean var45 = var43.drawn.getDefaultValue();
                  var44.setValue(var45);
               }

               String var46 = this.getDisplayName();
               String var47 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully set every module's drawn status to it's default value.", var46, var47);
               break;
            default:
               this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }
}
