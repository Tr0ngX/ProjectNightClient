package night.command.impl;

import java.util.List;
import net.minecraft.ChatFormatting;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.manager.ChatManager;
import night.module.Module;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "toggle",
   displayName = "Toggle",
   description = "Toggles a specified module or a setting on and off.",
   syntax = "<[module]> | <[module]> <[setting]>",
   aliases = "t"
)
public class ToggleCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         return this.getModuleNames();
      } else if (var1.length == 1) {
         String var2 = var1[0];
         Module var3 = Night.moduleManager.getModuleByName(var2);
         return var3 == null
            ? List.of()
            : var3.getSettings().stream().filter(var0 -> var0 instanceof BooleanSetting).map(var0 -> var0.getName().toLowerCase()).toList();
      } else {
         return List.of();
      }
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length != 1 && var1.length != 2) {
         this.sendSyntax();
      } else {
         String var2 = var1[0];
         Module var3 = Night.moduleManager.getModuleByName(var2);
         if (var3 == null) {
            String var4 = this.getDisplayName();
            String var5 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Could not find the module specified.", var4, var5);
            return;
         }

         if (var1.length == 1) {
            if (var3.isPersistent()) {
               String var6 = this.getDisplayName();
               String var7 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Cannot toggle a persistent module.", var6, var7);
               return;
            }

            Module var8 = var3;
            boolean var9;
            if (!var3.isEnabled()) {
               var9 = true;
            } else {
               var9 = false;
            }

            var8.setEnabled(var9, false);
            ChatManager var10 = Night.chatManager;
            String var11 = String.valueOf(ChatColorUtil.a());
            String var12 = var3.getName();
            String var13 = String.valueOf(ChatColorUtil.b());
            String var14;
            if (var3.isEnabled()) {
               var14 = ChatFormatting.GREEN + "on";
            } else {
               var14 = ChatFormatting.RED + "off";
            }

            String var15 = String.valueOf(ChatColorUtil.b());
            String var16 = this.getDisplayName();
            String var17 = this.getName();
            String var18 = var3.getName();
            var10.sendReplaceableTaggedMessage(var11 + var12 + var13 + " has been toggled " + var14 + var15 + ".", var16, var17 + "-cmd-" + var18);
         }

         if (var1.length == 2) {
            Module var19 = var3;
            String var20 = var1[1];
            Setting var21 = var19.getSetting(var20);
            if (var21 == null) {
               String var22 = this.getDisplayName();
               String var23 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Could not find the setting specified.", var22, var23);
               return;
            }

            if (!(var21 instanceof BooleanSetting var24)) {
               String var39 = String.valueOf(ChatColorUtil.a());
               String var40 = String.valueOf(ChatColorUtil.b());
               String var41 = this.getDisplayName();
               String var42 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("This command only works for " + var39 + "boolean" + var40 + " settings.", var41, var42);
               return;
            }

            boolean var25;
            if (!var24.getValue()) {
               var25 = true;
            } else {
               var25 = false;
            }

            var24.setValue(var25);
            ChatManager var26 = Night.chatManager;
            String var27 = String.valueOf(ChatColorUtil.a());
            String var28 = var21.getName();
            String var29 = String.valueOf(ChatColorUtil.b());
            String var30;
            if (var24.getValue()) {
               var30 = ChatFormatting.GREEN + "on";
            } else {
               var30 = ChatFormatting.RED + "off";
            }

            String var31 = String.valueOf(ChatColorUtil.b());
            String var32 = String.valueOf(ChatColorUtil.a());
            String var33 = var3.getName();
            String var34 = String.valueOf(ChatColorUtil.b());
            String var35 = this.getDisplayName();
            String var36 = this.getName();
            String var37 = var3.getName();
            String var38 = var21.getName();
            var26.sendReplaceableTaggedMessage(
               var27 + var28 + var29 + " has been toggled " + var30 + var31 + " for " + var32 + var33 + var34 + ".",
               var35,
               var36 + "-cmd-" + var37 + "-" + var38
            );
         }
      }
   }
}
