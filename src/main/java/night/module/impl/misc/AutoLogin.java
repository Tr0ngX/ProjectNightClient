package night.module.impl.misc;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.StringSetting;
import night.util.math.TimerUtil;

@ModuleInfo(name = "AutoLogin", description = "Automatically logs in on cracked servers.", category = Module.Category.MISCELLANEOUS)
public class AutoLogin extends Module {
   public StringSetting password = new StringSetting("Password", "The password to use when logging in.", "password");
   private final TimerUtil f = new TimerUtil();

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (!this.nullCheck()) {
         TimerUtil var2 = this.f;
         Integer var3 = 10000;
         if (var2.passedMs(var3)) {
            Packet var4 = var1.getPacket();
            if (var4 instanceof ClientboundSystemChatPacket) {
               String var5 = ((ClientboundSystemChatPacket)var4).content().getString().toLowerCase();
               if (var5.contains("/register")) {
                  ClientPacketListener var6 = mc.getConnection();
                  String var7 = this.password.getValue();
                  String var8 = this.password.getValue();
                  var6.sendCommand("register " + var7 + " " + var8);
                  String var9 = this.getName();
                  Night.chatManager.sendTaggedMessage("Registered successfully.", var9);
                  this.f.reset();
               } else if (var5.contains("/login")) {
                  mc.getConnection().sendCommand("login " + this.password.getValue());
                  String var10 = this.getName();
                  Night.chatManager.sendTaggedMessage("Logged in as " + mc.getUser().getName() + ".", var10);
                  this.f.reset();
               }
            }

            return;
         }
      }
   }
}
