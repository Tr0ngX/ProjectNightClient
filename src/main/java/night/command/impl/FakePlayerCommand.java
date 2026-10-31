package night.command.impl;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.events.impl.TotemPopEvent;
import night.manager.ChatManager;
import night.module.impl.misc.FakePlayer;
import night.setting.impl.StringSetting;

@CommandInfo(
   name = "fakeplayer",
   aliases = "fp",
   description = "Spawns a client-side fake player to test on.",
   syntax = "[remove|pop|swim|record|stop|play|<name>]"
)
public class FakePlayerCommand extends Command {
   private static final byte a = 35;

   @Override
   public void execute(String[] var1) {
      FakePlayer var2 = Night.moduleManager.getModule(FakePlayer.class);
      if (var2 != null) {
         if (var1.length == 0) {
            FakePlayer var3 = var2;
            boolean var4;
            if (!var2.isEnabled()) {
               var4 = true;
            } else {
               var4 = false;
            }

            var3.setEnabled(var4);
         } else {
            switch (var1[0].toLowerCase()) {
               case "remove":
                  if (var2.isEnabled()) {
                     var2.setEnabled(false);
                  } else {
                     Night.chatManager.sendWarning("No fake player spawned.");
                  }
                  break;
               case "pop":
                  FakePlayer var7 = var2;
                  this.a(var7);
                  break;
               case "swim":
                  FakePlayer var8 = var2;
                  this.b_void_FakePlayer(var8);
                  break;
               case "record":
                  var2.v();
                  break;
               case "stop":
                  var2.w();
                  break;
               case "play":
                  var2.x();
                  break;
               default:
                  StringSetting var9 = var2.name2;
                  String var10 = var1[0];
                  var9.setValue(var10);
                  if (!var2.isEnabled()) {
                     var2.setEnabled(true);
                  } else {
                     var2.setEnabled(false);
                     var2.setEnabled(true);
                  }
            }
         }
      }
   }

   @Override
   public List<String> getSuggestions(String[] var1) {
      return var1.length == 0 ? List.of("remove", "pop", "swim", "record", "stop", "play") : List.of();
   }

   private boolean h() {
      boolean var1;
      if (mc.level != null && mc.player != null) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   private void a(FakePlayer var1) {
      if (!this.h() && mc.getConnection() != null) {
         RemotePlayer var2 = var1.z();
         if (var2 != null) {
            Level var3 = var2.level();
            ClientLevel var4 = mc.level;
            if (var3 == var4) {
               ClientPacketListener var5 = mc.getConnection();
               RemotePlayer var6 = var2;
               var5.handleEntityEvent(new ClientboundEntityEventPacket(var6, (byte)35));
               RemotePlayer var7 = var2;
               Night.EVENT_BUS.post(new TotemPopEvent(var7, 1));
               Night.chatManager.sendTaggedMessage("Popped fake player.", "FakePlayer");
               return;
            }
         }

         Night.chatManager.sendWarning("No fake player spawned. Spawn one first.");
      } else {
         Night.chatManager.sendWarning("You need to be in a world to do that.");
      }
   }

   private void b_void_FakePlayer(FakePlayer var1) {
      if (this.h()) {
         Night.chatManager.sendWarning("You need to be in a world to do that.");
      } else {
         RemotePlayer var2 = var1.z();
         if (var2 != null) {
            Level var3 = var2.level();
            ClientLevel var4 = mc.level;
            if (var3 == var4) {
               Pose var5;
               if (var2.getPose() != Pose.SWIMMING) {
                  var5 = Pose.SWIMMING;
               } else {
                  var5 = Pose.STANDING;
               }

               Pose var6 = var5;
               RemotePlayer var7 = var2;
               Pose var8 = var6;
               var7.setPose(var8);
               var2.refreshDimensions();
               ChatManager var9 = Night.chatManager;
               String var10;
               if (var6 == Pose.SWIMMING) {
                  var10 = "Fake player now in swim pose.";
               } else {
                  var10 = "Fake player now standing.";
               }

               var9.sendTaggedMessage(var10, "FakePlayer");
               return;
            }
         }

         Night.chatManager.sendWarning("No fake player spawned. Spawn one first.");
      }
   }
}
