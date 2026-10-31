package night.command.impl;

import java.util.List;
import java.util.Map;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.manager.ChatManager;
import night.module.impl.visuals.StashFinder;
import night.module.impl.visuals.stashfinder.StashWebhookNotifier;
import night.setting.impl.StringSetting;
import night.util.color.ChatColorUtil;

@CommandInfo(
   name = "stashfinder",
   aliases = "stash",
   displayName = "StashFinder",
   description = "Configures StashFinder's Discord webhook and controls scan state.",
   syntax = "<webhook <url> | userid <id> | test | reset>"
)
public class StashFinderCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      return var1.length == 0 ? List.of("webhook", "userid", "test", "reset") : List.of();
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 0) {
         this.sendSyntax();
      } else {
         switch (var1[0].toLowerCase()) {
            case "webhook":
               if (var1.length < 2) {
                  ChatManager var27 = Night.chatManager;
                  String var28 = String.valueOf(ChatColorUtil.a());
                  String var29;
                  if (StashWebhookNotifier.a().isBlank()) {
                     var29 = "None";
                  } else {
                     var29 = StashWebhookNotifier.a();
                  }

                  String var30 = this.getDisplayName();
                  var27.sendTaggedMessage("Current webhook: " + var28 + var29, var30);
                  return;
               }

               String var31 = var1[1];
               StashWebhookNotifier.a(var31);
               StashFinder var32 = Night.moduleManager.getModule(StashFinder.class);
               if (var32 != null) {
                  StringSetting var33 = var32.y;
                  String var34 = var31;
                  var33.setValue(var34);
               }

               String var35 = this.getDisplayName();
               Night.chatManager.sendTaggedMessage("Webhook URL updated.", var35);
               break;
            case "userid":
               if (var1.length < 2) {
                  ChatManager var4 = Night.chatManager;
                  String var5 = String.valueOf(ChatColorUtil.a());
                  String var6;
                  if (StashWebhookNotifier.b().isBlank()) {
                     var6 = "None";
                  } else {
                     var6 = StashWebhookNotifier.b();
                  }

                  String var7 = this.getDisplayName();
                  var4.sendTaggedMessage("Current User ID: " + var5 + var6, var7);
                  return;
               }

               String var8 = var1[1];
               StashWebhookNotifier.b(var8);
               StashFinder var9 = Night.moduleManager.getModule(StashFinder.class);
               if (var9 != null) {
                  StringSetting var10 = var9.z;
                  String var11 = var8;
                  var10.setValue(var11);
               }

               String var12 = this.getDisplayName();
               Night.chatManager.sendTaggedMessage("User ID updated (will be pinged on stash finds).", var12);
               break;
            case "test":
               StashFinder var13 = Night.moduleManager.getModule(StashFinder.class);
               String var14;
               if (var13 != null && !var13.y.getValue().isBlank()) {
                  var14 = var13.y.getValue();
               } else {
                  var14 = StashWebhookNotifier.a();
               }

               String var15 = var14;
               String var16;
               if (var13 != null && !var13.z.getValue().isBlank()) {
                  var16 = var13.z.getValue();
               } else {
                  var16 = StashWebhookNotifier.b();
               }

               String var17 = var16;
               if (var15.isBlank()) {
                  String var18 = String.valueOf(ChatColorUtil.a());
                  String var19 = String.valueOf(ChatColorUtil.b());
                  String var20 = this.getDisplayName();
                  Night.chatManager.sendTaggedMessage("No webhook URL configured. Use " + var18 + ".stashfinder webhook <url>" + var19 + " first.", var20);
               } else {
                  Integer var21 = 25;
                  Integer var22 = 8;
                  Map var23 = Map.of("Chests", var21, "Shulkers", var22);
                  String var24 = var15;
                  String var25 = var17;
                  StashWebhookNotifier.a(0, 0, "Test Dimension", var23, var24, var25);
                  String var26 = this.getDisplayName();
                  Night.chatManager.sendTaggedMessage("Test webhook dispatched.", var26);
               }
               break;
            case "reset":
               StashFinder var36 = Night.moduleManager.getModule(StashFinder.class);
               if (var36 != null) {
                  var36.v();
                  String var37 = this.getDisplayName();
                  Night.chatManager.sendTaggedMessage("Scan history cleared for this world. All chunks will be re-evaluated.", var37);
               }
               break;
            default:
               this.sendSyntax();
         }
      }
   }
}
