package night.command.impl;

import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.impl.player.Rekit;

@CommandInfo(name = "kit", displayName = "Kit", description = "Manages Rekit's saved kits.", syntax = "<save|load|delete> <name> | <list|active>")
public class KitCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         return List.of("save", "load", "delete", "list", "active");
      } else {
         return var1.length != 1 || !var1[0].equalsIgnoreCase("load") && !var1[0].equalsIgnoreCase("delete")
            ? List.of()
            : Night.moduleManager.getModule(Rekit.class).w();
      }
   }

   @Override
   public void execute(String[] var1) {
      Rekit var2 = Night.moduleManager.getModule(Rekit.class);
      if (var1.length == 0) {
         this.sendSyntax();
      } else {
         switch (var1[0].toLowerCase()) {
            case "save":
               if (var1.length < 2) {
                  this.sendSyntax();
                  return;
               }

               Rekit var9 = var2;
               String var10 = var1[1];
               var9.b_void_String(var10);
               break;
            case "load":
               if (var1.length < 2) {
                  var2.x();
               } else {
                  Rekit var5 = var2;
                  String var6 = var1[1];
                  var5.c_void_String(var6);
               }
               break;
            case "delete":
               if (var1.length < 2) {
                  this.sendSyntax();
                  return;
               }

               Rekit var7 = var2;
               String var8 = var1[1];
               var7.d(var8);
               break;
            case "active":
               var2.y();
               break;
            case "list":
               var2.x();
               break;
            default:
               this.sendSyntax();
         }
      }
   }
}
