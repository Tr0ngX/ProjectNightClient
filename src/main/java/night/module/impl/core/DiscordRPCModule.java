package night.module.impl.core;

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import night.Night;
import night.events.EventTarget;
import night.events.impl.SettingChangeEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ModuleInfo(name = "RPC", description = "Enables Discord Rich Presence for the client.", category = Module.Category.CORE)
public class DiscordRPCModule extends Module {
   private static final Logger j = LoggerFactory.getLogger("Night/RPC");
   public BooleanSetting uUID = new BooleanSetting("UUID", "Whether or not to show your client UID on the Discord presence.", true);
   public ModeSetting details;
   public StringSetting customDetails;
   public ModeSetting image;
   private static final String k = "logo";
   private static final String l = "image";
   private static final long m = 1540242670633615450L;
   private static final long n = 1545423682883551253L;
   private static final int o = 32;
   public NumberSetting animatedDelay;
   private int p;
   private final TimerUtil q;
   private final String[] r;
   private final RichPresence s;
   private final TimerUtil t;

   public DiscordRPCModule() {
      String[] var1 = new String[]{"Custom", "Random"};
      this.details = new ModeSetting("Details", "The mode for the discord presence details.", "Random", var1);
      ModeSetting var2 = this.details;
      String[] var3 = new String[]{"Custom"};
      this.customDetails = new StringSetting("CustomDetails", "Custom RPC text.", new ModeSetting.ModeCondition(var2, var3), "Beyond the Gotham's darkness.");
      String[] var4 = new String[]{"Logo", "Image", "Animated"};
      this.image = new ModeSetting("Image", "Which large image to show on the Rich Presence card.", "Image", var4);
      Integer var5 = 1500;
      Integer var6 = 0;
      Integer var7 = 10000;
      this.animatedDelay = new NumberSetting("AnimatedDelay", "Milliseconds between each frame swap.", var5, var6, var7);
      this.p = 0;
      this.q = new TimerUtil();
      String[] var8 = new String[]{"Beyond the Gotham's darkness."};
      this.r = var8;
      this.s = new RichPresence();
      this.t = new TimerUtil();
   }

   @Override
   public void onEnable() {
      DiscordIPC.setOnError((var0, var1x) -> j.warn("[RPC] Discord IPC error {}: {}", var0, var1x));
      RichPresence var1 = this.s;
      long var2 = Night.START_TIME;
      long var4 = 1000L;
      long var6 = var2 / var4;
      var1.setStart(var6);
      this.v();
   }

   private void v() {
      DiscordIPC.stop();
      if (!DiscordIPC.start(this.x(), () -> j.info("[RPC] Connected to Discord IPC"))) {
         j.warn("[RPC] DiscordIPC.start() returned false -- no local Discord IPC pipe found (is Discord running?)");
      } else {
         RichPresence var1 = this.s;
         String var2 = this.A();
         var1.setDetails(var2);
         this.w();
         this.y();
         DiscordIPC.setActivity(this.s);
      }
   }

   private void w() {
      if (!this.uUID.getValue()) {
         this.s.setState(null);
      } else {
         String var1;
         if (Night.licenseService != null) {
            var1 = Night.licenseService.getRole();
         } else {
            var1 = null;
         }

         String var2 = var1;
         RichPresence var3 = this.s;
         String var4;
         if (var2 != null) {
            var4 = "UID " + var2;
         } else {
            var4 = null;
         }

         var3.setState(var4);
      }
   }

   private long x() {
      long var1;
      if (this.image.getValue().equals("Logo")) {
         var1 = 1540242670633615450L;
      } else {
         var1 = 1545423682883551253L;
      }

      return var1;
   }

   private void y() {
      if (this.image.getValue().equals("Logo")) {
         this.s.setLargeImage("logo", "Night");
      } else if (this.image.getValue().equals("Animated")) {
         RichPresence var1 = this.s;
         String var2 = this.z();
         var1.setLargeImage(var2, "Night");
      } else {
         this.s.setLargeImage("image", "Night");
      }
   }

   private String z() {
      Object[] var1 = new Object[1];
      Integer var2 = this.p + 1;
      var1[0] = var2;
      return String.format("frame_%02d", var1);
   }

   @EventTarget
   public void a(SettingChangeEvent var1) {
      Setting var2 = var1.getSetting();
      ModeSetting var3 = this.image;
      if (var2 == var3) {
         this.v();
      } else {
         Setting var4 = var1.getSetting();
         BooleanSetting var5 = this.uUID;
         if (var4 != var5) {
            Setting var6 = var1.getSetting();
            StringSetting var7 = this.customDetails;
            if (var6 != var7) {
               Setting var8 = var1.getSetting();
               ModeSetting var9 = this.details;
               if (var8 != var9) {
                  return;
               }
            }
         }

         RichPresence var10 = this.s;
         String var11;
         if (this.details.getValue().equals("Random")) {
            var11 = this.A();
         } else {
            var11 = this.customDetails.getValue();
         }

         var10.setDetails(var11);
         this.w();
         this.y();
         DiscordIPC.setActivity(this.s);
      }
   }

   @Override
   public void onDisable() {
      DiscordIPC.stop();
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (this.details.getValue().equals("Random")) {
         TimerUtil var2 = this.t;
         Integer var3 = 300000;
         if (var2.passedMs(var3)) {
            RichPresence var4 = this.s;
            String var5 = this.A();
            var4.setDetails(var5);
            this.w();
            this.y();
            DiscordIPC.setActivity(this.s);
            this.t.reset();
         }
      }

      if (this.image.getValue().equals("Animated")) {
         TimerUtil var6 = this.q;
         Long var7 = this.animatedDelay.getValue().longValue();
         if (var6.passedMs(var7)) {
            int var8 = (this.p + 1) % 32;
            this.p = var8;
            RichPresence var9 = this.s;
            String var10 = this.z();
            var9.setLargeImage(var10, "Night");
            DiscordIPC.setActivity(this.s);
            this.q.reset();
         }
      }
   }

   private String A() {
      String[] var1 = this.r;
      double var2 = this.r.length;
      double var4 = 0.0;
      int var6 = (int)MathUtil.a_double_double_double(var2, var4);
      return var1[var6];
   }
}
