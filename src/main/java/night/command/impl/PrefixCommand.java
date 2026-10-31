package night.command.impl;

import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "prefix",
   displayName = "Prefix",
   description = "Allows you to change the client's command prefix.",
   syntax = "<new_prefix> | reset",
   aliases = {"setprefix", "p"}
)
public class PrefixCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      List var2;
      if (var1.length == 0) {
         var2 = List.of("reset");
      } else {
         var2 = List.of();
      }

      return var2;
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 1) {
         if (var1[0].equalsIgnoreCase("reset")) {
            Night.commandManager.setPrefix(".");

            try {
               if (Night.configManager != null) {
                  Night.configManager.saveGeneral();
               }
            } catch (Exception var17) {
            }

            String var3 = this.getDisplayName();
            String var4 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Successfully reset the command's prefix back to it's default (.).", var3, var4);
         } else {
            if (var1[0].length() > 3) {
               String var5 = this.getDisplayName();
               String var6 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("The specified prefix is longer than the limit of 3 characters.", var5, var6);
               return;
            }

            if (var1[0].equalsIgnoreCase("/")) {
               String var7 = this.getDisplayName();
               String var8 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("The specified prefix would interfere with vanilla Minecraft commands.", var7, var8);
               return;
            }

            String var9 = var1[0];
            Night.commandManager.setPrefix(var9);

            try {
               if (Night.configManager != null) {
                  Night.configManager.saveGeneral();
               }
            } catch (Exception var16) {
            }

            String var11 = String.valueOf(ChatColorUtil.a());
            String var12 = var1[0];
            String var13 = String.valueOf(ChatColorUtil.b());
            String var14 = this.getDisplayName();
            String var15 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Successfully set the client's command prefix to " + var11 + var12 + var13 + ".", var14, var15);
         }
      } else {
         this.sendSyntax();
      }
   }
}
