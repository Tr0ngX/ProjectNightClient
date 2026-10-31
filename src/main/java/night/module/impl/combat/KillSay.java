package night.module.impl.combat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ChatSendEvent;
import night.events.impl.CommandExecuteEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.TargetDeathEvent;
import night.events.impl.TickEvent;
import night.events.impl.TotemPopEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.io.FileUtil;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;

@ModuleInfo(name = "KillSay", description = "Automatically sends a message when you kill someone.", category = Module.Category.COMBAT)
public class KillSay extends Module {
   private final String[] o;
   public BooleanSetting kills;
   public BooleanSetting pops;
   public ModeSetting mode;
   public StringSetting killFile;
   public StringSetting popFile;
   public NumberSetting delay;
   public BooleanSetting clientside;
   public BooleanSetting greenText;
   public BooleanSetting killStreak;
   public BooleanSetting suicideIgnore;
   private final ConcurrentLinkedQueue<String> p;
   private List<String> q;
   private List<String> r;
   private final TimerUtil s;
   private String t;
   private int u;
   private boolean v;

   public KillSay() {
      String[] var1 = new String[]{
         "Sit [username], Night owns me and all!",
         "[username] I killed you with the power of Night!",
         "Sorry [username], get good get Night!",
         "I just killed [username] thanks to Night!"
      };
      this.o = var1;
      this.kills = new BooleanSetting("Kills", "Sends a message when you kill someone.", true);
      this.pops = new BooleanSetting("Pops", "Sends a message when you pop someone.", false);
      String[] var2 = new String[]{"Default", "Custom"};
      this.mode = new ModeSetting("Mode", "The mode for the messages.", "Default", var2);
      this.killFile = new StringSetting("KillFile", "The name of the file containing the kill messages.", "killfile.txt");
      this.popFile = new StringSetting("PopFile", "The name of the file containing the pop messages.", "popfile.txt");
      Integer var3 = 5;
      Integer var4 = 0;
      Integer var5 = 10;
      this.delay = new NumberSetting("Delay", "The delay for the announcer.", var3, var4, var5);
      this.clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
      this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
      this.killStreak = new BooleanSetting("KillStreak", "Keeps track of your kills.", false);
      this.suicideIgnore = new BooleanSetting("SuicideIgnore", "Doesn't reset kill streak when you suicide.", false);
      this.p = new ConcurrentLinkedQueue<>();
      this.q = new ArrayList<>();
      this.r = new ArrayList<>();
      this.s = new TimerUtil();
      this.v = false;
   }

   @Override
   public void onEnable() {
      this.p.clear();
      this.s.reset();
      this.u = 0;
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck() && !this.p.isEmpty()) {
         List var2 = FileUtil.a_List_File(new File("Night/Client/" + this.killFile.getValue()));
         this.q = var2;
         List var3 = FileUtil.a_List_File(new File("Night/Client/" + this.popFile.getValue()));
         this.r = var3;
         synchronized (this.p) {
            TimerUtil var5 = this.s;
            Integer var6 = this.delay.getValue().intValue() * 1000;
            if (var5.passedMs(var6)) {
               String var7 = this.p.poll();
               if (!var7.isEmpty()) {
                  if (this.clientside.getValue()) {
                     String var8 = var7;
                     Night.chatManager.sendMessage(var8);
                  } else {
                     ClientPacketListener var9 = mc.player.connection;
                     String var10;
                     if (this.greenText.getValue()) {
                        var10 = "> ";
                     } else {
                        var10 = "";
                     }

                     String var11 = var7;
                     var9.sendChat(var10 + var11);
                  }

                  String var12 = var7;
                  this.t = var12;
               }

               this.s.reset();
            }
         }
      }
   }

   @EventTarget
   public void a(TargetDeathEvent var1) {
      if (!this.nullCheck()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 != var3) {
            int var4 = this.u + 1;
            this.u = var4;
            synchronized (this.p) {
               if (this.kills.getValue()) {
                  this.p.clear();
                  String var6 = var1.getPlayer().getName().getString();
                  String var7 = this.b(var6);
                  ConcurrentLinkedQueue var8 = this.p;
                  String var9 = var7;
                  var8.add(var9);
               }
            }

            if (this.killStreak.getValue() && this.u > 1) {
               Night.chatManager.sendMessage("You are on a " + this.u + " kill streak.");
            }

            return;
         }
      }
   }

   @EventTarget
   public void a(TotemPopEvent var1) {
      if (!this.nullCheck()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 != var3) {
            Player var4 = var1.getPlayer();
            if (Night.targetManager.isTarget(var4) && this.pops.getValue()) {
               synchronized (this.p) {
                  String var6 = var1.getPlayer().getName().getString();
                  int var7 = var1.getPops();
                  String var8 = this.a(var6, var7);
                  ConcurrentLinkedQueue var9 = this.p;
                  String var10 = var8;
                  var9.add(var10);
                  return;
               }
            }
         }
      }
   }

   @EventTarget
   public void a(CommandExecuteEvent var1) {
      if (!this.nullCheck()) {
         if (var1.getCommand().contains("/kill")) {
            this.v = true;
         }
      }
   }

   @EventTarget
   public void a(ChatSendEvent var1) {
      if (!this.nullCheck()) {
         this.s.reset();
      }
   }

   @EventTarget
   public void a(PlayerDeathEvent var1) {
      Player var2 = var1.getPlayer();
      LocalPlayer var3 = mc.player;
      if (var2 == var3 && (!this.suicideIgnore.getValue() || !this.v && !Night.moduleManager.getModule(Suicide.class).isEnabled())) {
         this.u = 0;
      }

      this.v = false;
   }

   private String a(String var1, int var2) {
      String var9;
      String var10;
      String var11;
      if (this.mode.getValue().equals("Custom") && !this.r.isEmpty()) {
         do {
            List var3 = this.r;
            double var4 = this.r.size();
            double var6 = 0.0;
            int var8 = (int)MathUtil.a_double_double_double(var4, var6);
            var9 = ((String)var3.get(var8)).replace("[username]", var1);
            var10 = var9;
            var11 = this.t;
         } while (var10.equals(var11) && this.r.size() > 1);
      } else {
         String var12;
         if (var2 > 1) {
            var12 = " pops ";
         } else {
            var12 = " pop ";
         }

         var9 = var2 + var12 + var1 + "!";
      }

      return var9;
   }

   private String b(String var1) {
      String var8;
      String var9;
      String var10;
      String var17;
      String var18;
      if (this.mode.getValue().equals("Custom") && !this.q.isEmpty()) {
         do {
            List var2 = this.q;
            double var3 = this.q.size();
            double var5 = 0.0;
            int var7 = (int)MathUtil.a_double_double_double(var3, var5);
            var8 = ((String)var2.get(var7)).replace("[username]", var1);
            var9 = var8;
            var10 = this.t;
         } while (var9.equals(var10) && this.q.size() > 1);
      } else {
         do {
            String[] var11 = this.o;
            double var12 = this.o.length;
            double var14 = 0.0;
            int var16 = (int)MathUtil.a_double_double_double(var12, var14);
            var8 = var11[var16].replace("[username]", var1);
            var17 = var8;
            var18 = this.t;
         } while (var17.equals(var18));
      }

      return var8;
   }

   @Override
   public String getDisplayInfo() {
      return this.u + "";
   }
}
