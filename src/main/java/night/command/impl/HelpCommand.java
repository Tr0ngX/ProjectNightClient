package night.command.impl;

import java.util.ArrayList;
import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "help",
   displayName = "Help",
   description = "Shows you a list of all of the client's commands or information about a certain command.",
   syntax = "empty | <[command]>",
   aliases = "cmds"
)
public class HelpCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      return var1.length != 0 ? List.of() : Night.commandManager.getCommands().stream().map(Command::getName).toList();
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 1) {
         String var2 = var1[0];
         Command var3 = Night.commandManager.getCommand(var2);
         if (var3 == null) {
            String var4 = this.getDisplayName();
            String var5 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Could not find the command specified.", var4, var5);
            return;
         }

         String var26 = var3.getDisplayName();
         String var27 = String.valueOf(ChatColorUtil.a());
         String var28 = String.valueOf(ChatColorUtil.b());
         String var29 = var3.getName();
         String var30 = var3.getSyntax();
         Night.chatManager.sendInfo(var26 + var27 + " - " + var28 + var29 + " " + var30);
         String var31 = var3.getDescription();
         Night.chatManager.sendInfo(var31);
      } else if (var1.length == 0) {
         ArrayList var6 = Night.commandManager.getCommands();
         if (var6.isEmpty()) {
            String var7 = this.getDisplayName();
            Night.chatManager.sendReplaceableTaggedMessage("There are currently no registered commands.", var7, this.getName() + "-list");
         } else {
            StringBuilder var8 = new StringBuilder();
            int var9 = 0;

            for (Command var11 : (Iterable<Command>)(Object)var6) {
               var9++;
               StringBuilder var12 = var8;
               Object var13 = ChatColorUtil.b();
               StringBuilder var14 = var12.append(var13);
               String var15 = var11.getName();
               StringBuilder var16 = var14.append(var15);
               int var17 = var9;
               int var18 = var6.size();
               String var19;
               if (var17 == var18) {
                  var19 = "";
               } else {
                  var19 = ", ";
               }

               var16.append(var19);
            }

            String var20 = String.valueOf(ChatColorUtil.a());
            String var21 = String.valueOf(ChatColorUtil.b());
            int var22 = var6.size();
            String var23 = String.valueOf(ChatColorUtil.a());
            String var24 = String.valueOf(ChatColorUtil.b());
            String var25 = String.valueOf(var8);
            Night.chatManager.sendReplaceableMessage("Commands " + var20 + "[" + var21 + var22 + var23 + "]: " + var24 + var25, this.getName() + "-list");
         }
      } else {
         this.sendSyntax();
      }
   }
}
