package night.command.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.manager.WaypointManager;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "waypoint",
   displayName = "Waypoint",
   description = "Allows you to manage the client's custom waypoints.",
   syntax = "<add|del> <[x, y, z]|[x, z]> | <clear|list>",
   aliases = "w"
)
public class WaypointCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         return List.of("add", "del", "clear", "list");
      }

      if (var1.length == 1 && var1[0].equalsIgnoreCase("del")) {
         ArrayList var2 = new ArrayList();

         for (WaypointManager.Waypoint var4 : (Iterable<WaypointManager.Waypoint>)(Object)Night.waypointManager.getWaypoints()) {
            ArrayList var5 = var2;
            String var6 = var4.getName();
            var5.add(var6);
         }

         return var2;
      } else {
         return List.of();
      }
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 1) {
         if (var1[0].equalsIgnoreCase("clear")) {
            Night.waypointManager.clearWaypoints();
            String var2 = this.getDisplayName();
            Night.chatManager.sendReplaceableTaggedMessage("Successfully cleared your custom waypoints.", var2, this.getName() + "-list");
         } else if (var1[0].equalsIgnoreCase("list")) {
            ArrayList var37 = Night.waypointManager.getWaypoints();
            if (var37.isEmpty()) {
               String var38 = this.getDisplayName();
               Night.chatManager.sendTaggedMessage("You currently have no custom waypoints set.", var38);
            } else {
               StringBuilder var39 = new StringBuilder();
               int var40 = 0;

               for (WaypointManager.Waypoint var42 : (Iterable<WaypointManager.Waypoint>)(Object)var37) {
                  var40++;
                  StringBuilder var43 = var39;
                  Object var44 = ChatColorUtil.b();
                  StringBuilder var45 = var43.append(var44);
                  String var46 = var42.getName();
                  StringBuilder var47 = var45.append(var46);
                  int var48 = var40;
                  int var49 = var37.size();
                  String var50;
                  if (var48 == var49) {
                     var50 = "";
                  } else {
                     var50 = ", ";
                  }

                  var47.append(var50);
               }

               String var51 = String.valueOf(ChatColorUtil.a());
               String var52 = String.valueOf(ChatColorUtil.b());
               int var53 = var37.size();
               String var54 = String.valueOf(ChatColorUtil.a());
               String var55 = String.valueOf(ChatColorUtil.b());
               String var56 = String.valueOf(var39);
               Night.chatManager
                  .sendReplaceableMessage("Custom waypoints " + var51 + "[" + var52 + var53 + var54 + "]: " + var55 + var56, this.getName() + "-list");
            }
         } else {
            this.sendSyntax();
         }
      } else if (var1.length != 2 && var1.length != 4 && var1.length != 5) {
         this.sendSyntax();
      } else {
         try {
            int var3;
            if (var1.length == 2) {
               var3 = (int)mc.player.getX();
            } else {
               var3 = Integer.parseInt(var1[2]);
            }

            int var4 = var3;
            int var5;
            if (var1.length != 2 && var1.length != 4) {
               var5 = Integer.parseInt(var1[3]);
            } else {
               var5 = (int)mc.player.getY() + 1;
            }

            int var6 = var5;
            int var7;
            if (var1.length == 2) {
               var7 = (int)mc.player.getZ();
            } else if (var1.length == 4) {
               var7 = Integer.parseInt(var1[3]);
            } else {
               var7 = Integer.parseInt(var1[4]);
            }

            int var8 = var7;
            double var9 = var4;
            double var11 = var6;
            double var13 = var8;
            Vec3 var15 = new Vec3(var9, var11, var13);
            if (var1[0].equalsIgnoreCase("add")) {
               String var16 = var1[1];
               Vec3 var17 = var15;
               Night.waypointManager.addWaypoint(var16, var17);
               String var18 = String.valueOf(ChatColorUtil.a());
               String var19 = var1[1];
               int var20 = (int)var15.x;
               int var21 = (int)var15.y;
               int var22 = (int)var15.z;
               String var23 = String.valueOf(ChatColorUtil.b());
               String var24 = this.getDisplayName();
               String var25 = this.getName();
               Night.chatManager
                  .sendReplaceableTaggedMessage(
                     "Successfully added " + var18 + var19 + " [" + var20 + ", " + var21 + ", " + var22 + "]" + var23 + " to your custom waypoints.",
                     var24,
                     var25
                  );
            } else if (var1[0].equalsIgnoreCase("del")) {
               String var26 = var1[1];
               Night.waypointManager.removeWaypoint(var26);
               String var27 = String.valueOf(ChatColorUtil.a());
               String var28 = var1[1];
               String var29 = String.valueOf(ChatColorUtil.b());
               String var30 = this.getDisplayName();
               String var31 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully removed " + var27 + var28 + var29 + " to your custom waypoints.", var30, var31);
            } else {
               this.sendSyntax();
            }
         } catch (NumberFormatException var57) {
            String var33 = String.valueOf(ChatColorUtil.a());
            String var34 = String.valueOf(ChatColorUtil.b());
            String var35 = this.getDisplayName();
            String var36 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Please input valid " + var33 + "integer" + var34 + " numbers for the coordinates.", var35, var36);
         }
      }
   }
}
