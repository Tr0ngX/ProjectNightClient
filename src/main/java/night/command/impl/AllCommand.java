package night.command.impl;

import java.util.ArrayList;
import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.Module;

@CommandInfo(
   name = "all",
   displayName = "All",
   description = "Mass-toggles modules. 'off' disables all enabled modules; 'off restore' re-enables them.",
   syntax = "off | off restore"
)
public class AllCommand extends Command {
   private static final List<Module> a = new ArrayList<>();

   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         return List.of("off");
      } else {
         return var1.length == 1 && var1[0].equalsIgnoreCase("off") ? List.of("restore") : List.of();
      }
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length != 0 && var1[0].equalsIgnoreCase("off")) {
         if (var1.length == 2 && var1[1].equalsIgnoreCase("restore")) {
            if (a.isEmpty()) {
               String var2 = this.getDisplayName();
               String var3 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Nothing to restore.", var2, var3);
            } else {
               int var10 = 0;

               for (Module var12 : (Iterable<Module>)(Object)a) {
                  if (!var12.isEnabled()) {
                     var12.setEnabled(true, false);
                     var10++;
                  }
               }

               a.clear();
               String var13 = this.getDisplayName();
               String var14 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Restored " + var10 + " module(s).", var13, var14);
            }
         } else if (var1.length == 1) {
            a.clear();
            int var4 = 0;

            for (Module var6 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
               if (!var6.isPersistent() && var6.isEnabled()) {
                  var6.setEnabled(false, false);
                  Module var7 = var6;
                  a.add(var7);
                  var4++;
               }
            }

            String var8 = this.getDisplayName();
            String var9 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Disabled " + var4 + " module(s). Use '.all off restore' to bring them back.", var8, var9);
         } else {
            this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }
}
