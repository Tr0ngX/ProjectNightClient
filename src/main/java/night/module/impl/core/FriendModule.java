package night.module.impl.core;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.EventTarget;
import night.events.impl.MouseButtonEvent;
import night.events.impl.SettingChangeEvent;
import night.manager.ChatManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.StringSetting;
import night.util.color.ChatColorUtil;

@ModuleInfo(
   name = "Friend",
   description = "Allows you to manage the client's friend system.",
   category = Module.Category.CORE,
   persistent = true,
   drawn = false
)
public class FriendModule extends Module {
   public BooleanSetting middleClick = new BooleanSetting("MiddleClick", "Adds whichever entity you middle click to your friends list.", true);
   public BooleanSetting friendlyFire = new BooleanSetting("FriendlyFire", "Disables the friend system and allows the attacking of friends.", false);
   public BooleanSetting friendMessage = new BooleanSetting("FriendMessage", "Sends a message to a player whenever you add them to your friends list.", false);
   public StringSetting content;

   public FriendModule() {
      BooleanSetting var1 = this.friendMessage;
      this.content = new StringSetting(
         "Content",
         "The message that will be sent to the people you add. $player is placeholder for their name.",
         new BooleanSetting.BooleanCondition(var1, true),
         "/msg $player I have just added you to my friends list!"
      );
   }

   public void b(String var1) {
      if (mc.level != null && mc.player != null) {
         if (this.friendMessage.getValue()) {
            if (!mc.getConnection().getOnlinePlayers().stream().noneMatch(var1x -> var1x.getProfile().name().equalsIgnoreCase(var1))) {
               if (this.content.getValue().startsWith("/")) {
                  ClientPacketListener var2 = mc.getConnection();
                  String var3 = this.content.getValue().substring(1).replace("$player", var1);
                  var2.sendCommand(var3);
               } else {
                  ClientPacketListener var4 = mc.getConnection();
                  String var5 = this.content.getValue().replace("$player", var1);
                  var4.sendChat(var5);
               }
            }
         }
      }
   }

   @EventTarget
   public void a(SettingChangeEvent var1) {
      Setting var2 = var1.getSetting();
      BooleanSetting var3 = this.friendlyFire;
      if (var2.equals(var3)) {
         ChatManager var4 = Night.chatManager;
         String var5 = String.valueOf(ChatColorUtil.a());
         String var6 = String.valueOf(ChatColorUtil.b());
         String var9;
         if (this.friendlyFire.getValue()) {
            String var7 = String.valueOf(ChatFormatting.GREEN);
            String var8 = String.valueOf(ChatFormatting.RESET);
            var9 = var7 + "on" + var8 + ". Friends will now be attacked.";
         } else {
            String var10 = String.valueOf(ChatFormatting.RED);
            String var11 = String.valueOf(ChatFormatting.RESET);
            var9 = var10 + "off" + var11 + ". Friends will now no longer be attacked.";
         }

         var4.sendWarning(var5 + "Friendly fire" + var6 + " has been turned " + var9);
      }
   }

   @EventTarget
   public void a(MouseButtonEvent var1) {
      if (this.middleClick.getValue() && (mc.gui == null || mc.gui.screen() == null)) {
         if (var1.getButton() == 2) {
            if (mc.crosshairPickEntity != null) {
               if (mc.crosshairPickEntity instanceof Player var3) {
                  String var4 = var3.getGameProfile().name();
                  if (Night.friendManager.isFriend(var4)) {
                     String var5 = var4;
                     Night.friendManager.removeFriend(var5);
                     String var6 = String.valueOf(ChatColorUtil.a());
                     String var7 = var4;
                     String var8 = String.valueOf(ChatColorUtil.b());
                     String var9 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Successfully removed " + var6 + var7 + var8 + " from your friends list.", "MCF", var9);
                  } else {
                     String var10 = var4;
                     Night.friendManager.addFriend(var10);
                     String var11 = String.valueOf(ChatColorUtil.a());
                     String var12 = var4;
                     String var13 = String.valueOf(ChatColorUtil.b());
                     String var14 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Successfully added " + var11 + var12 + var13 + " to your friends list.", "MCF", var14);
                  }
               }
            }
         }
      }
   }
}
