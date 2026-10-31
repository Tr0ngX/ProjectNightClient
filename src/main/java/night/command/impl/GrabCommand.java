package night.command.impl;

import java.util.List;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.util.color.ChatColorUtil;

@CommandInfo(name = "grab", description = "Lets you copy various things to your clipboard.", syntax = "<ip|coords|name>")
public class GrabCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      List var2;
      if (var1.length == 0) {
         var2 = List.of("ip", "coords", "name");
      } else {
         var2 = List.of();
      }

      return var2;
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 1) {
         switch (var1[0]) {
            case "ip":
               String var8 = Night.serverManager.getServerHost();
               this.c(var8);
               break;
            case "coords":
               int var4 = (int)mc.player.getX();
               int var5 = (int)mc.player.getY();
               int var6 = (int)mc.player.getZ();
               this.c("[" + var4 + ", " + var5 + ", " + var6 + "]");
               break;
            case "name":
               String var7 = mc.player.getName().getString();
               this.c(var7);
               break;
            default:
               this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }

   private void c(String var1) {
      mc.keyboardHandler.setClipboard(var1);
      String var2 = String.valueOf(ChatColorUtil.a());
      String var3 = String.valueOf(ChatColorUtil.b());
      String var4 = this.getDisplayName();
      String var5 = this.getName();
      Night.chatManager.sendReplaceableTaggedMessage("Successfully copied " + var2 + var1 + var3 + " to your clipboard.", var4, var5);
   }
}
