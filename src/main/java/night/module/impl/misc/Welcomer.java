package night.module.impl.misc;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ChatSendEvent;
import night.events.impl.PlayerJoinEvent;
import night.events.impl.PlayerLeaveEvent;
import night.events.impl.TickEvent;
import night.manager.ChatManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;

@ModuleInfo(name = "Welcomer", description = "Sends a message when a player joins or leaves the server.", category = Module.Category.MISCELLANEOUS)
public class Welcomer extends Module {
   public BooleanSetting joins = new BooleanSetting("Joins", "Sends messages when a player joins the server", true);
   public BooleanSetting leaves = new BooleanSetting("Leaves", "Sends messages when a player leaves the server", true);
   public BooleanSetting clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
   public BooleanSetting unicode = new BooleanSetting("Unicode", "Uses prettier unicode icons instead of normal arrows.", false);
   public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
   public NumberSetting delay;
   private final String[] k;
   private final String[] l;
   private final ConcurrentLinkedQueue<String> m;
   private final TimerUtil n;

   public Welcomer() {
      Integer var1 = 5;
      Integer var2 = 0;
      Integer var3 = 30;
      this.delay = new NumberSetting("Delay", "The delay for the announcer.", var1, var2, var3);
      String[] var4 = new String[]{"Hello, ", "Welcome to the server, ", "Good to see you, ", "Greetings, ", "Good evening, ", "Hey, "};
      this.k = var4;
      String[] var5 = new String[]{"Goodbye, ", "See you later, ", "Bye bye, ", "I hope you had a good time, ", "Farewell, ", "See you next time, "};
      this.l = var5;
      this.m = new ConcurrentLinkedQueue<>();
      this.n = new TimerUtil();
   }

   @Override
   public void onEnable() {
      this.m.clear();
      this.n.reset();
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck()) {
         TimerUtil var2 = this.n;
         Integer var3 = this.delay.getValue().intValue() * 1000;
         if (var2.passedMs(var3) && !this.m.isEmpty() && !this.clientside.getValue()) {
            String var4 = this.m.poll();
            if (this.clientside.getValue()) {
               String var5 = var4;
               Night.chatManager.sendMessage(var5);
            } else {
               ClientPacketListener var6 = mc.player.connection;
               String var7;
               if (this.greenText.getValue()) {
                  var7 = "> ";
               } else {
                  var7 = "";
               }

               String var8 = var4;
               var6.sendChat(var7 + var8);
            }

            this.n.reset();
         }
      }
   }

   @EventTarget
   public void a(ChatSendEvent var1) {
      if (!this.nullCheck()) {
         this.n.reset();
      }
   }

   @EventTarget
   public void a(PlayerJoinEvent var1) {
      if (!this.nullCheck()) {
         UUID var2 = var1.getUuid();
         String var3 = var1.getName();
         String var4 = this.a(var2, var3);
         if (var4 != null && !var4.isEmpty()) {
            if (mc.player != null) {
               String var5 = var4;
               String var6 = mc.player.getName().getString();
               if (var5.equalsIgnoreCase(var6)) {
                  return;
               }

               if (var1.getUuid() != null) {
                  UUID var7 = var1.getUuid();
                  UUID var8 = mc.player.getUUID();
                  if (var7.equals(var8)) {
                     return;
                  }
               }
            }

            if (this.joins.getValue()) {
               String var9 = var4;
               if (this.clientside.getValue()) {
                  String var10 = var9;
                  mc.execute(() -> {
                     ChatManager var2x = Night.chatManager;
                     String var3x = String.valueOf(ChatFormatting.DARK_GRAY);
                     String var4x = String.valueOf(ChatFormatting.GREEN);
                     String var5x;
                     if (this.unicode.getValue()) {
                        var5x = "\u00bb";
                     } else {
                        var5x = ">";
                     }

                     String var6x = String.valueOf(ChatFormatting.DARK_GRAY);
                     String var7x = String.valueOf(ChatFormatting.GRAY);
                     var2x.sendMessage(var3x + "[" + var4x + var5x + var6x + "] " + var7x + var10);
                  });
               } else {
                  ConcurrentLinkedQueue var11 = this.m;
                  String[] var12 = this.k;
                  double var13 = this.k.length;
                  double var15 = 0.0;
                  int var17 = (int)MathUtil.a_double_double_double(var13, var15);
                  String var18 = var12[var17];
                  String var19 = var9;
                  var11.add(var18 + var19);
               }
            }
         }
      }
   }

   @EventTarget
   public void a(PlayerLeaveEvent var1) {
      if (!this.nullCheck()) {
         UUID var2 = var1.getUuid();
         String var3 = var1.getName();
         String var4 = this.a(var2, var3);
         if (var4 != null && !var4.isEmpty()) {
            if (mc.player != null) {
               String var5 = var4;
               String var6 = mc.player.getName().getString();
               if (var5.equalsIgnoreCase(var6)) {
                  return;
               }

               if (var1.getUuid() != null) {
                  UUID var7 = var1.getUuid();
                  UUID var8 = mc.player.getUUID();
                  if (var7.equals(var8)) {
                     return;
                  }
               }
            }

            if (this.leaves.getValue()) {
               String var9 = var4;
               if (this.clientside.getValue()) {
                  String var10 = var9;
                  mc.execute(() -> {
                     ChatManager var2x = Night.chatManager;
                     String var3x = String.valueOf(ChatFormatting.DARK_GRAY);
                     String var4x = String.valueOf(ChatFormatting.RED);
                     String var5x;
                     if (this.unicode.getValue()) {
                        var5x = "\u00ab";
                     } else {
                        var5x = "<";
                     }

                     String var6x = String.valueOf(ChatFormatting.DARK_GRAY);
                     String var7x = String.valueOf(ChatFormatting.GRAY);
                     var2x.sendMessage(var3x + "[" + var4x + var5x + var6x + "] " + var7x + var10);
                  });
               } else {
                  ConcurrentLinkedQueue var11 = this.m;
                  String[] var12 = this.l;
                  double var13 = this.l.length;
                  double var15 = 0.0;
                  int var17 = (int)MathUtil.a_double_double_double(var13, var15);
                  String var18 = var12[var17];
                  String var19 = var9;
                  var11.add(var18 + var19);
               }
            }
         }
      }
   }

   private String a(UUID var1, String var2) {
      if (var2 != null && !var2.isEmpty()) {
         return var2;
      }

      if (var1 == null) {
         return null;
      }

      if (mc.getConnection() != null) {
         PlayerInfo var3 = mc.getConnection().getPlayerInfo(var1);
         if (var3 != null && var3.getProfile() != null && var3.getProfile().name() != null) {
            return var3.getProfile().name();
         }
      }

      return null;
   }
}
