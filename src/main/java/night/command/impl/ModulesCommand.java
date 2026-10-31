package night.command.impl;

import java.util.List;
import net.minecraft.ChatFormatting;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.Module;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "modules",
   displayName = "Modules",
   description = "Shows you a list of all of the client's modules and their toggle status.",
   aliases = "mods"
)
public class ModulesCommand extends Command {
   @Override
   public void execute(String[] var1) {
      if (var1.length == 0) {
         List var2 = Night.moduleManager.getModules();
         if (var2.isEmpty()) {
            String var3 = this.getDisplayName();
            String var4 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("There are currently no registered modules.", var3, var4);
         } else {
            StringBuilder var5 = new StringBuilder();
            int var6 = 0;

            for (Module var8 : (Iterable<Module>)(Object)var2) {
               var6++;
               StringBuilder var9 = var5;
               Object var10 = ChatColorUtil.b();
               StringBuilder var11 = var9.append(var10);
               String var12 = var8.getName();
               StringBuilder var13 = var11.append(var12);
               Object var14 = ChatColorUtil.a();
               StringBuilder var15 = var13.append(var14).append(" [");
               String var16;
               if (var8.isEnabled()) {
                  var16 = ChatFormatting.GREEN + "ON";
               } else {
                  var16 = ChatFormatting.RED + "OFF";
               }

               StringBuilder var17 = var15.append(var16);
               Object var18 = ChatColorUtil.a();
               StringBuilder var19 = var17.append(var18).append("]");
               int var20 = var6;
               int var21 = var2.size();
               String var22;
               if (var20 == var21) {
                  var22 = "";
               } else {
                  var22 = ", ";
               }

               var19.append(var22);
            }

            String var23 = String.valueOf(ChatColorUtil.a());
            String var24 = String.valueOf(ChatColorUtil.b());
            int var25 = var2.size();
            String var26 = String.valueOf(ChatColorUtil.a());
            String var27 = String.valueOf(ChatColorUtil.b());
            String var28 = String.valueOf(var5);
            String var29 = this.getName();
            Night.chatManager.sendReplaceableMessage("Modules " + var23 + "[" + var24 + var25 + var26 + "]: " + var27 + var28, var29);
         }
      } else {
         this.sendSyntax();
      }
   }
}
