package night.command.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.impl.core.FriendModule;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "friend",
   displayName = "Friend",
   description = "Allows you to manage the client's friend list.",
   syntax = "<add|del> <[player]> | <clear|list>",
   aliases = {"f", "friends"}
)
public class FriendCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         return List.of("add", "del", "clear", "list");
      } else if (var1.length == 1 && var1[0].equalsIgnoreCase("del")) {
         return Night.friendManager.getFriends();
      } else {
         return var1.length == 1 && var1[0].equalsIgnoreCase("add") && mc.level != null && mc.player != null ? mc.level.players().stream().filter(var0 -> {
            LocalPlayer var1x = mc.player;
            boolean var2;
            if (var0 != var1x) {
               var2 = true;
            } else {
               var2 = false;
            }

            return var2;
         }).map(var0 -> var0.getName().getString()).filter(var0 -> {
            boolean var1x;
            if (!Night.friendManager.isFriend(var0)) {
               var1x = true;
            } else {
               var1x = false;
            }

            return var1x;
         }).toList() : List.of();
      }
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 2) {
         switch (var1[0].toLowerCase()) {
            case "add":
               String var16 = var1[1];
               if (!Night.friendManager.isFriend(var16)) {
                  if (Night.moduleManager.getModule(FriendModule.class).friendMessage.getValue()) {
                     String var17 = var1[1];
                     Night.friendManager.sendFriendMessage(var17);
                  }

                  String var18 = var1[1];
                  Night.friendManager.addFriend(var18);
                  String var19 = String.valueOf(ChatColorUtil.a());
                  String var20 = var1[1];
                  String var21 = String.valueOf(ChatColorUtil.b());
                  String var22 = this.getDisplayName();
                  String var23 = this.getName();
                  Night.chatManager.sendReplaceableTaggedMessage("Successfully added " + var19 + var20 + var21 + " to your friends list.", var22, var23);
               } else {
                  String var24 = String.valueOf(ChatColorUtil.a());
                  String var25 = var1[1];
                  String var26 = String.valueOf(ChatColorUtil.b());
                  String var27 = this.getDisplayName();
                  String var28 = this.getName();
                  Night.chatManager.sendReplaceableTaggedMessage(var24 + var25 + var26 + " is already on your friends list.", var27, var28);
               }
               break;
            case "del":
               String var4 = var1[1];
               if (Night.friendManager.isFriend(var4)) {
                  String var5 = var1[1];
                  Night.friendManager.removeFriend(var5);
                  String var6 = String.valueOf(ChatColorUtil.a());
                  String var7 = var1[1];
                  String var8 = String.valueOf(ChatColorUtil.b());
                  String var9 = this.getDisplayName();
                  String var10 = this.getName();
                  Night.chatManager.sendReplaceableTaggedMessage("Successfully removed " + var6 + var7 + var8 + " from your friends list.", var9, var10);
               } else {
                  String var11 = String.valueOf(ChatColorUtil.a());
                  String var12 = var1[1];
                  String var13 = String.valueOf(ChatColorUtil.b());
                  String var14 = this.getDisplayName();
                  String var15 = this.getName();
                  Night.chatManager.sendReplaceableTaggedMessage(var11 + var12 + var13 + " is not on your friends list.", var14, var15);
               }
               break;
            default:
               this.sendSyntax();
         }
      } else if (var1.length == 1) {
         switch (var1[0].toLowerCase()) {
            case "clear":
               Night.friendManager.clearFriends();
               String var31 = this.getDisplayName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully cleared your friends list.", var31, this.getName() + "-list");
               break;
            case "list":
               ArrayList var32 = Night.friendManager.getFriends();
               if (var32.isEmpty()) {
                  String var33 = this.getDisplayName();
                  Night.chatManager.sendTaggedMessage("You currently have no friends.", var33);
               } else {
                  StringBuilder var34 = new StringBuilder();
                  int var35 = 0;

                  for (String var37 : (Iterable<String>)(Object)var32) {
                     var35++;
                     StringBuilder var38 = var34;
                     Object var39 = ChatColorUtil.b();
                     StringBuilder var40 = var38.append(var39);
                     String var41 = var37;
                     StringBuilder var42 = var40.append(var41);
                     int var43 = var35;
                     int var44 = var32.size();
                     String var45;
                     if (var43 == var44) {
                        var45 = "";
                     } else {
                        var45 = ", ";
                     }

                     var42.append(var45);
                  }

                  String var46 = String.valueOf(ChatColorUtil.a());
                  String var47 = String.valueOf(ChatColorUtil.b());
                  int var48 = var32.size();
                  String var49 = String.valueOf(ChatColorUtil.a());
                  String var50 = String.valueOf(ChatColorUtil.b());
                  String var51 = String.valueOf(var34);
                  Night.chatManager.sendReplaceableMessage("Friends " + var46 + "[" + var47 + var48 + var49 + "]: " + var50 + var51, this.getName() + "-list");
               }
               break;
            default:
               this.sendSyntax();
         }
      } else {
         this.sendSyntax();
      }
   }
}
