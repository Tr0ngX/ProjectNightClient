package night.command.impl;

import net.minecraft.client.multiplayer.ClientPacketListener;
import night.command.Command;
import night.command.CommandInfo;

@CommandInfo(
   name = "say",
   description = "Sends a message through commands, allowing you to bypass the client's prefix and minecraft's command prefix.",
   syntax = "<[message]>"
)
public class SayCommand extends Command {
   @Override
   public void execute(String[] var1) {
      if (var1.length >= 1) {
         StringBuilder var2 = new StringBuilder();
         int var3 = 0;
         int var4 = var1.length;
         int var5 = 0;

         while (true) {
            int var6 = var5;
            int var7 = var4;
            if (var6 >= var7) {
               ClientPacketListener var16 = mc.getConnection();
               String var17 = var2.toString();
               var16.sendChat(var17);
               break;
            }

            int var8 = var5;
            String var9 = var1[var8];
            StringBuilder var10 = var2;
            String var11 = var9;
            StringBuilder var12 = var10.append(var11);
            int var13 = var3 + 1;
            int var14 = var1.length;
            String var15;
            if (var13 == var14) {
               var15 = "";
            } else {
               var15 = " ";
            }

            var12.append(var15);
            var3++;
            var5++;
         }
      } else {
         this.sendSyntax();
      }
   }
}
