package night.manager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Stream;
import lombok.Generated;
import night.Night;
import night.module.impl.core.FriendModule;
import night.module.impl.core.PingBypassModule;
import night.pingbypass.PingBypassState;

public class FriendManager {
   private final ArrayList<String> friends = new ArrayList<>();

   public boolean isFriend(String var1) {
      if (this.isFriendlyFireEnabled()) {
         return false;
      }

      Stream<String> var2 = this.friends.stream();
      return var2.anyMatch(var1::equalsIgnoreCase);
   }

   public void addFriend(String var1) {
      if (!this.isFriend(var1)) {
         this.friends.add(var1);
         this.e();
      }
   }

   public void removeFriend(String var1) {
      ArrayList<String> var2 = this.friends;
      var2.removeIf(var1::equalsIgnoreCase);
      this.e();
   }

   public void clearFriends() {
      this.friends.clear();
      this.e();
   }

   private void e() {
      if (PingBypassState.d && Night.pingBypassConfig != null && !Night.pingBypassConfig.isServerMode()) {
         PingBypassModule var1 = Night.moduleManager.getModule(PingBypassModule.class);
         if (var1 != null) {
            var1.w();
         }
      }
   }

   public boolean isFriendlyFireEnabled() {
      if (Night.moduleManager == null) {
         return false;
      }

      FriendModule var1 = Night.moduleManager.getModule(FriendModule.class);
      boolean var2;
      if (var1 != null && var1.friendlyFire != null && var1.friendlyFire.getValue()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   public void sendFriendMessage(String var1) {
      if (Night.moduleManager != null) {
         FriendModule var2 = Night.moduleManager.getModule(FriendModule.class);
         if (var2 != null) {
            var2.b(var1);
         }
      }
   }

   public Color getFriendColor() {
      return this.getFriendColor(255);
   }

   public Color getFriendColor(int var1) {
      return new Color(85, 255, 255, var1);
   }

   @Generated
   public ArrayList<String> getFriends() {
      return this.friends;
   }
}
